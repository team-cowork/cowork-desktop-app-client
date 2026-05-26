package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.UserApi
import com.cowork.desktop.client.domain.model.UserProfile
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface UserRepository {
    suspend fun getMyProfile(): UserProfile?
    suspend fun getUserProfile(userId: Long): UserProfile?
    suspend fun uploadProfileImage(bytes: ByteArray, contentType: String)
}

class UserProfileImageUploadException(message: String, cause: Throwable? = null) : Exception(message, cause)

class DefaultUserRepository(
    private val authRepository: AuthRepository,
    private val userApi: UserApi,
) : UserRepository {

    override suspend fun getUserProfile(userId: Long): UserProfile? =
        runCatching {
            authRepository.authorized { token ->
                val response = userApi.getUserProfile(token, userId)
                val id = response.id ?: return@authorized null
                UserProfile(
                    id = id,
                    name = response.name ?: "",
                    email = response.email ?: "",
                    nickname = response.nickname,
                    profileImageUrl = response.profileImageUrl,
                    github = response.githubId,
                    studentRole = response.studentRole,
                    studentNumber = response.studentNumber,
                    major = response.major,
                    specialty = response.specialty,
                    description = response.description ?: response.accountDescription,
                    roles = response.roles,
                )
            }
        }.getOrNull()

    override suspend fun getMyProfile(): UserProfile? =
        runCatching {
            authRepository.authorized { token ->
                val response = userApi.getMyProfile(token)
                val id = response.id ?: return@authorized null
                UserProfile(
                    id = id,
                    name = response.name ?: "",
                    email = response.email ?: "",
                    nickname = response.nickname,
                    profileImageUrl = response.profileImageUrl,
                    github = response.githubId,
                    studentRole = response.studentRole,
                    studentNumber = response.studentNumber,
                    major = response.major,
                    specialty = response.specialty,
                    description = response.description ?: response.accountDescription,
                    roles = response.roles,
                )
            }
        }.getOrNull()

    override suspend fun uploadProfileImage(bytes: ByteArray, contentType: String) {
        try {
            authRepository.authorized { token ->
                val presigned = userApi.generatePresignedUrl(token, contentType)
                userApi.putBytesToS3(presigned.uploadUrl, bytes, contentType)
                userApi.confirmUpload(token, presigned.objectKey)
            }
        } catch (e: ResponseException) {
            throw UserProfileImageUploadException(buildUploadFailureMessage(e), e)
        }
    }

    private suspend fun buildUploadFailureMessage(exception: ResponseException): String {
        val status = exception.response.status
        val body = runCatching { exception.response.bodyAsText() }.getOrDefault("")
        val jsonMessage = runCatching {
            lenientJson.decodeFromString<ErrorBody>(body).message
        }.getOrNull()?.takeIf { it.isNotBlank() }
        val xmlMessage = body
            .substringAfter("<Message>", "")
            .substringBefore("</Message>", "")
            .takeIf { it.isNotBlank() }

        return jsonMessage
            ?: xmlMessage?.let { "스토리지 업로드 실패: $it" }
            ?: body.takeIf { it.isNotBlank() }
            ?: "프로필 사진 업로드에 실패했습니다. (${status.value} ${status.description})"
    }

    @Serializable
    private data class ErrorBody(@SerialName("message") val message: String? = null)

    companion object {
        private val lenientJson = Json { ignoreUnknownKeys = true }
    }
}
