package com.cowork.app_client.data.repository

import com.cowork.app_client.data.local.TokenStorage
import com.cowork.app_client.data.remote.AuthApi
import com.cowork.app_client.domain.model.AuthTokens
import com.cowork.app_client.feature.auth.OAuthAuthorizationCode
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultAuthRepository(
    private val tokenStorage: TokenStorage,
    private val authApi: AuthApi,
) : AuthRepository {

    private val refreshMutex = Mutex()

    override suspend fun getStoredTokens(): AuthTokens? {
        val access = tokenStorage.getAccessToken() ?: return null
        val refresh = tokenStorage.getRefreshToken() ?: return null
        return AuthTokens(access, refresh)
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
    }

    override suspend fun exchangeAuthorizationCode(authorizationCode: OAuthAuthorizationCode): AuthTokens =
        authApi.exchangeAuthorizationCode(authorizationCode)

    override suspend fun refreshTokens(): AuthTokens? = refreshMutex.withLock {
        val refreshToken = tokenStorage.getRefreshToken() ?: return@withLock null
        runCatching {
            val tokens = authApi.refresh(refreshToken)
            tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
            tokens
        }.getOrElse { e ->
            if (e is ClientRequestException &&
                (e.response.status == HttpStatusCode.Unauthorized ||
                        e.response.status == HttpStatusCode.Forbidden)
            ) {
                tokenStorage.clearTokens()
            }
            null
        }
    }

    override suspend fun signOut() {
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()
        if (accessToken != null && refreshToken != null) {
            runCatching { authApi.signOut(accessToken, refreshToken) }
        }
        tokenStorage.clearTokens()
    }

    override fun getSignInUrl(): String = authApi.getSignInUrl()
}
