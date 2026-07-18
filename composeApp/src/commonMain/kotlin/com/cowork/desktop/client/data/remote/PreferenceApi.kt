package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.ChannelNotificationSettings
import com.cowork.desktop.client.domain.model.ProjectSettings
import com.cowork.desktop.client.domain.model.TeamSettings
import com.cowork.desktop.client.domain.model.TextChannelSettings
import com.cowork.desktop.client.domain.model.TextChannelSettingsUpdate
import com.cowork.desktop.client.domain.model.VoiceChannelSettings
import com.cowork.desktop.client.domain.model.WebhookSettings
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class PreferenceApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getAccountSettings(accessToken: String, accountId: Long): AccountSettings =
        client.get("$baseUrl/preferences/account/$accountId") {
            bearerAuth(accessToken)
        }.bodyPayload<AccountSettings?>() ?: AccountSettings()

    suspend fun getTeamSettings(accessToken: String, teamId: Long): TeamSettings =
        client.get("$baseUrl/preferences/team/$teamId") {
            bearerAuth(accessToken)
        }.bodyPayload<TeamSettingsPayload?>()?.toDomain() ?: TeamSettings()

    suspend fun updateTeamSettings(
        accessToken: String,
        teamId: Long,
        settings: TeamSettings,
    ): TeamSettings =
        client.put("$baseUrl/preferences/team/$teamId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(settings.toPayload())
        }.bodyPayload<TeamSettingsPayload?>()?.toDomain() ?: TeamSettings()

    suspend fun getProjectSettings(accessToken: String, projectId: Long): ProjectSettings {
        client.get("$baseUrl/preferences/project/$projectId") {
            bearerAuth(accessToken)
        }.bodyPayload<ProjectSettingsPayload?>()
        return ProjectSettings
    }

    suspend fun updateProjectSettings(
        accessToken: String,
        projectId: Long,
        settings: ProjectSettings,
    ): ProjectSettings {
        client.put("$baseUrl/preferences/project/$projectId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(settings.toPayload())
        }.bodyPayload<ProjectSettingsPayload?>()
        return ProjectSettings
    }

    suspend fun getVoiceChannelSettings(accessToken: String, channelId: Long): VoiceChannelSettings =
        client.get("$baseUrl/preferences/voice-channel/$channelId") {
            bearerAuth(accessToken)
        }.bodyPayload<VoiceChannelSettingsPayload?>()?.toDomain() ?: VoiceChannelSettings()

    suspend fun updateVoiceChannelSettings(
        accessToken: String,
        channelId: Long,
        settings: VoiceChannelSettings,
    ): VoiceChannelSettings =
        client.put("$baseUrl/preferences/voice-channel/$channelId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(settings.toPayload())
        }.bodyPayload<VoiceChannelSettingsPayload?>()?.toDomain() ?: VoiceChannelSettings()

    suspend fun getTextChannelSettings(accessToken: String, channelId: Long): TextChannelSettings =
        client.get("$baseUrl/preferences/text-channel/$channelId") {
            bearerAuth(accessToken)
        }.bodyPayload<TextChannelSettingsPayload?>()?.toDomain() ?: TextChannelSettings()

    suspend fun updateTextChannelSettings(
        accessToken: String,
        channelId: Long,
        settings: TextChannelSettingsUpdate,
    ): TextChannelSettings =
        client.put("$baseUrl/preferences/text-channel/$channelId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(settings.toPayload())
        }.bodyPayload<TextChannelSettingsPayload?>()?.toDomain() ?: TextChannelSettings()

    suspend fun getChannelNotificationSettings(
        accessToken: String,
        accountId: Long,
        channelId: Long,
    ): ChannelNotificationSettings =
        client.get("$baseUrl/preferences/account/$accountId/channels/$channelId/notification") {
            bearerAuth(accessToken)
        }.bodyPayload<NotificationSettingsResponse?>()?.toDomain()
            ?: ChannelNotificationSettings(isEnabled = true)

    suspend fun updateChannelNotificationSettings(
        accessToken: String,
        accountId: Long,
        channelId: Long,
        settings: ChannelNotificationSettings,
    ): ChannelNotificationSettings =
        client.put("$baseUrl/preferences/account/$accountId/channels/$channelId/notification") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(NotificationSettingsRequest(notification = settings.isEnabled))
        }.bodyPayload<NotificationSettingsResponse?>()?.toDomain()
            ?: ChannelNotificationSettings(isEnabled = settings.isEnabled)

    suspend fun updateAccountSettings(
        accessToken: String,
        accountId: Long,
        request: UpdateAccountSettingsRequest,
    ): AccountSettings =
        client.put("$baseUrl/preferences/account/$accountId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyPayload<AccountSettings?>() ?: AccountSettings()

    @Serializable
    data class AccountSettings(
        val status: String? = null,
        @SerialName("status_expires_at") val statusExpiresAt: String? = null,
        @SerialName("marketing_email") val marketingEmail: Boolean? = null,
        val theme: String? = null,
        val language: String? = null,
        @SerialName("time_format") val timeFormat: String? = null,
        @SerialName("date_format") val dateFormat: String? = null,
    )

    @Serializable
    data class UpdateAccountSettingsRequest(
        val status: String? = null,
        @SerialName("status_expires_at") val statusExpiresAt: String? = null,
        @SerialName("marketing_email") val marketingEmail: Boolean? = null,
        val theme: String? = null,
        val language: String? = null,
        @SerialName("time_format") val timeFormat: String? = null,
        @SerialName("date_format") val dateFormat: String? = null,
    )

    @Serializable
    private data class TeamSettingsPayload(
        @SerialName("tag_spam_block") val tagSpamBlock: Boolean? = null,
        @SerialName("nickname_format_enforced") val nicknameFormatEnforced: Boolean? = null,
        @SerialName("nickname_format_example") val nicknameFormatExample: String? = null,
    ) {
        fun toDomain(): TeamSettings = TeamSettings(
            tagSpamBlock = tagSpamBlock,
            nicknameFormatEnforced = nicknameFormatEnforced,
            nicknameFormatExample = nicknameFormatExample,
        )
    }

    @Serializable
    private class ProjectSettingsPayload

    @Serializable
    private data class VoiceChannelSettingsPayload(
        val bitrate: Int? = null,
        @SerialName("max_participants") val maxParticipants: Int? = null,
    ) {
        fun toDomain(): VoiceChannelSettings = VoiceChannelSettings(
            bitrate = bitrate,
            maxParticipants = maxParticipants,
        )
    }

    @Serializable
    private data class TextChannelSettingsPayload(
        val webhook: WebhookSettingsPayload? = null,
    ) {
        fun toDomain(): TextChannelSettings = TextChannelSettings(webhook = webhook?.toDomain())
    }

    @Serializable
    private data class WebhookSettingsPayload(
        @SerialName("is_active") val isActive: Boolean? = null,
        @SerialName("secret_key") val secretKey: String? = null,
        @SerialName("retry_count") val retryCount: Int? = null,
        @SerialName("retry_interval_ms") val retryIntervalMs: Int? = null,
    ) {
        fun toDomain(): WebhookSettings = WebhookSettings(
            isActive = isActive,
            secretKey = secretKey,
            retryCount = retryCount,
            retryIntervalMs = retryIntervalMs,
        )
    }

    @Serializable
    private data class TextChannelSettingsUpdatePayload(
        val webhook: WebhookSettingsUpdatePayload,
    )

    @Serializable
    private data class WebhookSettingsUpdatePayload(
        @SerialName("is_active") val isActive: Boolean,
        @SerialName("secret_key") val secretKey: String,
        @SerialName("retry_count") val retryCount: Int,
        @SerialName("retry_interval_ms") val retryIntervalMs: Int,
    )

    /**
     * The deployed service returns Boolean `notification`; the OpenAPI enum
     * ALL/MENTIONS/NONE is not accepted by its current implementation.
     */
    @Serializable
    private data class NotificationSettingsResponse(
        val notification: Boolean = true,
    ) {
        fun toDomain(): ChannelNotificationSettings =
            ChannelNotificationSettings(isEnabled = notification)
    }

    @Serializable
    private data class NotificationSettingsRequest(
        val notification: Boolean,
    )

    private fun TeamSettings.toPayload(): TeamSettingsPayload = TeamSettingsPayload(
        tagSpamBlock = tagSpamBlock,
        nicknameFormatEnforced = nicknameFormatEnforced,
        nicknameFormatExample = nicknameFormatExample,
    )

    private fun ProjectSettings.toPayload(): ProjectSettingsPayload = ProjectSettingsPayload()

    private fun VoiceChannelSettings.toPayload(): VoiceChannelSettingsPayload = VoiceChannelSettingsPayload(
        bitrate = bitrate,
        maxParticipants = maxParticipants,
    )

    private fun TextChannelSettingsUpdate.toPayload(): TextChannelSettingsUpdatePayload =
        TextChannelSettingsUpdatePayload(
            webhook = WebhookSettingsUpdatePayload(
                isActive = webhook.isActive,
                secretKey = webhook.secretKey,
                retryCount = webhook.retryCount,
                retryIntervalMs = webhook.retryIntervalMs,
            ),
        )
}
