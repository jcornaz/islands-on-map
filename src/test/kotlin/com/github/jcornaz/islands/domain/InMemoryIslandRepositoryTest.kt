package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.persistence.impl.InMemoryIslandRepository
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.channels.toSet
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

class InMemoryIslandRepositoryTest {

    @Test
    fun testExpectedData() = runBlocking<Unit> {
        var count = 1
        val islands = detectIslands(expectedTiles.toTileMap()).map { Island(++count, it) }.toSet()
        val repository = InMemoryIslandRepository { islands }

        repository.findAll().toSet() shouldEqual islands
    }

    @Test
    fun testEmpty() = runBlocking<Unit> {
        val repository = InMemoryIslandRepository { emptyList() }

        repository.findAll().toList().shouldBeEmpty()
    }
}
