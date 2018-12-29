package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.Island
import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileMap
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.*

interface TileRepository {
    fun findAll(): ReceiveChannel<Tile>
}

interface IslandRepository {
    fun findAll(): ReceiveChannel<Island>
}

interface TileMapRepository {
    suspend fun create(map: TileMap)

    suspend fun findById(id: UUID): TileMap?

    fun findAll(): ReceiveChannel<TileMap>
}
