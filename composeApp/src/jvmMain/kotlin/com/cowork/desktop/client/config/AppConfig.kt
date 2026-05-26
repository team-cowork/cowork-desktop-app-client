package com.cowork.desktop.client.config

internal actual fun resolveCoworkApiBaseUrl(): String =
    System.getenv("COWORK_API_BASE_URL") ?: "http://localhost:8080/api"

internal actual fun resolveCoworkChatWsBaseUrl(): String =
    System.getenv("COWORK_CHAT_WS_BASE_URL") ?: "http://localhost:8080"
