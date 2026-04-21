package com.cowork.app_client.data.repository

import com.cowork.app_client.data.local.TokenStorage
import com.cowork.app_client.data.remote.AuthApi
import com.cowork.app_client.domain.model.AuthTokens

class DefaultAuthRepository(
    private val tokenStorage: TokenStorage,
    private val authApi: AuthApi,
) : AuthRepository {

    override suspend fun getStoredTokens(): AuthTokens? {
        val access = tokenStorage.getAccessToken() ?: return null
        val refresh = tokenStorage.getRefreshToken() ?: return null
        return AuthTokens(access, refresh)
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
    }

    override suspend fun refreshTokens(): AuthTokens? {
        val refreshToken = tokenStorage.getRefreshToken() ?: return null
        return runCatching {
            val tokens = authApi.refresh(refreshToken)
            tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
            tokens
        }.getOrNull()
    }

    override suspend fun signOut() {
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken != null) {
            runCatching { authApi.signOut(accessToken) }
        }
        tokenStorage.clearTokens()
    }

    override fun getSignInUrl(): String = authApi.getSignInUrl()
}
