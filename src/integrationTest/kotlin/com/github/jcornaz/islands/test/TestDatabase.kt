package com.github.jcornaz.islands.test

import com.github.jcornaz.islands.persistence.createConstaints
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Record
import org.neo4j.harness.TestServerBuilders
import java.io.Closeable
import java.io.File

class TestDatabase : Closeable {
    private val service = TestServerBuilders.newInProcessBuilder(File("build/tmp/db")).newServer()

    val driver: Driver = GraphDatabase.driver(service.boltURI())

    init {
        try {
            driver.createConstaints()
        } catch (t: Throwable) {
            close()
            throw t
        }
    }

    fun execute(statement: String): List<Record> = driver.session().use { session ->
        session.run(statement).list()
    }

    fun clear() {
        execute("MATCH (n) DETACH DELETE n")
    }

    override fun close() {
        driver.close()
        runCatching { service.close() }
    }
}
