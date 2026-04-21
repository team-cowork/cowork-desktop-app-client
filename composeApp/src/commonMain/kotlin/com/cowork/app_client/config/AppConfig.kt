package com.cowork.app_client.config

internal expect fun resolveCoworkApiBaseUrl(): String

object AppConfig {
    val COWORK_API_BASE_URL: String = resolveCoworkApiBaseUrl()
    const val DATAGSM_AUTHORIZE_URL = "https://oauth.authorization.datagsm.kr/v1/oauth/authorize"
    const val DESKTOP_OAUTH_SCHEME = "cowork"
    const val DESKTOP_OAUTH_CALLBACK_HOST = "oauth"
    const val DESKTOP_OAUTH_CALLBACK_PATH = "/callback"

    const val DATAGSM_CLIENT_ID = "e7cbba21-deb8-41a2-8648-f9a2234b343f"
}
