package com.github.jcornaz.islands.persistence.impl

import com.github.jcornaz.islands.domain.Island
import com.github.jcornaz.islands.persistence.IslandRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlin.coroutines.CoroutineContext

class InMemoryIslandRepository(
        islands: suspend () -> Collection<Island>
) : IslandRepository, CoroutineScope {

    override val coroutineContext: CoroutineContext get() = Dispatchers.Default

    private val map: Deferred<Map<Int, Island>> = async { islands().associateBy { it.id } }

    override fun findAll(): ReceiveChannel<Island> = produce {
        map.await().values.forEach { send(it) }
    }
}
