package com.github.jcornaz.islands

import com.github.jcornaz.islands.domain.detectIslands
import com.github.jcornaz.islands.persistence.Neo4JIslandRepository
import com.github.jcornaz.islands.persistence.Neo4JTileMapRepository
import com.github.jcornaz.islands.service.DefaultIslandService
import com.github.jcornaz.islands.service.DefaultMapService
import com.github.jcornaz.islands.service.IslandService
import com.github.jcornaz.islands.service.MapService
import io.ktor.application.Application
import io.ktor.application.call
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
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase

fun Application.main(driver: Driver) {
    val mapRepository = Neo4JTileMapRepository(driver)
    val islandRepository = Neo4JIslandRepository(driver)

    main(
        mapService = DefaultMapService(mapRepository, islandRepository, Iterable<Tile>::detectIslands),
        islandService = DefaultIslandService(islandRepository)
    )
}

fun Application.main(mapService: MapService, islandService: IslandService) {
    installContentNegotiation()
    installExceptionHandler()

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

    module { main(GraphDatabase.driver("bolt://localhost:7687")) }
}

fun main() {
    embeddedServer(Netty, environment).start(true)
}
