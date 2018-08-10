package com.github.jcornaz.islands.domain

fun TileMap.detectIslands(): Collection<Island> {
  val map = this
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

  val result = mutableSetOf<Island>()

  values.filter { it.isLand }.forEach { tile ->
    if (tile.coordinate in seenCoordinates) return@forEach

    val coordinates = hashSetOf<Coordinate>()
      .also { tile.coordinate.appendTo(it) }

    result += Island(coordinates)
  }

  return result
}
