package com.github.jcornaz.islands.endpoint

import com.github.jcornaz.islands.*
import com.github.jcornaz.islands.persistence.TestDatabase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.setBody
import org.amshove.kluent.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe
import java.util.*

class IslandsEndpointSpecification : Spek({
    val database by memoizedClosable(CachingMode.SCOPE) { TestDatabase() }
    val application by memoizedClosable { TestApplication(database.driver) }

    val mapIdSet = HashSet<String>()

    beforeEachTest {
        mapIdSet.clear()

        TestDataSet.maps.forEach { map ->
            val call = application.handleRequest(HttpMethod.Post, "/api/maps") {
                setBody(CreateTileMapRequest.newBuilder().addAllTile(map.tileList).build().toByteArray())
            }

            mapIdSet += TileMap.parseFrom(call.response.byteContent).id
        }
    }

    afterEachTest { database.clear() }

    describe("fetch all islands") {
        val fetchAllCall by memoized { application.handleRequest(HttpMethod.Get, "/api/islands") }
        val islands: List<Island> by memoized { IslandList.parseFrom(fetchAllCall.response.byteContent).islandList }

        itShouldHandleRequest { fetchAllCall }

        it("should return islands") {
            islands.shouldNotBeEmpty()
        }

        it("should return ${TestDataSet.islands.size} islands") {
            islands.size shouldEqualTo TestDataSet.islands.size
        }

        it("should return islands with id") {
            islands.forEach { it.id.shouldNotBeNullOrBlank() }
        }

        it("should return islands with map id") {
            islands.forEach { it.mapId.shouldNotBeNullOrBlank() }
        }

        it("should return islands from ${TestDataSet.maps.size} distinct maps") {
            islands.mapTo(HashSet()) { it.mapId }.size shouldEqualTo TestDataSet.maps.size
        }

        it("should return islands from created maps") {
            islands.map { it.mapId }.toSet() shouldContainSame mapIdSet
        }

        it("should return islands of expected coordinate sets") {
            islands.map { it.coordinateList.toSet() } shouldContainSame TestDataSet.islands.map { it.coordinateList.toSet() }
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

    describeBadRequest("fetch non-existing island", HttpMethod.Get, "/api/islands/${UUID(42L, 24L)}", expectedStatusCode = HttpStatusCode.NotFound) { application }
    describeBadRequest("fetch island by id with invalid id", HttpMethod.Get, "/api/islands/abc") { application }
})
