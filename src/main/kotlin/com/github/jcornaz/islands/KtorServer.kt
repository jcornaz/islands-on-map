package com.github.jcornaz.islands

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
  val server = embeddedServer(Netty, 8080) {
    install(ContentNegotiation) {
      jackson {
        enable(SerializationFeature.INDENT_OUTPUT)
      }
    }

    routing {
      get("/") {
        call.respondRedirect("https://github.com/jcornaz/islands-on-map")
      }
    }
  }

  server.start(true)
}
