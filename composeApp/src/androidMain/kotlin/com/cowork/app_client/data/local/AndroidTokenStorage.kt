package com.cowork.app_client.data.local

import android.content.Context

class AndroidTokenStorage(context: Context) : TokenStorage {
    private val prefs = context.getSharedPreferences("cowork_tokens", Context.MODE_PRIVATE)

    override suspend fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    override suspend fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    override suspend fun clearTokens() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
