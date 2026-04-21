package com.cowork.app_client.data.repository

import com.cowork.app_client.domain.model.AuthTokens

interface AuthRepository {
    suspend fun getStoredTokens(): AuthTokens?
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun refreshTokens(): AuthTokens?
    suspend fun signOut()
    fun getSignInUrl(): String
}
