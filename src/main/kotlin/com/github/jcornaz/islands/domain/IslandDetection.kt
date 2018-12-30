package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.Coordinate
import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileType

fun Iterable<Tile>.detectIslands(): Collection<Set<Coordinate>> {
    val result = ArrayList<Set<Coordinate>>()
    val unseenCoordinates = HashSet<Coordinate>()
    val map = HashMap<Coordinate, TileType>()

    filter { it.type != TileType.WATER }.forEach {
        unseenCoordinates += it.coordinate
        map[it.coordinate] = it.type
    }

    while (unseenCoordinates.isNotEmpty()) {
        val island = HashSet<Coordinate>()

        fun append(coordinate: Coordinate) {
            island += coordinate

            sequenceOf(coordinate.up, coordinate.down, coordinate.left, coordinate.right)
                .filter { it in map }
                .forEach {
                    if (unseenCoordinates.remove(it)) append(it)
                }
        }

        append(unseenCoordinates.pop())

        result += island
    }

    return result
}

private fun <E> MutableSet<E>.pop(): E = first().also { remove(it) }

private val Coordinate.up: Coordinate get() = Coordinate.newBuilder(this).setY(y + 1).build()
private val Coordinate.down: Coordinate get() = Coordinate.newBuilder(this).setY(y - 1).build()
private val Coordinate.left: Coordinate get() = Coordinate.newBuilder(this).setX(x - 1).build()
private val Coordinate.right: Coordinate get() = Coordinate.newBuilder(this).setX(x + 1).build()
