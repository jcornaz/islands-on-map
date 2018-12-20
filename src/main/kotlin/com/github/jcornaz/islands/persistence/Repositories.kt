package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.domain.Island
import com.github.jcornaz.islands.domain.Tile
import com.github.jcornaz.islands.domain.islands
import com.github.jcornaz.islands.persistence.impl.InMemoryIslandRepository
import com.github.jcornaz.islands.persistence.impl.RemoteTileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.associate
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.toList
import org.koin.dsl.module

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

private fun ReceiveChannel<Tile>.createIslands(): ReceiveChannel<Island> = GlobalScope.produce(Dispatchers.Unconfined) {
    val map = associate { it.coordinate to it.type }

    var count = 0

    map.islands.forEach { send(Island(++count, it)) }
}
