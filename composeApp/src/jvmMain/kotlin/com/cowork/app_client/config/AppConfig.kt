package com.cowork.app_client.config

internal actual fun resolveCoworkApiBaseUrl(): String =
    System.getenv("COWORK_API_BASE_URL") ?: "http://localhost:8080/api"
