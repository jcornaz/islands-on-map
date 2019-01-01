package com.github.jcornaz.islands.test

import com.github.jcornaz.islands.main
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import org.neo4j.driver.v1.Driver
import java.io.Closeable
import java.util.concurrent.TimeUnit

class TestApplication(driver: Driver) : Closeable {
    private val engine = TestApplicationEngine().apply {
        start()
        application.main(driver)
    }

    fun handleRequest(method: HttpMethod, uri: String, setup: TestApplicationRequest.() -> Unit = {}): TestApplicationCall =
        engine.handleRequest(method, uri, setup)

    override fun close() {
        engine.stop(0L, 0L, TimeUnit.MILLISECONDS)
    }
}
