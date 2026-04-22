package com.cowork.app_client.data.remote

import com.cowork.app_client.domain.model.ChatMessage
import com.cowork.app_client.domain.model.MessageType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ChatApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getMessages(
        accessToken: String,
        channelId: Long,
        before: String?,
        limit: Int,
    ): List<ChatMessage> =
        client.get("$baseUrl/channels/$channelId/messages") {
            bearerAuth(accessToken)
            parameter("before", before)
            parameter("limit", limit)
        }.body<ApiResponse<List<MessageResponse>>>().data.orEmpty().map(MessageResponse::toDomain)

    @Serializable
    private data class MessageResponse(
        @SerialName("_id")
        val mongoId: String? = null,
        val id: String? = null,
        val teamId: Long,
        val projectId: Long? = null,
        val channelId: Long,
        val authorId: Long,
        val content: String,
        val parentMessageId: String? = null,
        val type: String = "text",
        val fileUrl: String? = null,
        val fileName: String? = null,
        val fileSize: Long? = null,
        val createdAt: String? = null,
    ) {
        fun toDomain(): ChatMessage = ChatMessage(
            id = id ?: mongoId.orEmpty(),
            teamId = teamId,
            projectId = projectId,
            channelId = channelId,
            authorId = authorId,
            content = content,
            parentMessageId = parentMessageId,
            type = type.toMessageType(),
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize,
            createdAt = createdAt,
        )
    }
}

private fun String.toMessageType(): MessageType = when (lowercase()) {
    "text" -> MessageType.Text
    "file" -> MessageType.File
    else -> MessageType.Unknown
}
