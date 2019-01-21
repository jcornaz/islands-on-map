package com.github.jcornaz.islands

import com.github.jcornaz.islands.domain.domainLogic
import com.github.jcornaz.islands.persistence.httpEngine
import com.github.jcornaz.islands.persistence.neo4jRepositories
import com.github.jcornaz.islands.persistence.remoteRepositories
import com.github.jcornaz.islands.service.defaultServices
import io.ktor.application.Application
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.Logger.slf4jLogger
import org.koin.core.context.startKoin

val coreModules = listOf(
    domainLogic,
    defaultServices,
    neo4jRepositories,
    remoteRepositories
)

private val environment = applicationEngineEnvironment {
    connector {
        port = 8080
    }

    modules += Application::core
}

fun main() {
    startKoin {
        modules(coreModules + httpEngine)
        slf4jLogger()
    }

    embeddedServer(Netty, environment).start(true)
}
