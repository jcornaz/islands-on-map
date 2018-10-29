package com.github.jcornaz.islands.persistence.impl

import com.github.jcornaz.islands.domain.Coordinate
import com.github.jcornaz.islands.domain.Tile
import com.github.jcornaz.islands.domain.TileType
import com.github.jcornaz.islands.persistence.TileRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlin.coroutines.CoroutineContext

private const val URL = "https://private-2e8649-advapi.apiary-mock.com/map"

class RemoteTileRepository(engine: HttpClientEngine) : TileRepository, CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default

    private val client = HttpClient(engine) {
        install(JsonFeature)
    }

    override fun findAll(): ReceiveChannel<Tile> = produce(capacity = Channel.UNLIMITED) {
        val answer = client.get<RemoteAnswer>(URL)

        answer.attributes.tiles.forEach { (x, y, type) ->
            send(
                    Tile(Coordinate(x, y), when (type) {
                        "land" -> TileType.LAND
                        "water" -> TileType.WATER
                        else -> throw Exception("Unexpected tile type: $type")
                    })
            )
        }
    }

    private data class RemoteAnswer(val attributes: RemoteAttributes)
    private data class RemoteAttributes(val tiles: List<RemoteTile>)
    private data class RemoteTile(val x: Int, val y: Int, val type: String)
}
