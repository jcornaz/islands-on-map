package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.domain.Island
import com.github.jcornaz.islands.domain.Tile
import com.github.jcornaz.islands.domain.detectIslands
import com.github.jcornaz.islands.persistence.impl.InMemoryIslandRepository
import com.github.jcornaz.islands.persistence.impl.RemoteTileRepository
import kotlinx.coroutines.experimental.channels.*
import org.koin.dsl.module.module

interface TileRepository {
    fun findAll(): ReceiveChannel<Tile>
}

interface IslandRepository {
    fun findAll(): ReceiveChannel<Island>
}

val persistenceModule = module {
    single<TileRepository> { RemoteTileRepository(get()) }
    single<IslandRepository> { InMemoryIslandRepository { get<TileRepository>().findAll().createIslands().toList() } }
}

private fun ReceiveChannel<Tile>.createIslands(): ReceiveChannel<Island> = produce {
    val map = associateBy { it.coordinate }

    var count = 0

    detectIslands(map).consumeEach { send(Island(++count, it)) }
}
