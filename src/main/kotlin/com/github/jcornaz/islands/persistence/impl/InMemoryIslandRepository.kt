package com.github.jcornaz.islands.persistence.impl

import com.github.jcornaz.islands.domain.Island
import com.github.jcornaz.islands.persistence.IslandRepository
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce

class InMemoryIslandRepository(
        islands: suspend () -> Collection<Island>
) : IslandRepository {

    private val map: Deferred<Map<Int, Island>> = async { islands().associateBy { it.id } }

    override fun findAll(): ReceiveChannel<Island> = produce {
        map.await().values.forEach { send(it) }
    }
}
