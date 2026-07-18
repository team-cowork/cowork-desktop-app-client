package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.TeamApi
import com.cowork.desktop.client.domain.model.Team
import com.cowork.desktop.client.domain.model.TeamCustomRole
import com.cowork.desktop.client.domain.model.TeamInvite
import com.cowork.desktop.client.domain.model.TeamInviteDuration
import com.cowork.desktop.client.domain.model.TeamJoinResult
import com.cowork.desktop.client.domain.model.TeamMember
import com.cowork.desktop.client.domain.model.TeamRole
import com.cowork.desktop.client.domain.model.TeamRoleUpdate
import com.cowork.desktop.client.domain.model.TeamSummary

class DefaultTeamRepository(
    private val authRepository: AuthRepository,
    private val teamApi: TeamApi,
) : TeamRepository {

    override suspend fun getMyTeams(): List<TeamSummary> =
        authorized { accessToken -> teamApi.getMyTeams(accessToken) }

    override suspend fun getTeam(teamId: Long): Team =
        authorized { accessToken -> teamApi.getTeam(accessToken, teamId) }

    override suspend fun getTeamMembers(teamId: Long): List<Long> =
        authorized { accessToken -> teamApi.getTeamMembers(accessToken, teamId) }

    override suspend fun getMembers(teamId: Long): List<TeamMember> =
        authorized { accessToken -> teamApi.getMembers(accessToken, teamId) }

    override suspend fun createTeam(name: String, description: String?, iconUrl: String?): Team =
        authorized { accessToken -> teamApi.createTeam(accessToken, name, description, iconUrl) }

    override suspend fun updateTeam(
        teamId: Long,
        name: String?,
        description: String?,
        iconUrl: String?,
        clearDescription: Boolean,
    ): Team =
        authorized { accessToken -> teamApi.updateTeam(accessToken, teamId, name, description, iconUrl, clearDescription) }

    override suspend fun deleteTeam(teamId: Long) =
        authorized { accessToken -> teamApi.deleteTeam(accessToken, teamId) }

    override suspend fun inviteMembers(teamId: Long, userIds: List<Long>): List<TeamMember> =
        authorized { accessToken -> teamApi.inviteMembers(accessToken, teamId, userIds) }

    override suspend fun removeMember(teamId: Long, targetUserId: Long) =
        authorized { accessToken -> teamApi.removeMember(accessToken, teamId, targetUserId) }

    override suspend fun leaveTeam(teamId: Long, currentUserId: Long) =
        removeMember(teamId, currentUserId)

    override suspend fun changeMemberRole(teamId: Long, targetUserId: Long, role: TeamRole) =
        authorized { accessToken -> teamApi.changeMemberRole(accessToken, teamId, targetUserId, role) }

    override suspend fun isMember(teamId: Long, userId: Long): Boolean =
        authorized { accessToken -> teamApi.isMember(accessToken, teamId, userId) }

    override suspend fun createInvite(teamId: Long, duration: TeamInviteDuration): TeamInvite =
        authorized { accessToken -> teamApi.createInvite(accessToken, teamId, duration) }

    override suspend fun getInvites(teamId: Long): List<TeamInvite> =
        authorized { accessToken -> teamApi.getInvites(accessToken, teamId) }

    override suspend fun deleteInvite(teamId: Long, inviteCode: String) =
        authorized { accessToken -> teamApi.deleteInvite(accessToken, teamId, inviteCode) }

    override suspend fun joinTeam(inviteCode: String): TeamJoinResult =
        authorized { accessToken -> teamApi.joinTeam(accessToken, inviteCode) }

    override suspend fun getRoles(teamId: Long): List<TeamCustomRole> =
        authorized { accessToken -> teamApi.getRoles(accessToken, teamId) }

    override suspend fun createRole(
        teamId: Long,
        name: String,
        colorHex: String,
        priority: Int,
        mentionable: Boolean,
        permissions: Set<String>,
    ): TeamCustomRole =
        authorized { accessToken ->
            teamApi.createRole(accessToken, teamId, name, colorHex, priority, mentionable, permissions)
        }

    override suspend fun updateRole(teamId: Long, roleId: Long, update: TeamRoleUpdate): TeamCustomRole =
        authorized { accessToken -> teamApi.updateRole(accessToken, teamId, roleId, update) }

    override suspend fun deleteRole(teamId: Long, roleId: Long) =
        authorized { accessToken -> teamApi.deleteRole(accessToken, teamId, roleId) }

    override suspend fun assignRole(teamId: Long, targetUserId: Long, roleId: Long): TeamCustomRole =
        authorized { accessToken -> teamApi.assignRole(accessToken, teamId, targetUserId, roleId) }

    override suspend fun revokeRole(teamId: Long, targetUserId: Long, roleId: Long) =
        authorized { accessToken -> teamApi.revokeRole(accessToken, teamId, targetUserId, roleId) }

    override suspend fun getMemberRoles(teamId: Long, userId: Long): List<TeamCustomRole> =
        authorized { accessToken -> teamApi.getMemberRoles(accessToken, teamId, userId) }

    override suspend fun uploadTeamIcon(bytes: ByteArray, contentType: String): String =
        authorized { accessToken ->
            val presigned = teamApi.generateIconPresignedUrl(accessToken, contentType)
            teamApi.putIconToS3(presigned.uploadUrl, bytes, contentType)
            teamApi.confirmIconUpload(accessToken, presigned.objectKey)
        }

    override suspend fun updateTeamIcon(teamId: Long, iconUrl: String): String =
        authorized { accessToken -> teamApi.updateIcon(accessToken, teamId, iconUrl) }

    override suspend fun replaceTeamIcon(teamId: Long, bytes: ByteArray, contentType: String): String {
        val iconUrl = uploadTeamIcon(bytes, contentType)
        return updateTeamIcon(teamId, iconUrl)
    }

    override suspend fun deleteTeamIcon(teamId: Long) =
        authorized { accessToken -> teamApi.deleteIcon(accessToken, teamId) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
