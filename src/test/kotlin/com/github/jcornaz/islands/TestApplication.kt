package com.github.jcornaz.islands

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import java.io.Closeable
import java.util.concurrent.TimeUnit

class TestApplication(setup: Application.() -> Unit) : Closeable {
    private val engine = TestApplicationEngine().apply {
        start()
        application.setup()
    }

    fun handleRequest(method: HttpMethod, uri: String, setup: TestApplicationRequest.() -> Unit = {}): TestApplicationCall =
        engine.handleRequest(method, uri, setup)

    override fun close() {
        engine.stop(0L, 0L, TimeUnit.MILLISECONDS)
    }
}
