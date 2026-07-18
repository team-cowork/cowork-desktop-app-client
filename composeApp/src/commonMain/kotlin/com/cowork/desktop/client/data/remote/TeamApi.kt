package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.Team
import com.cowork.desktop.client.domain.model.TeamCustomRole
import com.cowork.desktop.client.domain.model.TeamInvite
import com.cowork.desktop.client.domain.model.TeamInviteDuration
import com.cowork.desktop.client.domain.model.TeamJoinResult
import com.cowork.desktop.client.domain.model.TeamMember
import com.cowork.desktop.client.domain.model.TeamRole
import com.cowork.desktop.client.domain.model.TeamRoleUpdate
import com.cowork.desktop.client.domain.model.TeamSummary
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TeamApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getMyTeams(accessToken: String): List<TeamSummary> =
        client.get("$baseUrl/teams") {
            bearerAuth(accessToken)
        }.bodyPayload<List<TeamSummaryResponse>>().map(TeamSummaryResponse::toDomain)

    suspend fun getTeam(accessToken: String, teamId: Long): Team =
        client.get("$baseUrl/teams/$teamId") {
            bearerAuth(accessToken)
        }.bodyPayload<TeamResponse>().toDomain()

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
        }.bodyPayload<TeamResponse>().toDomain()

    suspend fun updateTeam(
        accessToken: String,
        teamId: Long,
        name: String? = null,
        description: String? = null,
        iconUrl: String? = null,
        clearDescription: Boolean = false,
    ): Team =
        client.patch("$baseUrl/teams/$teamId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                name?.let { put("name", it) }
                if (description != null) put("description", description)
                else if (clearDescription) put("description", "")
                iconUrl?.let { put("iconUrl", it) }
            })
        }.bodyPayload<TeamResponse>().toDomain()

    suspend fun deleteTeam(accessToken: String, teamId: Long) {
        client.delete("$baseUrl/teams/$teamId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun getTeamMembers(accessToken: String, teamId: Long): List<Long> =
        getMembers(accessToken, teamId).map(TeamMember::userId)

    suspend fun getMembers(accessToken: String, teamId: Long): List<TeamMember> =
        client.get("$baseUrl/teams/$teamId/members") {
            bearerAuth(accessToken)
        }.bodyPayload<List<TeamMemberResponse>>().map(TeamMemberResponse::toDomain)

    suspend fun inviteMembers(
        accessToken: String,
        teamId: Long,
        userIds: List<Long>,
    ): List<TeamMember> {
        require(userIds.isNotEmpty()) { "초대할 사용자 ID가 하나 이상 필요합니다." }
        return client.post("$baseUrl/teams/$teamId/members") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(InviteMembersRequest(userIds.distinct()))
        }.bodyPayload<List<TeamMemberResponse>>().map(TeamMemberResponse::toDomain)
    }

    suspend fun removeMember(accessToken: String, teamId: Long, targetUserId: Long) {
        client.delete("$baseUrl/teams/$teamId/members/$targetUserId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun changeMemberRole(
        accessToken: String,
        teamId: Long,
        targetUserId: Long,
        role: TeamRole,
    ) {
        require(role == TeamRole.Admin || role == TeamRole.Member) {
            "기본 역할은 ADMIN 또는 MEMBER로만 변경할 수 있습니다."
        }
        client.patch("$baseUrl/teams/$teamId/members/$targetUserId/role") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ChangeRoleRequest(role.toApiValue()))
        }
    }

    suspend fun isMember(accessToken: String, teamId: Long, userId: Long): Boolean {
        val result = client.get("$baseUrl/teams/$teamId/members/$userId/exists") {
            bearerAuth(accessToken)
        }.bodyPayload<Map<String, Boolean>>()
        return result["isMember"] ?: result["exists"] ?: result.values.firstOrNull() ?: false
    }

    suspend fun createInvite(
        accessToken: String,
        teamId: Long,
        duration: TeamInviteDuration,
    ): TeamInvite {
        require(duration != TeamInviteDuration.Unknown) { "알 수 없는 초대 유효 기간은 사용할 수 없습니다." }
        return client.post("$baseUrl/teams/$teamId/invites") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateInviteRequest(duration.apiValue))
        }.bodyPayload<InviteResponse>().toDomain()
    }

    suspend fun getInvites(accessToken: String, teamId: Long): List<TeamInvite> =
        client.get("$baseUrl/teams/$teamId/invites") {
            bearerAuth(accessToken)
        }.bodyPayload<List<InviteResponse>>().map(InviteResponse::toDomain)

    suspend fun deleteInvite(accessToken: String, teamId: Long, inviteCode: String) {
        client.delete("$baseUrl/teams/$teamId/invites/$inviteCode") {
            bearerAuth(accessToken)
        }
    }

    suspend fun joinTeam(accessToken: String, inviteCode: String): TeamJoinResult =
        client.post("$baseUrl/teams/join/$inviteCode") {
            bearerAuth(accessToken)
        }.bodyPayload<JoinTeamResponse>().toDomain()

    suspend fun getRoles(accessToken: String, teamId: Long): List<TeamCustomRole> =
        client.get("$baseUrl/teams/$teamId/roles") {
            bearerAuth(accessToken)
        }.bodyPayload<List<TeamRoleResponse>>().map(TeamRoleResponse::toDomain)

    suspend fun createRole(
        accessToken: String,
        teamId: Long,
        name: String,
        colorHex: String,
        priority: Int,
        mentionable: Boolean,
        permissions: Set<String>,
    ): TeamCustomRole =
        client.post("$baseUrl/teams/$teamId/roles") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateTeamRoleRequest(name, colorHex, priority, mentionable, permissions))
        }.bodyPayload<TeamRoleResponse>().toDomain()

    suspend fun updateRole(
        accessToken: String,
        teamId: Long,
        roleId: Long,
        update: TeamRoleUpdate,
    ): TeamCustomRole =
        client.patch("$baseUrl/teams/$teamId/roles/$roleId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                UpdateTeamRoleRequest(
                    name = update.name,
                    colorHex = update.colorHex,
                    priority = update.priority,
                    mentionable = update.mentionable,
                    permissions = update.permissions,
                )
            )
        }.bodyPayload<TeamRoleResponse>().toDomain()

    suspend fun deleteRole(accessToken: String, teamId: Long, roleId: Long) {
        client.delete("$baseUrl/teams/$teamId/roles/$roleId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun assignRole(
        accessToken: String,
        teamId: Long,
        targetUserId: Long,
        roleId: Long,
    ): TeamCustomRole =
        client.put("$baseUrl/teams/$teamId/members/$targetUserId/roles/$roleId") {
            bearerAuth(accessToken)
        }.bodyPayload<TeamRoleResponse>().toDomain()

    suspend fun revokeRole(accessToken: String, teamId: Long, targetUserId: Long, roleId: Long) {
        client.delete("$baseUrl/teams/$teamId/members/$targetUserId/roles/$roleId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun getMemberRoles(accessToken: String, teamId: Long, userId: Long): List<TeamCustomRole> =
        client.get("$baseUrl/teams/$teamId/members/$userId/roles") {
            bearerAuth(accessToken)
        }.bodyPayload<List<TeamRoleResponse>>().map(TeamRoleResponse::toDomain)

    suspend fun generateIconPresignedUrl(accessToken: String, contentType: String): IconPresignedUploadResponse =
        client.post("$baseUrl/teams/icon/presigned") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(IconPresignedUrlRequest(contentType))
        }.bodyPayload()

    suspend fun confirmIconUpload(accessToken: String, objectKey: String): String =
        client.post("$baseUrl/teams/icon/confirm") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(IconConfirmRequest(objectKey))
        }.bodyPayload<IconConfirmResponse>().iconUrl

    suspend fun updateIcon(accessToken: String, teamId: Long, iconUrl: String): String =
        client.patch("$baseUrl/teams/$teamId/icon") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(UpdateIconRequest(iconUrl))
        }.bodyPayload<IconConfirmResponse>().iconUrl

    suspend fun deleteIcon(accessToken: String, teamId: Long) {
        client.delete("$baseUrl/teams/$teamId/icon") {
            bearerAuth(accessToken)
        }
    }

    suspend fun putIconToS3(uploadUrl: String, bytes: ByteArray, contentType: String) {
        client.put(uploadUrl) {
            setBody(ByteArrayContent(bytes, ContentType.parse(contentType)))
        }
    }

    @Serializable
    data class IconPresignedUploadResponse(val uploadUrl: String, val objectKey: String)

    @Serializable
    private data class CreateTeamRequest(
        val name: String,
        val description: String?,
        val iconUrl: String?,
    )

    @Serializable
    private data class InviteMembersRequest(val userIds: List<Long>)

    @Serializable
    private data class ChangeRoleRequest(val role: String)

    @Serializable
    private data class CreateInviteRequest(val duration: String)

    @Serializable
    private data class CreateTeamRoleRequest(
        val name: String,
        val colorHex: String,
        val priority: Int,
        val mentionable: Boolean,
        val permissions: Set<String>,
    )

    @Serializable
    private data class UpdateTeamRoleRequest(
        val name: String?,
        val colorHex: String?,
        val priority: Int?,
        val mentionable: Boolean?,
        val permissions: Set<String>?,
    )

    @Serializable
    private data class IconPresignedUrlRequest(val contentType: String)

    @Serializable
    private data class IconConfirmRequest(val objectKey: String)

    @Serializable
    private data class UpdateIconRequest(val iconUrl: String)

    @Serializable
    private data class IconConfirmResponse(val iconUrl: String)

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
        val createdAt: String? = null,
        val updatedAt: String? = null,
    ) {
        fun toDomain(): Team = Team(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            ownerId = ownerId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    @Serializable
    private data class TeamMemberResponse(
        val id: Long,
        val userId: Long,
        val role: String,
        val roles: List<TeamRoleResponse> = emptyList(),
        val joinedAt: String,
    ) {
        fun toDomain(): TeamMember = TeamMember(
            id = id,
            userId = userId,
            role = role.toTeamRole(),
            roles = roles.map(TeamRoleResponse::toDomain),
            joinedAt = joinedAt,
        )
    }

    @Serializable
    private data class TeamRoleResponse(
        val id: Long,
        val teamId: Long,
        val name: String,
        val colorHex: String,
        val priority: Int,
        val mentionable: Boolean,
        val permissions: Set<String> = emptySet(),
        val createdAt: String? = null,
        val updatedAt: String? = null,
    ) {
        fun toDomain(): TeamCustomRole = TeamCustomRole(
            id = id,
            teamId = teamId,
            name = name,
            colorHex = colorHex,
            priority = priority,
            mentionable = mentionable,
            permissions = permissions,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    @Serializable
    private data class InviteResponse(
        val inviteCode: String,
        val teamId: Long,
        val createdBy: Long,
        val duration: String,
        val expiresAt: String? = null,
        val expired: Boolean,
        val createdAt: String,
    ) {
        fun toDomain(): TeamInvite = TeamInvite(
            inviteCode = inviteCode,
            teamId = teamId,
            createdBy = createdBy,
            duration = duration.toInviteDuration(),
            expiresAt = expiresAt,
            expired = expired,
            createdAt = createdAt,
        )
    }

    @Serializable
    private data class JoinTeamResponse(
        val teamId: Long,
        val userId: Long,
        val role: String,
        val joinedAt: String,
    ) {
        fun toDomain(): TeamJoinResult = TeamJoinResult(
            teamId = teamId,
            userId = userId,
            role = role.toTeamRole(),
            joinedAt = joinedAt,
        )
    }

}

private fun String.toTeamRole(): TeamRole = when (uppercase()) {
    "OWNER" -> TeamRole.Owner
    "ADMIN" -> TeamRole.Admin
    "MEMBER" -> TeamRole.Member
    else -> TeamRole.Unknown
}

private fun TeamRole.toApiValue(): String = when (this) {
    TeamRole.Owner -> "OWNER"
    TeamRole.Admin -> "ADMIN"
    TeamRole.Member -> "MEMBER"
    TeamRole.Unknown -> "UNKNOWN"
}

private fun String.toInviteDuration(): TeamInviteDuration = when (lowercase()) {
    TeamInviteDuration.OneDay.apiValue -> TeamInviteDuration.OneDay
    TeamInviteDuration.SevenDays.apiValue -> TeamInviteDuration.SevenDays
    TeamInviteDuration.ThirtyDays.apiValue -> TeamInviteDuration.ThirtyDays
    TeamInviteDuration.Never.apiValue -> TeamInviteDuration.Never
    else -> TeamInviteDuration.Unknown
}
