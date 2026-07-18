package com.cowork.desktop.client.domain.model

data class Channel(
    val id: Long,
    val teamId: Long?,
    val projectId: Long?,
    val name: String,
    val type: ChannelType,
    val description: String?,
    val isPrivate: Boolean,
    val position: Int,
    val createdBy: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class ChannelMember(
    val id: Long,
    val channelId: Long,
    val userId: Long,
    val joinedAt: String?,
)

enum class ChannelType {
    Text,
    Voice,
    Webhook,
    MeetingNote,
    AccountShare,
    FileShare,
    Unknown,
}
