@file:Suppress("RemoveExplicitTypeArguments")

package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.IslandRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.Apache
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.channels.toList
import org.koin.dsl.module.module
import org.koin.ktor.ext.get

fun Application.core() {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    val islandRepo: IslandRepository = get()

    routing {
        get("/api/islands") {
            call.respond(islandRepo.findAll().toList())
        }
    }
}

val httpModule = module {
    single<HttpClientEngine> { Apache.create() }
}
