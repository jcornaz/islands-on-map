package com.github.jcornaz.islands.endpoint

import com.github.jcornaz.islands.*
import com.github.jcornaz.islands.persistence.Neo4JTileMapRepository
import com.github.jcornaz.islands.persistence.TestDatabase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.setBody
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe
import java.util.*

class MapsEndpointSpecification : Spek({
    val database by memoizedClosable(CachingMode.SCOPE) { TestDatabase() }
    val application by memoizedClosable { TestApplication { maps(Neo4JTileMapRepository(database.driver)) } }

    beforeEachTest { database += TestDataSet }
    afterEachTest { database.clear() }

    describe("post map") {
        val inputMap = CreateTileMapRequest.newBuilder()
            .addTile(Tile.newBuilder().setCoordinate(Coordinate.newBuilder().setX(0).setY(0)).setType(TileType.LAND))
            .addTile(Tile.newBuilder().setCoordinate(Coordinate.newBuilder().setX(0).setY(1)).setType(TileType.WATER))
            .addTile(Tile.newBuilder().setCoordinate(Coordinate.newBuilder().setX(1).setY(0)).setType(TileType.WATER))
            .addTile(Tile.newBuilder().setCoordinate(Coordinate.newBuilder().setX(1).setY(1)).setType(TileType.LAND))
            .build()

        val createCall by memoized {
            application.handleRequest(HttpMethod.Post, "/api/maps") { setBody(inputMap.toByteArray()) }
        }

        it("should handle request") {
            createCall.requestHandled.shouldBeTrue()
        }

        it("should return ${HttpStatusCode.Created}") {
            createCall.response.status() shouldEqual HttpStatusCode.Created
        }

        describe("output map") {
            val outputMap by memoized { TileMap.parseFrom(createCall.response.byteContent) }

            it("should return a valid tile map") {
                outputMap.tileList shouldContainSame inputMap.tileList
            }

            it("should return a map with an id") {
                UUID.fromString(outputMap.id)
            }

            describe("fetch map list") {
                val fetchAllCall by memoized {
                    createCall.requestHandled.shouldBeTrue()
                    application.handleRequest(HttpMethod.Get, "/api/maps")
                }

                it("should handle request") {
                    fetchAllCall.requestHandled.shouldBeTrue()
                }

                it("should return ${HttpStatusCode.OK}") {
                    fetchAllCall.response.status() shouldEqual HttpStatusCode.OK
                }

                it("should contain created map") {
                    val map = TileMapList.parseFrom(fetchAllCall.response.byteContent).tileMapList.first { it.id == outputMap.id }
                    map.tileList shouldContainSame outputMap.tileList
                }
            }

            describe("fetch created map") {
                val fetchResult by memoized {
                    createCall.requestHandled.shouldBeTrue()
                    application.handleRequest(HttpMethod.Get, "/api/maps/${outputMap.id}")
                }

                val resultMap by memoized { TileMap.parseFrom(fetchResult.response.byteContent) }

                it("should return ${HttpStatusCode.OK}") {
                    fetchResult.response.status() shouldEqual HttpStatusCode.OK
                }

                it("should return the same map id") {
                    resultMap.id shouldEqual outputMap.id
                }

                it("should return same tile set") {
                    resultMap.tileList shouldContainSame outputMap.tileList
                }
            }
        }
    }

    describe("post empty map") {
        val response by memoized {
            application.handleRequest(HttpMethod.Post, "/api/maps") { setBody(CreateTileMapRequest.getDefaultInstance().toByteArray()) }
        }

        it("should return bad ${HttpStatusCode.BadRequest}") {
            response.response.status() shouldEqual HttpStatusCode.BadRequest
        }
    }

    describe("get map list") {
        val response by memoized { application.handleRequest(HttpMethod.Get, "/api/maps") }

        it("should handle request") {
            response.requestHandled.shouldBeTrue()
        }

        it("should return ${HttpStatusCode.OK}") {
            response.response.status() shouldEqual HttpStatusCode.OK
        }

        it("should return a valid array of TileMap") {
            TileMapList.parseFrom(response.response.byteContent).shouldNotBeNull()
        }
    }
})
