package com.cowork.app_client.data.local

import java.util.prefs.Preferences

class JvmTokenStorage : TokenStorage {
    private val prefs: Preferences = Preferences.userRoot().node("com/cowork/app_client")

    override suspend fun getAccessToken(): String? = prefs.get(KEY_ACCESS_TOKEN, null)

    override suspend fun getRefreshToken(): String? = prefs.get(KEY_REFRESH_TOKEN, null)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.put(KEY_ACCESS_TOKEN, accessToken)
        prefs.put(KEY_REFRESH_TOKEN, refreshToken)
        prefs.flush()
    }

    override suspend fun clearTokens() {
        prefs.remove(KEY_ACCESS_TOKEN)
        prefs.remove(KEY_REFRESH_TOKEN)
        prefs.flush()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
