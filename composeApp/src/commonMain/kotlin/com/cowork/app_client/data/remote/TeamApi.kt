package com.cowork.app_client.data.remote

import com.cowork.app_client.domain.model.Team
import com.cowork.app_client.domain.model.TeamRole
import com.cowork.app_client.domain.model.TeamSummary
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class TeamApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getMyTeams(accessToken: String): List<TeamSummary> =
        client.get("$baseUrl/teams") {
            bearerAuth(accessToken)
        }.body<List<TeamSummaryResponse>>().map(TeamSummaryResponse::toDomain)

    suspend fun createTeam(
        accessToken: String,
        name: String,
        description: String?,
        iconUrl: String?,
    ): Team =
        client.post("$baseUrl/teams") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateTeamRequest(name, description, iconUrl))
        }.body<TeamResponse>().toDomain()

    @Serializable
    private data class CreateTeamRequest(
        val name: String,
        val description: String?,
        val iconUrl: String?,
    )

    @Serializable
    private data class TeamSummaryResponse(
        val id: Long,
        val name: String,
        val iconUrl: String? = null,
        val myRole: String,
    ) {
        fun toDomain(): TeamSummary = TeamSummary(
            id = id,
            name = name,
            iconUrl = iconUrl,
            myRole = myRole.toTeamRole(),
        )
    }

    @Serializable
    private data class TeamResponse(
        val id: Long,
        val name: String,
        val description: String? = null,
        val iconUrl: String? = null,
        val ownerId: Long,
    ) {
        fun toDomain(): Team = Team(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            ownerId = ownerId,
        )
    }
}

private fun String.toTeamRole(): TeamRole = when (uppercase()) {
    "OWNER" -> TeamRole.Owner
    "ADMIN" -> TeamRole.Admin
    "MEMBER" -> TeamRole.Member
    else -> TeamRole.Unknown
}
