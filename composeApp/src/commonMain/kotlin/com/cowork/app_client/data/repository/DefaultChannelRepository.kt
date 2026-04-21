package com.cowork.app_client.data.repository

import com.cowork.app_client.data.remote.ChannelApi
import com.cowork.app_client.domain.model.Channel
import com.cowork.app_client.domain.model.ChannelType
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

class DefaultChannelRepository(
    private val authRepository: AuthRepository,
    private val channelApi: ChannelApi,
) : ChannelRepository {

    override suspend fun getTeamChannels(teamId: Long): List<Channel> =
        authorized { accessToken -> channelApi.getTeamChannels(accessToken, teamId) }

    override suspend fun createChannel(
        teamId: Long,
        type: ChannelType,
        name: String,
        notice: String?,
        projectId: Long?,
    ): Channel =
        authorized { accessToken ->
            channelApi.createChannel(
                accessToken = accessToken,
                teamId = teamId,
                type = type,
                name = name,
                notice = notice,
                projectId = projectId,
            )
        }

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
