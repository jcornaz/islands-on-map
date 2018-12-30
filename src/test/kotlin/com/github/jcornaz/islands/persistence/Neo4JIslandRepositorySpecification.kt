package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.*
import kotlinx.coroutines.channels.toList
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe
import java.util.*

class Neo4JIslandRepositorySpecification : Spek({
    val database by memoizedClosable(CachingMode.SCOPE) { TestDatabase() }
    val repository: IslandRepository by memoized { Neo4JIslandRepository(database.driver) }

    val ids = generateSequence(0) { it + 1 }.map { UUID(0L, it.toLong()) }.iterator()

    afterEachTest { database.clear() }

    fun Suite.itShouldFailToCreateInvalidIsland(island: Island) {
        var exception: Throwable? = null

        beforeEachBlocking {
            exception = runCatching { repository.create(island) }.exceptionOrNull()
        }

        it("should fail to create the island") {
            exception.shouldNotBeNull()
        }

        it("should not create the island") {
            database.execute("MATCH (island:Island { id: \"${island.id}\" }) RETURN count(island)")
                .map { it[0].asInt() }.single() shouldEqual 0
        }
    }

    describe("create island for a non-existing map") {
        itShouldFailToCreateInvalidIsland(
            Island.newBuilder()
                .setId(ids.next().toString())
                .setMapId(ids.next().toString())
                .addCoordinate(Coordinate(0, 0))
                .build()
        )
    }

    describe("given an existing map") {
        val mapId = ids.next()

        beforeEach {
            database.execute(
                """
                    CREATE
                        (map:TileMap { id: "$mapId" }),
                        (map)-[:HAS_TILE]->(:Tile { coordinate: point({x: 0, y: 0}), type: 1 }),
                        (map)-[:HAS_TILE]->(:Tile { coordinate: point({x: 0, y: 1}), type: 1 }),
                        (map)-[:HAS_TILE]->(:Tile { coordinate: point({x: 1, y: 0}), type: 0 }),
                        (map)-[:HAS_TILE]->(:Tile { coordinate: point({x: 1, y: 1}), type: 0 })
                """.trimIndent()
            )
        }

        describe("create empty island") {
            itShouldFailToCreateInvalidIsland(Island.getDefaultInstance())
        }

        describe("create island with non-existing coordinate") {
            itShouldFailToCreateInvalidIsland(
                Island.newBuilder()
                    .setId(ids.next().toString())
                    .setMapId(mapId.toString())
                    .addCoordinate(Coordinate(2, 2))
                    .build()
            )
        }

        describe("create new island") {
            val islandId = ids.next()

            beforeEachBlocking {
                repository.create(
                    Island.newBuilder()
                        .setId(islandId.toString())
                        .setMapId(mapId.toString())
                        .addCoordinate(Coordinate(0, 0))
                        .addCoordinate(Coordinate(0, 1))
                        .build()
                )
            }

            it("should create the given island") {
                database.execute("MATCH (n:Island { id: \"$islandId\" }) RETURN count(n)")
                    .map { it[0].asInt() }.single() shouldEqual 1
            }

            it("should attach tiles to created island") {
                database.execute("MATCH (:Island { id: \"$islandId\" })-[:HAS_TILE]->(tile:Tile) RETURN tile.coordinate.x, tile.coordinate.y")
                    .map { Coordinate(it[0].asInt(), it[1].asInt()) }
                    .toList() shouldContainSame listOf(Coordinate(0, 0), Coordinate(0, 1))
            }

            it("should attach created island to the map") {
                database.execute("MATCH (:TileMap { id: \"$mapId\" })-[r:HAS_ISLAND]->(:Island { id: \"$islandId\" }) RETURN count(r)")
                    .map { it[0].asInt() }.single() shouldEqual 1
            }
        }

        describe("given an existing island") {
            val islandId = ids.next()

            beforeEach {
                database.execute(
                    """
                        MATCH (map:TileMap { id: "$mapId" })
                        CREATE (map)-[:HAS_ISLAND]->(island:Island { id: "$islandId" })
                    """.trimIndent()
                )

                database.execute(
                    """
                        MATCH (island:Island { id: "$islandId" })<-[:HAS_ISLAND]-(:TileMap)-[:HAS_TILE]->(tile:Tile { type: 1 })
                        CREATE (island)-[:HAS_TILE]->(tile)
                    """.trimIndent()
                )
            }

            describe("create existing island") {
                itShouldFailToCreateInvalidIsland(
                    Island.newBuilder()
                        .setId(ids.next().toString())
                        .setMapId(mapId.toString())
                        .addCoordinate(Coordinate(2, 2))
                        .build()
                )
            }

            describe("find by id the existing island") {
                val result by memoizedBlocking { repository.findById(islandId) }

                it("should not return null") {
                    result.shouldNotBeNull()
                }

                it("should return island with given id") {
                    result?.id shouldEqual islandId.toString()
                }

                it("should return island with expected map id") {
                    result?.mapId shouldEqual mapId.toString()
                }

                it("should return island with expected coordinates") {
                    result?.coordinateList.orEmpty() shouldContainSame listOf(Coordinate(0, 0), Coordinate(0, 1))
                }
            }

            describe("find by id a non-existing island") {
                val id = ids.next()
                val result by memoizedBlocking { repository.findById(id) }

                it("should return null") {
                    result.shouldBeNull()
                }
            }

            describe("given another existing map") {
                val otherMapId = ids.next()

                beforeEach {
                    database.execute(
                        """
                            CREATE
                                (map:TileMap { id: "$otherMapId" }),
                                (map)-[:HAS_TILE]->(:Tile { coordinate: point({x: 0, y: 0}), type: 0 }),
                                (map)-[:HAS_TILE]->(:Tile { coordinate: point({x: 0, y: 1}), type: 1 }),
                                (map)-[:HAS_TILE]->(:Tile { coordinate: point({x: 1, y: 0}), type: 1 }),
                                (map)-[:HAS_TILE]->(:Tile { coordinate: point({x: 1, y: 1}), type: 1 })
                        """.trimIndent()
                    )
                }

                describe("create new island") {
                    val newIslandId = ids.next()

                    beforeEachBlocking {
                        repository.create(
                            Island.newBuilder()
                                .setId(newIslandId.toString())
                                .setMapId(otherMapId.toString())
                                .addCoordinate(Coordinate(0, 1))
                                .addCoordinate(Coordinate(1, 0))
                                .addCoordinate(Coordinate(1, 1))
                                .build()
                        )
                    }

                    it("should create the given island") {
                        database.execute("MATCH (n:Island { id: \"$newIslandId\" }) RETURN count(n)")
                            .map { it[0].asInt() }.single() shouldEqual 1
                    }

                    it("should attach tiles to created island") {
                        database.execute("MATCH (:Island { id: \"$newIslandId\" })-[:HAS_TILE]->(tile:Tile) RETURN tile.coordinate.x, tile.coordinate.y")
                            .map { Coordinate(it[0].asInt(), it[1].asInt()) }
                            .toList() shouldContainSame listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(1, 1))
                    }

                    it("should attach created island to the map") {
                        database.execute("MATCH (:TileMap { id: \"$otherMapId\" })-[r:HAS_ISLAND]->(:Island { id: \"$newIslandId\" }) RETURN count(r)")
                            .map { it[0].asInt() }.single() shouldEqual 1
                    }
                }

                describe("given another island") {
                    val otherIslandId = ids.next()

                    beforeEach {
                        database.execute(
                            """
                                MATCH (map:TileMap { id: "$otherMapId" })
                                CREATE (map)-[:HAS_ISLAND]->(island:Island { id: "$otherIslandId" })
                            """.trimIndent()
                        )

                        database.execute(
                            """
                                MATCH (island:Island { id: "$otherIslandId" })<-[:HAS_ISLAND]-(:TileMap { id: "$otherMapId" })-[:HAS_TILE]->(tile:Tile { type: 1 })
                                CREATE (island)-[:HAS_TILE]->(tile)
                            """.trimIndent()
                        )
                    }

                    describe("find all islands") {
                        val islandList by memoizedBlocking { repository.findAll().toList() }

                        it("should return 2 islands") {
                            islandList.size shouldEqual 2
                        }

                        it("should return expected island ids") {
                            islandList.map { it.id } shouldContainSame listOf(islandId, otherIslandId).map { it.toString() }
                        }

                        describe("first island") {
                            val island by memoized { islandList.first { it.id == islandId.toString() } }

                            it("should have given id") {
                                island.id shouldEqual islandId.toString()
                            }

                            it("should have expected map id") {
                                island.mapId shouldEqual mapId.toString()
                            }

                            it("should contains expected coordinates") {
                                island.coordinateList shouldContainSame listOf(Coordinate(0, 0), Coordinate(0, 1))
                            }
                        }

                        describe("other island") {
                            val island by memoized { islandList.first { it.id == otherIslandId.toString() } }

                            it("should have given id") {
                                island.id shouldEqual otherIslandId.toString()
                            }

                            it("should have expected map id") {
                                island.mapId shouldEqual otherMapId.toString()
                            }

                            it("should contains expected coordinates") {
                                island.coordinateList shouldContainSame listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(1, 1))
                            }
                        }
                    }
                }
            }
        }
    }
})
