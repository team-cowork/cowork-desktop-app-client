package com.cowork.app_client.feature.auth

import com.cowork.app_client.domain.model.AuthTokens

class AndroidOAuthLauncher : OAuthLauncher {
    override suspend fun launch(signInUrl: String): AuthTokens? {
        // TODO: Android OAuth via Chrome Custom Tabs + deep link
        throw NotImplementedError("Android OAuth not yet implemented")
    }
}
