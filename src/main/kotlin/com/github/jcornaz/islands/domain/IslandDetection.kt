package com.github.jcornaz.islands.domain

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

fun detectIslands(map: TileMap): ReceiveChannel<Set<Coordinate>> = GlobalScope.produce(capacity = Channel.UNLIMITED) {
    val seenCoordinates = hashSetOf<Coordinate>()

    @Suppress("unused")
    fun Coordinate.appendTo(island: MutableSet<Coordinate>) {
        if (this in seenCoordinates) return

        island += this
        seenCoordinates += this

        sequenceOf(up, down, left, right)
                .mapNotNull { map[it] }
                .filter { it.isLand }
                .forEach { it.coordinate.appendTo(island) }
    }

    map.values.filter { it.isLand }.forEach { tile ->
        if (tile.coordinate in seenCoordinates) return@forEach

        val coordinates = hashSetOf<Coordinate>()
                .also { tile.coordinate.appendTo(it) }

        send(coordinates)
    }
}
