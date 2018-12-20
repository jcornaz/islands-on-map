package com.github.jcornaz.islands.domain

/**
 * Assumes that x axis is from left to right, and y axis is from down to up.
 */
data class Coordinate(val x: Int, val y: Int) {
    val up: Coordinate get() = copy(y = y + 1)
    val down: Coordinate get() = copy(y = y - 1)
    val left: Coordinate get() = copy(x = x - 1)
    val right: Coordinate get() = copy(x = x + 1)
}

enum class TileType { WATER, LAND }

data class Tile(val coordinate: Coordinate, val type: TileType)

data class Island(val id: Int, val coordinates: Set<Coordinate>)

fun Iterable<Tile>.toTileMap(): Map<Coordinate, TileType> =
        associate { it.coordinate to it.type }