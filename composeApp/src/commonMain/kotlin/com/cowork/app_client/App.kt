package com.cowork.app_client

import androidx.compose.runtime.Composable
import com.cowork.app_client.navigation.RootComponent
import com.cowork.app_client.navigation.RootContent
import com.cowork.app_client.ui.theme.CoworkTheme

@Composable
fun App(root: RootComponent) {
    CoworkTheme {
        RootContent(root)
    }
}
