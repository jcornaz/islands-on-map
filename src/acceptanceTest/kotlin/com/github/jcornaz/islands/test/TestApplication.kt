package com.github.jcornaz.islands.test

import com.github.jcornaz.islands.core
import com.github.jcornaz.islands.coreModules
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.http.*
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.io.ByteReadChannel
import org.koin.Logger.slf4jLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.Closeable
import java.util.concurrent.TimeUnit

class TestApplication(dbUrl: Url, vararg remoteResources: Pair<Url, String>) : Closeable {
    private val engine: TestApplicationEngine = TestApplicationEngine().apply {
        start()
    }

    init {
        startKoin {
            modules(coreModules + httpMockEngine(remoteResources))
            properties(mapOf("neo4j_url" to dbUrl.toString()))
            slf4jLogger()
        }

        engine.application.core()
    }

    private fun httpMockEngine(remoteResources: Array<out Pair<Url, String>>): Module {
        val resources = remoteResources.toMap()

        return module {
            single<HttpClientEngine> {
                MockEngine {
                    require(method == HttpMethod.Get)

                    val resource = resources[url]

                    if (resource == null) MockHttpResponse(call, HttpStatusCode.NotFound)
                    else MockHttpResponse(
                        call,
                        HttpStatusCode.OK,
                        ByteReadChannel(resource),
                        headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                    )
                }
            }
        }
    }

    fun handleRequest(method: HttpMethod, uri: String, setup: TestApplicationRequest.() -> Unit = {}): TestApplicationCall =
        engine.handleRequest(method, uri, setup)

    override fun close() {
        engine.stop(0L, 0L, TimeUnit.MILLISECONDS)
        stopKoin()
    }
}
