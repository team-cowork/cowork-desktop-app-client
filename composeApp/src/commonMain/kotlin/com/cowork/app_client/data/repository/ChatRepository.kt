package com.cowork.app_client.data.repository

import com.cowork.app_client.domain.model.ChatMessage

interface ChatRepository {
    suspend fun getMessages(channelId: Long, before: String? = null, limit: Int = 50): List<ChatMessage>
}
