package com.github.jcornaz.islands.test

import com.github.jcornaz.islands.Coordinate
import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileType

fun coordinate(x: Int, y: Int): Coordinate = Coordinate.newBuilder().setX(x).setY(y).build()
fun tile(x: Int, y: Int, type: TileType): Tile = Tile.newBuilder().setCoordinate(coordinate(x, y)).setType(type).build()
