package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelMember
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.SharedAccount
import com.cowork.desktop.client.domain.model.SharedAccountProvider
import com.cowork.desktop.client.domain.model.toSharedAccountProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ChannelApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getTeamChannels(accessToken: String, teamId: Long): List<Channel> =
        client.get("$baseUrl/teams/$teamId/channels") {
            bearerAuth(accessToken)
        }.bodyPayload<List<ChannelResponse>>().map(ChannelResponse::toDomain)

    suspend fun getProjectChannels(accessToken: String, projectId: Long): List<Channel> =
        client.get("$baseUrl/projects/$projectId/channels") {
            bearerAuth(accessToken)
        }.bodyPayload<List<ChannelResponse>>().map(ChannelResponse::toDomain)

    suspend fun searchChannels(accessToken: String, teamId: Long, query: String): List<Channel> =
        client.get("$baseUrl/search/channels") {
            bearerAuth(accessToken)
            parameter("teamId", teamId)
            parameter("q", query)
        }.bodyPayload<List<ChannelResponse>>().map(ChannelResponse::toDomain)

    suspend fun getChannel(accessToken: String, channelId: Long): Channel =
        client.get("$baseUrl/channels/$channelId") {
            bearerAuth(accessToken)
        }.bodyPayload<ChannelResponse>().toDomain()

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
        }.bodyPayload<ChannelResponse>().toDomain()

    suspend fun updateChannel(
        accessToken: String,
        channelId: Long,
        name: String? = null,
        description: String? = null,
        isPrivate: Boolean? = null,
        projectId: Long? = null,
        clearDescription: Boolean = false,
        updateProjectId: Boolean = false,
    ): Channel =
        client.patch("$baseUrl/channels/$channelId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                name?.let { put("name", it) }
                if (description != null) put("description", description)
                else if (clearDescription) put("description", "")
                isPrivate?.let { put("isPrivate", it) }
                if (updateProjectId) {
                    if (projectId == null) put("projectId", JsonNull) else put("projectId", projectId)
                }
            })
        }.bodyPayload<ChannelResponse>().toDomain()

    suspend fun reorderChannels(accessToken: String, teamId: Long, orderedChannelIds: List<Long>): List<Channel> =
        client.patch("$baseUrl/teams/$teamId/channels/reorder") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ReorderChannelsRequest(orderedChannelIds))
        }.bodyPayload<List<ChannelResponse>>().map(ChannelResponse::toDomain)

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
        }.bodyPayload<ChannelMemberResponse>().toDomain()

    suspend fun getMembers(accessToken: String, channelId: Long): List<ChannelMember> =
        client.get("$baseUrl/channels/$channelId/members") {
            bearerAuth(accessToken)
        }.bodyPayload<List<ChannelMemberResponse>>().map(ChannelMemberResponse::toDomain)

    suspend fun removeMember(accessToken: String, channelId: Long, memberId: Long) {
        client.delete("$baseUrl/channels/$channelId/members/$memberId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun openDirectMessage(accessToken: String, targetUserId: Long): Long =
        client.post("$baseUrl/dms") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(OpenDmRequest(targetUserId))
        }.bodyPayload<OpenDmResponse>().id

    suspend fun getSharedAccounts(accessToken: String, channelId: Long): List<SharedAccount> =
        client.get("$baseUrl/channels/$channelId/accounts") {
            bearerAuth(accessToken)
        }.bodyPayload<List<SharedAccountResponse>>().map(SharedAccountResponse::toDomain)

    suspend fun getSharedAccount(accessToken: String, channelId: Long, accountId: Long): SharedAccount =
        client.get("$baseUrl/channels/$channelId/accounts/$accountId") {
            bearerAuth(accessToken)
        }.bodyPayload<SharedAccountResponse>().toDomain()

    suspend fun createSharedAccount(
        accessToken: String,
        channelId: Long,
        provider: SharedAccountProvider,
        providerLabel: String?,
        accountIdentifier: String?,
        credential: String?,
    ): SharedAccount =
        client.post("$baseUrl/channels/$channelId/accounts") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateSharedAccountRequest(provider.apiValue, providerLabel, accountIdentifier, credential))
        }.bodyPayload<SharedAccountResponse>().toDomain()

    suspend fun updateSharedAccount(
        accessToken: String,
        channelId: Long,
        accountId: Long,
        providerLabel: String?,
        accountIdentifier: String?,
        credential: String?,
    ): SharedAccount =
        client.patch("$baseUrl/channels/$channelId/accounts/$accountId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                UpdateSharedAccountRequest(
                    accountIdentifier = accountIdentifier.orEmpty(),
                    credential = credential,
                    providerLabel = providerLabel.orEmpty(),
                )
            )
        }.bodyPayload<SharedAccountResponse>().toDomain()

    suspend fun deleteSharedAccount(accessToken: String, channelId: Long, accountId: Long) {
        client.delete("$baseUrl/channels/$channelId/accounts/$accountId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun copySharedAccountCredential(accessToken: String, channelId: Long, accountId: Long): String {
        val values = client.post("$baseUrl/channels/$channelId/accounts/$accountId/credential/copy") {
            bearerAuth(accessToken)
        }.bodyPayload<Map<String, String>>()
        return values["credential"] ?: values.values.firstOrNull()
            ?: error("공유 계정 credential 응답이 비어 있습니다")
    }

    suspend fun getSharedAccountOAuthUrl(
        accessToken: String,
        channelId: Long,
        provider: SharedAccountProvider,
    ): String {
        val values = client.get("$baseUrl/channels/$channelId/accounts/oauth/authorize/${provider.apiValue}") {
            bearerAuth(accessToken)
        }.bodyPayload<Map<String, String>>()
        return values["url"] ?: values["authorizeUrl"] ?: values["redirectUrl"] ?: values.values.firstOrNull()
            ?: error("OAuth 인증 URL 응답이 비어 있습니다")
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
    private data class ReorderChannelsRequest(val orderedChannelIds: List<Long>)

    @Serializable
    private data class AddMemberRequest(val userId: Long)

    @Serializable
    private data class OpenDmRequest(val targetUserId: Long)

    @Serializable
    private data class CreateSharedAccountRequest(
        val provider: String,
        val providerLabel: String?,
        val accountIdentifier: String?,
        val credential: String?,
    )

    @Serializable
    private data class UpdateSharedAccountRequest(
        val accountIdentifier: String?,
        val credential: String?,
        val providerLabel: String?,
    )

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
        val teamId: Long? = null,
        val projectId: Long? = null,
        val name: String,
        val type: String,
        val viewType: String,
        val description: String? = null,
        @SerialName("isPrivate")
        val isPrivate: Boolean = false,
        val position: Int = 0,
        val createdBy: Long? = null,
        val createdAt: String? = null,
        val updatedAt: String? = null,
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
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    @Serializable
    private data class OpenDmResponse(val id: Long)

    @Serializable
    private data class SharedAccountResponse(
        val id: Long,
        val channelId: Long,
        val provider: String,
        val providerLabel: String? = null,
        val displayName: String? = null,
        val loginUrl: String? = null,
        val accountIdentifier: String? = null,
        val maskedCredential: String? = null,
        val connectedViaOAuth: Boolean = false,
        val createdBy: Long,
        val createdAt: String? = null,
        val updatedAt: String? = null,
    ) {
        fun toDomain(): SharedAccount = SharedAccount(
            id = id,
            channelId = channelId,
            provider = provider.toSharedAccountProvider(),
            providerLabel = providerLabel,
            displayName = displayName,
            loginUrl = loginUrl,
            accountIdentifier = accountIdentifier,
            maskedCredential = maskedCredential,
            connectedViaOAuth = connectedViaOAuth,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
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
