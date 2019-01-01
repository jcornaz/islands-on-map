package com.github.jcornaz.islands

import com.github.jcornaz.islands.test.*
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.setBody
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldNotBeNullOrBlank
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe
import java.util.*

class MapsEndpointSpec : Spek({
    val database by memoizedClosable(CachingMode.SCOPE) { TestDatabase() }
    val application by memoizedClosable(CachingMode.GROUP) { TestApplication(database.driver) }

    afterEachTest { database.clear() }

    describe("create map") {
        val inputMap = CreateTileMapRequest.newBuilder()
            .addTile(Tile.newBuilder().setCoordinate(Coordinate.newBuilder().setX(0).setY(0)).setType(TileType.LAND))
            .addTile(Tile.newBuilder().setCoordinate(Coordinate.newBuilder().setX(0).setY(1)).setType(TileType.WATER))
            .addTile(Tile.newBuilder().setCoordinate(Coordinate.newBuilder().setX(1).setY(0)).setType(TileType.WATER))
            .addTile(Tile.newBuilder().setCoordinate(Coordinate.newBuilder().setX(1).setY(1)).setType(TileType.LAND))
            .build()

        lateinit var createCall: TestApplicationCall

        beforeEachTest {
            createCall = application.handleRequest(HttpMethod.Post, "/api/maps") { setBody(inputMap.toByteArray()) }
        }

        itShouldHandleRequest(HttpStatusCode.Created) { createCall }

        describe("given the created map") {
            val outputMap by memoized { TileMap.parseFrom(createCall.response.byteContent) }

            it("should contains a valid tile set") {
                outputMap.tileList shouldContainSame inputMap.tileList
            }

            it("should contains a valid id") {
                outputMap.id.shouldNotBeNullOrBlank()
            }

            describe("fetch map list") {
                val fetchAllCall by memoized { application.handleRequest(HttpMethod.Get, "/api/maps") }

                itShouldHandleRequest { fetchAllCall }

                it("should return created map") {
                    val map = TileMapList.parseFrom(fetchAllCall.response.byteContent).tileMapList.first { it.id == outputMap.id }
                    map.tileList shouldContainSame outputMap.tileList
                }
            }

            describe("fetch created map") {
                val fetchByIdCall by memoized { application.handleRequest(HttpMethod.Get, "/api/maps/${outputMap.id}") }

                val resultMap by memoized { TileMap.parseFrom(fetchByIdCall.response.byteContent) }

                itShouldHandleRequest { fetchByIdCall }

                it("should return the same map id") {
                    resultMap.id shouldEqual outputMap.id
                }

                it("should return same tile set") {
                    resultMap.tileList shouldContainSame outputMap.tileList
                }
            }
        }
    }

    describe("fetch map list") {
        val call by memoized { application.handleRequest(HttpMethod.Get, "/api/maps") }

        itShouldHandleRequest { call }

        it("should return a valid array of TileMap") {
            TileMapList.parseFrom(call.response.byteContent).shouldNotBeNull()
        }
    }

    describeBadRequest("create with invalid body", HttpMethod.Post, "/api/maps", CreateTileMapRequest.getDefaultInstance()) { application }
    describeBadRequest("fetch non-existing map", HttpMethod.Get, "/api/maps/${UUID(42L, 24L)}", expectedStatusCode = HttpStatusCode.NotFound) { application }
    describeBadRequest("fetch map by id with invalid id", HttpMethod.Get, "/api/maps/abc") { application }
})
