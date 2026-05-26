package com.cowork.desktop.client.feature.main.component

import com.cowork.desktop.client.data.local.LayoutPreferenceStorage
import com.cowork.desktop.client.domain.model.AppLanguage
import com.cowork.desktop.client.domain.model.AppTheme
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.DateFormat
import com.cowork.desktop.client.domain.model.TimeFormat
import com.cowork.desktop.client.domain.model.UserStatus
import com.cowork.desktop.client.feature.main.store.MainStore
import kotlinx.coroutines.flow.StateFlow

interface MainComponent {
    val state: StateFlow<MainStore.State>
    val layoutPreferenceStorage: LayoutPreferenceStorage

    fun onTeamClick(teamId: Long)
    fun onChannelClick(channelId: Long)
    fun onCreateTeamClick()
    fun onCreateTeamDismiss()
    fun onCreateTeamNameChange(name: String)
    fun onCreateTeamDescriptionChange(description: String)
    fun onCreateTeamIconChange(bytes: ByteArray, contentType: String)
    fun onCreateTeamSubmit()
    fun onCreateChannelClick()
    fun onCreateChannelDismiss()
    fun onCreateChannelNameChange(name: String)
    fun onCreateChannelDescriptionChange(description: String)
    fun onCreateChannelTypeChange(type: ChannelType)
    fun onCreateChannelPrivateChange(isPrivate: Boolean)
    fun onCreateChannelSubmit()
    fun onProjectClick(projectId: Long)
    fun onCreateProjectClick()
    fun onCreateProjectDismiss()
    fun onCreateProjectNameChange(name: String)
    fun onCreateProjectDescriptionChange(description: String)
    fun onCreateProjectSubmit()
    fun onAccountMenuClick()
    fun onAccountMenuDismiss()
    fun onSettingsClick()
    fun onSettingsDismiss()
    fun onStatusChange(status: UserStatus, expiresInHours: Double?)
    fun onSignOutClick()
    fun onUploadProfileImage(bytes: ByteArray, contentType: String)
    fun onReloadClick()
    fun onThemeChange(theme: AppTheme)
    fun onLanguageChange(language: AppLanguage)
    fun onTimeFormatChange(timeFormat: TimeFormat)
    fun onDateFormatChange(dateFormat: DateFormat)
    fun onMarketingEmailChange(enabled: Boolean)
    fun onAddWebhookClick()
    fun onAddWebhookDismiss()
    fun onAddWebhookNameChange(name: String)
    fun onAddWebhookSecureChange(isSecure: Boolean)
    fun onAddWebhookSubmit()
    fun onDeleteWebhook(webhookId: Long)
    fun onReorderChannels(fromIndex: Int, toIndex: Int)
    fun onReorderProjects(fromIndex: Int, toIndex: Int)
    fun onChatDraftChange(draft: String)
    fun onSendChatMessage()
    fun onCreateMeetingNoteClick()
    fun onCreateMeetingNoteDismiss()
    fun onCreateNoteTitleChange(title: String)
    fun onCreateNoteSectionContentChange(sectionTitle: String, content: String)
    fun onCreateMeetingNoteSubmit()
    fun onMeetingNoteClick(noteId: Long)
    fun onMeetingNoteDetailDismiss()
}
