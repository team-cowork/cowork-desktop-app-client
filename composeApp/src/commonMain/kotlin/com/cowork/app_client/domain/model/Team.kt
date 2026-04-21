package com.cowork.app_client.domain.model

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
)

enum class TeamRole {
    Owner,
    Admin,
    Member,
    Unknown,
}
