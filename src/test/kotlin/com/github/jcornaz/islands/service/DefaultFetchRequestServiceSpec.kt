package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.CreateFetchRequest
import com.github.jcornaz.islands.FetchRequest
import com.github.jcornaz.islands.ResourceNotFoundException
import com.github.jcornaz.islands.persistence.FetchRequestRepository
import com.github.jcornaz.islands.test.beforeEachBlocking
import com.github.jcornaz.islands.test.memoizedBlocking
import com.github.jcornaz.islands.test.memoizedMock
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.channels.ReceiveChannel
import org.amshove.kluent.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*

class DefaultFetchRequestServiceSpec : Spek({
    val repository: FetchRequestRepository by memoizedMock(relaxed = true)
    val service: FetchRequestService by memoized { DefaultFetchRequestService(repository) }

    describe("create request") {
        val createRequest = CreateFetchRequest.newBuilder().setUrl("my-url").build()

        lateinit var request: FetchRequest
        lateinit var subscription: ReceiveChannel<FetchRequest>

        beforeEachBlocking {
            subscription = service.openCreatedSubscription()
            request = service.create(createRequest)
        }

        afterEach { subscription.cancel() }

        it("should create the request") {
            coVerify(exactly = 1) {
                repository.create(match { it === request })
            }
        }

        it("should create a request containing the url") {
            request.url shouldEqual "my-url"
        }

        it("should generate a request id") {
            request.id shouldMatch Regex("^[0-9a-f-]+$")
        }

        it("should create a request in pending state") {
            request.status shouldEqual FetchRequest.Status.PENDING
        }

        it("should publish the request creation") {
            subscription.poll() shouldEqual request
        }
    }

    describe("create request without url") {
        var exception: Throwable? = null
        val request = CreateFetchRequest.getDefaultInstance()

        beforeEachBlocking {
            exception = runCatching { service.create(request) }.exceptionOrNull()
        }

        it("should throw ${IllegalArgumentException::class}") {
            exception shouldBeInstanceOf IllegalArgumentException::class
        }
    }

    describe("given an existing request") {
        val id = UUID(0L, 1L)
        val request = FetchRequest.newBuilder()
            .setUrl("fancy-url")
            .setStatus(FetchRequest.Status.IN_PROGRESS)
            .build()

        beforeEach {
            coEvery { repository.findById(eq(id)) } returns request
        }

        describe("get request") {
            val result by memoizedBlocking { service.get(id.toString()) }

            it("should return request returned by the repository") {
                result shouldBe request
            }
        }
    }

    describe("get an non-existing request") {
        val exception by memoizedBlocking { runCatching { service.get(UUID(0L, 2L).toString()) }.exceptionOrNull() }

        it("should throw ${ResourceNotFoundException::class}") {
            exception.shouldNotBeNull()
        }
    }
})
