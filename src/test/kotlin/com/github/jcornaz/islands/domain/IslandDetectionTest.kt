package com.github.jcornaz.islands.domain

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.Test

class IslandDetectionTest {

    @Test
    fun testDetectionInProvidedExample() {

        // given
        val map = listOf(
            Tile(1, 1, TileType.LAND),
            Tile(2, 1, TileType.LAND),
            Tile(3, 1, TileType.WATER),
            Tile(4, 1, TileType.WATER),
            Tile(5, 1, TileType.LAND),
            Tile(6, 1, TileType.WATER),
            Tile(1, 2, TileType.WATER),
            Tile(2, 2, TileType.LAND),
            Tile(3, 2, TileType.WATER),
            Tile(4, 2, TileType.WATER),
            Tile(5, 2, TileType.WATER),
            Tile(6, 2, TileType.WATER),
            Tile(1, 3, TileType.WATER),
            Tile(2, 3, TileType.WATER),
            Tile(3, 3, TileType.WATER),
            Tile(4, 3, TileType.WATER),
            Tile(5, 3, TileType.LAND),
            Tile(6, 3, TileType.WATER),
            Tile(1, 4, TileType.WATER),
            Tile(2, 4, TileType.WATER),
            Tile(3, 4, TileType.LAND),
            Tile(4, 4, TileType.LAND),
            Tile(5, 4, TileType.LAND),
            Tile(6, 4, TileType.WATER),
            Tile(1, 5, TileType.WATER),
            Tile(2, 5, TileType.WATER),
            Tile(3, 5, TileType.WATER),
            Tile(4, 5, TileType.LAND),
            Tile(5, 5, TileType.WATER),
            Tile(6, 5, TileType.WATER)
        ).toTileMap()

        // when
        val islands = map.detectIslands()

        // then
        islands.size shouldEqualTo 3
        islands.map { it.size }.toSet() shouldEqual setOf(1, 3, 5)

        islands.first { it.size == 1 }.coordinates shouldEqual setOf(Coordinate(5, 1))
        islands.first { it.size == 3 }.coordinates shouldEqual setOf(
            Coordinate(1, 1),
            Coordinate(2, 1),
            Coordinate(2, 2)
        )
        islands.first { it.size == 5 }.coordinates shouldEqual setOf(
            Coordinate(5, 3),
            Coordinate(3, 4),
            Coordinate(4, 4),
            Coordinate(5, 4),
            Coordinate(4, 5)
        )
    }

    @Test
    fun waterOnlyMapShouldHaveNoIsland() {

        //given
        val map = listOf(
            Tile(0, 0, TileType.WATER),
            Tile(0, 1, TileType.WATER),
            Tile(1, 0, TileType.WATER),
            Tile(1, 1, TileType.WATER)
        ).toTileMap()

        // when
        val islands = map.detectIslands()

        // then
        islands.shouldBeEmpty()
    }

    @Test
    fun landOnlyMapShouldHaveOneIsland() {

        //given
        val map = listOf(
            Tile(0, 0, TileType.LAND),
            Tile(0, 1, TileType.LAND),
            Tile(1, 0, TileType.LAND),
            Tile(1, 1, TileType.LAND)
        ).toTileMap()

        // when
        val islands = map.detectIslands()

        // then
        islands.size shouldEqualTo 1
        islands.first().size shouldEqualTo 4
    }

    @Test
    fun emptyMapShouldHaveNoIsland() {

        // given
        val map = emptyMap<Coordinate, Tile>()

        // when
        val islands = map.detectIslands()

        // then
        islands.shouldBeEmpty()
    }

    @Test
    fun diagonalTilesShouldBeSeparateIslands() {

        //given
        val map = listOf(
            Tile(0, 0, TileType.LAND),
            Tile(0, 1, TileType.WATER),
            Tile(1, 0, TileType.WATER),
            Tile(1, 1, TileType.LAND)
        ).toTileMap()

        // when
        val islands = map.detectIslands()

        // then
        islands.size shouldEqualTo 2
        islands.all { it.size == 1 }.shouldBeTrue()
    }
}