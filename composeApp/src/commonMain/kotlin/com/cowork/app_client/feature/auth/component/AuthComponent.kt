package com.cowork.app_client.feature.auth.component

import com.cowork.app_client.feature.auth.store.AuthStore
import kotlinx.coroutines.flow.StateFlow

interface AuthComponent {
    val state: StateFlow<AuthStore.State>
    fun onLoginClick()
}
