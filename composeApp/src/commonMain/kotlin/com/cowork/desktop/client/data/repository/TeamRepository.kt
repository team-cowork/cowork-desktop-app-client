package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.Team
import com.cowork.desktop.client.domain.model.TeamSummary

interface TeamRepository {
    suspend fun getMyTeams(): List<TeamSummary>
    suspend fun getTeamMembers(teamId: Long): List<Long>
    suspend fun createTeam(name: String, description: String?, iconUrl: String?): Team
    suspend fun uploadTeamIcon(bytes: ByteArray, contentType: String): String
}
