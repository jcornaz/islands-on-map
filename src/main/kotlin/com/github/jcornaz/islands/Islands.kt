package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.IslandRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import kotlinx.coroutines.channels.consumeEach
import java.util.*

fun Application.islands(islandRepo: IslandRepository) {
    routing {
        route("/api/islands") {
            get { _ ->
                val builder = IslandList.newBuilder()

                islandRepo.findAll().consumeEach { builder.addIsland(it) }

                call.respond(HttpStatusCode.OK, builder.build())
            }

            get("{id}") {
                val island = islandRepo.findById(UUID.fromString(call.parameters["id"])) ?: throw ResourceNotFoundException()
                call.respond(HttpStatusCode.OK, island)
            }
        }
    }
}
