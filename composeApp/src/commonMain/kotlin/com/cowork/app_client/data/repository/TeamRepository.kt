package com.cowork.app_client.data.repository

import com.cowork.app_client.domain.model.Team
import com.cowork.app_client.domain.model.TeamSummary

interface TeamRepository {
    suspend fun getMyTeams(): List<TeamSummary>
    suspend fun createTeam(name: String, description: String?, iconUrl: String?): Team
}
