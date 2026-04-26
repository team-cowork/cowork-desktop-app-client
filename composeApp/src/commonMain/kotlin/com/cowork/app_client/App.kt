package com.cowork.app_client

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.cowork.app_client.data.network.ConnectivityMonitor
import com.cowork.app_client.feature.reconnect.ReconnectingScreen
import com.cowork.app_client.navigation.RootComponent
import com.cowork.app_client.navigation.RootContent
import com.cowork.app_client.ui.theme.CoworkTheme
import org.koin.compose.koinInject

@Composable
fun App(root: RootComponent) {
    CoworkTheme {
        val monitor = koinInject<ConnectivityMonitor>()
        val isConnected by monitor.isConnected.collectAsState()
        val retryIn by monitor.retryIn.collectAsState()

        AnimatedContent(
            targetState = isConnected,
            transitionSpec = {
                if (targetState) {
                    fadeIn(tween(400, delayMillis = 200)) togetherWith fadeOut(tween(200))
                } else {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                }
            },
            label = "connectivityTransition",
        ) { connected ->
            if (connected) {
                RootContent(root)
            } else {
                ReconnectingScreen(retryIn = retryIn)
            }
        }
    }
}
