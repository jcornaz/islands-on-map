package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.test.TestDatabase
import com.github.jcornaz.islands.test.coordinate
import com.github.jcornaz.islands.test.memoizedClosable
import com.github.jcornaz.islands.test.tile
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.setBody
import org.amshove.kluent.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe
import java.util.*

class IslandsEndpointSpec : Spek({
    val database by memoizedClosable(CachingMode.SCOPE) { TestDatabase() }
    val application by memoizedClosable { TestApplication(database.driver) }

    afterEachTest { database.clear() }

    describe("given 2 created maps") {
        lateinit var maps: List<TileMap>

        beforeEach {
            maps = listOf(
                CreateTileMapRequest.newBuilder()
                    .addTile(tile(0, 0, TileType.LAND))
                    .addTile(tile(0, 1, TileType.WATER))
                    .addTile(tile(1, 0, TileType.WATER))
                    .addTile(tile(1, 1, TileType.LAND))
                    .build(),
                CreateTileMapRequest.newBuilder()
                    .addTile(tile(0, 0, TileType.WATER))
                    .addTile(tile(0, 1, TileType.LAND))
                    .addTile(tile(1, 0, TileType.LAND))
                    .addTile(tile(1, 1, TileType.LAND))
                    .build()
            ).map { request ->
                application.handleRequest(HttpMethod.Post, "/api/maps") { setBody(request.toByteArray()) }
                    .response.byteContent.let(TileMap::parseFrom)
            }
        }

        describe("fetch all islands") {
            val fetchAllCall by memoized { application.handleRequest(HttpMethod.Get, "/api/islands") }
            val islands: List<Island> by memoized { IslandList.parseFrom(fetchAllCall.response.byteContent).islandList }

            itShouldHandleRequest { fetchAllCall }

            it("should return islands") {
                islands.shouldNotBeEmpty()
            }

            it("should return 3 islands") {
                islands.size shouldEqual 3
            }

            it("should return islands with id") {
                islands.forEach { it.id.shouldNotBeNullOrBlank() }
            }

            it("should return islands with map id") {
                islands.forEach { it.mapId.shouldNotBeNullOrBlank() }
            }

            it("should return islands from 2 distinct maps") {
                islands.mapTo(HashSet()) { it.mapId }.size shouldEqual 2
            }

            it("should return islands from created maps") {
                islands.map { it.mapId }.toSet() shouldContainSame maps.map { it.id }
            }

            it("should return islands with coordinates") {
                islands.forEach { it.coordinateCount shouldBeGreaterThan 0 }
            }

            it("should return islands with expected coordinates") {
                islands.filter { it.mapId == maps[0].id }.map { it.coordinateList.toSet() }
                    .shouldContainSame(listOf(setOf(coordinate(0, 0)), setOf(coordinate(1, 1))))

                islands.single { it.mapId == maps[1].id }.coordinateList
                    .shouldContainSame(listOf(coordinate(0, 1), coordinate(1, 0), coordinate(1, 1)))
            }

            describe("fetch island by id") {
                val id by memoized { islands.first().id }
                val fetchByIdCall by memoized { application.handleRequest(HttpMethod.Get, "api/islands/$id") }
                val result by memoized { Island.parseFrom(fetchByIdCall.response.byteContent) }

                itShouldHandleRequest { fetchByIdCall }

                it("should not return null") {
                    result.shouldNotBeNull()
                }

                it("should return island with id") {
                    result.id shouldEqual id
                }

                it("should return island with expected coordinates") {
                    result.coordinateList shouldContainSame islands.first { it.id == id }.coordinateList
                }

                it("should return island from expected map") {
                    result.mapId shouldEqual islands.first { it.id == id }.mapId
                }
            }
        }
    }

    describeBadRequest("fetch non-existing island", HttpMethod.Get, "/api/islands/${UUID(42L, 24L)}", expectedStatusCode = HttpStatusCode.NotFound) { application }
    describeBadRequest("fetch island by id with invalid id", HttpMethod.Get, "/api/islands/abc") { application }
})
