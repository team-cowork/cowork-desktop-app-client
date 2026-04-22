package com.cowork.app_client.data.remote

import com.cowork.app_client.domain.model.Channel
import com.cowork.app_client.domain.model.ChannelType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
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

    suspend fun createChannel(
        accessToken: String,
        teamId: Long,
        type: ChannelType,
        name: String,
        notice: String?,
        projectId: Long?,
    ): Channel =
        client.post("$baseUrl/teams/$teamId/channels") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                CreateChannelRequest(
                    type = type.toApiValue(),
                    name = name,
                    notice = notice,
                    projectId = projectId,
                )
            )
        }.body<ApiResponse<ChannelResponse>>().data?.toDomain() ?: error("채널 생성 응답에 data가 없습니다")

    @Serializable
    private data class CreateChannelRequest(
        val type: String,
        val name: String,
        val notice: String?,
        val projectId: Long?,
    )

    @Serializable
    private data class ChannelResponse(
        val id: Long,
        val teamId: Long,
        val projectId: Long? = null,
        val type: String,
        val name: String,
        val notice: String? = null,
        @SerialName("archived")
        val archived: Boolean = false,
        val isArchived: Boolean = archived,
    ) {
        fun toDomain(): Channel = Channel(
            id = id,
            teamId = teamId,
            projectId = projectId,
            type = type.toChannelType(),
            name = name,
            notice = notice,
            isArchived = isArchived,
        )
    }
}

private fun String.toChannelType(): ChannelType = when (uppercase()) {
    "TEXT" -> ChannelType.Text
    "VOICE" -> ChannelType.Voice
    "WEBHOOK" -> ChannelType.Webhook
    "MEETING_NOTE" -> ChannelType.MeetingNote
    else -> ChannelType.Unknown
}

private fun ChannelType.toApiValue(): String = when (this) {
    ChannelType.Text -> "TEXT"
    ChannelType.Voice -> "VOICE"
    ChannelType.Webhook -> "WEBHOOK"
    ChannelType.MeetingNote -> "MEETING_NOTE"
    ChannelType.Unknown -> "TEXT"
}
