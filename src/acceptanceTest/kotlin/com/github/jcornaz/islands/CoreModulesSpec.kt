package com.github.jcornaz.islands

import com.github.jcornaz.islands.persistence.httpEngine
import com.github.jcornaz.islands.test.TestApplication
import io.ktor.http.Url
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.check.checkModules
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class CoreModulesSpec : Spek({
    describe("production environment") {
        beforeEach {
            startKoin {
                modules(coreModules + httpEngine)
            }
        }

        afterEach {
            stopKoin()
        }

        it("should have valid module definition") {
            GlobalContext.get().checkModules()
        }
    }

    describe("test environment") {
        lateinit var application: TestApplication

        beforeEach {
            application = TestApplication(Url("bolt://dummy-url.net"))
        }

        afterEach {
            application.close()
        }

        it("should have valid module definition") {
            GlobalContext.get().checkModules()
        }
    }
})
