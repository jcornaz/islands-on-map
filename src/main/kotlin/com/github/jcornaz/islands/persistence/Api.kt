package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.FetchRequest
import com.github.jcornaz.islands.Island
import com.github.jcornaz.islands.Tile
import com.github.jcornaz.islands.TileMap
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.Apache
import kotlinx.coroutines.channels.ReceiveChannel
import org.koin.dsl.module
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import java.util.*

interface TileRepositoryProvider {
    operator fun get(url: String): TileRepository
}

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

val httpEngine = module {
    single<HttpClientEngine> { Apache.create() }
}

val remoteRepositories = module {
    single<TileRepositoryProvider> {
        object : TileRepositoryProvider {
            override fun get(url: String): TileRepository = RemoteTileRepository(get(), url)
        }
    }
}

val neo4jRepositories = module {
    single<Driver> { GraphDatabase.driver(getProperty<String>("neo4j_url")).apply { createConstraints() } }
    single<TileMapRepository> { Neo4JTileMapRepository(get()) }
    single<IslandRepository> { Neo4JIslandRepository(get()) }
    single<FetchRequestRepository> { Neo4JFetchRequestRepository(get()) }
}
