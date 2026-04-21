package com.cowork.app_client.domain.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)
