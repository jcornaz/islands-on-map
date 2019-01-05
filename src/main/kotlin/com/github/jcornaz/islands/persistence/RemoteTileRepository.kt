package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.Coordinate
import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileType
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlin.coroutines.CoroutineContext

class RemoteTileRepository(private val engine: HttpClientEngine, private val url: String) : TileRepository, CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default

    override fun findAll(): ReceiveChannel<Tile> = produce(capacity = Channel.UNLIMITED) {
        withClient {
            val answer = get<RemoteAnswer>(url)

            answer.attributes.tiles.forEach { (x, y, type) ->
                send(
                    Tile.newBuilder()
                        .setCoordinate(Coordinate.newBuilder().setX(x).setY(y))
                        .setType(tileTypeOf(type))
                        .build()
                )
            }
        }
    }

    private fun tileTypeOf(value: String): TileType = when (value) {
        "land" -> TileType.LAND
        "water" -> TileType.WATER
        else -> TileType.UNRECOGNIZED
    }

    private inline fun <R> withClient(block: HttpClient.() -> R): R {
        val client = HttpClient(engine) {
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }

        return client.use(block)
    }

    private data class RemoteAnswer(val attributes: RemoteAttributes)
    private data class RemoteAttributes(val tiles: List<RemoteTile>)
    private data class RemoteTile(val x: Int, val y: Int, val type: String)
}
