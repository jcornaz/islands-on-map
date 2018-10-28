package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.persistence.persistenceModule
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.experimental.io.ByteReadChannel
import org.koin.dsl.module.module

private const val expectedRemoteAnswer = """
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

internal fun Tile(x: Int, y: Int, type: TileType) = Tile(Coordinate(x, y), type)

val expectedTiles = listOf(
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

/**
 * Always returns [expectedRemoteAnswer]
 */
val testDataMockEngine = MockEngine {
    MockHttpResponse(
        call,
        HttpStatusCode.OK,
        ByteReadChannel(expectedRemoteAnswer.toByteArray()),
        headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
    )
}

private val testHttpModule = module { single<HttpClientEngine> { testDataMockEngine } }

val testModules = listOf(testHttpModule, persistenceModule)
