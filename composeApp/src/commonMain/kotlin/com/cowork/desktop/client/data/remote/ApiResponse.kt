package com.cowork.desktop.client.data.remote

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class ApiResponse<T>(
    val status: String,
    val code: Int,
    val message: String,
    val data: T? = null,
)

/**
 * Decodes both gateway responses wrapped in `{ status, code, message, data }`
 * and raw service responses. Some environments bypass the gateway response
 * advice for larger project/channel payloads.
 */
internal suspend inline fun <reified T> HttpResponse.bodyPayload(): T {
    val response = body<JsonElement>()
    val wrapper = (response as? JsonObject)?.takeIf {
        it.containsKey("status") &&
            it.containsKey("code") &&
            it.containsKey("message") &&
            it.containsKey("data")
    }
    val payload = wrapper?.get("data") ?: response
    return responsePayloadJson.decodeFromJsonElement(payload)
}

@PublishedApi
internal val responsePayloadJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
