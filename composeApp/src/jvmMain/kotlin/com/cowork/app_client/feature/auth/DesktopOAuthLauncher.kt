package com.cowork.app_client.feature.auth

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

private const val CALLBACK_PORT = 19420
private const val TIMEOUT_MS = 5L * 60 * 1000

class DesktopOAuthLauncher : OAuthLauncher {
    override suspend fun launch(signInUrl: String): OAuthAuthorizationCode? {
        val redirectUri = "http://localhost:$CALLBACK_PORT/callback"
        val state = generateCodeVerifier()
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        val result = CompletableDeferred<OAuthAuthorizationCode?>()
        val server = HttpServer.create(InetSocketAddress(CALLBACK_PORT), 0)

        server.createContext("/callback") { exchange ->
            val params = parseParams(exchange.requestURI.rawQuery)

            val code = params["code"]
            val returnedState = params["state"]
            val error = params["error"]

            val html = if (code != null && returnedState == state && error == null) {
                SUCCESS_HTML
            } else {
                ERROR_HTML
            }

            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, html.toByteArray(Charsets.UTF_8).size.toLong())
            exchange.responseBody.use { it.write(html.toByteArray(Charsets.UTF_8)) }

            result.complete(
                if (code != null && returnedState == state && error == null) {
                    OAuthAuthorizationCode(
                        code = code,
                        state = state,
                        codeVerifier = codeVerifier,
                        redirectUri = redirectUri,
                    )
                } else null
            )
        }
        server.executor = null
        server.start()

        val fullSignInUrl = buildString {
            append(signInUrl)
            append(if ('?' in signInUrl) '&' else '?')
            appendQueryParam("redirect_uri", redirectUri)
            append("&")
            appendQueryParam("state", state)
            append("&")
            appendQueryParam("code_challenge", codeChallenge)
            append("&")
            appendQueryParam("code_challenge_method", "S256")
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

    private fun parseParams(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()

        return rawQuery
            .split("&")
            .filter { it.isNotBlank() && "=" in it }
            .associate {
                val parts = it.split("=", limit = 2)
                URLDecoder.decode(parts[0], "UTF-8") to URLDecoder.decode(parts[1], "UTF-8")
            }
    }

    private fun StringBuilder.appendQueryParam(name: String, value: String) {
        append(URLEncoder.encode(name, "UTF-8"))
        append("=")
        append(URLEncoder.encode(value, "UTF-8"))
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return base64UrlEncode(bytes)
    }

    private fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return base64UrlEncode(digest)
    }

    private fun base64UrlEncode(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private companion object {
        const val SUCCESS_HTML = """<!DOCTYPE html><html><body style="font-family:sans-serif;text-align:center;padding:40px">
<h2>로그인 완료</h2><p>cowork 앱으로 돌아가세요. 이 탭을 닫아도 됩니다.</p>
</body></html>"""
        const val ERROR_HTML = """<!DOCTYPE html><html><body style="font-family:sans-serif;text-align:center;padding:40px">
<h2>로그인 실패</h2><p>다시 시도해주세요.</p>
</body></html>"""
    }
}
