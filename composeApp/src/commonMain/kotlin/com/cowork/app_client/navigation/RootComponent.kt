package com.cowork.app_client.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.cowork.app_client.feature.auth.component.AuthComponent

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Auth(val component: AuthComponent) : Child
        data object Main : Child
    }
}
