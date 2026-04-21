package com.cowork.app_client.feature.auth

import com.cowork.app_client.domain.model.AuthTokens

interface OAuthLauncher {
    suspend fun launch(signInUrl: String): AuthTokens?
}
