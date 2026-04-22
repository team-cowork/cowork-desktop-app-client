package com.cowork.app_client.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.cowork.app_client.data.local.LayoutPreferenceStorage
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.data.repository.ChannelRepository
import com.cowork.app_client.data.repository.ChatRepository
import com.cowork.app_client.data.repository.PreferenceRepository
import com.cowork.app_client.data.repository.TeamRepository
import com.cowork.app_client.data.repository.UserRepository
import com.cowork.app_client.feature.auth.OAuthLauncher
import com.cowork.app_client.feature.auth.component.DefaultAuthComponent
import com.cowork.app_client.feature.main.component.DefaultMainComponent
import com.cowork.app_client.navigation.RootComponent.Child
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
    private val teamRepository: TeamRepository,
    private val channelRepository: ChannelRepository,
    private val chatRepository: ChatRepository,
    private val preferenceRepository: PreferenceRepository,
    private val userRepository: UserRepository,
    private val layoutPreferenceStorage: LayoutPreferenceStorage,
    private val oAuthLauncher: OAuthLauncher,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Auth,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    override val childStack: Value<ChildStack<*, Child>> = stack

    private fun createChild(config: Config, context: ComponentContext): Child =
        when (config) {
            Config.Auth -> Child.Auth(
                DefaultAuthComponent(
                    componentContext = context,
                    storeFactory = storeFactory,
                    authRepository = authRepository,
                    oAuthLauncher = oAuthLauncher,
                    onAuthenticated = { navigation.replaceAll(Config.Main) },
                )
            )
            Config.Main -> Child.Main(
                DefaultMainComponent(
                    componentContext = context,
                    storeFactory = storeFactory,
                    authRepository = authRepository,
                    teamRepository = teamRepository,
                    channelRepository = channelRepository,
                    chatRepository = chatRepository,
                    preferenceRepository = preferenceRepository,
                    userRepository = userRepository,
                    layoutPreferenceStorage = layoutPreferenceStorage,
                    onSignedOut = { navigation.replaceAll(Config.Auth) },
                )
            )
        }

    @Serializable
    private sealed interface Config {
        @Serializable data object Auth : Config
        @Serializable data object Main : Config
    }
}
