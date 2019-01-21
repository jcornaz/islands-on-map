package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.CreateTileMapRequest
import com.github.jcornaz.islands.FetchRequest
import com.github.jcornaz.islands.persistence.FetchRequestRepository
import com.github.jcornaz.islands.persistence.TileRepositoryProvider
import kotlinx.coroutines.channels.fold
import java.util.*

class DefaultMapFetcher(
    private val mapService: MapService,
    private val fetchRequestRepository: FetchRequestRepository,
    private val tileRepositoryProvider: TileRepositoryProvider
) : MapFetcher {
    override suspend fun fetch(requestId: UUID) {
        val request = fetchRequestRepository.findById(requestId)

        require(request != null && request.status == FetchRequest.Status.PENDING)

        fetchRequestRepository.setInProgress(requestId)

        try {
            val map = mapService.create(
                tileRepositoryProvider[request.url]
                    .findAll()
                    .fold(CreateTileMapRequest.newBuilder()) { builder, tile -> builder.addTile(tile) }
                    .build()
            )

            fetchRequestRepository.setSuccess(requestId, UUID.fromString(map.id))
        } catch (e: Exception) {
            fetchRequestRepository.setError(requestId, "Failed to create map from tiles fetched at: \"${request.url}\" (${e.message})")
        }
    }
}
