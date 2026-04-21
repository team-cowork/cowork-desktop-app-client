package com.cowork.app_client.di

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.cowork.app_client.data.network.createHttpClient
import com.cowork.app_client.data.network.createHttpEngine
import com.cowork.app_client.data.remote.AuthApi
import com.cowork.app_client.data.remote.ChannelApi
import com.cowork.app_client.data.remote.ChatApi
import com.cowork.app_client.data.remote.TeamApi
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.data.repository.ChannelRepository
import com.cowork.app_client.data.repository.ChatRepository
import com.cowork.app_client.data.repository.DefaultChannelRepository
import com.cowork.app_client.data.repository.DefaultAuthRepository
import com.cowork.app_client.data.repository.DefaultChatRepository
import com.cowork.app_client.data.repository.DefaultTeamRepository
import com.cowork.app_client.data.repository.TeamRepository
import org.koin.dsl.module

private const val BASE_URL = "http://localhost:8080/api"

val commonModule = module {
    single { DefaultStoreFactory() }
    single { createHttpEngine() }
    single { createHttpClient(get()) }
    single { AuthApi(client = get(), baseUrl = BASE_URL) }
    single { TeamApi(client = get(), baseUrl = BASE_URL) }
    single { ChannelApi(client = get(), baseUrl = BASE_URL) }
    single { ChatApi(client = get(), baseUrl = BASE_URL) }
    single<AuthRepository> { DefaultAuthRepository(tokenStorage = get(), authApi = get()) }
    single<TeamRepository> { DefaultTeamRepository(authRepository = get(), teamApi = get()) }
    single<ChannelRepository> { DefaultChannelRepository(authRepository = get(), channelApi = get()) }
    single<ChatRepository> { DefaultChatRepository(authRepository = get(), chatApi = get()) }
}
