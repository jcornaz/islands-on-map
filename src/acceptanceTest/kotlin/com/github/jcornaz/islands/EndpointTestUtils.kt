package com.github.jcornaz.islands

import com.google.protobuf.Message
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.setBody
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.dsl.Root
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe

fun Root.describeBadRequest(
    case: String,
    method: HttpMethod,
    uri: String,
    body: Message? = null,
    expectedStatusCode: HttpStatusCode = HttpStatusCode.BadRequest,
    getApplication: () -> TestApplication
) {

    describe(case) {
        val call by memoized { getApplication().handleRequest(method, uri) { body?.let { setBody(body.toByteArray()) } } }

        it("should handle request") {
            call.requestHandled.shouldBeTrue()
        }

        it("should return $expectedStatusCode") {
            call.response.status() shouldEqual expectedStatusCode
        }
    }
}

fun Suite.itShouldHandleRequest(expectedStatusCode: HttpStatusCode = HttpStatusCode.OK, getCall: () -> TestApplicationCall) {
    it("should handle request") {
        getCall().requestHandled.shouldBeTrue()
    }

    it("should return $expectedStatusCode") {
        getCall().response.status() shouldEqual expectedStatusCode
    }
}
