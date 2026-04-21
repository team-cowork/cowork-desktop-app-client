package com.cowork.app_client.feature.auth

import com.cowork.app_client.domain.model.AuthTokens
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI

private const val CALLBACK_PORT = 19420
private const val TIMEOUT_MS = 5L * 60 * 1000

class DesktopOAuthLauncher : OAuthLauncher {
    override suspend fun launch(signInUrl: String): AuthTokens? {
        val result = CompletableDeferred<AuthTokens?>()
        val server = HttpServer.create(InetSocketAddress(CALLBACK_PORT), 0)

        server.createContext("/callback") { exchange ->
            val query = exchange.requestURI.query.orEmpty()
            val params = query.split("&").associate {
                val (k, v) = it.split("=", limit = 2).let { p ->
                    p[0] to (p.getOrNull(1) ?: "")
                }
                k to java.net.URLDecoder.decode(v, "UTF-8")
            }

            val accessToken = params["access_token"]
            val refreshToken = params["refresh_token"]

            val html = if (accessToken != null && refreshToken != null) {
                SUCCESS_HTML
            } else {
                ERROR_HTML
            }

            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, html.toByteArray(Charsets.UTF_8).size.toLong())
            exchange.responseBody.use { it.write(html.toByteArray(Charsets.UTF_8)) }

            result.complete(
                if (accessToken != null && refreshToken != null) {
                    AuthTokens(accessToken, refreshToken)
                } else null
            )
        }
        server.executor = null
        server.start()

        val fullSignInUrl = buildString {
            append(signInUrl)
            append(if ('?' in signInUrl) '&' else '?')
            append("redirect_uri=http://localhost:$CALLBACK_PORT/callback")
        }

        runCatching {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(fullSignInUrl))
            }
        }

        val tokens = withTimeoutOrNull(TIMEOUT_MS) { result.await() }
        server.stop(0)
        return tokens
    }

    private companion object {
        const val SUCCESS_HTML = """<!DOCTYPE html><html><body style="font-family:sans-serif;text-align:center;padding:40px">
<h2>로그인 완료</h2><p>cowork 앱으로 돌아가세요. 이 탭을 닫아도 됩니다.</p>
</body></html>"""
        const val ERROR_HTML = """<!DOCTYPE html><html><body style="font-family:sans-serif;text-align:center;padding:40px">
<h2>로그인 실패</h2><p>다시 시도해주세요.</p>
</body></html>"""
    }
}
