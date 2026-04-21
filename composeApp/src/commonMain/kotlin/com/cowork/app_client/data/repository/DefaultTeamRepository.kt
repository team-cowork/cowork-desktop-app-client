package com.cowork.app_client.data.repository

import com.cowork.app_client.data.remote.TeamApi
import com.cowork.app_client.domain.model.Team
import com.cowork.app_client.domain.model.TeamSummary
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

class DefaultTeamRepository(
    private val authRepository: AuthRepository,
    private val teamApi: TeamApi,
) : TeamRepository {

    override suspend fun getMyTeams(): List<TeamSummary> =
        authorized { accessToken -> teamApi.getMyTeams(accessToken) }

    override suspend fun createTeam(name: String, description: String?, iconUrl: String?): Team =
        authorized { accessToken -> teamApi.createTeam(accessToken, name, description, iconUrl) }

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
