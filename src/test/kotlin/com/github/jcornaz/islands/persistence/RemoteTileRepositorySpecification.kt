package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.io.ByteReadChannel
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

class RemoteTileRepositorySpecification : Spek({

    describe("expected data") {
        val engine by memoizedClosable(CachingMode.SCOPE) {
            MockEngine {
                MockHttpResponse(
                    call,
                    HttpStatusCode.OK,
                    ByteReadChannel(expectedRemoteAnswer.toByteArray()),
                    headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                )
            }
        }

        val result by memoizedBlocking(CachingMode.SCOPE) { RemoteTileRepository(engine).findAll().toList() }

        it("should return ${expectedTiles.size} tiles") {
            result.size shouldEqualTo expectedTiles.size
        }

        it("should return expected tiles") {
            result shouldContainSame expectedTiles
        }
    }

    group("status codes") {
        HttpStatusCode.allStatusCodes.asSequence()
            .filterNot { it.isSuccess() }
            .forEach { status ->
                describe("remote returns $status") {
                    val engine by memoizedClosable { MockEngine { MockHttpResponse(call, status) } }
                    val repository by memoized { RemoteTileRepository(engine) }

                    it("should fail") {
                        assertFailsBlocking<Exception> {
                            repository.findAll().toList()
                        }
                    }
                }
            }
    }
})

const val expectedRemoteAnswer = """
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

private val expectedTiles = listOf(
    Tile(1, 1, TileType.LAND),
    Tile(2, 1, TileType.LAND),
    Tile(3, 1, TileType.WATER),
    Tile(4, 1, TileType.WATER),
    Tile(5, 1, TileType.LAND),
    Tile(6, 1, TileType.WATER),
    Tile(1, 2, TileType.WATER),
    Tile(2, 2, TileType.LAND),
    Tile(3, 2, TileType.WATER),
    Tile(4, 2, TileType.WATER),
    Tile(5, 2, TileType.WATER),
    Tile(6, 2, TileType.WATER),
    Tile(1, 3, TileType.WATER),
    Tile(2, 3, TileType.WATER),
    Tile(3, 3, TileType.WATER),
    Tile(4, 3, TileType.WATER),
    Tile(5, 3, TileType.LAND),
    Tile(6, 3, TileType.WATER),
    Tile(1, 4, TileType.WATER),
    Tile(2, 4, TileType.WATER),
    Tile(3, 4, TileType.LAND),
    Tile(4, 4, TileType.LAND),
    Tile(5, 4, TileType.LAND),
    Tile(6, 4, TileType.WATER),
    Tile(1, 5, TileType.WATER),
    Tile(2, 5, TileType.WATER),
    Tile(3, 5, TileType.WATER),
    Tile(4, 5, TileType.LAND),
    Tile(5, 5, TileType.WATER),
    Tile(6, 5, TileType.WATER)
)
