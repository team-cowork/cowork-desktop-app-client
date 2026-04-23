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
        }.body<ApiResponse<List<TeamSummaryResponse>>>().data.orEmpty().map(TeamSummaryResponse::toDomain)

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
        }.body<ApiResponse<TeamResponse>>().data?.toDomain() ?: error("팀 생성 응답에 data가 없습니다")

    // TODO: 서버 구현 후 활성화 (docs/api/team-icon-upload.md 참고)
    // suspend fun generateIconPresignedUrl(accessToken: String, contentType: String): IconPresignedUploadResponse
    // suspend fun putIconToS3(uploadUrl: String, bytes: ByteArray, contentType: String)
    //
    // @Serializable
    // data class IconPresignedUploadResponse(val uploadUrl: String, val iconUrl: String)

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
