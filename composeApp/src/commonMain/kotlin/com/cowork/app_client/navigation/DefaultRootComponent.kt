package com.cowork.app_client.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.feature.auth.OAuthLauncher
import com.cowork.app_client.feature.auth.component.DefaultAuthComponent
import com.cowork.app_client.navigation.RootComponent.Child
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
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
            Config.Main -> Child.Main
        }

    @Serializable
    private sealed interface Config {
        @Serializable data object Auth : Config
        @Serializable data object Main : Config
    }
}
