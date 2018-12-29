package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.Coordinate
import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileMap
import com.github.jcornaz.islands.TileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.neo4j.driver.v1.AccessMode
import org.neo4j.driver.v1.Driver
import java.util.*
import kotlin.coroutines.CoroutineContext

class Neo4JTileMapRepository(private val driver: Driver) : TileMapRepository, CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO

    init {
        launch {
            driver.session().use { session ->
                session.run("CREATE INDEX ON :TileMap(id)")
            }
        }
    }

    override suspend fun create(map: TileMap) {
        require(map.tileCount > 0)

        withContext<Unit>(Dispatchers.IO) {
            driver.session().use { session ->
                session.run(map.tileList.joinToString(
                    separator = ", ",
                    prefix = "CREATE (map:TileMap { id: \"${map.id}\" }), ",
                    transform = { tile -> "(map)-[:HAS_TILE]->(:Tile { coordinate: point({x: ${tile.coordinate.x}, y: ${tile.coordinate.y}}), type: ${tile.type.number}})" }
                ))
            }
        }
    }

    override suspend fun findById(id: UUID): TileMap? = withContext(Dispatchers.IO) {
        driver.session(AccessMode.READ).use { session ->
            val result = session.run(
                """
                MATCH (map:TileMap { id: ${'$'}id })-[:HAS_TILE]->(tile:Tile)
                RETURN tile.coordinate.x AS x, tile.coordinate.y AS y, tile.type AS type
                """.trimIndent(),
                mapOf("id" to id.toString())
            )

            if (!result.hasNext()) return@use null

            val builder = TileMap.newBuilder().setId(id.toString())

            result.forEach { record ->
                builder.addTile(
                    Tile.newBuilder()
                        .setCoordinate(Coordinate.newBuilder().setX(record["x"].asInt()).setY(record["y"].asInt()))
                        .setType(TileType.forNumber(record["type"].asInt()))
                )
            }

            return@use builder.build()
        }
    }

    override fun findAll(): ReceiveChannel<TileMap> = produce {
        driver.session(AccessMode.READ).use { session ->
            val result = session.run(
                """
                MATCH (map:TileMap)-[:HAS_TILE]->(tile:Tile)
                RETURN map.id as mapid, tile.coordinate.x AS x, tile.coordinate.y AS y, tile.type AS type
                ORDER BY mapid
                """.trimIndent()
            )

            var currentBuilder: TileMap.Builder? = null

            result.forEach { record ->
                val id = record["mapid"].asString()

                if (id != currentBuilder?.id) {
                    currentBuilder?.let { send(it.build()) }
                    currentBuilder = TileMap.newBuilder().setId(id)
                }

                currentBuilder!!.addTile(
                    Tile.newBuilder()
                        .setCoordinate(Coordinate.newBuilder().setX(record["x"].asInt()).setY(record["y"].asInt()))
                        .setType(TileType.forNumber(record["type"].asInt()))
                )
            }

            currentBuilder?.let { send(it.build()) }
        }
    }
}
