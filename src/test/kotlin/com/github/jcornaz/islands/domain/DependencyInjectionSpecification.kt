package com.github.jcornaz.islands.domain

import com.github.jcornaz.islands.productionModules
import org.koin.dsl.koinApplication
import org.koin.test.check.checkModules
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class DependencyInjectionSpecification : Spek({
    describe("Production environment") {
        val app = koinApplication {
            modules(productionModules)
        }

        it("should run") {
            app.checkModules()
        }
    }
})
