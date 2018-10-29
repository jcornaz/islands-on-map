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

data class Tile(val coordinate: Coordinate, val type: TileType) {
    val isLand: Boolean get() = type == TileType.LAND
}

data class Island(val id: Int, val coordinates: Set<Coordinate>)

typealias TileMap = Map<Coordinate, Tile>

fun Collection<Tile>.toTileMap(): TileMap =
        associate { it.coordinate to it }