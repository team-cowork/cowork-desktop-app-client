package com.cowork.app_client.feature.auth

import com.cowork.app_client.config.AppConfig
import kotlinx.coroutines.CompletableDeferred
import java.awt.Desktop
import java.net.URI

object DesktopOAuthCallbackRegistry {
    private val lock = Any()
    private var pendingCallback: CompletableDeferred<URI>? = null
    private var bufferedCallback: URI? = null

    fun prepareForCallback(): CompletableDeferred<URI> = synchronized(lock) {
        val deferred = CompletableDeferred<URI>()
        val buffered = bufferedCallback
        if (buffered != null) {
            bufferedCallback = null
            deferred.complete(buffered)
        } else {
            pendingCallback = deferred
        }
        deferred
    }

    fun acceptLaunchArgs(args: Array<String>) {
        args.asSequence()
            .mapNotNull { runCatching { URI(it) }.getOrNull() }
            .firstOrNull(::isCoworkOAuthCallback)
            ?.let(::acceptCallback)
    }

    fun installOpenUriHandler() {
        runCatching {
            if (!Desktop.isDesktopSupported()) return

            val desktop = Desktop.getDesktop()
            if (!desktop.isSupported(Desktop.Action.APP_OPEN_URI)) return

            desktop.setOpenURIHandler { event ->
                acceptCallback(event.uri)
            }
        }
    }

    fun acceptCallback(uri: URI): Boolean {
        if (!isCoworkOAuthCallback(uri)) return false

        synchronized(lock) {
            val pending = pendingCallback
            if (pending != null) {
                pendingCallback = null
                pending.complete(uri)
            } else {
                bufferedCallback = uri
            }
        }
        return true
    }

    private fun isCoworkOAuthCallback(uri: URI): Boolean {
        return uri.scheme == AppConfig.DESKTOP_OAUTH_SCHEME &&
            uri.host == AppConfig.DESKTOP_OAUTH_CALLBACK_HOST &&
            uri.path == AppConfig.DESKTOP_OAUTH_CALLBACK_PATH
    }
}
