package com.github.jcornaz.islands.test

import com.google.protobuf.Message
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.setBody
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.spekframework.spek2.style.specification.Suite

fun TestApplicationRequest.setBody(message: Message) {
    addHeader(HttpHeaders.Accept, ContentType.Application.OctetStream.toString())
    setBody(message.toByteArray())
}

fun Suite.itShouldHandleRequest(expectedStatusCode: HttpStatusCode = HttpStatusCode.OK, getCall: () -> TestApplicationCall) {
    it("should handle request") {
        getCall().requestHandled.shouldBeTrue()
    }

    it("should return $expectedStatusCode") {
        getCall().response.status() shouldEqual expectedStatusCode
    }

    if (expectedStatusCode.isSuccess()) {
        it("should return a body") {
            getCall().response.byteContent.shouldNotBeNull()
        }
    }
}
