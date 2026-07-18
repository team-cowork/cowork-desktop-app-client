package com.cowork.desktop.client.feature.main.store

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.cowork.desktop.client.config.AppConfig
import com.cowork.desktop.client.data.remote.ChatSocket
import com.cowork.desktop.client.data.remote.ChatSocketEvent
import com.cowork.desktop.client.data.repository.AuthRepository
import com.cowork.desktop.client.data.repository.ChannelRepository
import com.cowork.desktop.client.data.repository.ChatRepository
import com.cowork.desktop.client.data.repository.PreferenceRepository
import com.cowork.desktop.client.data.repository.ProjectRepository
import com.cowork.desktop.client.data.repository.SessionExpiredException
import com.cowork.desktop.client.data.repository.TeamRepository
import com.cowork.desktop.client.data.repository.ThreadRepository
import com.cowork.desktop.client.data.repository.UserRepository
import com.cowork.desktop.client.data.repository.MeetingNoteRepository
import com.cowork.desktop.client.data.repository.WebhookRepository
import com.cowork.desktop.client.domain.model.MeetingNote
import com.cowork.desktop.client.domain.model.MeetingNoteTemplate
import com.cowork.desktop.client.domain.model.Webhook
import com.cowork.desktop.client.domain.model.AppLanguage
import com.cowork.desktop.client.domain.model.AppTheme
import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.DateFormat
import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.TeamSummary
import com.cowork.desktop.client.domain.model.Thread
import com.cowork.desktop.client.domain.model.TimeFormat
import com.cowork.desktop.client.domain.model.UserStatus
import com.cowork.desktop.client.domain.model.toAppLanguage
import com.cowork.desktop.client.domain.model.toAppTheme
import com.cowork.desktop.client.domain.model.toDateFormat
import com.cowork.desktop.client.domain.model.toTimeFormat
import com.cowork.desktop.client.feature.main.store.MainStore.Intent
import com.cowork.desktop.client.feature.main.store.MainStore.Label
import com.cowork.desktop.client.feature.main.store.MainStore.State
import com.cowork.desktop.client.util.parseJwtClaims
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.put

private const val MESSAGE_FETCH_PAGE_SIZE = 100
private const val ROOT_MESSAGE_PAGE_SIZE = 50
private const val MAX_ROOT_MESSAGE_PAGES = 5

internal fun <T> List<T>.moveItem(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex !in indices || toIndex !in indices || fromIndex == toIndex) return this
    return toMutableList().apply {
        val item = removeAt(fromIndex)
        add(toIndex, item)
    }
}

internal fun State.reduceProjectSelection(projectId: Long): State = copy(
    selectedChannelId = null,
    selectedProjectId = projectId,
    chatDraft = "",
)

class MainStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
    private val teamRepository: TeamRepository,
    private val channelRepository: ChannelRepository,
    private val chatRepository: ChatRepository,
    private val preferenceRepository: PreferenceRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val threadRepository: ThreadRepository,
    private val webhookRepository: WebhookRepository,
    private val meetingNoteRepository: MeetingNoteRepository,
    private val chatSocket: ChatSocket,
) {
    fun create(): MainStore =
        object : MainStore, Store<Intent, State, Label> by storeFactory.create(
            name = "MainStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Action.Init),
            executorFactory = { Executor() },
            reducer = Reducer,
        ) {}

    private sealed interface Action {
        data object Init : Action
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        private var typingStopJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.Init -> init()
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Reload -> reloadWorkspace()
                is Intent.SelectTeam -> selectTeam(intent.teamId)
                is Intent.SelectChannel -> selectChannel(intent.channelId)
                Intent.OpenCreateTeam -> dispatch(Msg.SetCreateTeamOpen(true))
                Intent.CloseCreateTeam -> dispatch(Msg.ResetCreateTeamForm)
                is Intent.ChangeCreateTeamName -> dispatch(Msg.SetCreateTeamName(intent.name))
                is Intent.ChangeCreateTeamDescription -> dispatch(Msg.SetCreateTeamDescription(intent.description))
                is Intent.SetCreateTeamIcon -> dispatch(Msg.SetCreateTeamIcon(intent.bytes, intent.contentType))
                Intent.SubmitCreateTeam -> createTeam()
                Intent.OpenCreateChannel -> dispatch(Msg.SetCreateChannelOpen(true))
                Intent.CloseCreateChannel -> dispatch(Msg.ResetCreateChannelForm)
                is Intent.ChangeCreateChannelName -> dispatch(Msg.SetCreateChannelName(intent.name))
                is Intent.ChangeCreateChannelDescription -> dispatch(Msg.SetCreateChannelDescription(intent.description))
                is Intent.ChangeCreateChannelType -> dispatch(Msg.SetCreateChannelType(intent.type))
                is Intent.ChangeCreateChannelPrivate -> dispatch(Msg.SetCreateChannelPrivate(intent.isPrivate))
                Intent.SubmitCreateChannel -> createChannel()
                is Intent.SelectProject -> selectProject(intent.projectId)
                Intent.OpenCreateProject -> dispatch(Msg.SetCreateProjectOpen(true))
                Intent.CloseCreateProject -> dispatch(Msg.ResetCreateProjectForm)
                is Intent.ChangeCreateProjectName -> dispatch(Msg.SetCreateProjectName(intent.name))
                is Intent.ChangeCreateProjectDescription -> dispatch(Msg.SetCreateProjectDescription(intent.description))
                Intent.SubmitCreateProject -> createProject()
                Intent.OpenAccountMenu -> dispatch(Msg.SetAccountMenuOpen(true))
                Intent.ToggleAccountMenu -> dispatch(Msg.SetAccountMenuOpen(!state().isAccountMenuOpen))
                Intent.CloseAccountMenu -> dispatch(Msg.SetAccountMenuOpen(false))
                Intent.OpenSettings -> dispatch(Msg.SetSettingsOpen(true))
                Intent.CloseSettings -> dispatch(Msg.SetSettingsOpen(false))
                is Intent.SetStatus -> updateStatus(intent.status, intent.expiresInHours)
                Intent.SignOut -> signOut()
                is Intent.UploadProfileImage -> uploadProfileImage(intent.bytes, intent.contentType)
                is Intent.UpdateTheme -> updateAppearance(theme = intent.theme)
                is Intent.UpdateLanguage -> updateAppearance(language = intent.language)
                is Intent.UpdateTimeFormat -> updateAppearance(timeFormat = intent.timeFormat)
                is Intent.UpdateDateFormat -> updateAppearance(dateFormat = intent.dateFormat)
                is Intent.UpdateMarketingEmail -> updateMarketingEmail(intent.enabled)
                Intent.OpenAddWebhook -> dispatch(Msg.SetAddWebhookOpen(true))
                Intent.CloseAddWebhook -> dispatch(Msg.ResetAddWebhookForm)
                is Intent.ChangeAddWebhookName -> dispatch(Msg.SetAddWebhookName(intent.name))
                is Intent.ChangeAddWebhookSecure -> dispatch(Msg.SetAddWebhookSecure(intent.isSecure))
                Intent.SubmitAddWebhook -> submitAddWebhook()
                is Intent.DeleteWebhook -> deleteWebhook(intent.webhookId)
                is Intent.ReorderChannels -> reorderChannels(intent.fromIndex, intent.toIndex)
                is Intent.ReorderProjects -> reorderProjects(intent.fromIndex, intent.toIndex)
                is Intent.SetChatDraft -> updateChatDraft(intent.draft)
                Intent.SendChatMessage -> sendChatMessage()
                Intent.OpenCreateMeetingNote -> dispatch(Msg.SetCreateNoteOpen(true))
                Intent.CloseCreateMeetingNote -> dispatch(Msg.ResetCreateNoteForm)
                is Intent.ChangeCreateNoteTitle -> dispatch(Msg.SetCreateNoteTitle(intent.title))
                is Intent.ChangeCreateNoteSectionContent -> dispatch(Msg.SetCreateNoteSectionContent(intent.sectionTitle, intent.content))
                Intent.SubmitCreateMeetingNote -> submitCreateMeetingNote()
                is Intent.SelectMeetingNote -> dispatch(Msg.SelectMeetingNote(intent.noteId))
                Intent.CloseMeetingNoteDetail -> dispatch(Msg.SelectMeetingNote(null))
                is Intent.StartEditMessage -> {
                    val msg = state().messages.firstOrNull { it.id == intent.messageId } ?: return
                    dispatch(Msg.SetEditingMessage(intent.messageId, msg.content))
                }
                Intent.CancelEditMessage -> dispatch(Msg.SetEditingMessage(null, ""))
                is Intent.ChangeEditMessageContent -> dispatch(Msg.SetEditingContent(intent.content))
                Intent.SubmitEditMessage -> submitEditMessage()
                is Intent.DeleteMessage -> deleteMessage(intent.messageId)
                Intent.OpenThreadList -> dispatch(Msg.SetThreadListOpen(true))
                Intent.CloseThreadList -> dispatch(Msg.SetThreadListOpen(false))
                is Intent.OpenThread -> dispatch(Msg.SetSelectedThread(intent.threadId))
                Intent.CloseThread -> dispatch(Msg.SetSelectedThread(null))
            }
        }

        private fun Throwable.handleIfSessionExpired(): Boolean {
            if (this !is SessionExpiredException) return false
            scope.launch {
                runCatching { authRepository.signOut() }
                publish(Label.SignedOut)
            }
            return true
        }

        private fun init() {
            scope.launch {
                val tokens = authRepository.getStoredTokens()
                if (tokens != null) {
                    chatSocket.connectWithEvents(
                        wsBaseUrl = AppConfig.COWORK_CHAT_WS_BASE_URL,
                        token = tokens.accessToken,
                        onEvent = { event -> scope.launch { handleSocketEvent(event) } },
                    )
                    val claims = parseJwtClaims(tokens.accessToken)
                    dispatch(Msg.SetAccountInfo(claims.accountId, claims.email, claims.role))
                    if (claims.accountId != null) {
                        val settings = preferenceRepository.getAccountSettings(claims.accountId)
                        dispatch(Msg.SetAccountStatus(
                            when (settings.status?.uppercase()) {
                                "DO_NOT_DISTURB" -> UserStatus.DoNotDisturb
                                else -> UserStatus.Online
                            }
                        ))
                        dispatch(Msg.SetAccountSettings(
                            theme = settings.theme.toAppTheme(),
                            language = settings.language.toAppLanguage(),
                            timeFormat = settings.timeFormat.toTimeFormat(),
                            dateFormat = settings.dateFormat.toDateFormat(),
                            marketingEmail = settings.marketingEmail ?: false,
                        ))
                    }
                }
            }
            scope.launch {
                val profile = userRepository.getMyProfile()
                if (profile != null) {
                    dispatch(Msg.SetUserProfile(profile))
                }
            }
            loadTeams()
            startTeamPolling()
        }

        private fun loadTeams(silent: Boolean = false) {
            scope.launch {
                if (!silent) dispatch(Msg.SetLoadingTeams(true))
                runCatching { teamRepository.getMyTeams() }
                    .onSuccess { teams ->
                        val currentSelectedTeamId = state().selectedTeamId
                        val selectedTeamId = when {
                            teams.any { it.id == currentSelectedTeamId } -> currentSelectedTeamId
                            else -> teams.firstOrNull()?.id
                        }
                        if (selectedTeamId != currentSelectedTeamId && currentSelectedTeamId != null) {
                            state().selectedChannelId?.let(chatSocket::leaveChannel)
                            chatSocket.leaveTeam(currentSelectedTeamId)
                        }
                        dispatch(Msg.SetTeams(teams, selectedTeamId))
                        if (selectedTeamId != null && selectedTeamId != currentSelectedTeamId) {
                            selectTeam(selectedTeamId)
                        }
                    }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        if (!silent) dispatch(Msg.SetError("팀 목록을 불러오지 못했습니다."))
                    }
                if (!silent) dispatch(Msg.SetLoadingTeams(false))
            }
        }

        private fun reloadWorkspace() {
            val current = state()
            loadTeams()
            current.selectedTeamId?.let { teamId ->
                loadChannels(teamId, current.selectedChannelId)
                loadProjects(teamId)
                loadMemberProfiles(teamId)
                loadUnreadCounts(teamId)
            }
        }

        private fun loadMemberProfiles(teamId: Long) {
            scope.launch {
                val userIds = runCatching { teamRepository.getTeamMembers(teamId) }.getOrNull() ?: return@launch
                val profiles = userIds
                    .map { userId -> async { userRepository.getUserProfile(userId) } }
                    .mapNotNull { it.await() }
                    .associateBy { it.id }
                if (state().selectedTeamId != teamId) return@launch
                dispatch(Msg.SetMemberProfiles(profiles))
            }
        }

        private fun startTeamPolling() {
            scope.launch {
                while (true) {
                    delay(30_000)
                    loadTeams(silent = true)
                }
            }
        }

        private fun selectTeam(teamId: Long) {
            val previousTeamId = state().selectedTeamId
            state().selectedChannelId?.let(chatSocket::leaveChannel)
            if (previousTeamId != null && previousTeamId != teamId) chatSocket.leaveTeam(previousTeamId)
            dispatch(Msg.SelectTeam(teamId))
            chatSocket.joinTeam(teamId)
            loadChannels(teamId)
            loadProjects(teamId)
            loadMemberProfiles(teamId)
            loadUnreadCounts(teamId)
        }

        private fun loadUnreadCounts(teamId: Long) {
            scope.launch {
                val result = runCatching { chatRepository.getTeamUnread(teamId) }
                if (state().selectedTeamId != teamId) return@launch
                result.onSuccess { counts -> counts.forEach { dispatch(Msg.SetUnreadCount(it.channelId, it.unreadCount)) } }
            }
        }

        private fun handleSocketEvent(event: ChatSocketEvent) {
            when (event) {
                ChatSocketEvent.Connected -> {
                    state().selectedTeamId?.let(chatSocket::joinTeam)
                    state().selectedChannel
                        ?.takeIf { it.type == ChannelType.Text }
                        ?.id
                        ?.let(chatSocket::joinChannel)
                }
                is ChatSocketEvent.MessageReceived -> dispatch(Msg.PrependMessage(event.message))
                is ChatSocketEvent.MessageEdited -> dispatch(Msg.UpdateMessageContent(event.messageId, event.content))
                is ChatSocketEvent.MessageDeleted -> dispatch(Msg.RemoveMessage(event.messageId))
                is ChatSocketEvent.MessagePinned,
                is ChatSocketEvent.MessageUnpinned,
                is ChatSocketEvent.ReactionAdded,
                is ChatSocketEvent.ReactionRemoved -> state().selectedChannelId?.let(::loadMessages)
                is ChatSocketEvent.Typing -> {
                    if (event.channelId == state().selectedChannelId && event.userId != state().accountId) {
                        dispatch(Msg.SetTyping(event.userId, event.isTyping))
                    }
                }
                is ChatSocketEvent.ChannelUnreadUpdated -> dispatch(Msg.SetUnreadCount(event.channelId, event.unreadCount))
                is ChatSocketEvent.ChannelCreated,
                is ChatSocketEvent.ChannelUpdated,
                is ChatSocketEvent.ChannelDeleted -> state().selectedTeamId?.let { loadChannels(it, state().selectedChannelId) }
                is ChatSocketEvent.ProjectCreated,
                is ChatSocketEvent.ProjectUpdated,
                is ChatSocketEvent.ProjectDeleted -> state().selectedTeamId?.let(::loadProjects)
                is ChatSocketEvent.MemberJoined,
                is ChatSocketEvent.MemberLeft,
                is ChatSocketEvent.MemberRoleUpdated -> state().selectedTeamId?.let(::loadMemberProfiles)
                is ChatSocketEvent.ConnectionError,
                is ChatSocketEvent.Disconnected,
                is ChatSocketEvent.ServerError -> Unit
            }
        }

        private fun loadChannels(teamId: Long, preferredChannelId: Long? = null) {
            scope.launch {
                dispatch(Msg.SetLoadingChannels(true))
                val result = runCatching { channelRepository.getTeamChannels(teamId) }
                if (state().selectedTeamId != teamId) return@launch
                result
                    .onSuccess { channels -> replaceChannels(channels, preferredChannelId) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        replaceChannels(emptyList())
                    }
                dispatch(Msg.SetLoadingChannels(false))
            }
        }

        private fun replaceChannels(channels: List<Channel>, preferredChannelId: Long? = null) {
            if (preferredChannelId == null && state().selectedProjectId != null) {
                dispatch(Msg.SetChannels(channels))
                return
            }
            val channelToSelect = preferredChannelId
                ?.let { preferredId -> channels.firstOrNull { it.id == preferredId } }
                ?: channels.firstOrNull()

            state().selectedChannelId?.let(chatSocket::leaveChannel)
            dispatch(Msg.SetChannels(channels))
            channelToSelect?.let { selectChannel(it.id) }
        }

        private fun loadProjects(teamId: Long) {
            scope.launch {
                dispatch(Msg.SetLoadingProjects(true))
                val result = runCatching { projectRepository.getTeamProjects(teamId) }
                if (state().selectedTeamId != teamId) return@launch
                result
                    .onSuccess { projects -> dispatch(Msg.SetProjects(projects)) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetProjects(emptyList()))
                    }
                dispatch(Msg.SetLoadingProjects(false))
            }
        }

        private fun selectChannel(channelId: Long) {
            val prevChannelId = state().selectedChannelId
            if (prevChannelId != null && prevChannelId != channelId) {
                chatSocket.leaveChannel(prevChannelId)
            }
            val channel = state().channels.firstOrNull { it.id == channelId }
            prevChannelId?.let(chatSocket::stopTyping)
            dispatch(Msg.SelectChannel(channelId))
            when (channel?.type) {
                ChannelType.Webhook -> loadWebhooks(channelId)
                ChannelType.MeetingNote -> loadMeetingNotes(channelId)
                else -> {
                    if (channel?.type == ChannelType.Text) chatSocket.joinChannel(channelId)
                    loadMessages(channelId)
                    loadThreads(channelId)
                }
            }
        }

        private fun selectProject(projectId: Long) {
            state().selectedChannelId?.let(chatSocket::leaveChannel)
            dispatch(Msg.SelectProject(projectId))
        }

        private fun loadMessages(channelId: Long) {
            scope.launch {
                dispatch(Msg.SetLoadingMessages(true))
                val result = runCatching {
                    val roots = mutableListOf<ChatMessage>()
                    var before: String? = null
                    var pageCount = 0
                    while (roots.size < ROOT_MESSAGE_PAGE_SIZE && pageCount < MAX_ROOT_MESSAGE_PAGES) {
                        val page = chatRepository.getMessages(
                            channelId = channelId,
                            before = before,
                            limit = MESSAGE_FETCH_PAGE_SIZE,
                        )
                        roots += page.filter { it.parentMessageId == null }
                        if (page.size < MESSAGE_FETCH_PAGE_SIZE) break
                        before = page.lastOrNull()?.id ?: break
                        pageCount++
                    }
                    roots.take(ROOT_MESSAGE_PAGE_SIZE)
                }
                if (state().selectedChannelId != channelId) return@launch
                result
                    .onSuccess { messages -> dispatch(Msg.SetMessages(messages)) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetMessages(emptyList()))
                    }
                dispatch(Msg.SetLoadingMessages(false))
            }
        }

        private fun loadThreads(channelId: Long) {
            scope.launch {
                dispatch(Msg.SetLoadingThreads(true))
                val result = runCatching { threadRepository.getThreads(channelId) }
                if (state().selectedChannelId != channelId) return@launch
                result
                    .onSuccess { threads -> dispatch(Msg.SetThreads(threads)) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetThreads(emptyList()))
                    }
                dispatch(Msg.SetLoadingThreads(false))
            }
        }

        private fun createTeam() {
            val name = state().createTeamName.trim()
            val description = state().createTeamDescription.trim().ifBlank { null }
            val iconBytes = state().createTeamIconBytes
            val iconContentType = state().createTeamIconContentType
            if (name.isBlank() || state().isCreatingTeam) return

            scope.launch {
                dispatch(Msg.SetCreatingTeam(true))
                runCatching {
                    val iconUrl = if (iconBytes != null && iconContentType != null) {
                        teamRepository.uploadTeamIcon(iconBytes, iconContentType)
                    } else null
                    teamRepository.createTeam(name = name, description = description, iconUrl = iconUrl)
                }.onSuccess {
                    dispatch(Msg.ResetCreateTeamForm)
                    loadTeams()
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("팀을 생성하지 못했습니다."))
                    dispatch(Msg.SetCreatingTeam(false))
                }
            }
        }

        private fun createChannel() {
            val teamId = state().selectedTeamId ?: return
            val name = state().createChannelName.trim()
            val description = state().createChannelDescription.trim().ifBlank { null }
            val type = state().createChannelType
            val isPrivate = state().createChannelIsPrivate
            if (name.isBlank() || state().isCreatingChannel) return

            scope.launch {
                dispatch(Msg.SetCreatingChannel(true))
                runCatching {
                    channelRepository.createChannel(
                        teamId = teamId,
                        type = type,
                        name = name,
                        description = description,
                        isPrivate = isPrivate,
                    )
                }.onSuccess { channel ->
                    dispatch(Msg.ResetCreateChannelForm)
                    loadChannels(teamId, preferredChannelId = channel.id)
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("채널을 생성하지 못했습니다."))
                    dispatch(Msg.SetCreatingChannel(false))
                }
            }
        }

        private fun createProject() {
            val teamId = state().selectedTeamId ?: return
            val name = state().createProjectName.trim()
            val description = state().createProjectDescription.trim().ifBlank { null }
            if (name.isBlank() || state().isCreatingProject) return

            scope.launch {
                dispatch(Msg.SetCreatingProject(true))
                runCatching {
                    projectRepository.createProject(teamId = teamId, name = name, description = description)
                }.onSuccess { project ->
                    dispatch(Msg.ResetCreateProjectForm)
                    loadProjects(project.teamId)
                    selectProject(project.id)
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("프로젝트를 생성하지 못했습니다."))
                    dispatch(Msg.SetCreatingProject(false))
                }
            }
        }

        private fun updateStatus(status: UserStatus, expiresInHours: Double?) {
            val accountId = state().accountId ?: return
            if (state().isUpdatingStatus) return
            scope.launch {
                dispatch(Msg.SetUpdatingStatus(true))
                runCatching {
                    preferenceRepository.updateAccountStatus(accountId, status, expiresInHours)
                }.onSuccess {
                    dispatch(Msg.SetAccountStatus(status))
                    dispatch(Msg.SetAccountMenuOpen(false))
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("상태를 변경하지 못했습니다."))
                }
                dispatch(Msg.SetUpdatingStatus(false))
            }
        }

        private fun signOut() {
            scope.launch {
                chatSocket.disconnect()
                authRepository.signOut()
                publish(Label.SignedOut)
            }
        }

        private fun uploadProfileImage(bytes: ByteArray, contentType: String) {
            if (state().isUploadingProfileImage) return
            scope.launch {
                dispatch(Msg.SetUploadingProfileImage(true))
                val result = runCatching { userRepository.uploadProfileImage(bytes, contentType) }
                result.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError(it.message ?: "프로필 사진을 업로드하지 못했습니다."))
                }

                if (result.isSuccess) {
                    val profile = runCatching { userRepository.getMyProfile() }
                        .onFailure { it.handleIfSessionExpired() }
                        .getOrNull()
                    if (profile != null) dispatch(Msg.SetUserProfile(profile))
                }
                dispatch(Msg.SetUploadingProfileImage(false))
            }
        }

        private fun updateAppearance(
            theme: AppTheme = state().accountTheme,
            language: AppLanguage = state().accountLanguage,
            timeFormat: TimeFormat = state().accountTimeFormat,
            dateFormat: DateFormat = state().accountDateFormat,
        ) {
            val accountId = state().accountId ?: return
            dispatch(Msg.SetAccountSettings(
                theme = theme, language = language,
                timeFormat = timeFormat, dateFormat = dateFormat,
                marketingEmail = state().accountMarketingEmail,
            ))
            scope.launch {
                runCatching {
                    preferenceRepository.updateAppearance(accountId, theme, language, timeFormat, dateFormat)
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("설정을 저장하지 못했습니다."))
                }
            }
        }

        private fun updateMarketingEmail(enabled: Boolean) {
            val accountId = state().accountId ?: return
            dispatch(Msg.SetAccountSettings(
                theme = state().accountTheme,
                language = state().accountLanguage,
                timeFormat = state().accountTimeFormat,
                dateFormat = state().accountDateFormat,
                marketingEmail = enabled,
            ))
            scope.launch {
                runCatching {
                    preferenceRepository.updateMarketingEmail(accountId, enabled)
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("설정을 저장하지 못했습니다."))
                }
            }
        }

        private fun loadWebhooks(channelId: Long) {
            scope.launch {
                dispatch(Msg.SetLoadingWebhooks(true))
                val result = runCatching { webhookRepository.getWebhooks(channelId) }
                if (state().selectedChannelId != channelId) return@launch
                result
                    .onSuccess { dispatch(Msg.SetWebhooks(it)) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetWebhooks(emptyList()))
                    }
                dispatch(Msg.SetLoadingWebhooks(false))
            }
        }

        private fun submitAddWebhook() {
            val channelId = state().selectedChannelId ?: return
            val name = state().addWebhookName.trim()
            val isSecure = state().addWebhookIsSecure
            if (name.isBlank() || state().isAddingWebhook) return
            scope.launch {
                dispatch(Msg.SetAddingWebhook(true))
                runCatching { webhookRepository.createWebhook(channelId, name, null, isSecure) }
                    .onSuccess { webhook ->
                        dispatch(Msg.ResetAddWebhookForm)
                        dispatch(Msg.AddWebhook(webhook))
                    }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetError("웹훅을 추가하지 못했습니다."))
                        dispatch(Msg.SetAddingWebhook(false))
                    }
            }
        }

        private fun deleteWebhook(webhookId: Long) {
            val channelId = state().selectedChannelId ?: return
            scope.launch {
                runCatching { webhookRepository.deleteWebhook(channelId, webhookId) }
                    .onSuccess { dispatch(Msg.RemoveWebhook(webhookId)) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetError("웹훅을 삭제하지 못했습니다."))
                    }
            }
        }

        private fun loadMeetingNotes(channelId: Long) {
            scope.launch {
                dispatch(Msg.SetLoadingMeetingNotes(true))
                val result = runCatching {
                    meetingNoteRepository.getNotes(channelId) to meetingNoteRepository.getTemplates(channelId)
                }
                if (state().selectedChannelId != channelId) return@launch
                result.onSuccess { (notes, templates) ->
                    dispatch(Msg.SetMeetingNotes(notes))
                    dispatch(Msg.SetMeetingNoteTemplates(templates))
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetMeetingNotes(emptyList()))
                }
                dispatch(Msg.SetLoadingMeetingNotes(false))
            }
        }

        private fun submitCreateMeetingNote() {
            val channelId = state().selectedChannelId ?: return
            val templateId = state().activeTemplate?.id ?: return
            val title = state().createNoteTitle.trim()
            if (title.isBlank() || state().isCreatingNote) return
            val content = buildNoteContent(state().createNoteSectionContents)
            scope.launch {
                dispatch(Msg.SetCreatingNote(true))
                runCatching { meetingNoteRepository.createNote(channelId, templateId, title, content) }
                    .onSuccess { note ->
                        dispatch(Msg.ResetCreateNoteForm)
                        dispatch(Msg.PrependMeetingNote(note))
                    }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetError("회의록을 작성하지 못했습니다."))
                        dispatch(Msg.SetCreatingNote(false))
                    }
            }
        }

        private fun buildNoteContent(sections: Map<String, String>): String {
            if (sections.isEmpty()) return "{}"
            return kotlinx.serialization.json.buildJsonObject {
                sections.forEach { (k, v) -> put(k, v) }
            }.toString()
        }

        private fun sendChatMessage() {
            val channelId = state().selectedChannelId ?: return
            val teamId = state().selectedTeamId ?: return
            val content = state().chatDraft.trim()
            if (content.isBlank()) return
            val tempId = "optimistic-${System.currentTimeMillis()}"
            val optimistic = ChatMessage(
                id = tempId,
                teamId = teamId,
                projectId = null,
                channelId = channelId,
                authorId = state().accountId ?: -1,
                content = content,
                parentMessageId = null,
                type = com.cowork.desktop.client.domain.model.MessageType.Text,
                fileUrl = null,
                fileName = null,
                fileSize = null,
                createdAt = null,
            )
            dispatch(Msg.SetChatDraft(""))
            chatSocket.stopTyping(channelId)
            dispatch(Msg.PrependMessage(optimistic))
            scope.launch {
                runCatching { chatRepository.sendMessage(channelId, teamId, content, clientMessageId = tempId) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetChatDraft(content))
                        dispatch(Msg.RemoveOptimisticMessage(tempId))
                    }
            }
        }

        private fun updateChatDraft(draft: String) {
            dispatch(Msg.SetChatDraft(draft))
            val channelId = state().selectedChannelId ?: return
            typingStopJob?.cancel()
            if (draft.isBlank()) {
                chatSocket.stopTyping(channelId)
                return
            }
            chatSocket.startTyping(channelId)
            typingStopJob = scope.launch {
                delay(2_500)
                chatSocket.stopTyping(channelId)
            }
        }

        private fun submitEditMessage() {
            val messageId = state().editingMessageId ?: return
            val channelId = state().selectedChannelId ?: return
            val content = state().editingMessageContent.trim()
            if (content.isBlank()) return
            dispatch(Msg.SetEditingMessage(null, ""))
            dispatch(Msg.UpdateMessageContent(messageId, content))
            scope.launch {
                runCatching { chatRepository.editMessage(channelId, messageId, content) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetEditingMessage(messageId, content))
                    }
            }
        }

        private fun deleteMessage(messageId: String) {
            val channelId = state().selectedChannelId ?: return
            dispatch(Msg.RemoveMessage(messageId))
            scope.launch {
                runCatching { chatRepository.deleteMessage(channelId, messageId) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                    }
            }
        }

        private fun reorderChannels(fromIndex: Int, toIndex: Int) {
            val currentState = state()
            val teamId = currentState.selectedTeamId ?: return
            if (fromIndex !in currentState.channels.indices ||
                toIndex !in currentState.channels.indices ||
                fromIndex == toIndex
            ) return
            val orderedIds = currentState.channels.moveItem(fromIndex, toIndex).map { it.id }
            dispatch(Msg.ReorderChannels(fromIndex, toIndex))
            scope.launch {
                runCatching { channelRepository.reorderChannels(teamId, orderedIds) }
                    .onSuccess {
                        if (state().selectedTeamId == teamId) {
                            dispatch(Msg.SetChannels(it, state().selectedChannelId))
                        }
                    }
                    .onFailure { if (it.handleIfSessionExpired()) return@launch }
            }
        }

        private fun reorderProjects(fromIndex: Int, toIndex: Int) {
            val currentState = state()
            val teamId = currentState.selectedTeamId ?: return
            if (fromIndex !in currentState.projects.indices ||
                toIndex !in currentState.projects.indices ||
                fromIndex == toIndex
            ) return
            val orderedIds = currentState.projects.moveItem(fromIndex, toIndex).map { it.id }
            dispatch(Msg.ReorderProjects(fromIndex, toIndex))
            scope.launch {
                runCatching { projectRepository.reorderProjects(teamId, orderedIds) }
                    .onSuccess {
                        if (state().selectedTeamId == teamId) dispatch(Msg.SetProjects(it))
                    }
                    .onFailure { if (it.handleIfSessionExpired()) return@launch }
            }
        }

    }

    private sealed interface Msg {
        data class SetTeams(val teams: List<TeamSummary>, val selectedTeamId: Long?) : Msg
        data class SelectTeam(val teamId: Long) : Msg
        data class SelectChannel(val channelId: Long) : Msg
        data class SelectProject(val projectId: Long) : Msg
        data class SetChannels(val channels: List<Channel>, val selectedChannelId: Long? = null) : Msg
        data class SetMessages(val messages: List<ChatMessage>) : Msg
        data class SetThreads(val threads: List<Thread>) : Msg
        data class SetProjects(val projects: List<Project>) : Msg
        data class SetLoadingTeams(val isLoading: Boolean) : Msg
        data class SetLoadingChannels(val isLoading: Boolean) : Msg
        data class SetLoadingMessages(val isLoading: Boolean) : Msg
        data class SetLoadingThreads(val isLoading: Boolean) : Msg
        data class SetLoadingProjects(val isLoading: Boolean) : Msg
        data class SetCreateTeamOpen(val isOpen: Boolean) : Msg
        data class SetCreateTeamName(val name: String) : Msg
        data class SetCreateTeamDescription(val description: String) : Msg
        data class SetCreateTeamIcon(val bytes: ByteArray, val contentType: String) : Msg
        data class SetCreatingTeam(val isCreating: Boolean) : Msg
        data class SetCreateChannelOpen(val isOpen: Boolean) : Msg
        data class SetCreateChannelName(val name: String) : Msg
        data class SetCreateChannelDescription(val description: String) : Msg
        data class SetCreateChannelType(val type: ChannelType) : Msg
        data class SetCreateChannelPrivate(val isPrivate: Boolean) : Msg
        data class SetCreatingChannel(val isCreating: Boolean) : Msg
        data class SetCreateProjectOpen(val isOpen: Boolean) : Msg
        data class SetCreateProjectName(val name: String) : Msg
        data class SetCreateProjectDescription(val description: String) : Msg
        data class SetCreatingProject(val isCreating: Boolean) : Msg
        data class SetError(val error: String?) : Msg
        data object ResetCreateTeamForm : Msg
        data object ResetCreateChannelForm : Msg
        data object ResetCreateProjectForm : Msg
        data class SetAccountInfo(val accountId: Long?, val email: String?, val role: String?) : Msg
        data class SetUserProfile(val profile: com.cowork.desktop.client.domain.model.UserProfile) : Msg
        data class SetAccountStatus(val status: UserStatus) : Msg
        data class SetAccountMenuOpen(val isOpen: Boolean) : Msg
        data class SetSettingsOpen(val isOpen: Boolean) : Msg
        data class SetUpdatingStatus(val isUpdating: Boolean) : Msg
        data class SetUploadingProfileImage(val isUploading: Boolean) : Msg
        data class SetAccountSettings(
            val theme: AppTheme,
            val language: AppLanguage,
            val timeFormat: TimeFormat,
            val dateFormat: DateFormat,
            val marketingEmail: Boolean,
        ) : Msg
        data class SetUpdatingSettings(val isUpdating: Boolean) : Msg
        data class SetWebhooks(val webhooks: List<Webhook>) : Msg
        data class SetLoadingWebhooks(val isLoading: Boolean) : Msg
        data class SetAddWebhookOpen(val isOpen: Boolean) : Msg
        data class SetAddWebhookName(val name: String) : Msg
        data class SetAddWebhookSecure(val isSecure: Boolean) : Msg
        data class SetAddingWebhook(val isAdding: Boolean) : Msg
        data class AddWebhook(val webhook: Webhook) : Msg
        data class RemoveWebhook(val webhookId: Long) : Msg
        data object ResetAddWebhookForm : Msg
        data class ReorderChannels(val fromIndex: Int, val toIndex: Int) : Msg
        data class ReorderProjects(val fromIndex: Int, val toIndex: Int) : Msg
        data class PrependMessage(val message: ChatMessage) : Msg
        data class RemoveOptimisticMessage(val tempId: String) : Msg
        data class SetMemberProfiles(val profiles: Map<Long, com.cowork.desktop.client.domain.model.UserProfile>) : Msg
        data class SetChatDraft(val draft: String) : Msg
        data class SetMeetingNotes(val notes: List<MeetingNote>) : Msg
        data class SetMeetingNoteTemplates(val templates: List<MeetingNoteTemplate>) : Msg
        data class SetLoadingMeetingNotes(val isLoading: Boolean) : Msg
        data class PrependMeetingNote(val note: MeetingNote) : Msg
        data class SelectMeetingNote(val noteId: Long?) : Msg
        data class SetCreateNoteOpen(val isOpen: Boolean) : Msg
        data class SetCreateNoteTitle(val title: String) : Msg
        data class SetCreateNoteSectionContent(val sectionTitle: String, val content: String) : Msg
        data class SetCreatingNote(val isCreating: Boolean) : Msg
        data object ResetCreateNoteForm : Msg
        data class SetThreadListOpen(val isOpen: Boolean) : Msg
        data class SetSelectedThread(val threadId: Long?) : Msg
        data class SetEditingMessage(val messageId: String?, val content: String) : Msg
        data class SetEditingContent(val content: String) : Msg
        data class UpdateMessageContent(val messageId: String, val content: String) : Msg
        data class RemoveMessage(val messageId: String) : Msg
        data class SetTyping(val userId: Long, val isTyping: Boolean) : Msg
        data class SetUnreadCount(val channelId: Long, val count: Int) : Msg
    }

    private object Reducer : com.arkivanov.mvikotlin.core.store.Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.SetTeams -> copy(
                teams = msg.teams,
                selectedTeamId = msg.selectedTeamId,
                error = null,
            )
            is Msg.SelectTeam -> copy(
                selectedTeamId = msg.teamId,
                channels = emptyList(),
                selectedChannelId = null,
                messages = emptyList(),
                threads = emptyList(),
                projects = emptyList(),
                selectedProjectId = null,
                unreadCounts = emptyMap(),
                typingUserIds = emptySet(),
                chatDraft = "",
                error = null,
            )
            is Msg.SelectChannel -> copy(
                selectedChannelId = msg.channelId,
                selectedProjectId = null,
                unreadCounts = unreadCounts + (msg.channelId to 0),
                typingUserIds = emptySet(),
                messages = emptyList(),
                threads = emptyList(),
                webhooks = emptyList(),
                meetingNotes = emptyList(),
                meetingNoteTemplates = emptyList(),
                selectedMeetingNoteId = null,
                chatDraft = "",
            )
            is Msg.SelectProject -> reduceProjectSelection(msg.projectId)
            is Msg.SetChannels -> copy(
                channels = msg.channels,
                selectedChannelId = msg.selectedChannelId,
                messages = if (msg.selectedChannelId == null) emptyList() else messages,
            )
            is Msg.SetMessages -> copy(messages = msg.messages)
            is Msg.SetThreads -> copy(threads = msg.threads)
            is Msg.SetProjects -> copy(projects = msg.projects)
            is Msg.SetLoadingTeams -> copy(isLoadingTeams = msg.isLoading)
            is Msg.SetLoadingChannels -> copy(isLoadingChannels = msg.isLoading)
            is Msg.SetLoadingMessages -> copy(isLoadingMessages = msg.isLoading)
            is Msg.SetLoadingThreads -> copy(isLoadingThreads = msg.isLoading)
            is Msg.SetLoadingProjects -> copy(isLoadingProjects = msg.isLoading)
            is Msg.SetCreateTeamOpen -> copy(isCreateTeamOpen = msg.isOpen, error = null)
            is Msg.SetCreateTeamName -> copy(createTeamName = msg.name)
            is Msg.SetCreateTeamDescription -> copy(createTeamDescription = msg.description)
            is Msg.SetCreateTeamIcon -> copy(createTeamIconBytes = msg.bytes, createTeamIconContentType = msg.contentType)
            is Msg.SetCreatingTeam -> copy(isCreatingTeam = msg.isCreating)
            is Msg.SetCreateChannelOpen -> copy(isCreateChannelOpen = msg.isOpen, error = null)
            is Msg.SetCreateChannelName -> copy(createChannelName = msg.name)
            is Msg.SetCreateChannelDescription -> copy(createChannelDescription = msg.description)
            is Msg.SetCreateChannelType -> copy(createChannelType = msg.type)
            is Msg.SetCreateChannelPrivate -> copy(createChannelIsPrivate = msg.isPrivate)
            is Msg.SetCreatingChannel -> copy(isCreatingChannel = msg.isCreating)
            is Msg.SetCreateProjectOpen -> copy(isCreateProjectOpen = msg.isOpen, error = null)
            is Msg.SetCreateProjectName -> copy(createProjectName = msg.name)
            is Msg.SetCreateProjectDescription -> copy(createProjectDescription = msg.description)
            is Msg.SetCreatingProject -> copy(isCreatingProject = msg.isCreating)
            is Msg.SetError -> copy(error = msg.error)
            Msg.ResetCreateTeamForm -> copy(
                isCreateTeamOpen = false,
                createTeamName = "",
                createTeamDescription = "",
                createTeamIconBytes = null,
                createTeamIconContentType = null,
                isCreatingTeam = false,
            )
            Msg.ResetCreateChannelForm -> copy(
                isCreateChannelOpen = false,
                createChannelName = "",
                createChannelDescription = "",
                createChannelType = ChannelType.Text,
                createChannelIsPrivate = false,
                isCreatingChannel = false,
            )
            Msg.ResetCreateProjectForm -> copy(
                isCreateProjectOpen = false,
                createProjectName = "",
                createProjectDescription = "",
                isCreatingProject = false,
            )
            is Msg.SetAccountInfo -> copy(accountId = msg.accountId, accountEmail = msg.email, accountSystemRole = msg.role)
            is Msg.SetUserProfile -> copy(
                accountName = msg.profile.name,
                accountEmail = msg.profile.email.ifBlank { accountEmail },
                accountNickname = msg.profile.nickname,
                accountProfileImageUrl = msg.profile.profileImageUrl,
                accountGithub = msg.profile.github,
                accountStudentNumber = msg.profile.studentNumber,
                accountMajor = msg.profile.major,
                accountStudentRole = msg.profile.studentRole,
                accountDescription = msg.profile.description,
                accountRoles = msg.profile.roles,
                accountStatusMessage = msg.profile.statusMessage,
                accountStatusExpiresAt = msg.profile.statusExpiresAt,
            )
            is Msg.SetAccountStatus -> copy(accountStatus = msg.status)
            is Msg.SetAccountMenuOpen -> copy(isAccountMenuOpen = msg.isOpen)
            is Msg.SetSettingsOpen -> copy(isSettingsOpen = msg.isOpen, isAccountMenuOpen = false)
            is Msg.SetUpdatingStatus -> copy(isUpdatingStatus = msg.isUpdating)
            is Msg.SetUploadingProfileImage -> copy(isUploadingProfileImage = msg.isUploading)
            is Msg.SetAccountSettings -> copy(
                accountTheme = msg.theme,
                accountLanguage = msg.language,
                accountTimeFormat = msg.timeFormat,
                accountDateFormat = msg.dateFormat,
                accountMarketingEmail = msg.marketingEmail,
            )
            is Msg.SetUpdatingSettings -> copy(isUpdatingSettings = msg.isUpdating)
            is Msg.SetWebhooks -> copy(webhooks = msg.webhooks)
            is Msg.SetLoadingWebhooks -> copy(isLoadingWebhooks = msg.isLoading)
            is Msg.SetAddWebhookOpen -> copy(isAddWebhookOpen = msg.isOpen)
            is Msg.SetAddWebhookName -> copy(addWebhookName = msg.name)
            is Msg.SetAddWebhookSecure -> copy(addWebhookIsSecure = msg.isSecure)
            is Msg.SetAddingWebhook -> copy(isAddingWebhook = msg.isAdding)
            is Msg.AddWebhook -> copy(webhooks = webhooks + msg.webhook)
            is Msg.RemoveWebhook -> copy(webhooks = webhooks.filter { it.id != msg.webhookId })
            Msg.ResetAddWebhookForm -> copy(
                isAddWebhookOpen = false,
                addWebhookName = "",
                addWebhookIsSecure = false,
                isAddingWebhook = false,
            )
            is Msg.ReorderChannels -> {
                copy(channels = channels.moveItem(msg.fromIndex, msg.toIndex))
            }
            is Msg.ReorderProjects -> {
                copy(projects = projects.moveItem(msg.fromIndex, msg.toIndex))
            }
            is Msg.PrependMessage -> {
                if (msg.message.channelId != selectedChannelId || msg.message.parentMessageId != null) this
                else {
                    // 소켓으로 실제 메시지가 오면 같은 authorId+content의 옵티미스틱 메시지 제거
                    val withoutOptimistic = if (!msg.message.id.startsWith("optimistic-")) {
                        messages.filterNot { optimistic ->
                            optimistic.id.startsWith("optimistic-") &&
                                if (msg.message.clientMessageId != null) {
                                    optimistic.clientMessageId == msg.message.clientMessageId
                                } else {
                                    optimistic.authorId == msg.message.authorId && optimistic.content == msg.message.content
                                }
                        }
                    } else messages
                    copy(messages = listOf(msg.message) + withoutOptimistic)
                }
            }
            is Msg.RemoveOptimisticMessage -> copy(messages = messages.filterNot { it.id == msg.tempId })
            is Msg.SetMemberProfiles -> copy(memberProfiles = memberProfiles + msg.profiles)
            is Msg.SetChatDraft -> copy(chatDraft = msg.draft)
            is Msg.SetMeetingNotes -> copy(meetingNotes = msg.notes)
            is Msg.SetMeetingNoteTemplates -> copy(meetingNoteTemplates = msg.templates)
            is Msg.SetLoadingMeetingNotes -> copy(isLoadingMeetingNotes = msg.isLoading)
            is Msg.PrependMeetingNote -> copy(meetingNotes = listOf(msg.note) + meetingNotes)
            is Msg.SelectMeetingNote -> copy(selectedMeetingNoteId = msg.noteId)
            is Msg.SetCreateNoteOpen -> copy(isCreateMeetingNoteOpen = msg.isOpen, error = null)
            is Msg.SetCreateNoteTitle -> copy(createNoteTitle = msg.title)
            is Msg.SetCreateNoteSectionContent -> copy(
                createNoteSectionContents = createNoteSectionContents + (msg.sectionTitle to msg.content)
            )
            is Msg.SetCreatingNote -> copy(isCreatingNote = msg.isCreating)
            Msg.ResetCreateNoteForm -> copy(
                isCreateMeetingNoteOpen = false,
                createNoteTitle = "",
                createNoteSectionContents = emptyMap(),
                isCreatingNote = false,
            )
            is Msg.SetThreadListOpen -> copy(isThreadListOpen = msg.isOpen, selectedThreadId = if (msg.isOpen) null else selectedThreadId)
            is Msg.SetSelectedThread -> copy(selectedThreadId = msg.threadId, isThreadListOpen = false)
            is Msg.SetEditingMessage -> copy(editingMessageId = msg.messageId, editingMessageContent = msg.content)
            is Msg.SetEditingContent -> copy(editingMessageContent = msg.content)
            is Msg.UpdateMessageContent -> copy(
                messages = messages.map { if (it.id == msg.messageId) it.copy(content = msg.content) else it }
            )
            is Msg.RemoveMessage -> copy(messages = messages.filterNot { it.id == msg.messageId })
            is Msg.SetTyping -> copy(
                typingUserIds = if (msg.isTyping) typingUserIds + msg.userId else typingUserIds - msg.userId,
            )
            is Msg.SetUnreadCount -> copy(unreadCounts = unreadCounts + (msg.channelId to msg.count))
        }
    }
}
