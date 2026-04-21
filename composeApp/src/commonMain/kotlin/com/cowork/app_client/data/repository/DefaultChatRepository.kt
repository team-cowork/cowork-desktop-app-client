package com.cowork.app_client.data.repository

import com.cowork.app_client.data.remote.ChatApi
import com.cowork.app_client.domain.model.ChatMessage
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

class DefaultChatRepository(
    private val authRepository: AuthRepository,
    private val chatApi: ChatApi,
) : ChatRepository {

    override suspend fun getMessages(channelId: Long, before: String?, limit: Int): List<ChatMessage> =
        authorized { accessToken -> chatApi.getMessages(accessToken, channelId, before, limit) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T {
        val tokens = authRepository.getStoredTokens() ?: error("로그인이 필요합니다.")
        return try {
            block(tokens.accessToken)
        } catch (exception: ClientRequestException) {
            if (exception.response.status != HttpStatusCode.Unauthorized) {
                throw exception
            }

            val refreshed = authRepository.refreshTokens() ?: throw exception
            block(refreshed.accessToken)
        }
    }
}
