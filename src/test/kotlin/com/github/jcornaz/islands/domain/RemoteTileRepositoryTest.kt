package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.persistence.impl.RemoteTileRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.experimental.channels.toList
import kotlinx.coroutines.experimental.runBlocking
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class RemoteTileRepositoryTest {

    @Test
    fun testExpectedData() = runBlocking {
        val repository = RemoteTileRepository(testDataMockEngine)

        assertEquals(expectedTiles, repository.findAll().toList())
    }

    @Test
    fun testError() {
        lateinit var status: HttpStatusCode

        val repository = RemoteTileRepository(MockEngine { MockHttpResponse(call, status) })

        HttpStatusCode.allStatusCodes.asSequence()
                .filterNot { it.isSuccess() }
                .forEach {
                    status = it
                    assertFails { runBlocking { repository.findAll().toList() } }
                }
    }
}
