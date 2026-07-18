package com.cowork.desktop.client.domain.model

data class TeamSummary(
    val id: Long,
    val name: String,
    val iconUrl: String?,
    val myRole: TeamRole,
)

data class Team(
    val id: Long,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val ownerId: Long,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

enum class TeamRole {
    Owner,
    Admin,
    Member,
    Unknown,
}

data class TeamMember(
    val id: Long,
    val userId: Long,
    val role: TeamRole,
    val roles: List<TeamCustomRole>,
    val joinedAt: String,
)

data class TeamCustomRole(
    val id: Long,
    val teamId: Long,
    val name: String,
    val colorHex: String,
    val priority: Int,
    val mentionable: Boolean,
    val permissions: Set<String>,
    val createdAt: String?,
    val updatedAt: String?,
)

data class TeamRoleUpdate(
    val name: String? = null,
    val colorHex: String? = null,
    val priority: Int? = null,
    val mentionable: Boolean? = null,
    val permissions: Set<String>? = null,
)

enum class TeamInviteDuration(val apiValue: String) {
    OneDay("1d"),
    SevenDays("7d"),
    ThirtyDays("30d"),
    Never("never"),
    Unknown(""),
}

data class TeamInvite(
    val inviteCode: String,
    val teamId: Long,
    val createdBy: Long,
    val duration: TeamInviteDuration,
    val expiresAt: String?,
    val expired: Boolean,
    val createdAt: String,
)

data class TeamJoinResult(
    val teamId: Long,
    val userId: Long,
    val role: TeamRole,
    val joinedAt: String,
)
