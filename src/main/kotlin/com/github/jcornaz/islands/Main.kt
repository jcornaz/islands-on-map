package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.Neo4JTileMapRepository
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.neo4j.driver.v1.GraphDatabase

private val environment = applicationEngineEnvironment {
    connector {
        port = 8080
    }

    module {
        maps(Neo4JTileMapRepository(GraphDatabase.driver("bolt://localhost:7687")))
    }
}

fun main() {
    embeddedServer(Netty, environment).start(true)
}
