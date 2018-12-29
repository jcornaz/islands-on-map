package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.*
import kotlinx.coroutines.channels.toList
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe
import java.util.*

class Neo4JTileMapRepositorySpecification : Spek({
    val database by memoizedClosable(CachingMode.SCOPE) { TestDatabase() }

    beforeEachTest { database += TestDataSet }
    afterEachTest { database.clear() }

    val repository: TileMapRepository by memoized { Neo4JTileMapRepository(database.driver) }

    val newId = UUID(42L, 24L).toString()

    describe("create map") {
        beforeEachTestBlocking {
            repository.create(
                TileMap.newBuilder()
                    .setId(newId)
                    .addTile(Tile(0, 0, TileType.LAND))
                    .addTile(Tile(0, 1, TileType.WATER))
                    .build()
            )
        }

        it("should create the map and tiles") {
            val tiles = database.execute(
                """
                MATCH (:TileMap { id: "$newId" })-[:HAS_TILE]->(tile:Tile)
                RETURN tile.coordinate.x AS x, tile.coordinate.y AS y, tile.type AS type
                """.trimIndent()
            ).map { Tile(it["x"].asInt(), it["y"].asInt(), TileType.forNumber(it["type"].asInt())) }.toList()

            tiles shouldContainSame listOf(Tile(0, 0, TileType.LAND), Tile(0, 1, TileType.WATER))
        }
    }

    describe("create empty map") {
        assertFailsBlocking<IllegalArgumentException> {
            repository.create(TileMap.newBuilder().setId(UUID(42L, 24L).toString()).build())
        }
    }

    describe("find existing map by id") {
        val result: TileMap? by memoizedBlocking(CachingMode.SCOPE) { repository.findById(UUID.fromString(TestDataSet.providedMap.id)) }

        it("should not return null") {
            result.shouldNotBeNull()
        }

        it("should return same map id") {
            result?.id shouldEqual TestDataSet.providedMap.id
        }

        it("should return same tiles set") {
            result?.tileList.orEmpty() shouldContainSame TestDataSet.providedMap.tileList
        }
    }

    describe("find non-existing map by id") {
        val result: TileMap? by memoizedBlocking(CachingMode.SCOPE) { repository.findById(UUID(42L, 24L)) }

        it("should return null") {
            result.shouldBeNull()
        }
    }

    describe("find all maps") {
        val result: List<TileMap> by memoizedBlocking(CachingMode.SCOPE) { repository.findAll().toList() }

        it("should return ${TestDataSet.maps.size} maps") {
            result.size shouldEqual TestDataSet.maps.size
        }

        it("should return same map ids") {
            result.map { it.id } shouldContainSame TestDataSet.maps.map { it.id }
        }

        it("should return same tile sets") {
            result.map { it.tileList.toSet() } shouldContainSame TestDataSet.maps.map { it.tileList.toSet() }
        }
    }
})
