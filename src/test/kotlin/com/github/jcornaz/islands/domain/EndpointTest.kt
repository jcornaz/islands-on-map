package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.core
import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.test.KoinTest

class EndpointTest : KoinTest {

    @BeforeEach
    fun setup() {
        startKoin(testModules)
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testGetIslandList(): Unit = withTestApplication(Application::core) {
        with(handleRequest(HttpMethod.Get, "/api/islands")) {
            requestHandled.shouldBeTrue()
            response.status() shouldBe HttpStatusCode.OK
            response.contentType().match(ContentType.Application.Json).shouldBeTrue()
            response.content.shouldNotBeNull()

            val islands = Gson().fromJson(response.content, Array<Island>::class.java)
                    .shouldNotBeNull()

            islands.size shouldEqual 3  // there must be 3 island with the test data
            islands.map { it.id }.toSet().size shouldEqual 3  // ids must be uniques
            islands.map { it.coordinates.size }.toSet() shouldEqual setOf(1, 3, 5) // islands should contains the tiles
        }
    }
}
