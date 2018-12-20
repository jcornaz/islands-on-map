package com.github.jcornaz.islands.domain

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class DetectIslandsSpecification : Spek({
    describe("from provided tile-set") {
        val islands = expectedTiles.toTileMap().islands.toList()

        it("should return 3 islands") {
            islands.size shouldEqual 3
        }

        it("islands sizes should be 1, 3 and 5") {
            islands.map { it.size } shouldContainSame setOf(1, 3, 5)
        }

        it("smallest island should be formed of expected coordinate") {
            islands.first { it.size == 1 } shouldEqual setOf(Coordinate(5, 1))
        }

        it("medium island should be formed of expected coordinates") {
            islands.first { it.size == 3 } shouldEqual setOf(Coordinate(1, 1), Coordinate(2, 1), Coordinate(2, 2))
        }

        it("biggest island should be formed of expected coordinates") {
            islands.first { it.size == 5 } shouldEqual setOf(
                Coordinate(5, 3),
                Coordinate(3, 4),
                Coordinate(4, 4),
                Coordinate(5, 4),
                Coordinate(4, 5)
            )
        }
    }

    describe("from water tiles") {
        val map = listOf(
            Tile(0, 0, TileType.WATER),
            Tile(0, 1, TileType.WATER),
            Tile(1, 0, TileType.WATER),
            Tile(1, 1, TileType.WATER)
        ).toTileMap()

        val islands = map.islands.toList()

        it("should not return any island") {
            islands.shouldBeEmpty()
        }
    }

    describe("from land tiles") {
        val tiles = listOf(
            Tile(0, 0, TileType.LAND),
            Tile(0, 1, TileType.LAND),
            Tile(1, 0, TileType.LAND),
            Tile(1, 1, TileType.LAND)
        )

        val islands = tiles.toTileMap().islands.toList()

        it("should return one island") {
            islands.size shouldEqual 1
        }

        it("the result should contains all the tiles") {
            islands.first() shouldContainSame tiles.map { it.coordinate }
        }
    }

    describe("from empty tile-map") {
        val islands = emptyMap<Coordinate, TileType>().islands.toList()

        it("should not return any island") {
            islands.shouldBeEmpty()
        }
    }

    describe("land tiles in diagonal") {
        val map = listOf(
            Tile(0, 0, TileType.LAND),
            Tile(0, 1, TileType.WATER),
            Tile(1, 0, TileType.WATER),
            Tile(1, 1, TileType.LAND)
        ).toTileMap()

        val islands = map.islands.toList()

        it("should detect 2 islands") {
            islands.size shouldEqual 2
        }

        it("islands should contain 1 tile each") {
            islands.forEach {
                it.size shouldEqual 1
            }
        }
    }
})
