package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.Coordinate
import com.github.jcornaz.islands.Island
import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import java.util.*

object DefaultIslandDetector : IslandDetector {
    override fun detectIslands(tiles: Iterable<Tile>): ReceiveChannel<Island> = GlobalScope.produce {
        val unseenCoordinates = HashSet<Coordinate>()
        val map = HashMap<Coordinate, TileType>()

        tiles.asSequence().filter { it.type != TileType.WATER }.forEach {
            unseenCoordinates += it.coordinate
            map[it.coordinate] = it.type
        }

        while (unseenCoordinates.isNotEmpty()) {
            val island = Island.newBuilder().setId(UUID.randomUUID().toString())

            fun append(coordinate: Coordinate) {
                island.addCoordinate(coordinate)

                sequenceOf(coordinate.up, coordinate.down, coordinate.left, coordinate.right)
                    .filter { it in map }
                    .forEach {
                        if (unseenCoordinates.remove(it)) append(it)
                    }
            }

            append(unseenCoordinates.pop())

            send(island.build())
        }
    }

    private fun <E> MutableSet<E>.pop(): E = first().also { remove(it) }

    private val Coordinate.up: Coordinate get() = Coordinate.newBuilder(this).setY(y + 1).build()
    private val Coordinate.down: Coordinate get() = Coordinate.newBuilder(this).setY(y - 1).build()
    private val Coordinate.left: Coordinate get() = Coordinate.newBuilder(this).setX(x - 1).build()
    private val Coordinate.right: Coordinate get() = Coordinate.newBuilder(this).setX(x + 1).build()
}
