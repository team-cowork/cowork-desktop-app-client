package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.ChatMessage

interface ChatRepository {
    suspend fun getMessages(channelId: Long, before: String? = null, limit: Int = 50): List<ChatMessage>
    suspend fun sendMessage(channelId: Long, teamId: Long, content: String)
    suspend fun editMessage(channelId: Long, messageId: String, content: String)
    suspend fun deleteMessage(channelId: Long, messageId: String)
}
