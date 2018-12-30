package com.github.jcornaz.islands

import com.google.protobuf.Message
import com.google.protobuf.Parser
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.features.ContentConverter
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.util.cio.toByteArray
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.io.ByteChannel
import kotlin.reflect.full.staticFunctions

fun Application.installContentNegotiation() {
    install(ContentNegotiation) {
        register(ContentType.Any, ProtobufConverter)
    }
}

private object ProtobufConverter : ContentConverter {
    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val parser = context.subject.type.staticFunctions.first { it.name == "parser" }.call() as Parser<*>

        return parser.parseFrom((context.subject.value as ByteChannel).toByteArray())
    }

    override suspend fun convertForSend(context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any): Any? {
        return (value as Message).toByteArray()
    }
}
