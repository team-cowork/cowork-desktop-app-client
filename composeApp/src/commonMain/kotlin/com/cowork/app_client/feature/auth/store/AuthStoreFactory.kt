package com.cowork.app_client.feature.auth.store

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.domain.model.AuthTokens
import com.cowork.app_client.feature.auth.OAuthLauncher
import com.cowork.app_client.feature.auth.store.AuthStore.Intent
import com.cowork.app_client.feature.auth.store.AuthStore.Label
import com.cowork.app_client.feature.auth.store.AuthStore.State
import kotlinx.coroutines.launch

class AuthStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
    private val oAuthLauncher: OAuthLauncher,
) {
    fun create(): AuthStore =
        object : AuthStore, Store<Intent, State, Label> by storeFactory.create(
            name = "AuthStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { Executor() },
            reducer = Reducer,
        ) {}

    private inner class Executor : CoroutineExecutor<Intent, Unit, State, Msg, Label>() {

        override fun executeAction(action: Unit) {
            scope.launch {
                val tokens = authRepository.getStoredTokens()
                if (tokens != null) {
                    publish(Label.Authenticated)
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.Login -> login()
                is Intent.SignOut -> signOut()
            }
        }

        private fun login() {
            scope.launch {
                dispatch(Msg.SetLoading(true))
                val signInUrl = authRepository.getSignInUrl()
                val tokens = oAuthLauncher.launch(signInUrl)
                if (tokens != null) {
                    authRepository.saveTokens(tokens)
                    dispatch(Msg.SetLoading(false))
                    publish(Label.Authenticated)
                } else {
                    dispatch(Msg.SetError("로그인에 실패했습니다. 다시 시도해주세요."))
                }
            }
        }

        private fun signOut() {
            scope.launch {
                authRepository.signOut()
                publish(Label.SignedOut)
            }
        }
    }

    private sealed interface Msg {
        data class SetLoading(val isLoading: Boolean) : Msg
        data class SetError(val error: String?) : Msg
    }

    private object Reducer : com.arkivanov.mvikotlin.core.store.Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.SetLoading -> copy(isLoading = msg.isLoading, error = null)
            is Msg.SetError -> copy(isLoading = false, error = msg.error)
        }
    }
}
