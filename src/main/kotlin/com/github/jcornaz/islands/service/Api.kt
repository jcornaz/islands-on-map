package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.*

interface MapService {
    suspend fun create(request: CreateTileMapRequest): TileMap
    suspend fun get(id: String): TileMap
    suspend fun getAll(): TileMapList
}

interface IslandService {
    suspend fun get(id: String): Island
    suspend fun getAll(): IslandList
}
