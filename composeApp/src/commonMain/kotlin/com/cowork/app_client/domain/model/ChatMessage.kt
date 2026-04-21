package com.cowork.app_client.domain.model

data class ChatMessage(
    val id: String,
    val teamId: Long,
    val projectId: Long?,
    val channelId: Long,
    val authorId: Long,
    val content: String,
    val parentMessageId: String?,
    val type: MessageType,
    val fileUrl: String?,
    val fileName: String?,
    val fileSize: Long?,
    val createdAt: String?,
)

enum class MessageType {
    Text,
    File,
    Unknown,
}
