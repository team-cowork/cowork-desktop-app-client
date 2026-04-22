package com.cowork.app_client.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class PreferenceApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getAccountSettings(accessToken: String, accountId: Long): AccountSettings =
        client.get("$baseUrl/preferences/account/$accountId") {
            bearerAuth(accessToken)
        }.body<ApiResponse<AccountSettings>>().data ?: AccountSettings()

    suspend fun updateAccountSettings(
        accessToken: String,
        accountId: Long,
        status: String,
        statusExpiresAt: String?,
    ): AccountSettings =
        client.put("$baseUrl/preferences/account/$accountId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(UpdateAccountSettingsRequest(status = status, statusExpiresAt = statusExpiresAt))
        }.body<ApiResponse<AccountSettings>>().data ?: AccountSettings()

    @Serializable
    data class AccountSettings(
        val status: String? = null,
        @SerialName("status_expires_at")
        val statusExpiresAt: String? = null,
    )

    @Serializable
    private data class UpdateAccountSettingsRequest(
        val status: String,
        @SerialName("status_expires_at")
        val statusExpiresAt: String?,
    )
}
