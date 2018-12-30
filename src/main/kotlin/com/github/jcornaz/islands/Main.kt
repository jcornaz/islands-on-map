package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.Neo4JIslandRepository
import com.github.jcornaz.islands.persistence.Neo4JTileMapRepository
import com.github.jcornaz.islands.persistence.createConstaints
import io.ktor.application.Application
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase

private val environment = applicationEngineEnvironment {
    connector {
        port = 8080
    }

    module { main(GraphDatabase.driver("bolt://localhost:7687")) }
}

fun Application.main(driver: Driver) {
    installContentNegotiation()
    installExceptionHandler()

    driver.createConstaints()

    val islandRepository = Neo4JIslandRepository(driver)

    maps(Neo4JTileMapRepository(driver), islandRepository)
    islands(islandRepository)
}

fun main() {
    embeddedServer(Netty, environment).start(true)
}
