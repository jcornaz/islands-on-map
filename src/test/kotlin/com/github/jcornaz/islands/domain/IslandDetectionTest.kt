package com.github.jcornaz.islands.domain

import kotlinx.coroutines.experimental.channels.toList
import kotlinx.coroutines.experimental.runBlocking
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.Test

class IslandDetectionTest {

    @Test
    fun testDetectionInProvidedExample() {

        // given
        val map = expectedTiles.toTileMap()

        // when
        val islands = runBlocking { detectIslands(map).toList() }

        // then
        islands.size shouldEqualTo 3
        islands.map { it.size }.toSet() shouldEqual setOf(1, 3, 5)

        islands.first { it.size == 1 } shouldEqual setOf(Coordinate(5, 1))
        islands.first { it.size == 3 } shouldEqual setOf(
                Coordinate(1, 1),
                Coordinate(2, 1),
                Coordinate(2, 2)
        )
        islands.first { it.size == 5 } shouldEqual setOf(
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
        val islands = runBlocking { detectIslands(map).toList() }

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
        val islands = runBlocking { detectIslands(map).toList() }

        // then
        islands.size shouldEqualTo 1
        islands.first().size shouldEqualTo 4
    }

    @Test
    fun emptyMapShouldHaveNoIsland() {

        // given
        val map = emptyMap<Coordinate, Tile>()

        // when
        val islands = runBlocking { detectIslands(map).toList() }

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
        val islands = runBlocking { detectIslands(map).toList() }

        // then
        islands.size shouldEqualTo 2
        islands.all { it.size == 1 }.shouldBeTrue()
    }
}