package com.cowork.app_client.data.remote

import com.cowork.app_client.domain.model.AuthTokens
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun refresh(refreshToken: String): AuthTokens {
        val response = client.post("$baseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }
        val body = json.decodeFromString<TokenResponse>(response.bodyAsText())
        return AuthTokens(body.accessToken, body.refreshToken)
    }

    suspend fun signOut(accessToken: String) {
        client.post("$baseUrl/auth/signout") {
            bearerAuth(accessToken)
        }
    }

    fun getSignInUrl(): String = "$baseUrl/auth/signin"

    @Serializable
    private data class RefreshRequest(val refreshToken: String)

    @Serializable
    data class TokenResponse(
        val accessToken: String,
        val refreshToken: String,
    )
}
