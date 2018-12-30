package com.github.jcornaz.islands

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

class ResourceNotFoundException : Exception()

fun Application.installExceptionHandler() {
    install(StatusPages) {
        exception<Throwable> {
            log.error(it.message, it)
            call.respond(HttpStatusCode.InternalServerError)
        }
        exception<ResourceNotFoundException> {
            log.debug(it.message, it)
            call.respond(HttpStatusCode.NotFound)
        }
        exception<IllegalArgumentException> {
            log.debug(it.message, it)
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}
