package com.cowork.desktop.client.domain.model

data class ChatAttachment(
    val name: String,
    val url: String,
    val size: Long,
    val mimeType: String,
)

data class ChatReaction(
    val emoji: String,
    val count: Int,
    val myReaction: Boolean,
)

data class MentionedChatMessage(
    val id: String,
    val authorId: Long,
    val content: String,
    val type: MessageType,
    val createdAt: String?,
)

data class ChatFileUpload(
    val objectKey: String,
    val uploadUrl: String,
    val fileUrl: String,
    val expiresInSeconds: Long,
    val headers: Map<String, String>,
)

data class ChatFileItem(
    val fileId: String,
    val messageId: String,
    val fileName: String,
    val fileSize: Long,
    val fileUrl: String,
    val mimeType: String,
    val uploaderId: Long,
    val uploaderName: String,
    val uploadedAt: String,
)

data class ChatFilePage(
    val files: List<ChatFileItem>,
    val nextCursor: String?,
)

data class ChannelUnreadCount(
    val channelId: Long,
    val unreadCount: Int,
)

data class ChatMessageSearchQuery(
    val query: String,
    val channelId: Long? = null,
    val authorId: Long? = null,
    val type: MessageType? = null,
    val hasFile: Boolean? = null,
    val before: String? = null,
    val limit: Int = 50,
)

data class ChatMessageSearchItem(
    val messageId: String,
    val channelId: Long,
    val authorId: Long,
    val content: String,
    val highlights: List<String>,
    val type: MessageType,
    val hasAttachments: Boolean,
    val isPinned: Boolean,
    val createdAt: String,
)

data class ChatMessageSearchPage(
    val messages: List<ChatMessageSearchItem>,
    val nextCursor: String?,
)

data class DirectMessageConversation(
    val channelId: Long,
    val otherUserId: Long?,
    val unreadCount: Int,
    val lastMessage: DirectMessagePreview?,
)

data class DirectMessagePreview(
    val messageId: String,
    val authorId: Long,
    val content: String,
    val type: MessageType,
    val createdAt: String,
)
