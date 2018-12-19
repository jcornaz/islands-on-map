package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.persistenceModule
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

val productionModules = listOf(httpModule, persistenceModule)

private val environment = applicationEngineEnvironment {
    connector {
        port = 8080
    }

    module {
        core(productionModules)
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, environment).start(true)
}
