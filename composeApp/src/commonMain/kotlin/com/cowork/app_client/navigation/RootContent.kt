package com.cowork.app_client.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.cowork.app_client.feature.auth.ui.LoginScreen
import com.cowork.app_client.feature.main.ui.MainScreen

@Composable
fun RootContent(component: RootComponent) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(slide()),
        ) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.Auth -> LoginScreen(instance.component)
            is RootComponent.Child.Main -> MainScreen(instance.component)
        }
    }
}
