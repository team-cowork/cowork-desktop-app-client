package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.TeamApi
import com.cowork.desktop.client.domain.model.Team
import com.cowork.desktop.client.domain.model.TeamSummary

class DefaultTeamRepository(
    private val authRepository: AuthRepository,
    private val teamApi: TeamApi,
) : TeamRepository {

    override suspend fun getMyTeams(): List<TeamSummary> =
        authorized { accessToken -> teamApi.getMyTeams(accessToken) }

    override suspend fun getTeamMembers(teamId: Long): List<Long> =
        authorized { accessToken -> teamApi.getTeamMembers(accessToken, teamId) }

    override suspend fun createTeam(name: String, description: String?, iconUrl: String?): Team =
        authorized { accessToken -> teamApi.createTeam(accessToken, name, description, iconUrl) }

    override suspend fun uploadTeamIcon(bytes: ByteArray, contentType: String): String =
        authorized { accessToken ->
            val presigned = teamApi.generateIconPresignedUrl(accessToken, contentType)
            teamApi.putIconToS3(presigned.uploadUrl, bytes, contentType)
            teamApi.confirmIconUpload(accessToken, presigned.objectKey)
        }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
