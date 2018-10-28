package com.github.jcornaz.islands.domain

import kotlinx.coroutines.experimental.channels.*

fun detectIslands(map: TileMap): ReceiveChannel<Set<Coordinate>> = produce(capacity = Channel.UNLIMITED) {
    val seenCoordinates = hashSetOf<Coordinate>()

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
