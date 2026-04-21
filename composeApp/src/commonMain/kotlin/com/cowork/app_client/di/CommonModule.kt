package com.cowork.app_client.di

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.cowork.app_client.data.network.createHttpClient
import com.cowork.app_client.data.network.createHttpEngine
import com.cowork.app_client.data.remote.AuthApi
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.data.repository.DefaultAuthRepository
import org.koin.dsl.module

private const val BASE_URL = "http://localhost:8080"

val commonModule = module {
    single { DefaultStoreFactory() }
    single { createHttpEngine() }
    single { createHttpClient(get()) }
    single { AuthApi(client = get(), baseUrl = BASE_URL) }
    single<AuthRepository> { DefaultAuthRepository(tokenStorage = get(), authApi = get()) }
}
