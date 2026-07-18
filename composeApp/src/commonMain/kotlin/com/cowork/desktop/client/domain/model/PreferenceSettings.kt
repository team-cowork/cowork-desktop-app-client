package com.cowork.desktop.client.domain.model

data class TeamSettings(
    val tagSpamBlock: Boolean? = null,
    val nicknameFormatEnforced: Boolean? = null,
    val nicknameFormatExample: String? = null,
)

/**
 * The preference service currently has no supported project-setting fields.
 * GET and PUT therefore use an empty JSON object until that server schema is extended.
 */
data object ProjectSettings

data class VoiceChannelSettings(
    val bitrate: Int? = null,
    val maxParticipants: Int? = null,
)

data class TextChannelSettings(
    val webhook: WebhookSettings? = null,
)

data class WebhookSettings(
    val isActive: Boolean? = null,
    val secretKey: String? = null,
    val retryCount: Int? = null,
    val retryIntervalMs: Int? = null,
)

/**
 * Text-channel webhook settings are replaced as one nested object by the server,
 * so updates require every currently supported webhook field.
 */
data class TextChannelSettingsUpdate(
    val webhook: WebhookSettingsUpdate,
)

data class WebhookSettingsUpdate(
    val isActive: Boolean,
    val secretKey: String,
    val retryCount: Int,
    val retryIntervalMs: Int,
)

/**
 * The deployed preference service represents channel notifications as a Boolean
 * `notification` field. Its OpenAPI document still advertises ALL/MENTIONS/NONE;
 * this on/off type intentionally follows the working server contract.
 */
data class ChannelNotificationSettings(
    val isEnabled: Boolean,
)
