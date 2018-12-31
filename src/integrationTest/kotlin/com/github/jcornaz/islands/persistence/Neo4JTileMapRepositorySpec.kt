package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.TileMap
import com.github.jcornaz.islands.TileType
import com.github.jcornaz.islands.persistence.test.TestDatabase
import com.github.jcornaz.islands.test.beforeEachBlocking
import com.github.jcornaz.islands.test.memoizedBlocking
import com.github.jcornaz.islands.test.memoizedClosable
import com.github.jcornaz.islands.test.tile
import kotlinx.coroutines.channels.toList
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe
import java.util.*

class Neo4JTileMapRepositorySpec : Spek({
    val database by memoizedClosable(CachingMode.SCOPE) { TestDatabase() }
    val repository: TileMapRepository by memoized { Neo4JTileMapRepository(database.driver) }

    val idSequence = generateSequence(1) { it + 1 }.map { UUID(0L, it.toLong()) }.iterator()

    afterEachTest { database.clear() }

    describe("create map") {
        val newId = idSequence.next()

        beforeEachBlocking {
            repository.create(
                TileMap.newBuilder()
                    .setId(newId.toString())
                    .addTile(tile(0, 0, TileType.LAND))
                    .addTile(tile(0, 1, TileType.WATER))
                    .build()
            )
        }

        it("should create the map and tiles") {
            val tiles = database.execute(
                """
                MATCH (:TileMap { id: "$newId" })-[:HAS_TILE]->(tile:Tile)
                RETURN tile.coordinate.x AS x, tile.coordinate.y AS y, tile.type AS type
                """.trimIndent()
            ).map { tile(it["x"].asInt(), it["y"].asInt(), TileType.forNumber(it["type"].asInt())) }.toList()

            tiles shouldContainSame listOf(tile(0, 0, TileType.LAND), tile(0, 1, TileType.WATER))
        }
    }

    describe("find non-existing map by id") {
        val id = idSequence.next()
        val result: TileMap? by memoizedBlocking { repository.findById(id) }

        it("should return null") {
            result.shouldBeNull()
        }
    }

    describe("create empty map") {
        val id = idSequence.next()
        val exception by memoizedBlocking {
            runCatching { repository.create(TileMap.newBuilder().setId(id.toString()).build()) }.exceptionOrNull()
        }

        it("should fail") {
            exception.shouldNotBeNull()
        }
    }

    describe("given an existing map") {
        val mapId = idSequence.next()

        beforeEach {
            database.execute(
                """
                    CREATE
                        (map:TileMap { id: "$mapId" }),
                        (map)-[:HAS_TILE]->(:Tile { coordinate: point({ x: 0, y: 0}), type: 0}),
                        (map)-[:HAS_TILE]->(:Tile { coordinate: point({ x: 0, y: 1}), type: 1})
                """.trimIndent()
            )
        }

        describe("find existing map by id") {
            val map: TileMap? by memoizedBlocking { repository.findById(mapId) }

            it("should not return null") {
                map.shouldNotBeNull()
            }

            it("should return same map id") {
                map?.id shouldEqual mapId.toString()
            }

            it("should return same tiles set") {
                map?.tileList.orEmpty() shouldContainSame listOf(tile(0, 0, TileType.WATER), tile(0, 1, TileType.LAND))
            }
        }

        describe("given another existing map") {
            val otherMapId = idSequence.next()

            beforeEach {
                database.execute(
                    """
                        CREATE
                            (map:TileMap { id: "$otherMapId" }),
                            (map)-[:HAS_TILE]->(:Tile { coordinate: point({ x: 1, y: 0}), type: 1}),
                            (map)-[:HAS_TILE]->(:Tile { coordinate: point({ x: 1, y: 1}), type: 1})
                    """.trimIndent()
                )
            }

            describe("find all maps") {
                val maps: List<TileMap> by memoizedBlocking { repository.findAll().toList() }

                it("should return 2 maps") {
                    maps.size shouldEqual 2
                }

                it("should return same map ids") {
                    maps.map { it.id } shouldContainSame listOf(mapId, otherMapId).map { it.toString() }
                }

                it("should return same tile sets") {
                    maps.map { it.tileList.toSet() } shouldContainSame listOf(
                        setOf(tile(0, 0, TileType.WATER), tile(0, 1, TileType.LAND)),
                        setOf(tile(1, 0, TileType.LAND), tile(1, 1, TileType.LAND))
                    )
                }
            }
        }
    }
})
