package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.koin.dsl.module
import java.util.*

interface MapService {
    suspend fun create(request: CreateTileMapRequest): TileMap
    suspend fun get(id: String): TileMap
    suspend fun getAll(): TileMapList
}

interface IslandService {
    suspend fun get(id: String): Island
    suspend fun getAll(): IslandList
}

interface FetchRequestService {
    suspend fun create(request: CreateFetchRequest): FetchRequest
    suspend fun get(id: String): FetchRequest

    fun openCreatedSubscription(): ReceiveChannel<FetchRequest>
}

interface MapFetcher {
    suspend fun fetch(requestId: UUID)
}

val defaultServices = module {
    single<MapService> { DefaultMapService(get(), get(), get()) }
    single<IslandService> { DefaultIslandService(get()) }
    single<FetchRequestService> { DefaultFetchRequestService(get()) }
    single<MapFetcher> { DefaultMapFetcher(get(), get(), get()) }
}
