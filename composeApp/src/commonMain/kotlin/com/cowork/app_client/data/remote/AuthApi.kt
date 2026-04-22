package com.cowork.app_client.data.remote

import com.cowork.app_client.config.AppConfig
import com.cowork.app_client.domain.model.AuthTokens
import com.cowork.app_client.feature.auth.OAuthAuthorizationCode
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.call.body
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun exchangeAuthorizationCode(authorizationCode: OAuthAuthorizationCode): AuthTokens {
        val body = client.post("$baseUrl/auth/token") {
            contentType(ContentType.Application.Json)
            setBody(
                AuthorizationCodeExchangeRequest(
                    code = authorizationCode.code,
                    codeVerifier = authorizationCode.codeVerifier,
                    redirectUri = authorizationCode.redirectUri,
                )
            )
        }.body<ApiResponse<TokenResponse>>().data ?: error("토큰 교환 응답에 data가 없습니다")
        return AuthTokens(body.accessToken, body.refreshToken)
    }

    suspend fun refresh(refreshToken: String): AuthTokens {
        val body = client.post("$baseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }.body<ApiResponse<TokenResponse>>().data ?: error("토큰 갱신 응답에 data가 없습니다")
        return AuthTokens(body.accessToken, body.refreshToken)
    }

    suspend fun signOut(accessToken: String, refreshToken: String) {
        client.post("$baseUrl/auth/signout") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }
    }

    fun getSignInUrl(): String = AppConfig.DATAGSM_AUTHORIZE_URL

    @Serializable
    private data class AuthorizationCodeExchangeRequest(
        val code: String,
        @SerialName("code_verifier")
        val codeVerifier: String,
        @SerialName("redirect_uri")
        val redirectUri: String,
    )

    @Serializable
    private data class RefreshRequest(
        @SerialName("refresh_token")
        val refreshToken: String,
    )

    @Serializable
    data class TokenResponse(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("refresh_token")
        val refreshToken: String,
    )
}
