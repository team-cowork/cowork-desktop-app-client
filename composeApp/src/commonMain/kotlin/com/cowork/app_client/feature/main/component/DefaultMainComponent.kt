package com.cowork.app_client.feature.main.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cowork.app_client.data.local.LayoutPreferenceStorage
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.data.repository.ChannelRepository
import com.cowork.app_client.data.repository.ChatRepository
import com.cowork.app_client.data.repository.PreferenceRepository
import com.cowork.app_client.data.repository.TeamRepository
import com.cowork.app_client.data.repository.UserRepository
import com.cowork.app_client.domain.model.ChannelType
import com.cowork.app_client.domain.model.UserStatus
import com.cowork.app_client.feature.main.store.MainStore
import com.cowork.app_client.feature.main.store.MainStoreFactory
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
    override fun onCreateTeamSubmit() = store.accept(MainStore.Intent.SubmitCreateTeam)
    override fun onCreateChannelClick() = store.accept(MainStore.Intent.OpenCreateChannel)
    override fun onCreateChannelDismiss() = store.accept(MainStore.Intent.CloseCreateChannel)
    override fun onCreateChannelNameChange(name: String) = store.accept(MainStore.Intent.ChangeCreateChannelName(name))
    override fun onCreateChannelNoticeChange(notice: String) = store.accept(MainStore.Intent.ChangeCreateChannelNotice(notice))
    override fun onCreateChannelTypeChange(type: ChannelType) = store.accept(MainStore.Intent.ChangeCreateChannelType(type))
    override fun onCreateChannelSubmit() = store.accept(MainStore.Intent.SubmitCreateChannel)
    override fun onAccountMenuClick() = store.accept(MainStore.Intent.ToggleAccountMenu)
    override fun onAccountMenuDismiss() = store.accept(MainStore.Intent.CloseAccountMenu)
    override fun onSettingsClick() = store.accept(MainStore.Intent.OpenSettings)
    override fun onSettingsDismiss() = store.accept(MainStore.Intent.CloseSettings)
    override fun onStatusChange(status: UserStatus, expiresInHours: Double?) = store.accept(MainStore.Intent.SetStatus(status, expiresInHours))
    override fun onSignOutClick() = store.accept(MainStore.Intent.SignOut)
    override fun onUploadProfileImage(bytes: ByteArray, contentType: String) =
        store.accept(MainStore.Intent.UploadProfileImage(bytes, contentType))
    override fun onReloadClick() = store.accept(MainStore.Intent.Reload)
}
