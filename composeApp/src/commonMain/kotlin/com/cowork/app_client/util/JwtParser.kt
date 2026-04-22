package com.cowork.app_client.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal data class JwtClaims(
    val accountId: Long? = null,
    val email: String? = null,
)

@Serializable
private data class JwtPayload(
    val sub: String? = null,
    val email: String? = null,
)

private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

@OptIn(ExperimentalEncodingApi::class)
internal fun parseJwtClaims(jwt: String): JwtClaims {
    val rawPayload = jwt.split(".").getOrNull(1) ?: return JwtClaims()
    return try {
        val padLength = (4 - rawPayload.length % 4) % 4
        val padded = rawPayload + "=".repeat(padLength)
        val decoded = Base64.UrlSafe.decode(padded)
        val payload = lenientJson.decodeFromString<JwtPayload>(decoded.decodeToString())
        JwtClaims(
            accountId = payload.sub?.toLongOrNull(),
            email = payload.email,
        )
    } catch (_: Exception) {
        JwtClaims()
    }
}
