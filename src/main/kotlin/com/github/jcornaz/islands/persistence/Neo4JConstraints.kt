package com.github.jcornaz.islands.persistence

import org.neo4j.driver.v1.Driver

fun Driver.createConstaints() {
    session().use { session ->
        session.run("CREATE CONSTRAINT ON (map:TileMap) ASSERT map.id IS UNIQUE").consume()
        session.run("CREATE CONSTRAINT ON (island:Island) ASSERT island.id IS UNIQUE").consume()
        session.run("CREATE INDEX ON :Tile(coordinate)").consume()
    }
}
