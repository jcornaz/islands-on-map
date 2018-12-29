@file:Suppress("RemoveExplicitTypeArguments")

package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.TileMapRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import kotlinx.coroutines.channels.consumeEach
import java.util.*

fun Application.maps(mapRepo: TileMapRepository) {

    install(StatusPages) {
        exception<IllegalArgumentException> {
            log.error(it.message, it)
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    routing {
        route("api/maps") {
            post {
                val request = CreateTileMapRequest.parseFrom(call.receive<ByteArray>())

                val map = TileMap.newBuilder()
                    .setId(java.util.UUID.randomUUID().toString())
                    .addAllTile(request.tileList)
                    .build()

                mapRepo.create(map)

                call.respond(HttpStatusCode.Created, map.toByteArray())
            }

            get { _ ->
                val builder = TileMapList.newBuilder()

                mapRepo.findAll().consumeEach { builder.addTileMap(it) }

                call.respond(HttpStatusCode.OK, builder.build().toByteArray())
            }

            get("{id}") {
                log.info("fetch map id: \"${call.parameters["id"]}\"")
                val map = mapRepo.findById(UUID.fromString(call.parameters["id"]))

                if (map == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(HttpStatusCode.OK, map.toByteArray())
                }
            }
        }
    }
}
