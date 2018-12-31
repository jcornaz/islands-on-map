package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileType
import com.github.jcornaz.islands.test.coordinate
import com.github.jcornaz.islands.test.tile
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

class DetectIslandsSpec : Spek({
    describe("from provided map") {
        val tiles = listOf(
            tile(1, 1, TileType.LAND),
            tile(2, 1, TileType.LAND),
            tile(3, 1, TileType.WATER),
            tile(4, 1, TileType.WATER),
            tile(5, 1, TileType.LAND),
            tile(6, 1, TileType.WATER),
            tile(1, 2, TileType.WATER),
            tile(2, 2, TileType.LAND),
            tile(3, 2, TileType.WATER),
            tile(4, 2, TileType.WATER),
            tile(5, 2, TileType.WATER),
            tile(6, 2, TileType.WATER),
            tile(1, 3, TileType.WATER),
            tile(2, 3, TileType.WATER),
            tile(3, 3, TileType.WATER),
            tile(4, 3, TileType.WATER),
            tile(5, 3, TileType.LAND),
            tile(6, 3, TileType.WATER),
            tile(1, 4, TileType.WATER),
            tile(2, 4, TileType.WATER),
            tile(3, 4, TileType.LAND),
            tile(4, 4, TileType.LAND),
            tile(5, 4, TileType.LAND),
            tile(6, 4, TileType.WATER),
            tile(1, 5, TileType.WATER),
            tile(2, 5, TileType.WATER),
            tile(3, 5, TileType.WATER),
            tile(4, 5, TileType.LAND),
            tile(5, 5, TileType.WATER),
            tile(6, 5, TileType.WATER)
        )

        val islands by memoized(CachingMode.SCOPE) { tiles.detectIslands() }

        it("should return 3 islands") {
            islands.size shouldEqual 3
        }

        it("islands sizes should be 1, 3 and 5") {
            islands.map { it.size } shouldContainSame setOf(1, 3, 5)
        }

        it("smallest island should be formed of expected coordinate") {
            islands.first { it.size == 1 } shouldEqual setOf(coordinate(5, 1))
        }

        it("medium island should be formed of expected coordinates") {
            islands.first { it.size == 3 } shouldEqual setOf(coordinate(1, 1), coordinate(2, 1), coordinate(2, 2))
        }

        it("biggest island should be formed of expected coordinates") {
            islands.first { it.size == 5 } shouldEqual setOf(
                coordinate(5, 3),
                coordinate(3, 4),
                coordinate(4, 4),
                coordinate(5, 4),
                coordinate(4, 5)
            )
        }
    }

    describe("from water tiles") {
        val tiles = listOf(
            tile(0, 0, TileType.WATER),
            tile(0, 1, TileType.WATER),
            tile(1, 0, TileType.WATER),
            tile(1, 1, TileType.WATER)
        )

        val islands by memoized { tiles.detectIslands() }

        it("should not return any island") {
            islands.shouldBeEmpty()
        }
    }

    describe("from land tiles") {
        val tiles = listOf(
            tile(0, 0, TileType.LAND),
            tile(0, 1, TileType.LAND),
            tile(1, 0, TileType.LAND),
            tile(1, 1, TileType.LAND)
        )

        val islands by memoized { tiles.detectIslands() }

        it("should return one island") {
            islands.size shouldEqual 1
        }

        it("the result should contains all the tiles") {
            islands.first() shouldContainSame tiles.map { it.coordinate }
        }
    }

    describe("from empty tile-map") {
        val islands by memoized { emptyList<Tile>().detectIslands() }

        it("should not return any island") {
            islands.shouldBeEmpty()
        }
    }

    describe("land tiles in diagonal") {
        val map = listOf(
            tile(0, 0, TileType.LAND),
            tile(0, 1, TileType.WATER),
            tile(1, 0, TileType.WATER),
            tile(1, 1, TileType.LAND)
        )

        val islands by memoized { map.detectIslands() }

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
