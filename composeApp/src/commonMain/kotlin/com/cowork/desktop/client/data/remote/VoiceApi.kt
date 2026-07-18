package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.LiveJoinResult
import com.cowork.desktop.client.domain.model.LiveStartResult
import com.cowork.desktop.client.domain.model.LiveStatus
import com.cowork.desktop.client.domain.model.VoiceChannelJoinResult
import com.cowork.desktop.client.domain.model.VoiceChannelParticipant
import com.cowork.desktop.client.domain.model.VoiceChannelParticipants
import com.cowork.desktop.client.domain.model.VoiceSession
import com.cowork.desktop.client.domain.model.VoiceSessionStatus
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class VoiceApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun joinVoiceChannel(accessToken: String, channelId: Long): VoiceChannelJoinResult =
        client.post("$baseUrl/voice/channels/$channelId/join") {
            bearerAuth(accessToken)
        }.bodyPayload<VoiceChannelJoinResponse?>()?.toDomain()
            ?: error("음성 채널 입장 응답에 data가 없습니다")

    suspend fun leaveVoiceChannel(accessToken: String, channelId: Long) {
        client.post("$baseUrl/voice/channels/$channelId/leave") {
            bearerAuth(accessToken)
        }
    }

    suspend fun getVoiceChannelParticipants(
        accessToken: String,
        channelId: Long,
    ): VoiceChannelParticipants =
        client.get("$baseUrl/voice/channels/$channelId/participants") {
            bearerAuth(accessToken)
        }.bodyPayload<VoiceChannelParticipantsResponse?>()?.toDomain()
            ?: error("음성 채널 참여자 응답에 data가 없습니다")

    suspend fun getVoiceSession(accessToken: String, sessionId: String): VoiceSession =
        client.get("$baseUrl/voice/sessions/$sessionId") {
            bearerAuth(accessToken)
        }.bodyPayload<VoiceSessionResponse?>()?.toDomain()
            ?: error("음성 세션 조회 응답에 data가 없습니다")

    suspend fun getLiveStatus(accessToken: String, channelId: Long): LiveStatus =
        client.get("$baseUrl/live/channels/$channelId") {
            bearerAuth(accessToken)
        }.bodyPayload<LiveStatusResponse?>()?.toDomain()
            ?: error("라이브 상태 조회 응답에 data가 없습니다")

    suspend fun startLive(accessToken: String, channelId: Long): LiveStartResult =
        client.post("$baseUrl/live/channels/$channelId/start") {
            bearerAuth(accessToken)
        }.bodyPayload<LiveStartResponse?>()?.toDomain()
            ?: error("라이브 시작 응답에 data가 없습니다")

    suspend fun joinLive(accessToken: String, channelId: Long): LiveJoinResult =
        client.post("$baseUrl/live/channels/$channelId/join") {
            bearerAuth(accessToken)
        }.bodyPayload<LiveJoinResponse?>()?.toDomain()
            ?: error("라이브 참여 응답에 data가 없습니다")

    suspend fun leaveLive(accessToken: String, channelId: Long) {
        client.post("$baseUrl/live/channels/$channelId/leave") {
            bearerAuth(accessToken)
        }
    }

    @Serializable
    private data class VoiceChannelJoinResponse(
        val token: String,
        @SerialName("livekit_url") val liveKitUrl: String,
        @SerialName("session_id") val sessionId: String,
        @SerialName("room_name") val roomName: String,
    ) {
        fun toDomain(): VoiceChannelJoinResult = VoiceChannelJoinResult(
            token = token,
            liveKitUrl = liveKitUrl,
            sessionId = sessionId,
            roomName = roomName,
        )
    }

    @Serializable
    private data class VoiceChannelParticipantResponse(
        @SerialName("user_id") val userId: Long,
        @SerialName("joined_at") val joinedAt: String,
    ) {
        fun toDomain(): VoiceChannelParticipant = VoiceChannelParticipant(
            userId = userId,
            joinedAt = joinedAt,
        )
    }

    @Serializable
    private data class VoiceChannelParticipantsResponse(
        @SerialName("channel_id") val channelId: Long,
        @SerialName("room_name") val roomName: String,
        val participants: List<VoiceChannelParticipantResponse> = emptyList(),
    ) {
        fun toDomain(): VoiceChannelParticipants = VoiceChannelParticipants(
            channelId = channelId,
            roomName = roomName,
            participants = participants.map(VoiceChannelParticipantResponse::toDomain),
        )
    }

    @Serializable
    private data class VoiceSessionResponse(
        @SerialName("session_id") val sessionId: String,
        @SerialName("channel_id") val channelId: Long,
        @SerialName("team_id") val teamId: Long,
        val status: String,
        @SerialName("started_at") val startedAt: String,
        @SerialName("ended_at") val endedAt: String? = null,
    ) {
        fun toDomain(): VoiceSession = VoiceSession(
            sessionId = sessionId,
            channelId = channelId,
            teamId = teamId,
            status = status.toVoiceSessionStatus(),
            startedAt = startedAt,
            endedAt = endedAt,
        )
    }

    @Serializable
    private data class LiveStartResponse(
        val token: String,
        @SerialName("livekit_url") val liveKitUrl: String,
        @SerialName("session_id") val sessionId: String,
        @SerialName("room_name") val roomName: String,
    ) {
        fun toDomain(): LiveStartResult = LiveStartResult(
            token = token,
            liveKitUrl = liveKitUrl,
            sessionId = sessionId,
            roomName = roomName,
        )
    }

    @Serializable
    private data class LiveJoinResponse(
        val token: String,
        @SerialName("livekit_url") val liveKitUrl: String,
        @SerialName("session_id") val sessionId: String,
        @SerialName("room_name") val roomName: String,
        @SerialName("host_user_id") val hostUserId: Long,
    ) {
        fun toDomain(): LiveJoinResult = LiveJoinResult(
            token = token,
            liveKitUrl = liveKitUrl,
            sessionId = sessionId,
            roomName = roomName,
            hostUserId = hostUserId,
        )
    }

    @Serializable
    private data class LiveStatusResponse(
        val live: Boolean,
        @SerialName("session_id") val sessionId: String? = null,
        @SerialName("host_user_id") val hostUserId: Long? = null,
        @SerialName("started_at") val startedAt: String? = null,
        @SerialName("viewer_count") val viewerCount: Int = 0,
    ) {
        fun toDomain(): LiveStatus = LiveStatus(
            isLive = live,
            sessionId = sessionId,
            hostUserId = hostUserId,
            startedAt = startedAt,
            viewerCount = viewerCount,
        )
    }
}

private fun String.toVoiceSessionStatus(): VoiceSessionStatus = when (lowercase()) {
    "active" -> VoiceSessionStatus.Active
    "ended" -> VoiceSessionStatus.Ended
    else -> VoiceSessionStatus.Unknown
}
