package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.TileType
import com.github.jcornaz.islands.test.memoizedBlocking
import com.github.jcornaz.islands.test.memoizedClosable
import com.github.jcornaz.islands.test.tile
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.http.*
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.io.ByteReadChannel
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class RemoteTileRepositorySpec : Spek({

    describe("expected data") {
        val engine by memoizedClosable {
            MockEngine {
                url shouldEqual Url("http://my-url.net/")

                MockHttpResponse(
                    call,
                    HttpStatusCode.OK,
                    ByteReadChannel(
                        """
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
                                        { "x": 4, "y": 1, "type": "water" }
                                    ]
                                }
                            }
                        """
                    ),
                    headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                )
            }
        }

        val result by memoizedBlocking { RemoteTileRepository(engine, "http://my-url.net/").findAll().toList() }

        it("should return 4 tiles") {
            result.size shouldEqual 4
        }

        it("should return expected tiles") {
            result shouldContainSame listOf(
                tile(1, 1, TileType.LAND),
                tile(2, 1, TileType.LAND),
                tile(3, 1, TileType.WATER),
                tile(4, 1, TileType.WATER)
            )
        }
    }

    group("status codes") {
        HttpStatusCode.allStatusCodes.asSequence()
            .filterNot { it.isSuccess() }
            .forEach { status ->
                describe("remote returning $status") {
                    val engine by memoizedClosable {
                        MockEngine {
                            url shouldEqual Url("http://failing-url.net/")
                            MockHttpResponse(call, status)
                        }
                    }
                    val repository by memoized { RemoteTileRepository(engine, "http://failing-url.net/") }
                    val exception by memoizedBlocking { runCatching { repository.findAll().toList() }.exceptionOrNull() }

                    it("should fail") {
                        exception.shouldNotBeNull()
                    }
                }
            }
    }
})
