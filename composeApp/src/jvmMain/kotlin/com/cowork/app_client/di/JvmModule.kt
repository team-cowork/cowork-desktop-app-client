package com.cowork.app_client.di

import com.cowork.app_client.data.local.JvmLayoutPreferenceStorage
import com.cowork.app_client.data.local.JvmTokenStorage
import com.cowork.app_client.data.local.LayoutPreferenceStorage
import com.cowork.app_client.data.local.TokenStorage
import com.cowork.app_client.feature.auth.DesktopOAuthLauncher
import com.cowork.app_client.feature.auth.OAuthLauncher
import org.koin.dsl.module

val jvmModule = module {
    single<TokenStorage> { JvmTokenStorage() }
    single<LayoutPreferenceStorage> { JvmLayoutPreferenceStorage() }
    single<OAuthLauncher> { DesktopOAuthLauncher() }
}
