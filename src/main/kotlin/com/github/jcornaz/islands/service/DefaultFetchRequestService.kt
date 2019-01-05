package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.CreateFetchRequest
import com.github.jcornaz.islands.FetchRequest
import com.github.jcornaz.islands.ResourceNotFoundException
import com.github.jcornaz.islands.persistence.FetchRequestRepository
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.*

class DefaultFetchRequestService(private val repository: FetchRequestRepository) : FetchRequestService {
    private val creationBroadcast = BroadcastChannel<FetchRequest>(16)

    override suspend fun create(request: CreateFetchRequest): FetchRequest {
        require(request.url.isNotBlank())

        val result = FetchRequest.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setUrl(request.url)
            .build()

        repository.create(result)
        creationBroadcast.send(result)

        return result
    }

    override suspend fun get(id: String): FetchRequest =
        repository.findById(UUID.fromString(id)) ?: throw ResourceNotFoundException()

    override fun openCreatedSubscription(): ReceiveChannel<FetchRequest> =
        creationBroadcast.openSubscription()
}
