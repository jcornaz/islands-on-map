package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.persistence.impl.RemoteTileRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFails

class RemoteTileRepositorySpecification : Spek({

    describe("expected data") {
        val repository = RemoteTileRepository(MockEngine {
            MockHttpResponse(
                call,
                HttpStatusCode.OK,
                ByteReadChannel(expectedRemoteAnswer.toByteArray()),
                headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        })

        runBlocking { repository.findAll().toList() } shouldEqual expectedTiles
    }

    group("status codes") {
        HttpStatusCode.allStatusCodes.asSequence()
            .filterNot { it.isSuccess() }
            .forEach { status ->
                describe("https status code $status") {
                    val repository = RemoteTileRepository(MockEngine { MockHttpResponse(call, status) })

                    it("should fail") {
                        assertFails {
                            runBlocking { runBlocking { repository.findAll().toList() } }
                        }
                    }
                }
            }
    }
})
