package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.persistenceModule
import io.ktor.application.Application
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.log.Logger.SLF4JLogger
import org.koin.standalone.StandAloneContext.startKoin

private val environment = applicationEngineEnvironment {
    connector {
        port = 8080
    }

    modules += Application::core
}

val productionModules = listOf(httpModule, persistenceModule)

fun main(args: Array<String>) {
    startKoin(productionModules, logger = SLF4JLogger())
    embeddedServer(Netty, environment).start(true)
}
