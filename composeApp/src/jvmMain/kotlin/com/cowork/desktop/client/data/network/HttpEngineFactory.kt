package com.cowork.desktop.client.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

actual fun createHttpEngine(): HttpClientEngine = CIO.create {
    https {
        trustManager = CoworkPinnedTrustManager
    }
}

/**
 * The currently deployed gateway uses a self-signed certificate. Keep normal
 * platform trust for every public endpoint (including object storage), and
 * accept only the explicitly pinned gateway certificate as the fallback.
 */
internal object CoworkPinnedTrustManager : X509TrustManager {
    private const val GATEWAY_SHA256 = "2795AC2A5A6B00E7CDB2BF1044A9DFE120379733A05E83413796A417CBE3F6D2"

    private val platformTrustManager: X509TrustManager =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).run {
            init(null as KeyStore?)
            trustManagers.filterIsInstance<X509TrustManager>().first()
        }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        platformTrustManager.checkClientTrusted(chain, authType)
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        try {
            platformTrustManager.checkServerTrusted(chain, authType)
            return
        } catch (_: CertificateException) {
            val leaf = chain?.firstOrNull() ?: throw CertificateException("서버 인증서가 없습니다.")
            leaf.checkValidity()
            val fingerprint = MessageDigest.getInstance("SHA-256")
                .digest(leaf.encoded)
                .joinToString("") { "%02X".format(it) }
            if (fingerprint != GATEWAY_SHA256) {
                throw CertificateException("신뢰할 수 없는 서버 인증서입니다.")
            }
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = platformTrustManager.acceptedIssuers
}
