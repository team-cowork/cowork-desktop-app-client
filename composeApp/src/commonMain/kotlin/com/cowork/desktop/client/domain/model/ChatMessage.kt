package com.cowork.desktop.client.domain.model

data class ChatMessage(
    val id: String,
    val teamId: Long?,
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
    val attachments: List<ChatAttachment> = emptyList(),
    val reactions: List<ChatReaction> = emptyList(),
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val clientMessageId: String? = null,
    val mentions: List<Long> = emptyList(),
    val mentionedMessage: MentionedChatMessage? = null,
    val updatedAt: String? = null,
)

enum class MessageType {
    Text,
    File,
    System,
    Unknown,
}
