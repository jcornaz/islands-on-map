package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.Island
import com.github.jcornaz.islands.Tile
import kotlinx.coroutines.channels.ReceiveChannel
import org.koin.dsl.module

interface IslandDetector {
    fun detectIslands(tiles: Iterable<Tile>): ReceiveChannel<Island>
}

val domainLogic = module {
    single<IslandDetector> { DefaultIslandDetector }
}
