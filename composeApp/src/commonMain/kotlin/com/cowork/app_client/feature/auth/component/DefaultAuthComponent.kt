package com.cowork.app_client.feature.auth.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.feature.auth.OAuthLauncher
import com.cowork.app_client.feature.auth.store.AuthStore
import com.cowork.app_client.feature.auth.store.AuthStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultAuthComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    authRepository: AuthRepository,
    oAuthLauncher: OAuthLauncher,
    private val onAuthenticated: () -> Unit,
) : AuthComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val store = instanceKeeper.getStore {
        AuthStoreFactory(storeFactory, authRepository, oAuthLauncher).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<AuthStore.State> = store.stateFlow(scope)

    init {
        lifecycle.doOnDestroy { scope.cancel() }

        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    AuthStore.Label.Authenticated -> onAuthenticated()
                    AuthStore.Label.SignedOut -> Unit
                }
            }
        }
    }

    override fun onLoginClick() = store.accept(AuthStore.Intent.Login)
}
