package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.Webhook
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class WebhookApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getWebhooks(accessToken: String, channelId: Long): List<Webhook> =
        client.get("$baseUrl/channels/$channelId/webhooks") {
            bearerAuth(accessToken)
        }.bodyPayload<List<WebhookResponse>?>().orEmpty().map(WebhookResponse::toDomain)

    suspend fun createWebhook(
        accessToken: String,
        channelId: Long,
        name: String,
        avatarUrl: String? = null,
        isSecure: Boolean = false,
    ): Webhook =
        client.post("$baseUrl/channels/$channelId/webhooks") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateWebhookRequest(name = name, avatarUrl = avatarUrl, isSecure = isSecure))
        }.bodyPayload<WebhookResponse?>()?.toDomain()
            ?: error("웹훅 생성 응답에 data가 없습니다")

    suspend fun updateWebhook(
        accessToken: String,
        channelId: Long,
        webhookId: Long,
        name: String? = null,
        avatarUrl: String? = null,
        isSecure: Boolean? = null,
    ): Webhook =
        client.patch("$baseUrl/channels/$channelId/webhooks/$webhookId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(UpdateWebhookRequest(name = name, avatarUrl = avatarUrl, isSecure = isSecure))
        }.bodyPayload<WebhookResponse?>()?.toDomain()
            ?: error("웹훅 수정 응답에 data가 없습니다")

    suspend fun deleteWebhook(accessToken: String, channelId: Long, webhookId: Long) {
        client.delete("$baseUrl/channels/$channelId/webhooks/$webhookId") {
            bearerAuth(accessToken)
        }
    }

    @Serializable
    private data class CreateWebhookRequest(
        val name: String,
        @SerialName("avatarUrl") val avatarUrl: String?,
        val isSecure: Boolean,
    )

    @Serializable
    private data class UpdateWebhookRequest(
        val name: String?,
        @SerialName("avatarUrl") val avatarUrl: String?,
        val isSecure: Boolean?,
    )

    @Serializable
    private data class WebhookResponse(
        val id: Long,
        val channelId: Long,
        val name: String,
        val isSecure: Boolean,
        val token: String? = null,
        val avatarUrl: String? = null,
        val createdBy: Long,
    ) {
        fun toDomain(): Webhook = Webhook(
            id = id,
            channelId = channelId,
            name = name,
            isSecure = isSecure,
            token = token,
            avatarUrl = avatarUrl,
            createdBy = createdBy,
        )
    }
}
