package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.Coordinate
import com.github.jcornaz.islands.TestDataSet
import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileType
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

class DetectIslandsSpecification : Spek({
    describe("from provided map") {
        val islands by memoized(CachingMode.SCOPE) { TestDataSet.providedMap.tileList.islands() }

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
        val tiles = listOf(
            Tile(0, 0, TileType.WATER),
            Tile(0, 1, TileType.WATER),
            Tile(1, 0, TileType.WATER),
            Tile(1, 1, TileType.WATER)
        )

        val islands by memoized { tiles.islands() }

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

        val islands by memoized { tiles.islands() }

        it("should return one island") {
            islands.size shouldEqual 1
        }

        it("the result should contains all the tiles") {
            islands.first() shouldContainSame tiles.map { it.coordinate }
        }
    }

    describe("from empty tile-map") {
        val islands by memoized { emptyList<Tile>().islands() }

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
        )

        val islands by memoized { map.islands() }

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
