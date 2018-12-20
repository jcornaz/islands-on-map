package com.github.jcornaz.islands.domain

val Map<Coordinate, TileType>.islands: Sequence<Set<Coordinate>>
    get() = sequence {

        val unseenCoordinates = entries.asSequence()
            .filter { it.value != TileType.WATER }
            .map { it.key }
            .toHashSet()

        while (unseenCoordinates.isNotEmpty()) {
            val island = HashSet<Coordinate>()

            fun append(coordinate: Coordinate) {
                island += coordinate

                sequenceOf(coordinate.up, coordinate.down, coordinate.left, coordinate.right)
                    .filter { it in this@islands }
                    .forEach {
                        if (unseenCoordinates.remove(it)) append(it)
                    }
            }

            append(unseenCoordinates.pop())

            yield(island)
        }
    }

private fun <E> MutableSet<E>.pop(): E = first().also { remove(it) }
