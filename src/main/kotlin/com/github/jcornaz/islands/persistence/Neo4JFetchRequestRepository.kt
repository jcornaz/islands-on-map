package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.FetchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.neo4j.driver.v1.AccessMode
import org.neo4j.driver.v1.Driver
import java.util.*

class Neo4JFetchRequestRepository(private val driver: Driver) : FetchRequestRepository {

    override suspend fun create(request: FetchRequest) {
        withContext(Dispatchers.IO) {
            driver.session().use { session ->
                session.run(
                    "CREATE (request:FetchRequest { id: \$id, url: \$url, status: \$status })",
                    mapOf("id" to request.id, "url" to request.url, "status" to request.status.number)
                ).consume()
            }

            when (request.resultCase) {
                FetchRequest.ResultCase.MAP_ID -> setSuccess(UUID.fromString(request.id), UUID.fromString(request.mapId))
                FetchRequest.ResultCase.ERROR -> setError(UUID.fromString(request.id), request.error)
                else -> Unit
            }
        }
    }

    override suspend fun setInProgress(id: UUID) = withContext(Dispatchers.IO) {
        driver.session().use { session ->
            session.run(
                """
                    MATCH (request:FetchRequest { id: ${'$'}id })
                    WHERE NOT (request)-[:CREATED]->()
                    SET request.status = ${FetchRequest.Status.IN_PROGRESS_VALUE}
                    RETURN count(DISTINCT request)
                """.trimIndent(),
                mapOf("id" to id.toString())
            ).single()[0].asInt().let { check(it == 1) }
        }
    }

    override suspend fun setSuccess(id: UUID, mapId: UUID) {
        driver.session().use { session ->
            session.run(
                """
                    MATCH (request:FetchRequest { id: ${'$'}id }), (map:TileMap { id: ${'$'}mapId })
                    WHERE NOT (request)-[:CREATED]->(map)
                    SET request.status = ${FetchRequest.Status.DONE_VALUE}
                    REMOVE request.error
                    CREATE (request)-[:CREATED]->(map)
                    RETURN count(DISTINCT request)
                """.trimIndent(),
                mapOf("id" to id.toString(), "mapId" to mapId.toString())
            ).single()[0].asInt().let { check(it == 1) }
        }
    }

    override suspend fun setError(id: UUID, error: String) {
        driver.session().use { session ->
            session.run(
                """
                    MATCH (request:FetchRequest { id: ${'$'}id })
                    WHERE NOT (request)-[:CREATED]->()
                    SET request.status = ${FetchRequest.Status.DONE_VALUE}, request.error = ${'$'}error
                    RETURN count(DISTINCT request)
                """.trimIndent(),
                mapOf("id" to id.toString(), "error" to error)
            ).single()[0].asInt().let { check(it == 1) }
        }
    }

    override suspend fun findById(id: UUID): FetchRequest? = withContext(Dispatchers.IO) {
        driver.session(AccessMode.READ).use { session ->
            val result = session.run(
                """
                    MATCH (request:FetchRequest { id: ${'$'}id })
                    OPTIONAL MATCH (request)-[:CREATED]->(map:TileMap)
                    RETURN request.status AS status, request.url AS url, request.error AS error, map.id AS mapId
                """.trimIndent(),
                mapOf("id" to id.toString())
            )

            if (!result.hasNext()) return@use null

            val record = result.single()

            val builder = FetchRequest.newBuilder()
                .setId(id.toString())
                .setUrl(record["url"].asString())
                .setStatus(FetchRequest.Status.forNumber(record["status"].asInt()))


            record["mapId"].takeUnless { it.isNull }?.asString()?.let { builder.setMapId(it) } ?: run {
                record["error"].takeUnless { it.isNull }?.asString()?.let { builder.setError(it) }
            }

            builder.build()
        }
    }
}
