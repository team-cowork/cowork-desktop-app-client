package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.Team
import com.cowork.desktop.client.domain.model.TeamCustomRole
import com.cowork.desktop.client.domain.model.TeamInvite
import com.cowork.desktop.client.domain.model.TeamInviteDuration
import com.cowork.desktop.client.domain.model.TeamJoinResult
import com.cowork.desktop.client.domain.model.TeamMember
import com.cowork.desktop.client.domain.model.TeamRole
import com.cowork.desktop.client.domain.model.TeamRoleUpdate
import com.cowork.desktop.client.domain.model.TeamSummary

interface TeamRepository {
    suspend fun getMyTeams(): List<TeamSummary>
    suspend fun getTeam(teamId: Long): Team
    suspend fun getTeamMembers(teamId: Long): List<Long>
    suspend fun getMembers(teamId: Long): List<TeamMember>
    suspend fun createTeam(name: String, description: String?, iconUrl: String?): Team
    suspend fun updateTeam(
        teamId: Long,
        name: String? = null,
        description: String? = null,
        iconUrl: String? = null,
        clearDescription: Boolean = false,
    ): Team
    suspend fun deleteTeam(teamId: Long)
    suspend fun inviteMembers(teamId: Long, userIds: List<Long>): List<TeamMember>
    suspend fun removeMember(teamId: Long, targetUserId: Long)
    suspend fun leaveTeam(teamId: Long, currentUserId: Long)
    suspend fun changeMemberRole(teamId: Long, targetUserId: Long, role: TeamRole)
    suspend fun isMember(teamId: Long, userId: Long): Boolean
    suspend fun createInvite(teamId: Long, duration: TeamInviteDuration): TeamInvite
    suspend fun getInvites(teamId: Long): List<TeamInvite>
    suspend fun deleteInvite(teamId: Long, inviteCode: String)
    suspend fun joinTeam(inviteCode: String): TeamJoinResult
    suspend fun getRoles(teamId: Long): List<TeamCustomRole>
    suspend fun createRole(
        teamId: Long,
        name: String,
        colorHex: String,
        priority: Int,
        mentionable: Boolean,
        permissions: Set<String>,
    ): TeamCustomRole
    suspend fun updateRole(teamId: Long, roleId: Long, update: TeamRoleUpdate): TeamCustomRole
    suspend fun deleteRole(teamId: Long, roleId: Long)
    suspend fun assignRole(teamId: Long, targetUserId: Long, roleId: Long): TeamCustomRole
    suspend fun revokeRole(teamId: Long, targetUserId: Long, roleId: Long)
    suspend fun getMemberRoles(teamId: Long, userId: Long): List<TeamCustomRole>
    suspend fun uploadTeamIcon(bytes: ByteArray, contentType: String): String
    suspend fun updateTeamIcon(teamId: Long, iconUrl: String): String
    suspend fun replaceTeamIcon(teamId: Long, bytes: ByteArray, contentType: String): String
    suspend fun deleteTeamIcon(teamId: Long)
}
