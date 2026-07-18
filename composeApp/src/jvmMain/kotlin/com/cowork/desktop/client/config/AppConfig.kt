package com.cowork.desktop.client.config

internal actual fun resolveCoworkApiBaseUrl(): String =
    System.getenv("COWORK_API_BASE_URL") ?: "https://ssh.gsmsv.site:22132/api"

internal actual fun resolveCoworkChatWsBaseUrl(): String =
    System.getenv("COWORK_CHAT_WS_BASE_URL") ?: "https://ssh.gsmsv.site:22132"
