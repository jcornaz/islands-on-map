package com.github.jcornaz.islands

import com.github.jcornaz.islands.service.FetchRequestService
import com.github.jcornaz.islands.service.IslandService
import com.github.jcornaz.islands.service.MapFetcher
import com.github.jcornaz.islands.service.MapService
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.error
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.util.*

fun Application.core() {
    installContentNegotiation()
    installExceptionHandler()

    val fetchRequestService: FetchRequestService by inject()
    val mapService: MapService by inject()
    val islandService: IslandService by inject()

    launch {
        val mapFetcher: MapFetcher by inject()

        fetchRequestService.openCreatedSubscription().consumeEach { request ->
            try {
                mapFetcher.fetch(UUID.fromString(request.id))
            } catch (e: Exception) {
                log.error(e)
            }
        }
    }

    routing {
        route("api") {
            route("maps") {
                post {
                    call.respond(HttpStatusCode.Created, mapService.create(call.receive()))
                }

                get { _ ->
                    call.respond(HttpStatusCode.OK, mapService.getAll())
                }

                get("{id}") {
                    call.respond(HttpStatusCode.OK, mapService.get(call.parameters["id"] ?: throw IllegalArgumentException()))
                }

                route("fetch-requests") {
                    post {
                        call.respond(HttpStatusCode.Created, fetchRequestService.create(call.receive()))
                    }

                    get("{id}") {
                        call.respond(HttpStatusCode.OK, fetchRequestService.get(call.parameters["id"] ?: throw IllegalArgumentException()))
                    }
                }
            }

            route("islands") {
                get { _ ->
                    call.respond(HttpStatusCode.OK, islandService.getAll())
                }

                get("{id}") {
                    call.respond(HttpStatusCode.OK, islandService.get(call.parameters["id"] ?: throw IllegalArgumentException()))
                }
            }
        }
    }
}
