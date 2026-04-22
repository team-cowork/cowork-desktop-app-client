package com.cowork.app_client.feature.main.store

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.data.repository.ChannelRepository
import com.cowork.app_client.data.repository.ChatRepository
import com.cowork.app_client.data.repository.PreferenceRepository
import com.cowork.app_client.data.repository.TeamRepository
import com.cowork.app_client.data.repository.UserRepository
import com.cowork.app_client.domain.model.Channel
import com.cowork.app_client.domain.model.ChannelType
import com.cowork.app_client.domain.model.ChatMessage
import com.cowork.app_client.domain.model.TeamSummary
import com.cowork.app_client.domain.model.UserStatus
import com.cowork.app_client.feature.main.store.MainStore.Intent
import com.cowork.app_client.feature.main.store.MainStore.Label
import com.cowork.app_client.feature.main.store.MainStore.State
import com.cowork.app_client.util.parseJwtClaims
import kotlinx.coroutines.launch

class MainStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
    private val teamRepository: TeamRepository,
    private val channelRepository: ChannelRepository,
    private val chatRepository: ChatRepository,
    private val preferenceRepository: PreferenceRepository,
    private val userRepository: UserRepository,
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

        override fun executeAction(action: Action) {
            when (action) {
                Action.Init -> init()
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Reload -> loadTeams()
                is Intent.SelectTeam -> selectTeam(intent.teamId)
                is Intent.SelectChannel -> selectChannel(intent.channelId)
                Intent.OpenCreateTeam -> dispatch(Msg.SetCreateTeamOpen(true))
                Intent.CloseCreateTeam -> dispatch(Msg.ResetCreateTeamForm)
                is Intent.ChangeCreateTeamName -> dispatch(Msg.SetCreateTeamName(intent.name))
                is Intent.ChangeCreateTeamDescription -> dispatch(Msg.SetCreateTeamDescription(intent.description))
                Intent.SubmitCreateTeam -> createTeam()
                Intent.OpenCreateChannel -> dispatch(Msg.SetCreateChannelOpen(true))
                Intent.CloseCreateChannel -> dispatch(Msg.ResetCreateChannelForm)
                is Intent.ChangeCreateChannelName -> dispatch(Msg.SetCreateChannelName(intent.name))
                is Intent.ChangeCreateChannelNotice -> dispatch(Msg.SetCreateChannelNotice(intent.notice))
                is Intent.ChangeCreateChannelType -> dispatch(Msg.SetCreateChannelType(intent.type))
                Intent.SubmitCreateChannel -> createChannel()
                Intent.OpenAccountMenu -> dispatch(Msg.SetAccountMenuOpen(true))
                Intent.ToggleAccountMenu -> dispatch(Msg.SetAccountMenuOpen(!state().isAccountMenuOpen))
                Intent.CloseAccountMenu -> dispatch(Msg.SetAccountMenuOpen(false))
                is Intent.SetStatus -> updateStatus(intent.status, intent.expiresInHours)
                Intent.SignOut -> signOut()
            }
        }

        private fun init() {
            scope.launch {
                val tokens = authRepository.getStoredTokens()
                if (tokens != null) {
                    val claims = parseJwtClaims(tokens.accessToken)
                    dispatch(Msg.SetAccountInfo(claims.accountId, claims.email))
                    if (claims.accountId != null) {
                        val status = runCatching {
                            preferenceRepository.getAccountStatus(claims.accountId)
                        }.getOrDefault(UserStatus.Online)
                        dispatch(Msg.SetAccountStatus(status))
                    }
                }
            }
            scope.launch {
                val profile = runCatching { userRepository.getMyProfile() }.getOrNull()
                if (profile != null) {
                    dispatch(Msg.SetUserProfile(profile))
                }
            }
            loadTeams()
        }

        private fun loadTeams() {
            scope.launch {
                dispatch(Msg.SetLoadingTeams(true))
                runCatching { teamRepository.getMyTeams() }
                    .onSuccess { teams ->
                        val selectedTeamId = teams.firstOrNull()?.id
                        dispatch(Msg.SetTeams(teams, selectedTeamId))
                        if (selectedTeamId != null) {
                            loadChannels(selectedTeamId)
                        }
                    }
                    .onFailure {
                        dispatch(Msg.SetError("팀 목록을 불러오지 못했습니다."))
                    }
                dispatch(Msg.SetLoadingTeams(false))
            }
        }

        private fun selectTeam(teamId: Long) {
            dispatch(Msg.SelectTeam(teamId))
            loadChannels(teamId)
        }

        private fun loadChannels(teamId: Long) {
            scope.launch {
                dispatch(Msg.SetLoadingChannels(true))
                runCatching { channelRepository.getTeamChannels(teamId) }
                    .onSuccess { channels ->
                        val selectedChannelId = channels.firstOrNull()?.id
                        dispatch(Msg.SetChannels(channels, selectedChannelId))
                        if (selectedChannelId != null) {
                            loadMessages(selectedChannelId)
                        }
                    }
                    .onFailure { dispatch(Msg.SetChannels(emptyList())) }
                dispatch(Msg.SetLoadingChannels(false))
            }
        }

        private fun selectChannel(channelId: Long) {
            dispatch(Msg.SelectChannel(channelId))
            loadMessages(channelId)
        }

        private fun loadMessages(channelId: Long) {
            scope.launch {
                dispatch(Msg.SetLoadingMessages(true))
                runCatching { chatRepository.getMessages(channelId) }
                    .onSuccess { messages -> dispatch(Msg.SetMessages(messages)) }
                    .onFailure { dispatch(Msg.SetMessages(emptyList())) }
                dispatch(Msg.SetLoadingMessages(false))
            }
        }

        private fun createTeam() {
            val name = state().createTeamName.trim()
            val description = state().createTeamDescription.trim().ifBlank { null }
            if (name.isBlank() || state().isCreatingTeam) return

            scope.launch {
                dispatch(Msg.SetCreatingTeam(true))
                runCatching {
                    teamRepository.createTeam(
                        name = name,
                        description = description,
                        iconUrl = null,
                    )
                }.onSuccess {
                    dispatch(Msg.ResetCreateTeamForm)
                    loadTeams()
                }.onFailure {
                    dispatch(Msg.SetError("팀을 생성하지 못했습니다."))
                    dispatch(Msg.SetCreatingTeam(false))
                }
            }
        }

        private fun createChannel() {
            val teamId = state().selectedTeamId ?: return
            val name = state().createChannelName.trim()
            val notice = state().createChannelNotice.trim().ifBlank { null }
            val type = state().createChannelType
            if (name.isBlank() || state().isCreatingChannel) return

            scope.launch {
                dispatch(Msg.SetCreatingChannel(true))
                runCatching {
                    channelRepository.createChannel(
                        teamId = teamId,
                        type = type,
                        name = name,
                        notice = notice,
                        projectId = null,
                    )
                }.onSuccess { channel ->
                    dispatch(Msg.ResetCreateChannelForm)
                    loadChannels(channel.teamId)
                    selectChannel(channel.id)
                }.onFailure {
                    dispatch(Msg.SetError("채널을 생성하지 못했습니다. 서버의 cowork-channel API가 필요합니다."))
                    dispatch(Msg.SetCreatingChannel(false))
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
                    dispatch(Msg.SetError("상태를 변경하지 못했습니다."))
                }
                dispatch(Msg.SetUpdatingStatus(false))
            }
        }

        private fun signOut() {
            scope.launch {
                authRepository.signOut()
                publish(Label.SignedOut)
            }
        }
    }

    private sealed interface Msg {
        data class SetTeams(val teams: List<TeamSummary>, val selectedTeamId: Long?) : Msg
        data class SelectTeam(val teamId: Long) : Msg
        data class SelectChannel(val channelId: Long) : Msg
        data class SetChannels(val channels: List<Channel>, val selectedChannelId: Long? = null) : Msg
        data class SetMessages(val messages: List<ChatMessage>) : Msg
        data class SetLoadingTeams(val isLoading: Boolean) : Msg
        data class SetLoadingChannels(val isLoading: Boolean) : Msg
        data class SetLoadingMessages(val isLoading: Boolean) : Msg
        data class SetCreateTeamOpen(val isOpen: Boolean) : Msg
        data class SetCreateTeamName(val name: String) : Msg
        data class SetCreateTeamDescription(val description: String) : Msg
        data class SetCreatingTeam(val isCreating: Boolean) : Msg
        data class SetCreateChannelOpen(val isOpen: Boolean) : Msg
        data class SetCreateChannelName(val name: String) : Msg
        data class SetCreateChannelNotice(val notice: String) : Msg
        data class SetCreateChannelType(val type: ChannelType) : Msg
        data class SetCreatingChannel(val isCreating: Boolean) : Msg
        data class SetError(val error: String?) : Msg
        data object ResetCreateTeamForm : Msg
        data object ResetCreateChannelForm : Msg
        data class SetAccountInfo(val accountId: Long?, val email: String?) : Msg
        data class SetUserProfile(val profile: com.cowork.app_client.domain.model.UserProfile) : Msg
        data class SetAccountStatus(val status: UserStatus) : Msg
        data class SetAccountMenuOpen(val isOpen: Boolean) : Msg
        data class SetUpdatingStatus(val isUpdating: Boolean) : Msg
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
                error = null,
            )
            is Msg.SelectChannel -> copy(selectedChannelId = msg.channelId, messages = emptyList())
            is Msg.SetChannels -> copy(
                channels = msg.channels,
                selectedChannelId = msg.selectedChannelId,
                messages = if (msg.selectedChannelId == null) emptyList() else messages,
            )
            is Msg.SetMessages -> copy(messages = msg.messages)
            is Msg.SetLoadingTeams -> copy(isLoadingTeams = msg.isLoading)
            is Msg.SetLoadingChannels -> copy(isLoadingChannels = msg.isLoading)
            is Msg.SetLoadingMessages -> copy(isLoadingMessages = msg.isLoading)
            is Msg.SetCreateTeamOpen -> copy(isCreateTeamOpen = msg.isOpen, error = null)
            is Msg.SetCreateTeamName -> copy(createTeamName = msg.name)
            is Msg.SetCreateTeamDescription -> copy(createTeamDescription = msg.description)
            is Msg.SetCreatingTeam -> copy(isCreatingTeam = msg.isCreating)
            is Msg.SetCreateChannelOpen -> copy(isCreateChannelOpen = msg.isOpen, error = null)
            is Msg.SetCreateChannelName -> copy(createChannelName = msg.name)
            is Msg.SetCreateChannelNotice -> copy(createChannelNotice = msg.notice)
            is Msg.SetCreateChannelType -> copy(createChannelType = msg.type)
            is Msg.SetCreatingChannel -> copy(isCreatingChannel = msg.isCreating)
            is Msg.SetError -> copy(error = msg.error)
            Msg.ResetCreateTeamForm -> copy(
                isCreateTeamOpen = false,
                createTeamName = "",
                createTeamDescription = "",
                isCreatingTeam = false,
            )
            Msg.ResetCreateChannelForm -> copy(
                isCreateChannelOpen = false,
                createChannelName = "",
                createChannelNotice = "",
                createChannelType = ChannelType.Text,
                isCreatingChannel = false,
            )
            is Msg.SetAccountInfo -> copy(accountId = msg.accountId, accountEmail = msg.email)
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
            )
            is Msg.SetAccountStatus -> copy(accountStatus = msg.status)
            is Msg.SetAccountMenuOpen -> copy(isAccountMenuOpen = msg.isOpen)
            is Msg.SetUpdatingStatus -> copy(isUpdatingStatus = msg.isUpdating)
        }
    }
}
