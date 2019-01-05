package com.github.jcornaz.islands.test

import com.github.jcornaz.islands.main
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.http.*
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.io.ByteReadChannel
import org.neo4j.driver.v1.Driver
import java.io.Closeable
import java.util.concurrent.TimeUnit

class TestApplication(driver: Driver) : Closeable {

    val remoteResources: MutableMap<Url, String> = HashMap()

    private val engine = TestApplicationEngine().apply {
        start()

        application.main(driver, MockEngine {
            require(method == HttpMethod.Get)

            val resource = remoteResources[url]

            if (resource == null) MockHttpResponse(call, HttpStatusCode.NotFound)
            else MockHttpResponse(call, HttpStatusCode.OK, ByteReadChannel(resource), headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())))
        })
    }

    fun handleRequest(method: HttpMethod, uri: String, setup: TestApplicationRequest.() -> Unit = {}): TestApplicationCall =
        engine.handleRequest(method, uri, setup)

    override fun close() {
        engine.stop(0L, 0L, TimeUnit.MILLISECONDS)
    }
}
