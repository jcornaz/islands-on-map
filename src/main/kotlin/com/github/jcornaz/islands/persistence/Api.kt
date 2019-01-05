package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.FetchRequest
import com.github.jcornaz.islands.Island
import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileMap
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.*

interface TileRepository {
    fun findAll(): ReceiveChannel<Tile>
}

interface IslandRepository {
    suspend fun create(island: Island)

    suspend fun findById(id: UUID): Island?

    fun findAll(): ReceiveChannel<Island>
}

interface TileMapRepository {
    suspend fun create(map: TileMap)

    suspend fun findById(id: UUID): TileMap?

    fun findAll(): ReceiveChannel<TileMap>
}

interface FetchRequestRepository {
    suspend fun create(request: FetchRequest)

    suspend fun setInProgress(id: UUID)
    suspend fun setSuccess(id: UUID, mapId: UUID)
    suspend fun setError(id: UUID, error: String)

    suspend fun findById(id: UUID): FetchRequest?
}
