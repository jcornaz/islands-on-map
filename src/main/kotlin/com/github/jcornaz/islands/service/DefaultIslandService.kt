package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.Island
import com.github.jcornaz.islands.IslandList
import com.github.jcornaz.islands.ResourceNotFoundException
import com.github.jcornaz.islands.persistence.IslandRepository
import kotlinx.coroutines.channels.fold
import java.util.*

class DefaultIslandService(private val repository: IslandRepository) : IslandService {
    override suspend fun get(id: String): Island =
        repository.findById(UUID.fromString(id)) ?: throw ResourceNotFoundException()

    override suspend fun getAll(): IslandList =
        repository.findAll()
            .fold(IslandList.newBuilder()) { list, island -> list.addIsland(island) }
            .build()
}
