package com.github.jcornaz.islands.service

import com.github.jcornaz.islands.FetchRequest
import com.github.jcornaz.islands.TileMap
import com.github.jcornaz.islands.TileType
import com.github.jcornaz.islands.persistence.FetchRequestRepository
import com.github.jcornaz.islands.persistence.TileRepository
import com.github.jcornaz.islands.test.beforeEachBlocking
import com.github.jcornaz.islands.test.memoizedMock
import com.github.jcornaz.islands.test.tile
import com.github.jcornaz.miop.produce
import io.mockk.*
import kotlinx.coroutines.GlobalScope
import org.amshove.kluent.shouldBeInstanceOf
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*

class DefaultFetchServiceSpec : Spek({
    val mapService: MapService by memoizedMock()
    val fetchRequestRepository: FetchRequestRepository by memoizedMock(relaxed = true)
    val tileRepositoryProvider: (url: String) -> TileRepository by memoizedMock()

    val service: FetchService by memoized { DefaultFetchService(mapService, fetchRequestRepository, tileRepositoryProvider) }

    describe("given an existing pending fetch-request") {
        val id = UUID(0L, 1L)

        val request = FetchRequest.newBuilder()
            .setId(id.toString())
            .setUrl("url-to-fetch.net")
            .setStatus(FetchRequest.Status.PENDING)
            .build()

        beforeEach {
            coEvery { fetchRequestRepository.findById(eq(id)) } returns request
        }

        describe("given the tile repository returning a set of tiles") {
            val tiles = setOf(
                tile(0, 0, TileType.WATER),
                tile(0, 1, TileType.WATER),
                tile(1, 0, TileType.LAND),
                tile(1, 1, TileType.LAND)
            )

            val mapId = UUID(0L, 2L)

            beforeEach {
                val tileRepository = mockk<TileRepository> {
                    every { findAll() } answers { GlobalScope.produce(tiles) }
                }

                every { tileRepositoryProvider(eq("url-to-fetch.net")) } returns tileRepository
                coEvery { mapService.create(match { it.tileList.toSet() == tiles }) } returns TileMap.newBuilder().setId(mapId.toString()).addAllTile(tiles).build()
            }

            describe("fetch the request") {
                beforeEachBlocking {
                    service.fetch(id)
                }

                it("should set the request status to in progress") {
                    coVerify(exactly = 1) { fetchRequestRepository.setInProgress(assert { it == id }) }
                }

                it("should set the request status to successful") {
                    coVerify(exactly = 1) { fetchRequestRepository.setSuccess(assert { it == id }, assert { it == mapId }) }
                }

                it("should not call setError") {
                    coVerify(exactly = 0) { fetchRequestRepository.setError(any(), any()) }
                }

                it("should set the request status to successful AFTER setting it to progress") {
                    coVerify(Ordering.ORDERED) {
                        fetchRequestRepository.setInProgress(any())
                        fetchRequestRepository.setSuccess(any(), any())
                    }
                }

                it("should create the map") {
                    coVerify(exactly = 1) { mapService.create(assert { it.tileList.toSet() == tiles }) }
                }
            }
        }

        describe("given the tile repository throw an error") {

            beforeEach {
                every { tileRepositoryProvider.invoke(eq("url-to-fetch.net")) } throws Exception("oops")
                coEvery { fetchRequestRepository.setError(eq(id), eq("oops")) } returns Unit
            }

            describe("fetch the request") {
                beforeEachBlocking {
                    service.fetch(id)
                }

                it("should set the request status to in progress") {
                    coVerify(exactly = 1) { fetchRequestRepository.setInProgress(assert { it == id }) }
                }

                it("should set the request status to error") {
                    coVerify(exactly = 1) { fetchRequestRepository.setError(assert { it == id }, any()) }
                }

                it("should not call setSuccess") {
                    coVerify(exactly = 0) { fetchRequestRepository.setSuccess(any(), any()) }
                }

                it("should report error message") {
                    coVerify { fetchRequestRepository.setError(any(), assert { "oops" in it }) }
                }

                it("should report url in error message") {
                    coVerify { fetchRequestRepository.setError(any(), assert { "url-to-fetch.net" in it }) }
                }

                it("should set the request status to error AFTER setting it to progress") {
                    coVerify(Ordering.ORDERED) {
                        fetchRequestRepository.setInProgress(any())
                        fetchRequestRepository.setError(any(), any())
                    }
                }

                it("should not create any map") {
                    coVerify(exactly = 0) { mapService.create(any()) }
                }
            }
        }
    }

    describe("fetch for a non-existing request") {
        var exception: Throwable? = null

        beforeEachBlocking {
            exception = runCatching { service.fetch(UUID(0L, 0L)) }.exceptionOrNull()
        }

        it("should should throw an exception") {
            exception shouldBeInstanceOf Exception::class
        }

        it("should not update the fetch-request") {
            coVerify(exactly = 0) { fetchRequestRepository.setInProgress(any()) }
            coVerify(exactly = 0) { fetchRequestRepository.setSuccess(any(), any()) }
            coVerify(exactly = 0) { fetchRequestRepository.setError(any(), any()) }
        }

        it("should not create any map") {
            coVerify(exactly = 0) { mapService.create(any()) }
        }
    }

    describe("fetch for an already in-progress request") {
        var exception: Throwable? = null
        val id = UUID(0L, 0L)

        beforeEachBlocking {
            coEvery { fetchRequestRepository.findById(eq(id)) } returns FetchRequest.newBuilder().setId(id.toString()).setStatus(FetchRequest.Status.IN_PROGRESS).build()
            exception = runCatching { service.fetch(id) }.exceptionOrNull()
        }

        it("should should throw an exception") {
            exception shouldBeInstanceOf Exception::class
        }

        it("should not update the fetch-request") {
            coVerify(exactly = 0) { fetchRequestRepository.setInProgress(any()) }
            coVerify(exactly = 0) { fetchRequestRepository.setSuccess(any(), any()) }
            coVerify(exactly = 0) { fetchRequestRepository.setError(any(), any()) }
        }

        it("should not create any map") {
            coVerify(exactly = 0) { mapService.create(any()) }
        }
    }

    describe("fetch for an already fetched request") {
        var exception: Throwable? = null
        val id = UUID(0L, 0L)

        beforeEachBlocking {
            coEvery { fetchRequestRepository.findById(eq(id)) } returns FetchRequest.newBuilder().setId(id.toString()).setStatus(FetchRequest.Status.DONE).build()
            exception = runCatching { service.fetch(id) }.exceptionOrNull()
        }

        it("should should throw an exception") {
            exception shouldBeInstanceOf Exception::class
        }

        it("should not update the fetch-request") {
            coVerify(exactly = 0) { fetchRequestRepository.setInProgress(any()) }
            coVerify(exactly = 0) { fetchRequestRepository.setSuccess(any(), any()) }
            coVerify(exactly = 0) { fetchRequestRepository.setError(any(), any()) }
        }

        it("should not create any map") {
            coVerify(exactly = 0) { mapService.create(any()) }
        }
    }
})
