package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.CreateTileMapRequest
import com.github.jcornaz.islands.ResourceNotFoundException
import com.github.jcornaz.islands.TileMap
import com.github.jcornaz.islands.TileMapList
import com.github.jcornaz.islands.domain.IslandDetector
import com.github.jcornaz.islands.persistence.IslandRepository
import com.github.jcornaz.islands.persistence.TileMapRepository
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.fold
import java.util.*

class DefaultMapService(
    private val mapRepository: TileMapRepository,
    private val islandRepository: IslandRepository,
    private val islandDetector: IslandDetector
) : MapService {

    override suspend fun create(request: CreateTileMapRequest): TileMap {
        require(request.tileCount > 0)

        val map = TileMap.newBuilder()
            .setId(UUID.randomUUID().toString())
            .addAllTile(request.tileList)
            .build()

        mapRepository.create(map)

        islandDetector.detectIslands(map.tileList).consumeEach {
            islandRepository.create(it)
        }

        return map
    }

    override suspend fun get(id: String): TileMap =
        mapRepository.findById(UUID.fromString(id)) ?: throw ResourceNotFoundException()

    override suspend fun getAll(): TileMapList =
        mapRepository.findAll()
            .fold(TileMapList.newBuilder()) { list, map -> list.addTileMap(map) }
            .build()
}
