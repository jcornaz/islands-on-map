package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.Coordinate
import com.github.jcornaz.islands.Island
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.withContext
import org.neo4j.driver.v1.AccessMode
import org.neo4j.driver.v1.Driver
import java.util.*
import kotlin.coroutines.CoroutineContext

class Neo4JIslandRepository(private val driver: Driver) : IslandRepository, CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO

    override suspend fun create(island: Island) {
        withContext(coroutineContext) {
            driver.session().use { session ->
                session.writeTransaction { trx ->
                    trx.run(
                        """
                            MATCH (map:TileMap { id: "${island.mapId}" })
                            CREATE (map)-[:HAS_ISLAND]->(island:Island { id: "${island.id}" })
                            RETURN count(map)
                        """.trimIndent()
                    ).single().get(0).asInt().let { require(it == 1) { "Cannot create island for non-existing map" } }

                    island.coordinateList.forEach { coordinate ->
                        trx.run(
                            """
                                MATCH
                                    (map:TileMap { id: "${island.mapId}" })-[:HAS_ISLAND]->(island:Island { id: "${island.id}" }),
                                    (map)-[:HAS_TILE]->(tile:Tile { coordinate: point({x: ${coordinate.x}, y: ${coordinate.y}}) })
                                CREATE
                                    (island)-[:HAS_TILE]->(tile)
                                RETURN
                                    count(tile)
                            """.trimIndent()
                        ).single().get(0).asInt().let { require(it == 1) { "Cannot create island for non-existing tile: (${coordinate.x}, ${coordinate.y})" } }
                    }
                }
            }
        }
    }

    override suspend fun findById(id: UUID): Island? = withContext(coroutineContext) {
        driver.session(AccessMode.READ).use { session ->
            val result = session.run(
                """
                    MATCH (map:TileMap)-[:HAS_ISLAND]->(island:Island { id: ${'$'}id })-[:HAS_TILE]->(tile:Tile)
                    RETURN map.id AS mapId, tile.coordinate.x AS x, tile.coordinate.y AS y
                """.trimIndent(),
                mapOf("id" to id.toString())
            )

            if (!result.hasNext()) return@use null

            val builder = Island.newBuilder().setId(id.toString())

            result.forEach { record ->
                builder.mapId = record["mapId"].asString()
                builder.addCoordinate(Coordinate.newBuilder().setX(record["x"].asInt()).setY(record["y"].asInt()))
            }

            return@use builder.build()
        }
    }

    override fun findAll(): ReceiveChannel<Island> = produce {
        driver.session(AccessMode.READ).use { session ->
            val result = session.run(
                """
                MATCH (map:TileMap)-[:HAS_ISLAND]->(island:Island)-[:HAS_TILE]->(tile:Tile)
                RETURN island.id AS islandId, map.id AS mapId, tile.coordinate.x AS x, tile.coordinate.y AS y
                ORDER BY islandId
                """.trimIndent()
            )

            var currentBuilder: Island.Builder? = null

            result.forEach { record ->
                val id = record["islandId"].asString()

                if (id != currentBuilder?.id) {
                    currentBuilder?.let { send(it.build()) }
                    currentBuilder = Island.newBuilder().setId(id).setMapId(record["mapId"].asString())
                }

                currentBuilder!!.addCoordinate(Coordinate.newBuilder().setX(record["x"].asInt()).setY(record["y"].asInt()))
            }

            currentBuilder?.let { send(it.build()) }
        }
    }
}
