package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.ChatApi
import com.cowork.desktop.client.domain.model.ChatMessage

class DefaultChatRepository(
    private val authRepository: AuthRepository,
    private val chatApi: ChatApi,
) : ChatRepository {

    override suspend fun getMessages(channelId: Long, before: String?, limit: Int): List<ChatMessage> =
        authorized { accessToken -> chatApi.getMessages(accessToken, channelId, before, limit) }

    override suspend fun sendMessage(channelId: Long, teamId: Long, content: String) =
        authorized { accessToken -> chatApi.sendMessage(accessToken, channelId, teamId, content) }

    override suspend fun editMessage(channelId: Long, messageId: String, content: String) =
        authorized { accessToken -> chatApi.editMessage(accessToken, channelId, messageId, content) }

    override suspend fun deleteMessage(channelId: Long, messageId: String) =
        authorized { accessToken -> chatApi.deleteMessage(accessToken, channelId, messageId) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
