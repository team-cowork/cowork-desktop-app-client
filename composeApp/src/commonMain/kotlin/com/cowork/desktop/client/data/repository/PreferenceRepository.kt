package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.PreferenceApi
import com.cowork.desktop.client.domain.model.AppLanguage
import com.cowork.desktop.client.domain.model.AppTheme
import com.cowork.desktop.client.domain.model.ChannelNotificationSettings
import com.cowork.desktop.client.domain.model.DateFormat
import com.cowork.desktop.client.domain.model.ProjectSettings
import com.cowork.desktop.client.domain.model.TeamSettings
import com.cowork.desktop.client.domain.model.TextChannelSettings
import com.cowork.desktop.client.domain.model.TextChannelSettingsUpdate
import com.cowork.desktop.client.domain.model.TimeFormat
import com.cowork.desktop.client.domain.model.UserStatus
import com.cowork.desktop.client.domain.model.VoiceChannelSettings
import com.cowork.desktop.client.domain.model.toAppLanguage
import com.cowork.desktop.client.domain.model.toAppTheme
import com.cowork.desktop.client.domain.model.toDateFormat
import com.cowork.desktop.client.domain.model.toTimeFormat
import com.cowork.desktop.client.util.nowPlusHoursIso8601

interface PreferenceRepository {
    suspend fun getAccountStatus(accountId: Long): UserStatus
    suspend fun getAccountSettings(accountId: Long): PreferenceApi.AccountSettings
    suspend fun updateAccountStatus(accountId: Long, status: UserStatus, expiresInHours: Double?)
    suspend fun updateAppearance(accountId: Long, theme: AppTheme, language: AppLanguage, timeFormat: TimeFormat, dateFormat: DateFormat)
    suspend fun updateMarketingEmail(accountId: Long, enabled: Boolean)
    suspend fun getTeamSettings(teamId: Long): TeamSettings
    suspend fun updateTeamSettings(teamId: Long, settings: TeamSettings): TeamSettings
    suspend fun getProjectSettings(projectId: Long): ProjectSettings
    suspend fun updateProjectSettings(projectId: Long, settings: ProjectSettings = ProjectSettings): ProjectSettings
    suspend fun getVoiceChannelSettings(channelId: Long): VoiceChannelSettings
    suspend fun updateVoiceChannelSettings(channelId: Long, settings: VoiceChannelSettings): VoiceChannelSettings
    suspend fun getTextChannelSettings(channelId: Long): TextChannelSettings
    suspend fun updateTextChannelSettings(channelId: Long, settings: TextChannelSettingsUpdate): TextChannelSettings
    suspend fun getChannelNotificationSettings(accountId: Long, channelId: Long): ChannelNotificationSettings
    suspend fun updateChannelNotificationSettings(
        accountId: Long,
        channelId: Long,
        settings: ChannelNotificationSettings,
    ): ChannelNotificationSettings
}

class DefaultPreferenceRepository(
    private val authRepository: AuthRepository,
    private val preferenceApi: PreferenceApi,
) : PreferenceRepository {

    override suspend fun getAccountStatus(accountId: Long): UserStatus =
        runCatching {
            authRepository.authorized { token ->
                preferenceApi.getAccountSettings(token, accountId).status.toUserStatus()
            }
        }.getOrDefault(UserStatus.Online)

    override suspend fun getAccountSettings(accountId: Long): PreferenceApi.AccountSettings =
        runCatching {
            authRepository.authorized { token ->
                preferenceApi.getAccountSettings(token, accountId)
            }
        }.getOrDefault(PreferenceApi.AccountSettings())

    override suspend fun updateAccountStatus(accountId: Long, status: UserStatus, expiresInHours: Double?) {
        val expiresAt = if (status == UserStatus.DoNotDisturb && expiresInHours != null) {
            nowPlusHoursIso8601(expiresInHours)
        } else {
            null
        }
        authRepository.authorized { token ->
            preferenceApi.updateAccountSettings(
                accessToken = token,
                accountId = accountId,
                request = PreferenceApi.UpdateAccountSettingsRequest(
                    status = status.toApiValue(),
                    statusExpiresAt = expiresAt,
                ),
            )
        }
    }

    override suspend fun updateAppearance(
        accountId: Long,
        theme: AppTheme,
        language: AppLanguage,
        timeFormat: TimeFormat,
        dateFormat: DateFormat,
    ) {
        authRepository.authorized { token ->
            preferenceApi.updateAccountSettings(
                accessToken = token,
                accountId = accountId,
                request = PreferenceApi.UpdateAccountSettingsRequest(
                    theme = theme.apiValue,
                    language = language.apiValue,
                    timeFormat = timeFormat.apiValue,
                    dateFormat = dateFormat.apiValue,
                ),
            )
        }
    }

    override suspend fun updateMarketingEmail(accountId: Long, enabled: Boolean) {
        authRepository.authorized { token ->
            preferenceApi.updateAccountSettings(
                accessToken = token,
                accountId = accountId,
                request = PreferenceApi.UpdateAccountSettingsRequest(marketingEmail = enabled),
            )
        }
    }

    override suspend fun getTeamSettings(teamId: Long): TeamSettings =
        authRepository.authorized { preferenceApi.getTeamSettings(it, teamId) }

    override suspend fun updateTeamSettings(teamId: Long, settings: TeamSettings): TeamSettings =
        authRepository.authorized { preferenceApi.updateTeamSettings(it, teamId, settings) }

    override suspend fun getProjectSettings(projectId: Long): ProjectSettings =
        authRepository.authorized { preferenceApi.getProjectSettings(it, projectId) }

    override suspend fun updateProjectSettings(projectId: Long, settings: ProjectSettings): ProjectSettings =
        authRepository.authorized { preferenceApi.updateProjectSettings(it, projectId, settings) }

    override suspend fun getVoiceChannelSettings(channelId: Long): VoiceChannelSettings =
        authRepository.authorized { preferenceApi.getVoiceChannelSettings(it, channelId) }

    override suspend fun updateVoiceChannelSettings(
        channelId: Long,
        settings: VoiceChannelSettings,
    ): VoiceChannelSettings =
        authRepository.authorized { preferenceApi.updateVoiceChannelSettings(it, channelId, settings) }

    override suspend fun getTextChannelSettings(channelId: Long): TextChannelSettings =
        authRepository.authorized { preferenceApi.getTextChannelSettings(it, channelId) }

    override suspend fun updateTextChannelSettings(
        channelId: Long,
        settings: TextChannelSettingsUpdate,
    ): TextChannelSettings =
        authRepository.authorized { preferenceApi.updateTextChannelSettings(it, channelId, settings) }

    override suspend fun getChannelNotificationSettings(
        accountId: Long,
        channelId: Long,
    ): ChannelNotificationSettings =
        authRepository.authorized { preferenceApi.getChannelNotificationSettings(it, accountId, channelId) }

    override suspend fun updateChannelNotificationSettings(
        accountId: Long,
        channelId: Long,
        settings: ChannelNotificationSettings,
    ): ChannelNotificationSettings =
        authRepository.authorized {
            preferenceApi.updateChannelNotificationSettings(it, accountId, channelId, settings)
        }

    private fun String?.toUserStatus(): UserStatus = when (this?.uppercase()) {
        "DO_NOT_DISTURB" -> UserStatus.DoNotDisturb
        else -> UserStatus.Online
    }

    private fun UserStatus.toApiValue(): String = when (this) {
        UserStatus.Online -> "ONLINE"
        UserStatus.DoNotDisturb -> "DO_NOT_DISTURB"
    }
}
