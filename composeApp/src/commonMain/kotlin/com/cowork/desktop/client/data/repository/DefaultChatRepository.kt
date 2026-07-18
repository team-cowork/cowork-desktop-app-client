package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.ChatApi
import com.cowork.desktop.client.domain.model.ChannelUnreadCount
import com.cowork.desktop.client.domain.model.ChatAttachment
import com.cowork.desktop.client.domain.model.ChatFilePage
import com.cowork.desktop.client.domain.model.ChatFileUpload
import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.ChatMessageSearchPage
import com.cowork.desktop.client.domain.model.ChatMessageSearchQuery
import com.cowork.desktop.client.domain.model.DirectMessageConversation

class DefaultChatRepository(
    private val authRepository: AuthRepository,
    private val chatApi: ChatApi,
) : ChatRepository {

    override suspend fun getMessages(
        channelId: Long,
        before: String?,
        limit: Int,
        parentMessageId: String?,
    ): List<ChatMessage> =
        authorized { accessToken ->
            chatApi.getMessages(accessToken, channelId, before, limit, parentMessageId)
        }

    override suspend fun sendMessage(
        channelId: Long,
        teamId: Long?,
        content: String,
        projectId: Long?,
        attachments: List<ChatAttachment>,
        parentMessageId: String?,
        clientMessageId: String?,
    ) =
        authorized { accessToken ->
            chatApi.sendMessage(
                accessToken = accessToken,
                channelId = channelId,
                teamId = teamId,
                content = content,
                projectId = projectId,
                attachments = attachments,
                parentMessageId = parentMessageId,
                clientMessageId = clientMessageId,
            )
        }

    override suspend fun editMessage(channelId: Long, messageId: String, content: String) =
        authorized { accessToken -> chatApi.editMessage(accessToken, channelId, messageId, content) }

    override suspend fun deleteMessage(channelId: Long, messageId: String) =
        authorized { accessToken -> chatApi.deleteMessage(accessToken, channelId, messageId) }

    override suspend fun createFileUploadUrl(
        channelId: Long,
        filename: String,
        contentType: String,
        size: Long,
    ): ChatFileUpload =
        authorized { accessToken ->
            chatApi.createFileUploadUrl(accessToken, channelId, filename, contentType, size)
        }

    override suspend fun putFile(upload: ChatFileUpload, bytes: ByteArray) =
        chatApi.putFile(upload, bytes)

    override suspend fun confirmFileUpload(channelId: Long, objectKey: String): String =
        authorized { accessToken -> chatApi.confirmFileUpload(accessToken, channelId, objectKey) }

    override suspend fun getFiles(channelId: Long, before: String?, limit: Int): ChatFilePage =
        authorized { accessToken -> chatApi.getFiles(accessToken, channelId, before, limit) }

    override suspend fun deleteFile(channelId: Long, fileId: String) =
        authorized { accessToken -> chatApi.deleteFile(accessToken, channelId, fileId) }

    override suspend fun pinMessage(channelId: Long, messageId: String): ChatMessage =
        authorized { accessToken -> chatApi.pinMessage(accessToken, channelId, messageId) }

    override suspend fun unpinMessage(channelId: Long, messageId: String) =
        authorized { accessToken -> chatApi.unpinMessage(accessToken, channelId, messageId) }

    override suspend fun getPinnedMessages(channelId: Long): List<ChatMessage> =
        authorized { accessToken -> chatApi.getPinnedMessages(accessToken, channelId) }

    override suspend fun addReaction(channelId: Long, messageId: String, emoji: String) =
        authorized { accessToken -> chatApi.addReaction(accessToken, channelId, messageId, emoji) }

    override suspend fun removeReaction(channelId: Long, messageId: String, emoji: String) =
        authorized { accessToken -> chatApi.removeReaction(accessToken, channelId, messageId, emoji) }

    override suspend fun markChannelRead(channelId: Long, lastReadMessageId: String) =
        authorized { accessToken -> chatApi.markChannelRead(accessToken, channelId, lastReadMessageId) }

    override suspend fun getTeamUnread(teamId: Long): List<ChannelUnreadCount> =
        authorized { accessToken -> chatApi.getTeamUnread(accessToken, teamId) }

    override suspend fun searchTeamMessages(
        teamId: Long,
        query: ChatMessageSearchQuery,
    ): ChatMessageSearchPage =
        authorized { accessToken -> chatApi.searchTeamMessages(accessToken, teamId, query) }

    override suspend fun searchProjectMessages(
        projectId: Long,
        query: ChatMessageSearchQuery,
    ): ChatMessageSearchPage =
        authorized { accessToken -> chatApi.searchProjectMessages(accessToken, projectId, query) }

    override suspend fun createGithubIssue(
        channelId: Long,
        projectId: Long,
        title: String,
        body: String?,
    ): Boolean =
        authorized { accessToken ->
            chatApi.createGithubIssue(accessToken, channelId, projectId, title, body)
        }

    override suspend fun getDms(): List<DirectMessageConversation> =
        authorized(chatApi::getDms)

    override suspend fun hideDm(channelId: Long) =
        authorized { accessToken -> chatApi.hideDm(accessToken, channelId) }

    override suspend fun blockUser(targetUserId: Long) =
        authorized { accessToken -> chatApi.blockUser(accessToken, targetUserId) }

    override suspend fun unblockUser(targetUserId: Long) =
        authorized { accessToken -> chatApi.unblockUser(accessToken, targetUserId) }

    override suspend fun getBlockedUserIds(): List<Long> =
        authorized(chatApi::getBlockedUserIds)

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
