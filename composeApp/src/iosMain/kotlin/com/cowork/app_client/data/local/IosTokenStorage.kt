package com.cowork.app_client.data.local

import platform.Foundation.NSUserDefaults

class IosTokenStorage : TokenStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun getAccessToken(): String? =
        defaults.stringForKey(KEY_ACCESS_TOKEN)

    override suspend fun getRefreshToken(): String? =
        defaults.stringForKey(KEY_REFRESH_TOKEN)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        defaults.setObject(accessToken, KEY_ACCESS_TOKEN)
        defaults.setObject(refreshToken, KEY_REFRESH_TOKEN)
    }

    override suspend fun clearTokens() {
        defaults.removeObjectForKey(KEY_ACCESS_TOKEN)
        defaults.removeObjectForKey(KEY_REFRESH_TOKEN)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "cowork_access_token"
        const val KEY_REFRESH_TOKEN = "cowork_refresh_token"
    }
}
