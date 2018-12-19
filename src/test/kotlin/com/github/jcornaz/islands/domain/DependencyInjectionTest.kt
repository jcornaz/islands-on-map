package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.productionModules
import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import org.koin.test.check.checkModules

class DependencyInjectionTest {

    @Test
    fun testDiModules() {
        val app = koinApplication {
            modules(productionModules)
        }

        app.checkModules()
    }
}