package com.github.jcornaz.islands.persistence

import com.github.jcornaz.islands.TestDataSet
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Record
import org.neo4j.driver.v1.Session
import org.neo4j.harness.TestServerBuilders
import java.io.Closeable
import java.io.File

class TestDatabase : Closeable {
    private val service = TestServerBuilders.newInProcessBuilder(File("tmp")).newServer()

    val driver: Driver = GraphDatabase.driver(service.boltURI())

    private val session: Session = driver.session()

    fun execute(statement: String): Sequence<Record> = session.run(statement).asSequence()

    operator fun plusAssign(dataSet: TestDataSet) {
        dataSet.maps.forEach { map ->
            execute(map.tileList.joinToString(
                separator = ", ",
                prefix = "CREATE (map:TileMap { id: \"${map.id}\" }), ",
                transform = { tile -> "(map)-[:HAS_TILE]->(:Tile { coordinate: point({x: ${tile.coordinate.x}, y: ${tile.coordinate.y}}), type: ${tile.type.number} })" }
            ))
        }
    }

    fun clear() {
        execute("MATCH (n) DETACH DELETE n")
    }

    override fun close() {
        session.close()
        driver.close()
        runCatching { service.close() }
    }
}
