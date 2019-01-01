package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.*
import com.github.jcornaz.islands.persistence.IslandRepository
import com.github.jcornaz.islands.persistence.TileMapRepository
import kotlinx.coroutines.channels.fold
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

class DefaultMapService(
    private val mapRepository: TileMapRepository,
    private val islandRepository: IslandRepository,
    private val detectIslands: (List<Tile>) -> Iterable<Set<Coordinate>>
) : MapService {

    override suspend fun create(request: CreateTileMapRequest): TileMap {
        require(request.tileCount > 0)

        val map = TileMap.newBuilder()
            .setId(UUID.randomUUID().toString())
            .addAllTile(request.tileList)
            .build()

        mapRepository.create(map)

        coroutineScope {
            detectIslands(map.tileList).forEach { coordinates ->
                launch {
                    islandRepository.create(
                        Island.newBuilder()
                            .setId(UUID.randomUUID().toString())
                            .setMapId(map.id)
                            .addAllCoordinate(coordinates)
                            .build()
                    )
                }
            }
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
