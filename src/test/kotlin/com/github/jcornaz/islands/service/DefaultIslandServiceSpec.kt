package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.Coordinate
import com.github.jcornaz.islands.FetchRequest
import com.github.jcornaz.islands.Island
import com.github.jcornaz.islands.ResourceNotFoundException
import com.github.jcornaz.islands.persistence.IslandRepository
import com.github.jcornaz.islands.test.memoizedBlocking
import com.github.jcornaz.islands.test.memoizedMock
import com.github.jcornaz.miop.emptyReceiveChannel
import com.github.jcornaz.miop.produce
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.GlobalScope
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*

class DefaultIslandServiceSpec : Spek({
    val repository by memoizedMock<IslandRepository>()
    val service by memoized { DefaultIslandService(repository) }

    describe("given repository.findById(UUID) returns an island") {
        val id = UUID(0L, 1L)

        val island = Island.newBuilder()
            .setId(id.toString())
            .setMapId(UUID(0L, 2L).toString())
            .addCoordinate(Coordinate.newBuilder().setX(1).setY(2))
            .addCoordinate(Coordinate.newBuilder().setX(3).setY(4))
            .build()

        beforeEach { coEvery { repository.findById(eq(id)) } returns island }

        describe("get(String)") {
            val result by memoizedBlocking { service.get(id.toString()) }

            it("should return the island given by the repository") {
                result shouldBe island
            }
        }
    }

    describe("given repository.findById(UUID) return null") {
        val id = UUID(0L, 1L)

        beforeEach { coEvery { repository.findById(eq(id)) } returns null }

        describe("get(String)") {
            val exception by memoizedBlocking { runCatching { service.get(id.toString()) }.exceptionOrNull() }

            it("should throw ${ResourceNotFoundException::class}") {
                exception.shouldBeInstanceOf<ResourceNotFoundException>()
            }
        }
    }

    describe("given repository.findAll() returns 2 islands") {
        val islands = listOf(
            Island.newBuilder()
                .setId(UUID(0L, 1L).toString())
                .setMapId(UUID(0L, 0L).toString())
                .addCoordinate(Coordinate.newBuilder().setX(1).setY(2))
                .addCoordinate(Coordinate.newBuilder().setX(3).setY(4))
                .build(),
            Island.newBuilder()
                .setId(UUID(0L, 2L).toString())
                .setMapId(UUID(0L, 0L).toString())
                .addCoordinate(Coordinate.newBuilder().setX(0).setY(1))
                .addCoordinate(Coordinate.newBuilder().setX(1).setY(0))
                .build()
        )

        beforeEach { every { repository.findAll() } answers { GlobalScope.produce(islands) } }

        describe("getAll()") {
            val result by memoizedBlocking { service.getAll() }

            it("should return 2 islands") {
                result.islandCount shouldEqual 2
            }

            it("should return the same islands given by the repository") {
                result.islandList shouldContainSame islands
            }
        }
    }

    describe("given repository.findAll() returns no islands") {

        beforeEach { every { repository.findAll() } returns emptyReceiveChannel() }

        describe("getAll()") {
            val result by memoizedBlocking { service.getAll() }

            it("should return no island") {
                result.islandCount shouldEqual 0
            }
        }
    }
})
