package com.cowork.app_client

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.data.repository.ChannelRepository
import com.cowork.app_client.data.repository.ChatRepository
import com.cowork.app_client.data.repository.TeamRepository
import com.cowork.app_client.di.commonModule
import com.cowork.app_client.di.jvmModule
import com.cowork.app_client.feature.auth.OAuthLauncher
import com.cowork.app_client.navigation.DefaultRootComponent
import org.koin.core.context.startKoin

fun main() {
    val koin = startKoin {
        modules(commonModule, jvmModule)
    }.koin

    val lifecycle = LifecycleRegistry()
    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
        storeFactory = DefaultStoreFactory(),
        authRepository = koin.get<AuthRepository>(),
        teamRepository = koin.get<TeamRepository>(),
        channelRepository = koin.get<ChannelRepository>(),
        chatRepository = koin.get<ChatRepository>(),
        oAuthLauncher = koin.get<OAuthLauncher>(),
    )

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "cowork",
        ) {
            App(root)
        }
    }
}
