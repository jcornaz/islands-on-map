package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.FetchRequest
import com.github.jcornaz.islands.test.TestDatabase
import com.github.jcornaz.islands.test.beforeEachBlocking
import com.github.jcornaz.islands.test.memoizedBlocking
import com.github.jcornaz.islands.test.memoizedClosable
import org.amshove.kluent.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe
import java.util.*

class Neo4JFetchRequestRepositorySpec : Spek({
    val database by memoizedClosable(CachingMode.SCOPE) { TestDatabase() }
    val repository: FetchRequestRepository by memoized { Neo4JFetchRequestRepository(database.driver) }

    afterEachTest { database.clear() }

    describe("create a new request") {
        val id = UUID(0L, 1L)
        val request = FetchRequest.newBuilder().setId(id.toString()).setUrl("my-url.net").build()

        beforeEachBlocking {
            repository.create(request)
        }

        it("should create the request node") {
            database.execute("MATCH (:FetchRequest { id: \"$id\" }) RETURN count(*)")
                .single()[0].asInt() shouldEqual 1
        }

        it("should set url of the created node") {
            database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN r.url")
                .single()[0].asString() shouldEqual "my-url.net"
        }

        it("should set the status of the created node") {
            database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN r.status")
                .single()[0].asInt() shouldEqual FetchRequest.Status.PENDING_VALUE
        }
    }

    describe("given an existing pending request") {
        val id = UUID(0L, 1L)

        beforeEach {
            database.execute("CREATE (:FetchRequest { id: \"$id\", status: ${FetchRequest.Status.PENDING_VALUE}, url: \"my-url.net\" })")
        }

        describe("create a request with id of the existing") {
            val request = FetchRequest.newBuilder().setId(id.toString()).setUrl("my-url.net").build()

            var exception: Throwable? = null

            beforeEachBlocking {
                exception = runCatching { repository.create(request) }.exceptionOrNull()
            }

            it("should throw an exception") {
                exception shouldBeInstanceOf Exception::class
            }

            it("should not create a new request node") {
                database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN count(r)")
                    .single()[0].asInt() shouldEqual 1
            }
        }

        describe("create a new request") {
            val newRequestId = UUID(0L, 2L)
            val request = FetchRequest.newBuilder()
                .setId(newRequestId.toString())
                .setUrl("new-request.url")
                .build()

            beforeEachBlocking {
                repository.create(request)
            }

            it("should create the new request node") {
                database.execute("MATCH (:FetchRequest { id: \"$newRequestId\" }) RETURN count(*)")
                    .single()[0].asInt() shouldEqual 1
            }

            it("should set url of the new created node") {
                database.execute("MATCH (r:FetchRequest { id: \"$newRequestId\" }) RETURN r.url")
                    .single()[0].asString() shouldEqual "new-request.url"
            }

            it("should set the status of the new created node") {
                database.execute("MATCH (r:FetchRequest { id: \"$newRequestId\" }) RETURN r.status")
                    .single()[0].asInt() shouldEqual FetchRequest.Status.PENDING_VALUE
            }
        }

        describe("findById the existing request") {
            val foundRequest by memoizedBlocking { repository.findById(id) }

            it("should not return null") {
                foundRequest.shouldNotBeNull()
            }

            it("should return request with id") {
                foundRequest?.id shouldEqual id.toString()
            }

            it("should return request with url") {
                foundRequest?.url shouldEqual "my-url.net"
            }

            it("should return request with status") {
                foundRequest?.status shouldEqual FetchRequest.Status.PENDING
            }

            it("should return request without result") {
                foundRequest?.resultCase shouldEqual FetchRequest.ResultCase.RESULT_NOT_SET
            }
        }

        describe("setInProgress") {
            beforeEachBlocking {
                repository.setInProgress(id)
            }

            it("should set the status to the node in in-progress") {
                database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN r.status")
                    .single()[0].asInt() shouldEqual FetchRequest.Status.IN_PROGRESS_VALUE
            }
        }

        describe("setError") {
            beforeEachBlocking {
                repository.setError(id, "something wrong happened")
            }

            it("should update status the node to done") {
                database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN r.status")
                    .single()[0].asInt() shouldEqual FetchRequest.Status.DONE_VALUE
            }

            it("should add error cause to the node") {
                database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN r.error")
                    .single()[0].asString() shouldEqual "something wrong happened"
            }
        }

        describe("given an existing map") {
            val mapId = UUID(0L, 3L)

            beforeEach {
                database.execute("CREATE (:TileMap { id: \"$mapId\" })")
            }

            describe("setSuccess for the existing map") {
                beforeEachBlocking {
                    repository.setSuccess(id, mapId)
                }

                it("should update status of the node") {
                    database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN r.status")
                        .single()[0].asInt() shouldEqual FetchRequest.Status.DONE_VALUE
                }

                it("should link the node to the created map") {
                    database.execute("MATCH (:FetchRequest { id: \"$id\" })-[:CREATED]->(map:TileMap) RETURN map.id")
                        .single()[0].asString() shouldEqual mapId.toString()
                }
            }
        }

        describe("setSuccess for a non-existing map") {
            var exception: Throwable? = null

            beforeEachBlocking {
                exception = runCatching { repository.setSuccess(id, UUID(0L, 4L)) }.exceptionOrNull()
            }

            it("should throw an exception") {
                exception shouldBeInstanceOf Exception::class
            }

            it("should not update the status the node") {
                database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN r.status")
                    .single()[0].asInt() shouldEqual FetchRequest.Status.PENDING_VALUE
            }

            it("should not link the node to any map") {
                database.execute("MATCH (:FetchRequest { id: \"$id\" })-[r:CREATED]->(:TileMap) RETURN count(r)")
                    .single()[0].asInt() shouldEqual 0
            }
        }
    }

    describe("given a successful request") {
        val id = UUID(0L, 1L)
        val mapId = UUID(0L, 2L)

        beforeEach {
            database.execute("CREATE (request:FetchRequest { id: \"$id\", url: \"my-url.net\", status: ${FetchRequest.Status.DONE_VALUE} })-[:CREATED]->(:TileMap { id: \"$mapId\" })")
        }

        describe("findById the completed request") {
            val foundRequest by memoizedBlocking { repository.findById(id) }

            it("should not return null") {
                foundRequest.shouldNotBeNull()
            }

            it("should return request with id") {
                foundRequest?.id shouldEqual id.toString()
            }

            it("should return request with url") {
                foundRequest?.url shouldEqual "my-url.net"
            }

            it("should return request with status") {
                foundRequest?.status shouldEqual FetchRequest.Status.DONE
            }

            it("should return request with a map id") {
                foundRequest?.resultCase shouldEqual FetchRequest.ResultCase.MAP_ID
            }

            it("should return request with the expected map id") {
                foundRequest?.mapId shouldEqual mapId.toString()
            }
        }

        describe("setError") {
            var exception: Throwable? = null

            beforeEachBlocking {
                exception = runCatching { repository.setError(id, "oops") }.exceptionOrNull()
            }

            it("should throw an exception") {
                exception shouldBeInstanceOf Exception::class
            }

            it("should not change status of the request node") {
                database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN r.status")
                    .single()[0].asInt() shouldEqual FetchRequest.Status.DONE_VALUE
            }

            it("should not break the link between the request node and the created map") {
                database.execute("MATCH (:FetchRequest { id: \"$id\" })-[r:CREATED]->(:TileMap) RETURN count(r)")
                    .single()[0].asInt() shouldEqual 1
            }

            it("should not add error message to the request node") {
                database.execute("MATCH (r:FetchRequest { id: \"$id\" }) RETURN r.error")
                    .single()[0].isNull.shouldBeTrue()
            }
        }
    }

    describe("findById a non-existing request") {
        val foundRequest by memoizedBlocking { repository.findById(UUID(1L, 2L)) }

        it("should return null") {
            foundRequest.shouldBeNull()
        }
    }

    describe("setInProgress of non-existing request") {
        var exception: Throwable? = null

        beforeEachBlocking {
            exception = runCatching { repository.setInProgress(UUID(1L, 2L)) }.exceptionOrNull()
        }

        it("should throw an exception") {
            exception shouldBeInstanceOf Exception::class
        }
    }

    describe("setSuccess of a non-existing request") {
        var exception: Throwable? = null

        beforeEachBlocking {
            exception = runCatching { repository.setSuccess(UUID(1L, 2L), UUID(1L, 2L)) }.exceptionOrNull()
        }

        it("should throw an exception") {
            exception shouldBeInstanceOf Exception::class
        }
    }

    describe("setError of a non-existing request") {
        var exception: Throwable? = null

        beforeEachBlocking {
            exception = runCatching { repository.setError(UUID(1L, 2L), "oops") }.exceptionOrNull()
        }

        it("should throw an exception") {
            exception shouldBeInstanceOf Exception::class
        }
    }
})
