package com.github.jcornaz.islands

import com.github.jcornaz.islands.domain.detectIslands
import com.github.jcornaz.islands.persistence.Neo4JFetchRequestRepository
import com.github.jcornaz.islands.persistence.Neo4JIslandRepository
import com.github.jcornaz.islands.persistence.Neo4JTileMapRepository
import com.github.jcornaz.islands.persistence.RemoteTileRepository
import com.github.jcornaz.islands.service.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.Apache
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.error
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import java.util.*

fun Application.main(driver: Driver, httpEngine: HttpClientEngine) {
    val mapRepository = Neo4JTileMapRepository(driver)
    val islandRepository = Neo4JIslandRepository(driver)
    val fetchRequestRepository = Neo4JFetchRequestRepository(driver)

    val mapService = DefaultMapService(mapRepository, islandRepository, Iterable<Tile>::detectIslands)

    main(
        mapService = mapService,
        islandService = DefaultIslandService(islandRepository),
        fetchRequestService = DefaultFetchRequestService(fetchRequestRepository),
        fetchService = DefaultFetchService(mapService, fetchRequestRepository) { RemoteTileRepository(httpEngine, it) }
    )
}

fun Application.main(
    mapService: MapService,
    islandService: IslandService,
    fetchRequestService: FetchRequestService,
    fetchService: FetchService
) {
    installContentNegotiation()
    installExceptionHandler()

    launch {
        fetchRequestService.openCreatedSubscription().consumeEach { request ->
            try {
                fetchService.fetch(UUID.fromString(request.id))
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

private val environment = applicationEngineEnvironment {
    connector {
        port = 8080
    }

    module { main(GraphDatabase.driver("bolt://localhost:7687"), Apache.create()) }
}

fun main() {
    embeddedServer(Netty, environment).start(true)
}
