package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelMember
import com.cowork.desktop.client.domain.model.ChannelType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ChannelApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getTeamChannels(accessToken: String, teamId: Long): List<Channel> =
        client.get("$baseUrl/teams/$teamId/channels") {
            bearerAuth(accessToken)
        }.body<ApiResponse<List<ChannelResponse>>>().data.orEmpty().map(ChannelResponse::toDomain)

    suspend fun getChannel(accessToken: String, channelId: Long): Channel =
        client.get("$baseUrl/channels/$channelId") {
            bearerAuth(accessToken)
        }.body<ApiResponse<ChannelResponse>>().data?.toDomain()
            ?: error("채널 조회 응답에 data가 없습니다")

    suspend fun createChannel(
        accessToken: String,
        teamId: Long,
        type: ChannelType,
        name: String,
        description: String?,
        isPrivate: Boolean,
    ): Channel =
        client.post("$baseUrl/channels") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                CreateChannelRequest(
                    teamId = teamId,
                    name = name,
                    type = type.toApiType(),
                    viewType = type.toApiViewType(),
                    description = description,
                    isPrivate = isPrivate,
                )
            )
        }.body<ApiResponse<ChannelResponse>>().data?.toDomain()
            ?: error("채널 생성 응답에 data가 없습니다")

    suspend fun updateChannel(
        accessToken: String,
        channelId: Long,
        name: String? = null,
        description: String? = null,
        isPrivate: Boolean? = null,
        projectId: Long? = null,
    ): Channel =
        client.patch("$baseUrl/channels/$channelId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(UpdateChannelRequest(name = name, description = description, isPrivate = isPrivate, projectId = projectId))
        }.body<ApiResponse<ChannelResponse>>().data?.toDomain()
            ?: error("채널 수정 응답에 data가 없습니다")

    suspend fun reorderChannels(accessToken: String, teamId: Long, orderedChannelIds: List<Long>): List<Channel> =
        client.patch("$baseUrl/teams/$teamId/channels/reorder") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ReorderChannelsRequest(orderedChannelIds))
        }.body<ApiResponse<List<ChannelResponse>>>().data.orEmpty().map(ChannelResponse::toDomain)

    suspend fun deleteChannel(accessToken: String, channelId: Long) {
        client.delete("$baseUrl/channels/$channelId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun addMember(accessToken: String, channelId: Long, userId: Long): ChannelMember =
        client.post("$baseUrl/channels/$channelId/members") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(AddMemberRequest(userId = userId))
        }.body<ApiResponse<ChannelMemberResponse>>().data?.toDomain()
            ?: error("채널 멤버 추가 응답에 data가 없습니다")

    suspend fun getMembers(accessToken: String, channelId: Long): List<ChannelMember> =
        client.get("$baseUrl/channels/$channelId/members") {
            bearerAuth(accessToken)
        }.body<ApiResponse<List<ChannelMemberResponse>>>().data.orEmpty().map(ChannelMemberResponse::toDomain)

    suspend fun removeMember(accessToken: String, channelId: Long, memberId: Long) {
        client.delete("$baseUrl/channels/$channelId/members/$memberId") {
            bearerAuth(accessToken)
        }
    }

    @Serializable
    private data class CreateChannelRequest(
        val teamId: Long,
        val name: String,
        val type: String,
        val viewType: String,
        val description: String?,
        val isPrivate: Boolean,
    )

    @Serializable
    private data class UpdateChannelRequest(
        val name: String?,
        val description: String?,
        val isPrivate: Boolean?,
        val projectId: Long?,
    )

    @Serializable
    private data class ReorderChannelsRequest(val orderedChannelIds: List<Long>)

    @Serializable
    private data class AddMemberRequest(val userId: Long)

    @Serializable
    data class ChannelMemberResponse(
        val id: Long,
        val channelId: Long,
        val userId: Long,
        val joinedAt: String? = null,
    ) {
        fun toDomain(): ChannelMember = ChannelMember(
            id = id,
            channelId = channelId,
            userId = userId,
            joinedAt = joinedAt,
        )
    }

    @Serializable
    private data class ChannelResponse(
        val id: Long,
        val teamId: Long,
        val projectId: Long? = null,
        val name: String,
        val type: String,
        val viewType: String,
        val description: String? = null,
        @SerialName("isPrivate")
        val isPrivate: Boolean = false,
        val position: Int = 0,
    ) {
        fun toDomain(): Channel = Channel(
            id = id,
            teamId = teamId,
            projectId = projectId,
            name = name,
            type = toChannelType(type, viewType),
            description = description,
            isPrivate = isPrivate,
            position = position,
        )
    }
}

private fun toChannelType(type: String, viewType: String): ChannelType = when (viewType.uppercase()) {
    "WEBHOOK" -> ChannelType.Webhook
    "MEETING_NOTE" -> ChannelType.MeetingNote
    "ACCOUNT_SHARE" -> ChannelType.AccountShare
    "FILE_SHARE" -> ChannelType.FileShare
    "VOICE" -> ChannelType.Voice
    "TEXT" -> ChannelType.Text
    else -> when (type.uppercase()) {
        "VOICE" -> ChannelType.Voice
        "TEXT" -> ChannelType.Text
        else -> ChannelType.Unknown
    }
}

private fun ChannelType.toApiType(): String = when (this) {
    ChannelType.Voice -> "VOICE"
    else -> "TEXT"
}

private fun ChannelType.toApiViewType(): String = when (this) {
    ChannelType.Text -> "TEXT"
    ChannelType.Voice -> "VOICE"
    ChannelType.Webhook -> "WEBHOOK"
    ChannelType.MeetingNote -> "MEETING_NOTE"
    ChannelType.AccountShare -> "ACCOUNT_SHARE"
    ChannelType.FileShare -> "FILE_SHARE"
    ChannelType.Unknown -> "TEXT"
}
