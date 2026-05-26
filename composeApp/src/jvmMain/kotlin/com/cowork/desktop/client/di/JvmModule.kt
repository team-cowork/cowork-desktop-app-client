package com.cowork.desktop.client.di

import com.cowork.desktop.client.data.local.JvmLayoutPreferenceStorage
import com.cowork.desktop.client.data.local.JvmTokenStorage
import com.cowork.desktop.client.data.local.LayoutPreferenceStorage
import com.cowork.desktop.client.data.local.TokenStorage
import com.cowork.desktop.client.data.remote.ChatSocket
import com.cowork.desktop.client.data.remote.JvmChatSocket
import com.cowork.desktop.client.feature.auth.DesktopOAuthLauncher
import com.cowork.desktop.client.feature.auth.OAuthLauncher
import org.koin.dsl.module

val jvmModule = module {
    single<TokenStorage> { JvmTokenStorage() }
    single<LayoutPreferenceStorage> { JvmLayoutPreferenceStorage() }
    single<OAuthLauncher> { DesktopOAuthLauncher() }
    single<ChatSocket> { JvmChatSocket() }
}
