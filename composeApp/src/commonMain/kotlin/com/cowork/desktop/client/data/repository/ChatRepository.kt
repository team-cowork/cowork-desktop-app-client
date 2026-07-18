package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.ChannelUnreadCount
import com.cowork.desktop.client.domain.model.ChatAttachment
import com.cowork.desktop.client.domain.model.ChatFilePage
import com.cowork.desktop.client.domain.model.ChatFileUpload
import com.cowork.desktop.client.domain.model.ChatMessageSearchPage
import com.cowork.desktop.client.domain.model.ChatMessageSearchQuery
import com.cowork.desktop.client.domain.model.DirectMessageConversation

interface ChatRepository {
    suspend fun getMessages(
        channelId: Long,
        before: String? = null,
        limit: Int = 50,
        parentMessageId: String? = null,
    ): List<ChatMessage>

    suspend fun sendMessage(
        channelId: Long,
        teamId: Long?,
        content: String,
        projectId: Long? = null,
        attachments: List<ChatAttachment> = emptyList(),
        parentMessageId: String? = null,
        clientMessageId: String? = null,
    )

    suspend fun editMessage(channelId: Long, messageId: String, content: String)
    suspend fun deleteMessage(channelId: Long, messageId: String)

    suspend fun createFileUploadUrl(
        channelId: Long,
        filename: String,
        contentType: String,
        size: Long,
    ): ChatFileUpload

    suspend fun putFile(upload: ChatFileUpload, bytes: ByteArray)
    suspend fun confirmFileUpload(channelId: Long, objectKey: String): String
    suspend fun getFiles(channelId: Long, before: String? = null, limit: Int = 20): ChatFilePage
    suspend fun deleteFile(channelId: Long, fileId: String)

    suspend fun pinMessage(channelId: Long, messageId: String): ChatMessage
    suspend fun unpinMessage(channelId: Long, messageId: String)
    suspend fun getPinnedMessages(channelId: Long): List<ChatMessage>

    suspend fun addReaction(channelId: Long, messageId: String, emoji: String)
    suspend fun removeReaction(channelId: Long, messageId: String, emoji: String)

    suspend fun markChannelRead(channelId: Long, lastReadMessageId: String)
    suspend fun getTeamUnread(teamId: Long): List<ChannelUnreadCount>

    suspend fun searchTeamMessages(
        teamId: Long,
        query: ChatMessageSearchQuery,
    ): ChatMessageSearchPage

    suspend fun searchProjectMessages(
        projectId: Long,
        query: ChatMessageSearchQuery,
    ): ChatMessageSearchPage

    suspend fun createGithubIssue(
        channelId: Long,
        projectId: Long,
        title: String,
        body: String? = null,
    ): Boolean

    suspend fun getDms(): List<DirectMessageConversation>
    suspend fun hideDm(channelId: Long)
    suspend fun blockUser(targetUserId: Long)
    suspend fun unblockUser(targetUserId: Long)
    suspend fun getBlockedUserIds(): List<Long>
}
