package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.*
import com.github.jcornaz.islands.persistence.IslandRepository
import com.github.jcornaz.islands.persistence.TileMapRepository
import com.github.jcornaz.islands.test.*
import com.github.jcornaz.miop.produce
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.GlobalScope
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*

class DefaultMapServiceSpec : Spek({

    val mapRepository by memoizedMock<TileMapRepository>(relaxed = true)
    val islandRepository by memoizedMock<IslandRepository>(relaxed = true)
    val islandDetection by memoizedMock<(Iterable<Tile>) -> Collection<Set<Coordinate>>>()

    val service by memoized { DefaultMapService(mapRepository, islandRepository, islandDetection) }

    describe("given mapRepository.findById(UUID) returns a map") {
        val id = UUID(0L, 1L)

        val map = TileMap.newBuilder()
            .setId(UUID(0L, 2L).toString())
            .addTile(tile(0, 0, TileType.WATER))
            .addTile(tile(1, 0, TileType.LAND))
            .build()

        beforeEach {
            coEvery { mapRepository.findById(eq(id)) } returns map
        }

        describe("get(String)") {
            val result by memoizedBlocking { service.get(id.toString()) }

            it("should return the map given by the repository") {
                result shouldBe map
            }
        }
    }

    describe("given mapRepository.findById(UUID) returns null") {
        val id = UUID(0L, 1L)

        beforeEach {
            coEvery { mapRepository.findById(eq(id)) } returns null
        }

        describe("get(String)") {
            val exception by memoizedBlocking { runCatching { service.get(id.toString()) }.exceptionOrNull() }

            it("should throw ${ResourceNotFoundException::class}") {
                exception.shouldBeInstanceOf<ResourceNotFoundException>()
            }
        }
    }

    describe("given mapRepository.findAll() returns 2 maps") {
        val maps = listOf(
            TileMap.newBuilder()
                .setId(UUID(0L, 2L).toString())
                .addTile(tile(0, 0, TileType.WATER))
                .addTile(tile(1, 0, TileType.LAND))
                .build(),
            TileMap.newBuilder()
                .setId(UUID(0L, 3L).toString())
                .addTile(tile(0, 0, TileType.LAND))
                .addTile(tile(0, 1, TileType.WATER))
                .addTile(tile(1, 0, TileType.WATER))
                .addTile(tile(1, 1, TileType.LAND))
                .build()
        )

        beforeEach {
            coEvery { mapRepository.findAll() } answers { GlobalScope.produce(maps) }
        }

        describe("getAll()") {
            val result by memoizedBlocking { service.getAll() }

            it("should return 2 maps") {
                result.tileMapCount shouldEqual 2
            }

            it("should return the maps given by the repository") {
                result.tileMapList shouldContainSame maps
            }
        }
    }

    describe("create()") {

        val tiles = listOf(
            tile(0, 0, TileType.WATER),
            tile(0, 1, TileType.LAND),
            tile(1, 0, TileType.LAND),
            tile(1, 1, TileType.WATER)
        )

        beforeEachBlocking {
            coEvery { islandDetection(eq(tiles)) } returns listOf(setOf(coordinate(0, 1)), setOf(coordinate(1, 0)))

            service.create(CreateTileMapRequest.newBuilder().addAllTile(tiles).build())
        }

        it("should create the map") {
            coVerify(exactly = 1) { mapRepository.create(match { it.tileList == tiles }) }
        }

        listOf(coordinate(0, 1), coordinate(1, 0)).forEach { islandCoordinate ->
            it("should create island at $islandCoordinate") {
                coVerify(exactly = 1) { islandRepository.create(match { it.coordinateList == listOf(islandCoordinate) }) }
            }
        }

        it("should create islands after creating the map") {
            coVerify(Ordering.SEQUENCE) {
                mapRepository.create(any())
                islandRepository.create(any())
                islandRepository.create(any())
            }
        }
    }

    describe("given map creation failed") {

        beforeEachBlocking {
            coEvery { mapRepository.create(any()) } throws Exception()

            runCatching {
                service.create(
                    CreateTileMapRequest.newBuilder()
                        .addTile(tile(0, 0, TileType.WATER))
                        .addTile(tile(0, 1, TileType.LAND))
                        .build()
                )
            }
        }

        it("should not call create any the islands") {
            coVerify(inverse = true) { islandRepository.create(any()) }
        }
    }
})
