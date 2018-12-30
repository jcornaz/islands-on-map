package com.github.jcornaz.islands

import java.util.*

fun Coordinate(x: Int, y: Int): Coordinate = Coordinate.newBuilder().setX(x).setY(y).build()
fun Tile(x: Int, y: Int, type: TileType): Tile = Tile.newBuilder().setCoordinate(Coordinate(x, y)).setType(type).build()

object TestDataSet {
    val providedMap: TileMap = TileMap.newBuilder()
        .setId(UUID(0L, 1L).toString())
        .addTile(Tile(1, 1, TileType.LAND))
        .addTile(Tile(2, 1, TileType.LAND))
        .addTile(Tile(3, 1, TileType.WATER))
        .addTile(Tile(4, 1, TileType.WATER))
        .addTile(Tile(5, 1, TileType.LAND))
        .addTile(Tile(6, 1, TileType.WATER))
        .addTile(Tile(1, 2, TileType.WATER))
        .addTile(Tile(2, 2, TileType.LAND))
        .addTile(Tile(3, 2, TileType.WATER))
        .addTile(Tile(4, 2, TileType.WATER))
        .addTile(Tile(5, 2, TileType.WATER))
        .addTile(Tile(6, 2, TileType.WATER))
        .addTile(Tile(1, 3, TileType.WATER))
        .addTile(Tile(2, 3, TileType.WATER))
        .addTile(Tile(3, 3, TileType.WATER))
        .addTile(Tile(4, 3, TileType.WATER))
        .addTile(Tile(5, 3, TileType.LAND))
        .addTile(Tile(6, 3, TileType.WATER))
        .addTile(Tile(1, 4, TileType.WATER))
        .addTile(Tile(2, 4, TileType.WATER))
        .addTile(Tile(3, 4, TileType.LAND))
        .addTile(Tile(4, 4, TileType.LAND))
        .addTile(Tile(5, 4, TileType.LAND))
        .addTile(Tile(6, 4, TileType.WATER))
        .addTile(Tile(1, 5, TileType.WATER))
        .addTile(Tile(2, 5, TileType.WATER))
        .addTile(Tile(3, 5, TileType.WATER))
        .addTile(Tile(4, 5, TileType.LAND))
        .addTile(Tile(5, 5, TileType.WATER))
        .addTile(Tile(6, 5, TileType.WATER))
        .build()

    val smallMap: TileMap = TileMap.newBuilder()
        .setId(UUID(0L, 2L).toString())
        .addTile(Tile(0, 0, TileType.LAND))
        .addTile(Tile(0, 1, TileType.WATER))
        .addTile(Tile(1, 0, TileType.WATER))
        .addTile(Tile(1, 1, TileType.LAND))
        .build()

    val maps = listOf(providedMap, smallMap)

    val islands: List<Island> = listOf(
        Island.newBuilder()
            .setId(UUID(0L, 3L).toString())
            .setMapId(smallMap.id)
            .addCoordinate(Coordinate(0, 0))
            .build(),

        Island.newBuilder()
            .setId(UUID(0L, 4L).toString())
            .setMapId(smallMap.id)
            .addCoordinate(Coordinate(1, 1))
            .build(),

        Island.newBuilder()
            .setId(UUID(0L, 5L).toString())
            .setMapId(providedMap.id)
            .addCoordinate(Coordinate(5, 1))
            .build(),

        Island.newBuilder()
            .setId(UUID(0L, 6L).toString())
            .setMapId(providedMap.id)
            .addCoordinate(Coordinate(1, 1))
            .addCoordinate(Coordinate(2, 1))
            .addCoordinate(Coordinate(2, 2))
            .build(),

        Island.newBuilder()
            .setId(UUID(0L, 7L).toString())
            .setMapId(providedMap.id)
            .addCoordinate(Coordinate(5, 3))
            .addCoordinate(Coordinate(3, 4))
            .addCoordinate(Coordinate(4, 4))
            .addCoordinate(Coordinate(5, 4))
            .addCoordinate(Coordinate(4, 5))
            .build()
    )
}
