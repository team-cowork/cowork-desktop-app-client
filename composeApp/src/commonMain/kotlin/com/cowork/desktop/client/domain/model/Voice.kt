package com.cowork.desktop.client.domain.model

data class VoiceChannelJoinResult(
    val token: String,
    val liveKitUrl: String,
    val sessionId: String,
    val roomName: String,
)

data class VoiceChannelParticipant(
    val userId: Long,
    val joinedAt: String,
)

data class VoiceChannelParticipants(
    val channelId: Long,
    val roomName: String,
    val participants: List<VoiceChannelParticipant>,
)

data class VoiceSession(
    val sessionId: String,
    val channelId: Long,
    val teamId: Long,
    val status: VoiceSessionStatus,
    val startedAt: String,
    val endedAt: String?,
)

enum class VoiceSessionStatus {
    Active,
    Ended,
    Unknown,
}

data class LiveStartResult(
    val token: String,
    val liveKitUrl: String,
    val sessionId: String,
    val roomName: String,
)

data class LiveJoinResult(
    val token: String,
    val liveKitUrl: String,
    val sessionId: String,
    val roomName: String,
    val hostUserId: Long,
)

data class LiveStatus(
    val isLive: Boolean,
    val sessionId: String?,
    val hostUserId: Long?,
    val startedAt: String?,
    val viewerCount: Int,
)
