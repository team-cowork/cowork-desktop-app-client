package com.cowork.app_client.feature.auth

import com.cowork.app_client.config.AppConfig
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
import java.util.UUID

private const val CALLBACK_PORT = 19420
private const val TIMEOUT_MS = 5L * 60 * 1000
private const val REDIRECT_URI_PROPERTY = "cowork.oauth.redirectUri"
private const val REDIRECT_URI_ENV = "COWORK_OAUTH_REDIRECT_URI"

class DesktopOAuthLauncher : OAuthLauncher {
    override suspend fun launch(signInUrl: String): OAuthAuthorizationCode? {
        val redirectUri = resolveRedirectUri()
        val state = UUID.randomUUID().toString()
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        val fullSignInUrl = buildSignInUrl(
            signInUrl = signInUrl,
            redirectUri = redirectUri,
            state = state,
            codeChallenge = codeChallenge,
        )

        return if (redirectUri.startsWith("${AppConfig.DESKTOP_OAUTH_SCHEME}://")) {
            launchWithCustomScheme(
                fullSignInUrl = fullSignInUrl,
                state = state,
                codeVerifier = codeVerifier,
                redirectUri = redirectUri,
            )
        } else {
            launchWithLocalCallbackServer(
                fullSignInUrl = fullSignInUrl,
                state = state,
                codeVerifier = codeVerifier,
                redirectUri = redirectUri,
            )
        }
    }

    private suspend fun launchWithCustomScheme(
        fullSignInUrl: String,
        state: String,
        codeVerifier: String,
        redirectUri: String,
    ): OAuthAuthorizationCode {
        val callback = DesktopOAuthCallbackRegistry.prepareForCallback()

        openBrowser(fullSignInUrl)

        val callbackUri = withTimeoutOrNull(TIMEOUT_MS) { callback.await() }
            ?: throw OAuthLaunchException("OAuth callback 대기 시간이 초과되었습니다.")

        return parseAuthorizationCode(
            rawQuery = callbackUri.rawQuery,
            expectedState = state,
            codeVerifier = codeVerifier,
            redirectUri = redirectUri,
        )
    }

    private suspend fun launchWithLocalCallbackServer(
        fullSignInUrl: String,
        state: String,
        codeVerifier: String,
        redirectUri: String,
    ): OAuthAuthorizationCode {
        val result = CompletableDeferred<Result<OAuthAuthorizationCode?>>()
        val server = HttpServer.create(InetSocketAddress(CALLBACK_PORT), 0)

        server.createContext("/callback") { exchange ->
            val params = parseParams(exchange.requestURI.rawQuery)

            val code = params["code"]
            val returnedState = params["state"]
            val error = params["error"]
            val errorDescription = params["error_description"]

            val html = if (code != null && returnedState == state && error == null) {
                SUCCESS_HTML
            } else {
                ERROR_HTML
            }

            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, html.toByteArray(Charsets.UTF_8).size.toLong())
            exchange.responseBody.use { it.write(html.toByteArray(Charsets.UTF_8)) }

            result.complete(
                when {
                    error != null -> Result.failure(
                        OAuthLaunchException("DataGSM OAuth 오류: $error ${errorDescription.orEmpty()}".trim())
                    )
                    code == null -> Result.failure(
                        OAuthLaunchException("OAuth callback에 authorization code가 없습니다. params=${params.keys}")
                    )
                    returnedState != state -> Result.failure(
                        OAuthLaunchException("OAuth state 검증 실패")
                    )
                    else -> Result.success(
                        OAuthAuthorizationCode(
                            code = code,
                            state = state,
                            codeVerifier = codeVerifier,
                            redirectUri = redirectUri,
                        )
                    )
                }
            )
        }
        server.executor = null
        server.start()

        openBrowser(fullSignInUrl)

        val authorizationCode = withTimeoutOrNull(TIMEOUT_MS) { result.await() }
        server.stop(0)
        return authorizationCode?.getOrThrow()
            ?: throw OAuthLaunchException("OAuth callback 대기 시간이 초과되었습니다.")
    }

    private fun buildSignInUrl(
        signInUrl: String,
        redirectUri: String,
        state: String,
        codeChallenge: String,
    ): String {
        val fullSignInUrl = buildString {
            append(signInUrl)
            append("?")
            appendQueryParam("client_id", AppConfig.DATAGSM_CLIENT_ID)
            append("&")
            appendQueryParam("response_type", "code")
            append("&")
            appendQueryParam("redirect_uri", redirectUri)
            append("&")
            appendQueryParam("state", state)
            append("&")
            appendQueryParam("code_challenge", codeChallenge)
            append("&")
            appendQueryParam("code_challenge_method", "S256")
            append("&")
            appendQueryParam("scope", "datagsm:self_read")
        }

        return fullSignInUrl
    }

    private fun openBrowser(url: String) {
        val opened = runCatching {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
                true
            } else {
                false
            }
        }.getOrDefault(false)

        if (!opened) {
            throw OAuthLaunchException("시스템 브라우저를 열 수 없습니다.")
        }
    }

    private fun parseAuthorizationCode(
        rawQuery: String?,
        expectedState: String,
        codeVerifier: String,
        redirectUri: String,
    ): OAuthAuthorizationCode {
        val params = parseParams(rawQuery)
        val code = params["code"]
        val returnedState = params["state"]
        val error = params["error"]
        val errorDescription = params["error_description"]

        return when {
            error != null -> throw OAuthLaunchException(
                "DataGSM OAuth 오류: $error ${errorDescription.orEmpty()}".trim()
            )
            code == null -> throw OAuthLaunchException(
                "OAuth callback에 authorization code가 없습니다. params=${params.keys}"
            )
            returnedState != expectedState -> throw OAuthLaunchException("OAuth state 검증 실패")
            else -> OAuthAuthorizationCode(
                code = code,
                state = expectedState,
                codeVerifier = codeVerifier,
                redirectUri = redirectUri,
            )
        }
    }

    private fun resolveRedirectUri(): String {
        return System.getProperty(REDIRECT_URI_PROPERTY)
            ?: System.getenv(REDIRECT_URI_ENV)
            ?: defaultRedirectUri()
    }

    private fun defaultRedirectUri(): String {
        val customSchemeUri =
            "${AppConfig.DESKTOP_OAUTH_SCHEME}://${AppConfig.DESKTOP_OAUTH_CALLBACK_HOST}${AppConfig.DESKTOP_OAUTH_CALLBACK_PATH}"
        val localCallbackUri = "http://localhost:$CALLBACK_PORT${AppConfig.DESKTOP_OAUTH_CALLBACK_PATH}"

        return if (isPackagedMacApplication()) customSchemeUri else localCallbackUri
    }

    private fun isPackagedMacApplication(): Boolean {
        val osName = System.getProperty("os.name").lowercase()
        if (!osName.contains("mac")) return false
        val javaHome = System.getProperty("java.home") ?: return false

        return "runtime/Contents/Home" in javaHome
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
