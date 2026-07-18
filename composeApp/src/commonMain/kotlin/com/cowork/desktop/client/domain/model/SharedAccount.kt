package com.cowork.desktop.client.domain.model

data class SharedAccount(
    val id: Long,
    val channelId: Long,
    val provider: SharedAccountProvider,
    val providerLabel: String?,
    val displayName: String?,
    val loginUrl: String?,
    val accountIdentifier: String?,
    val maskedCredential: String?,
    val connectedViaOAuth: Boolean,
    val createdBy: Long,
    val createdAt: String?,
    val updatedAt: String?,
)

enum class SharedAccountProvider(val apiValue: String, val label: String, val supportsOAuth: Boolean) {
    GitHub("GITHUB", "GitHub", true),
    Notion("NOTION", "Notion", true),
    Jira("JIRA", "Jira", true),
    Google("GOOGLE", "Google", true),
    Facebook("FACEBOOK", "Facebook", true),
    Instagram("INSTAGRAM", "Instagram", false),
    Npm("NPM", "npm", false),
    OpenAI("OPENAI", "OpenAI", false),
    PyPi("PYPI", "PyPI", false),
    Vercel("VERCEL", "Vercel", false),
    Aws("AWS", "AWS", false),
    Custom("CUSTOM", "직접 입력", false),
    Unknown("UNKNOWN", "알 수 없음", false),
}

fun String.toSharedAccountProvider(): SharedAccountProvider =
    SharedAccountProvider.entries.firstOrNull { it.apiValue == uppercase() } ?: SharedAccountProvider.Unknown
