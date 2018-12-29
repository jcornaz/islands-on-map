package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.IslandRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.channels.toList

fun Application.islands(islandRepo: IslandRepository) {
    routing {
        get("api/islands") {
            call.respond(islandRepo.findAll().toList())
        }
    }
}
