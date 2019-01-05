package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.TileType
import com.github.jcornaz.islands.test.memoizedBlocking
import com.github.jcornaz.islands.test.memoizedClosable
import io.ktor.client.engine.apache.Apache
import kotlinx.coroutines.channels.toList
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeEmpty
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

class RemoteTileRepositoryIntegrationSpec : Spek({
    val engine by memoizedClosable { Apache.create() }
    val repository by memoized { RemoteTileRepository(engine, "https://private-2e8649-advapi.apiary-mock.com/map") }

    describe("find all") {
        val result by memoizedBlocking(CachingMode.SCOPE) { repository.findAll().toList() }

        it("should return tiles") {
            result.shouldNotBeEmpty()
        }

        it("should return tiles with different coordinates") {
            result.map { it.coordinate }.toSet().size shouldEqual result.size
        }

        it("should return tiles with water") {
            result.any { it.type == TileType.WATER }.shouldBeTrue()
        }

        it("should return tiles with land") {
            result.any { it.type == TileType.LAND }.shouldBeTrue()
        }
    }
})
