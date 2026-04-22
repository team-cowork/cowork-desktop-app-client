package com.cowork.app_client.di

import com.cowork.app_client.data.local.AndroidLayoutPreferenceStorage
import com.cowork.app_client.data.local.AndroidTokenStorage
import com.cowork.app_client.data.local.LayoutPreferenceStorage
import com.cowork.app_client.data.local.TokenStorage
import com.cowork.app_client.feature.auth.AndroidOAuthLauncher
import com.cowork.app_client.feature.auth.OAuthLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<TokenStorage> { AndroidTokenStorage(androidContext()) }
    single<LayoutPreferenceStorage> { AndroidLayoutPreferenceStorage(androidContext()) }
    single<OAuthLauncher> { AndroidOAuthLauncher() }
}
