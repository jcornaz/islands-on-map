package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.productionModules
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.checkModules

class DependencyInjectionTest : KoinTest {

    @Test
    fun testDiModules() {
        checkModules(productionModules)
    }
}