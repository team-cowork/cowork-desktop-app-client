package com.cowork.app_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.data.repository.ChannelRepository
import com.cowork.app_client.data.repository.ChatRepository
import com.cowork.app_client.data.repository.TeamRepository
import com.cowork.app_client.di.androidModule
import com.cowork.app_client.di.commonModule
import com.cowork.app_client.feature.auth.OAuthLauncher
import com.cowork.app_client.navigation.DefaultRootComponent
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(applicationContext)
            modules(commonModule, androidModule)
        }

        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            storeFactory = DefaultStoreFactory(),
            authRepository = get<AuthRepository>(),
            teamRepository = get<TeamRepository>(),
            channelRepository = get<ChannelRepository>(),
            chatRepository = get<ChatRepository>(),
            oAuthLauncher = get<OAuthLauncher>(),
        )

        setContent {
            App(root)
        }
    }
}
