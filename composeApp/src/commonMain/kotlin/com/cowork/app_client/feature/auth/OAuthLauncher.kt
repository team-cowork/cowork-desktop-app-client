package com.cowork.app_client.feature.auth

interface OAuthLauncher {
    suspend fun launch(signInUrl: String): OAuthAuthorizationCode?
}

data class OAuthAuthorizationCode(
    val code: String,
    val state: String,
    val codeVerifier: String,
    val redirectUri: String,
)
