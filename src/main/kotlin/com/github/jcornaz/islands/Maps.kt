@file:Suppress("RemoveExplicitTypeArguments")

package com.github.jcornaz.islands

import com.github.jcornaz.islands.domain.detectIslands
import com.github.jcornaz.islands.persistence.IslandRepository
import com.github.jcornaz.islands.persistence.TileMapRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

fun Application.maps(mapRepository: TileMapRepository, islandRepository: IslandRepository) {
    routing {
        route("/api/maps") {
            post {
                val request = call.receive<CreateTileMapRequest>()

                require(request.tileCount > 0)

                val map = TileMap.newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .addAllTile(request.tileList)
                    .build()

                mapRepository.create(map)

                coroutineScope {
                    map.tileList.detectIslands().forEach { coordinates ->
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


                call.respond(HttpStatusCode.Created, map)
            }

            get { _ ->
                val builder = TileMapList.newBuilder()

                mapRepository.findAll().consumeEach { builder.addTileMap(it) }

                call.respond(HttpStatusCode.OK, builder.build())
            }

            get("{id}") {
                val map = mapRepository.findById(UUID.fromString(call.parameters["id"])) ?: throw ResourceNotFoundException()
                call.respond(HttpStatusCode.OK, map)
            }
        }
    }
}
