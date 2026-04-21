package com.cowork.app_client.feature.auth

class AndroidOAuthLauncher : OAuthLauncher {
    override suspend fun launch(signInUrl: String): OAuthAuthorizationCode? {
        // TODO: Android OAuth via Chrome Custom Tabs + deep link
        throw NotImplementedError("Android OAuth not yet implemented")
    }
}
