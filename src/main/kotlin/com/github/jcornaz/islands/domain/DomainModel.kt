package com.github.jcornaz.islands.domain

/**
 * Assumes that x axis is from left to right, and y axis is from down to up.
 */
data class Coordinate(val x: Int, val y: Int) {
  val up: Coordinate by lazy { copy(y = y + 1) }
  val down: Coordinate by lazy { copy(y = y - 1) }
  val left: Coordinate by lazy { copy(x = x - 1) }
  val right: Coordinate by lazy { copy(x = x + 1) }
}

enum class TileType { WATER, LAND }

data class Tile(val coordinate: Coordinate, val type: TileType) {
  val isLand: Boolean get() = type == TileType.LAND

  constructor(x: Int, y: Int, type: TileType) : this(Coordinate(x, y), type)
}

data class Island(val coordinates: Set<Coordinate>) {
  val size: Int get() = coordinates.size
}

typealias TileMap = Map<Coordinate, Tile>

fun Collection<Tile>.toTileMap(): TileMap =
  associate { it.coordinate to it }