package com.cowork.desktop.client.feature.main.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cowork.desktop.client.data.local.LayoutPreferenceStorage
import com.cowork.desktop.client.data.remote.ChatSocket
import com.cowork.desktop.client.data.repository.AuthRepository
import com.cowork.desktop.client.data.repository.ChannelRepository
import com.cowork.desktop.client.data.repository.ChatRepository
import com.cowork.desktop.client.data.repository.MeetingNoteRepository
import com.cowork.desktop.client.data.repository.PreferenceRepository
import com.cowork.desktop.client.data.repository.ProjectRepository
import com.cowork.desktop.client.data.repository.TeamRepository
import com.cowork.desktop.client.data.repository.ThreadRepository
import com.cowork.desktop.client.data.repository.UserRepository
import com.cowork.desktop.client.data.repository.WebhookRepository
import com.cowork.desktop.client.domain.model.AppLanguage
import com.cowork.desktop.client.domain.model.AppTheme
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.DateFormat
import com.cowork.desktop.client.domain.model.TimeFormat
import com.cowork.desktop.client.domain.model.UserStatus
import com.cowork.desktop.client.feature.main.store.MainStore
import com.cowork.desktop.client.feature.main.store.MainStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultMainComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    authRepository: AuthRepository,
    teamRepository: TeamRepository,
    channelRepository: ChannelRepository,
    chatRepository: ChatRepository,
    preferenceRepository: PreferenceRepository,
    userRepository: UserRepository,
    projectRepository: ProjectRepository,
    threadRepository: ThreadRepository,
    webhookRepository: WebhookRepository,
    meetingNoteRepository: MeetingNoteRepository,
    chatSocket: ChatSocket,
    override val layoutPreferenceStorage: LayoutPreferenceStorage,
    private val onSignedOut: () -> Unit,
) : MainComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val store = instanceKeeper.getStore {
        MainStoreFactory(
            storeFactory = storeFactory,
            authRepository = authRepository,
            teamRepository = teamRepository,
            channelRepository = channelRepository,
            chatRepository = chatRepository,
            preferenceRepository = preferenceRepository,
            userRepository = userRepository,
            projectRepository = projectRepository,
            threadRepository = threadRepository,
            webhookRepository = webhookRepository,
            meetingNoteRepository = meetingNoteRepository,
            chatSocket = chatSocket,
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<MainStore.State> = store.stateFlow(scope)

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    MainStore.Label.SignedOut -> onSignedOut()
                }
            }
        }
    }

    override fun onTeamClick(teamId: Long) = store.accept(MainStore.Intent.SelectTeam(teamId))
    override fun onChannelClick(channelId: Long) = store.accept(MainStore.Intent.SelectChannel(channelId))
    override fun onCreateTeamClick() = store.accept(MainStore.Intent.OpenCreateTeam)
    override fun onCreateTeamDismiss() = store.accept(MainStore.Intent.CloseCreateTeam)
    override fun onCreateTeamNameChange(name: String) = store.accept(MainStore.Intent.ChangeCreateTeamName(name))
    override fun onCreateTeamDescriptionChange(description: String) = store.accept(MainStore.Intent.ChangeCreateTeamDescription(description))
    override fun onCreateTeamIconChange(bytes: ByteArray, contentType: String) = store.accept(MainStore.Intent.SetCreateTeamIcon(bytes, contentType))
    override fun onCreateTeamSubmit() = store.accept(MainStore.Intent.SubmitCreateTeam)
    override fun onCreateChannelClick() = store.accept(MainStore.Intent.OpenCreateChannel)
    override fun onCreateChannelDismiss() = store.accept(MainStore.Intent.CloseCreateChannel)
    override fun onCreateChannelNameChange(name: String) = store.accept(MainStore.Intent.ChangeCreateChannelName(name))
    override fun onCreateChannelDescriptionChange(description: String) = store.accept(MainStore.Intent.ChangeCreateChannelDescription(description))
    override fun onCreateChannelTypeChange(type: ChannelType) = store.accept(MainStore.Intent.ChangeCreateChannelType(type))
    override fun onCreateChannelPrivateChange(isPrivate: Boolean) = store.accept(MainStore.Intent.ChangeCreateChannelPrivate(isPrivate))
    override fun onCreateChannelSubmit() = store.accept(MainStore.Intent.SubmitCreateChannel)
    override fun onProjectClick(projectId: Long) = store.accept(MainStore.Intent.SelectProject(projectId))
    override fun onCreateProjectClick() = store.accept(MainStore.Intent.OpenCreateProject)
    override fun onCreateProjectDismiss() = store.accept(MainStore.Intent.CloseCreateProject)
    override fun onCreateProjectNameChange(name: String) = store.accept(MainStore.Intent.ChangeCreateProjectName(name))
    override fun onCreateProjectDescriptionChange(description: String) = store.accept(MainStore.Intent.ChangeCreateProjectDescription(description))
    override fun onCreateProjectSubmit() = store.accept(MainStore.Intent.SubmitCreateProject)
    override fun onAccountMenuClick() = store.accept(MainStore.Intent.ToggleAccountMenu)
    override fun onAccountMenuDismiss() = store.accept(MainStore.Intent.CloseAccountMenu)
    override fun onSettingsClick() = store.accept(MainStore.Intent.OpenSettings)
    override fun onSettingsDismiss() = store.accept(MainStore.Intent.CloseSettings)
    override fun onStatusChange(status: UserStatus, expiresInHours: Double?) = store.accept(MainStore.Intent.SetStatus(status, expiresInHours))
    override fun onSignOutClick() = store.accept(MainStore.Intent.SignOut)
    override fun onUploadProfileImage(bytes: ByteArray, contentType: String) =
        store.accept(MainStore.Intent.UploadProfileImage(bytes, contentType))
    override fun onReloadClick() = store.accept(MainStore.Intent.Reload)
    override fun onThemeChange(theme: AppTheme) = store.accept(MainStore.Intent.UpdateTheme(theme))
    override fun onLanguageChange(language: AppLanguage) = store.accept(MainStore.Intent.UpdateLanguage(language))
    override fun onTimeFormatChange(timeFormat: TimeFormat) = store.accept(MainStore.Intent.UpdateTimeFormat(timeFormat))
    override fun onDateFormatChange(dateFormat: DateFormat) = store.accept(MainStore.Intent.UpdateDateFormat(dateFormat))
    override fun onMarketingEmailChange(enabled: Boolean) = store.accept(MainStore.Intent.UpdateMarketingEmail(enabled))
    override fun onAddWebhookClick() = store.accept(MainStore.Intent.OpenAddWebhook)
    override fun onAddWebhookDismiss() = store.accept(MainStore.Intent.CloseAddWebhook)
    override fun onAddWebhookNameChange(name: String) = store.accept(MainStore.Intent.ChangeAddWebhookName(name))
    override fun onAddWebhookSecureChange(isSecure: Boolean) = store.accept(MainStore.Intent.ChangeAddWebhookSecure(isSecure))
    override fun onAddWebhookSubmit() = store.accept(MainStore.Intent.SubmitAddWebhook)
    override fun onDeleteWebhook(webhookId: Long) = store.accept(MainStore.Intent.DeleteWebhook(webhookId))
    override fun onReorderChannels(fromIndex: Int, toIndex: Int) = store.accept(MainStore.Intent.ReorderChannels(fromIndex, toIndex))
    override fun onReorderProjects(fromIndex: Int, toIndex: Int) = store.accept(MainStore.Intent.ReorderProjects(fromIndex, toIndex))
    override fun onChatDraftChange(draft: String) = store.accept(MainStore.Intent.SetChatDraft(draft))
    override fun onSendChatMessage() = store.accept(MainStore.Intent.SendChatMessage)
    override fun onCreateMeetingNoteClick() = store.accept(MainStore.Intent.OpenCreateMeetingNote)
    override fun onCreateMeetingNoteDismiss() = store.accept(MainStore.Intent.CloseCreateMeetingNote)
    override fun onCreateNoteTitleChange(title: String) = store.accept(MainStore.Intent.ChangeCreateNoteTitle(title))
    override fun onCreateNoteSectionContentChange(sectionTitle: String, content: String) =
        store.accept(MainStore.Intent.ChangeCreateNoteSectionContent(sectionTitle, content))
    override fun onCreateMeetingNoteSubmit() = store.accept(MainStore.Intent.SubmitCreateMeetingNote)
    override fun onMeetingNoteClick(noteId: Long) = store.accept(MainStore.Intent.SelectMeetingNote(noteId))
    override fun onMeetingNoteDetailDismiss() = store.accept(MainStore.Intent.CloseMeetingNoteDetail)
    override fun onOpenThreadList() = store.accept(MainStore.Intent.OpenThreadList)
    override fun onCloseThreadList() = store.accept(MainStore.Intent.CloseThreadList)
    override fun onThreadClick(threadId: Long) = store.accept(MainStore.Intent.OpenThread(threadId))
    override fun onCloseThread() = store.accept(MainStore.Intent.CloseThread)
    override fun onStartEditMessage(messageId: String) = store.accept(MainStore.Intent.StartEditMessage(messageId))
    override fun onCancelEditMessage() = store.accept(MainStore.Intent.CancelEditMessage)
    override fun onChangeEditMessageContent(content: String) = store.accept(MainStore.Intent.ChangeEditMessageContent(content))
    override fun onSubmitEditMessage() = store.accept(MainStore.Intent.SubmitEditMessage)
    override fun onDeleteMessage(messageId: String) = store.accept(MainStore.Intent.DeleteMessage(messageId))
}
