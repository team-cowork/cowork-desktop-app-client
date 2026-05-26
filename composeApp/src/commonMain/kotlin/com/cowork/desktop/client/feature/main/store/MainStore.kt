package com.cowork.desktop.client.feature.main.store

import com.arkivanov.mvikotlin.core.store.Store
import com.cowork.desktop.client.domain.model.AppLanguage
import com.cowork.desktop.client.domain.model.AppTheme
import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.DateFormat
import com.cowork.desktop.client.domain.model.MeetingNote
import com.cowork.desktop.client.domain.model.MeetingNoteTemplate
import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.TeamSummary
import com.cowork.desktop.client.domain.model.Thread
import com.cowork.desktop.client.domain.model.TimeFormat
import com.cowork.desktop.client.domain.model.UserProfile
import com.cowork.desktop.client.domain.model.UserStatus
import com.cowork.desktop.client.domain.model.Webhook
import com.cowork.desktop.client.feature.main.store.MainStore.Intent
import com.cowork.desktop.client.feature.main.store.MainStore.Label
import com.cowork.desktop.client.feature.main.store.MainStore.State

interface MainStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object Reload : Intent
        data class SelectTeam(val teamId: Long) : Intent
        data class SelectChannel(val channelId: Long) : Intent
        data object OpenCreateTeam : Intent
        data object CloseCreateTeam : Intent
        data class ChangeCreateTeamName(val name: String) : Intent
        data class ChangeCreateTeamDescription(val description: String) : Intent
        data class SetCreateTeamIcon(val bytes: ByteArray, val contentType: String) : Intent
        data object SubmitCreateTeam : Intent
        data object OpenCreateChannel : Intent
        data object CloseCreateChannel : Intent
        data class ChangeCreateChannelName(val name: String) : Intent
        data class ChangeCreateChannelDescription(val description: String) : Intent
        data class ChangeCreateChannelType(val type: ChannelType) : Intent
        data class ChangeCreateChannelPrivate(val isPrivate: Boolean) : Intent
        data object SubmitCreateChannel : Intent
        data class SelectProject(val projectId: Long) : Intent
        data object OpenCreateProject : Intent
        data object CloseCreateProject : Intent
        data class ChangeCreateProjectName(val name: String) : Intent
        data class ChangeCreateProjectDescription(val description: String) : Intent
        data object SubmitCreateProject : Intent
        data object OpenAccountMenu : Intent
        data object ToggleAccountMenu : Intent
        data object CloseAccountMenu : Intent
        data object OpenSettings : Intent
        data object CloseSettings : Intent
        data class SetStatus(val status: UserStatus, val expiresInHours: Double?) : Intent
        data object SignOut : Intent
        data class UploadProfileImage(val bytes: ByteArray, val contentType: String) : Intent
        data class UpdateTheme(val theme: AppTheme) : Intent
        data class UpdateLanguage(val language: AppLanguage) : Intent
        data class UpdateTimeFormat(val timeFormat: TimeFormat) : Intent
        data class UpdateDateFormat(val dateFormat: DateFormat) : Intent
        data class UpdateMarketingEmail(val enabled: Boolean) : Intent
        data object OpenAddWebhook : Intent
        data object CloseAddWebhook : Intent
        data class ChangeAddWebhookName(val name: String) : Intent
        data class ChangeAddWebhookSecure(val isSecure: Boolean) : Intent
        data object SubmitAddWebhook : Intent
        data class DeleteWebhook(val webhookId: Long) : Intent
        data class ReorderChannels(val fromIndex: Int, val toIndex: Int) : Intent
        data class ReorderProjects(val fromIndex: Int, val toIndex: Int) : Intent
        data class SetChatDraft(val draft: String) : Intent
        data object SendChatMessage : Intent
        data object OpenCreateMeetingNote : Intent
        data object CloseCreateMeetingNote : Intent
        data class ChangeCreateNoteTitle(val title: String) : Intent
        data class ChangeCreateNoteSectionContent(val sectionTitle: String, val content: String) : Intent
        data object SubmitCreateMeetingNote : Intent
        data class SelectMeetingNote(val noteId: Long) : Intent
        data object CloseMeetingNoteDetail : Intent
        data object OpenThreadList : Intent
        data object CloseThreadList : Intent
        data class OpenThread(val threadId: Long) : Intent
        data object CloseThread : Intent
        data class StartEditMessage(val messageId: String) : Intent
        data object CancelEditMessage : Intent
        data class ChangeEditMessageContent(val content: String) : Intent
        data object SubmitEditMessage : Intent
        data class DeleteMessage(val messageId: String) : Intent
    }

    data class State(
        val teams: List<TeamSummary> = emptyList(),
        val selectedTeamId: Long? = null,
        val memberProfiles: Map<Long, UserProfile> = emptyMap(),
        val channels: List<Channel> = emptyList(),
        val selectedChannelId: Long? = null,
        val messages: List<ChatMessage> = emptyList(),
        val threads: List<Thread> = emptyList(),
        val isThreadListOpen: Boolean = false,
        val selectedThreadId: Long? = null,
        val editingMessageId: String? = null,
        val editingMessageContent: String = "",
        val webhooks: List<Webhook> = emptyList(),
        val isLoadingWebhooks: Boolean = false,
        val isAddWebhookOpen: Boolean = false,
        val addWebhookName: String = "",
        val addWebhookIsSecure: Boolean = false,
        val isAddingWebhook: Boolean = false,
        val meetingNotes: List<MeetingNote> = emptyList(),
        val isLoadingMeetingNotes: Boolean = false,
        val meetingNoteTemplates: List<MeetingNoteTemplate> = emptyList(),
        val selectedMeetingNoteId: Long? = null,
        val isCreateMeetingNoteOpen: Boolean = false,
        val createNoteTitle: String = "",
        val createNoteSectionContents: Map<String, String> = emptyMap(),
        val isCreatingNote: Boolean = false,
        val projects: List<Project> = emptyList(),
        val selectedProjectId: Long? = null,
        val isLoadingTeams: Boolean = false,
        val isLoadingChannels: Boolean = false,
        val isLoadingMessages: Boolean = false,
        val isLoadingThreads: Boolean = false,
        val isLoadingProjects: Boolean = false,
        val chatDraft: String = "",
        val isCreateTeamOpen: Boolean = false,
        val createTeamName: String = "",
        val createTeamDescription: String = "",
        val createTeamIconBytes: ByteArray? = null,
        val createTeamIconContentType: String? = null,
        val isCreatingTeam: Boolean = false,
        val isCreateChannelOpen: Boolean = false,
        val createChannelName: String = "",
        val createChannelDescription: String = "",
        val createChannelType: ChannelType = ChannelType.Text,
        val createChannelIsPrivate: Boolean = false,
        val isCreatingChannel: Boolean = false,
        val isCreateProjectOpen: Boolean = false,
        val createProjectName: String = "",
        val createProjectDescription: String = "",
        val isCreatingProject: Boolean = false,
        val error: String? = null,
        val accountId: Long? = null,
        val accountEmail: String? = null,
        val accountSystemRole: String? = null,
        val accountName: String? = null,
        val accountNickname: String? = null,
        val accountProfileImageUrl: String? = null,
        val accountGithub: String? = null,
        val accountStudentNumber: String? = null,
        val accountMajor: String? = null,
        val accountStudentRole: String? = null,
        val accountDescription: String? = null,
        val accountRoles: List<String> = emptyList(),
        val accountStatus: UserStatus = UserStatus.Online,
        val accountTheme: AppTheme = AppTheme.Dark,
        val accountLanguage: AppLanguage = AppLanguage.Korean,
        val accountTimeFormat: TimeFormat = TimeFormat.H24,
        val accountDateFormat: DateFormat = DateFormat.YYYY_MM_DD,
        val accountMarketingEmail: Boolean = false,
        val isAccountMenuOpen: Boolean = false,
        val isSettingsOpen: Boolean = false,
        val isUpdatingStatus: Boolean = false,
        val isUploadingProfileImage: Boolean = false,
        val isUpdatingSettings: Boolean = false,
    ) {
        val isSystemAdmin: Boolean
            get() = accountSystemRole == "ROLE_ADMIN"

        fun canEditMessage(authorId: Long): Boolean = authorId == accountId || isSystemAdmin
        fun canDeleteMessage(authorId: Long): Boolean = authorId == accountId || isSystemAdmin

        val selectedTeam: TeamSummary?
            get() = teams.firstOrNull { it.id == selectedTeamId }

        val selectedChannel: Channel?
            get() = channels.firstOrNull { it.id == selectedChannelId }

        val selectedProject: Project?
            get() = projects.firstOrNull { it.id == selectedProjectId }

        val canSubmitTeam: Boolean
            get() = createTeamName.isNotBlank() && !isCreatingTeam

        val canSubmitChannel: Boolean
            get() = selectedTeamId != null && createChannelName.isNotBlank() && !isCreatingChannel

        val canSubmitProject: Boolean
            get() = selectedTeamId != null && createProjectName.isNotBlank() && !isCreatingProject

        val canSubmitWebhook: Boolean
            get() = addWebhookName.isNotBlank() && !isAddingWebhook

        val selectedMeetingNote: MeetingNote?
            get() = meetingNotes.firstOrNull { it.id == selectedMeetingNoteId }

        val activeTemplate: MeetingNoteTemplate?
            get() = meetingNoteTemplates.firstOrNull { it.isActive } ?: meetingNoteTemplates.firstOrNull()

        val canSubmitNote: Boolean
            get() = createNoteTitle.isNotBlank() && !isCreatingNote
    }

    sealed interface Label {
        data object SignedOut : Label
    }
}
