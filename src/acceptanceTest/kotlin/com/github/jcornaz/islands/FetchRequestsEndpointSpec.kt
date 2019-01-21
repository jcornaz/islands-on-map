package com.github.jcornaz.islands

import com.github.jcornaz.islands.test.*
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.testing.TestApplicationCall
import kotlinx.coroutines.delay
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNullOrBlank
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

class FetchRequestEndpointSpec : Spek({
    val database by memoizedClosable(CachingMode.SCOPE) { TestDatabase() }
    val application by memoizedClosable { TestApplication(database.url, REMOTE_URL to REMOTE_ANSWER) }

    afterEachTest {
        database.clear()
    }

    describe("create valid fetch request") {
        lateinit var createCall: TestApplicationCall

        val initialFetchRequest by memoized { FetchRequest.parseFrom(createCall.response.byteContent) }

        beforeGroup {
            createCall = application.handleRequest(HttpMethod.Post, "/api/maps/fetch-requests") {
                setBody(CreateFetchRequest.newBuilder().setUrl(REMOTE_URL.toString()).build())
            }
        }

        itShouldHandleRequest(HttpStatusCode.Created) { createCall }

        it("should return pending fetch-request") {
            initialFetchRequest.status shouldEqual FetchRequest.Status.PENDING
        }

        it("should return a fetch-request with same url") {
            initialFetchRequest.url shouldEqual REMOTE_URL.toString()
        }

        describe("get fetch-request") {
            val getRequestCall by memoized {
                application.handleRequest(HttpMethod.Get, "/api/maps/fetch-requests/${initialFetchRequest.id}")
            }

            itShouldHandleRequest { getRequestCall }

            it("should return a fetch-request with same id") {
                FetchRequest.parseFrom(getRequestCall.response.byteContent).id shouldEqual initialFetchRequest.id
            }

            it("should return a fetch-request with same url") {
                FetchRequest.parseFrom(getRequestCall.response.byteContent).url shouldEqual REMOTE_URL.toString()
            }
        }

        describe("fetch completed") {
            val finalFetchRequest by memoizedBlocking {
                var request = initialFetchRequest

                while (request.status != FetchRequest.Status.DONE) {
                    delay(100)

                    request = application.handleRequest(HttpMethod.Get, "/api/maps/fetch-requests/${initialFetchRequest.id}")
                        .response.byteContent.let(FetchRequest::parseFrom)
                }

                request
            }

            it("should return same fetch-request id") {
                finalFetchRequest.id shouldEqual initialFetchRequest.id
            }

            it("should return same url") {
                finalFetchRequest.url shouldEqual REMOTE_URL.toString()
            }

            it("should be successful") {
                finalFetchRequest.resultCase shouldEqual FetchRequest.ResultCase.MAP_ID
            }

            it("should contains the id of created map") {
                finalFetchRequest.mapId.shouldNotBeNullOrBlank()
            }

            describe("fetch created map") {
                val call by memoized {
                    application.handleRequest(HttpMethod.Get, "/api/maps/${finalFetchRequest.mapId}")
                }

                itShouldHandleRequest { call }

                it("should return map with expected id") {
                    TileMap.parseFrom(call.response.byteContent).id shouldEqual finalFetchRequest.mapId
                }
            }

            describe("fetch islands") {
                val call by memoized {
                    application.handleRequest(HttpMethod.Get, "/api/islands")
                }

                itShouldHandleRequest { call }

                it("should return islands from the created map") {
                    IslandList.parseFrom(call.response.byteContent)
                        .islandList.any { it.mapId == finalFetchRequest.mapId }.shouldBeTrue()
                }
            }
        }
    }

    describe("create fetch-request without url") {
        itShouldHandleRequest(HttpStatusCode.BadRequest) {
            application.handleRequest(HttpMethod.Post, "/api/maps/fetch-requests") {
                setBody(CreateFetchRequest.getDefaultInstance())
            }
        }
    }

    describe("create fetch request for invalid url") {
        val call by memoized {
            application.handleRequest(HttpMethod.Post, "/api/maps/fetch-requests") {
                setBody(CreateFetchRequest.newBuilder().setUrl("invalid-url.net").build())
            }
        }

        itShouldHandleRequest(HttpStatusCode.Created) { call }

        describe("fetch completed") {
            val initialFetchRequest by memoized { FetchRequest.parseFrom(call.response.byteContent) }

            val finalFetchRequest by memoizedBlocking {
                var request = initialFetchRequest

                while (request.status != FetchRequest.Status.DONE) {
                    delay(100)

                    request = application.handleRequest(HttpMethod.Get, "/api/maps/fetch-requests/${initialFetchRequest.id}")
                        .response.byteContent.let(FetchRequest::parseFrom)
                }

                request
            }

            it("should be failure") {
                finalFetchRequest.resultCase shouldEqual FetchRequest.ResultCase.ERROR
            }

            it("should return an error message") {
                finalFetchRequest.error.shouldNotBeNullOrBlank()
            }
        }
    }
})

private val REMOTE_URL = Url("https://private-2e8649-advapi.apiary-mock.com/map")

private const val REMOTE_ANSWER = """
{
    "data": {
        "id": "imaginary",
        "type": "map",
        "links": {
            "self": "https://private-2e8649-advapi.apiary-mock.com/map"
        }
    },
    "attributes": {
        "tiles": [
            { "x": 1, "y": 1, "type": "land" },
            { "x": 2, "y": 1, "type": "land" },
            { "x": 3, "y": 1, "type": "water" },
            { "x": 4, "y": 1, "type": "water" },
            { "x": 5, "y": 1, "type": "land" },
            { "x": 6, "y": 1, "type": "water" },
            { "x": 1, "y": 2, "type": "water" },
            { "x": 2, "y": 2, "type": "land" },
            { "x": 3, "y": 2, "type": "water" },
            { "x": 4, "y": 2, "type": "water" },
            { "x": 5, "y": 2, "type": "water" },
            { "x": 6, "y": 2, "type": "water" },
            { "x": 1, "y": 3, "type": "water" },
            { "x": 2, "y": 3, "type": "water" },
            { "x": 3, "y": 3, "type": "water" },
            { "x": 4, "y": 3, "type": "water" },
            { "x": 5, "y": 3, "type": "land" },
            { "x": 6, "y": 3, "type": "water" },
            { "x": 1, "y": 4, "type": "water" },
            { "x": 2, "y": 4, "type": "water" },
            { "x": 3, "y": 4, "type": "land" },
            { "x": 4, "y": 4, "type": "land" },
            { "x": 5, "y": 4, "type": "land" },
            { "x": 6, "y": 4, "type": "water" },
            { "x": 1, "y": 5, "type": "water" },
            { "x": 2, "y": 5, "type": "water" },
            { "x": 3, "y": 5, "type": "water" },
            { "x": 4, "y": 5, "type": "land" },
            { "x": 5, "y": 5, "type": "water" },
            { "x": 6, "y": 5, "type": "water" }
        ]
    }
}
"""