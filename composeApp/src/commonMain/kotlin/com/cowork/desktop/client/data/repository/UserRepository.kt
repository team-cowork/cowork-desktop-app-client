package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.UserApi
import com.cowork.desktop.client.domain.model.UserProfile
import com.cowork.desktop.client.domain.model.UserProfileUpdate
import com.cowork.desktop.client.domain.model.UserSearchCriteria
import com.cowork.desktop.client.domain.model.UserSearchPage
import com.cowork.desktop.client.domain.model.UserStatusUpdate
import com.cowork.desktop.client.domain.model.UserUpsert
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface UserRepository {
    suspend fun getMyProfile(): UserProfile?
    suspend fun getUserProfile(userId: Long): UserProfile?
    suspend fun updateMyProfile(update: UserProfileUpdate): UserProfile?
    suspend fun updateMyStatus(update: UserStatusUpdate): UserProfile?
    suspend fun deleteProfileImage()
    suspend fun searchUsers(criteria: UserSearchCriteria = UserSearchCriteria()): UserSearchPage
    suspend fun upsertUser(userId: Long, user: UserUpsert): UserProfile?
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
                userApi.getUserProfile(token, userId).toDomain()
            }
        }.getOrNull()

    override suspend fun getMyProfile(): UserProfile? =
        runCatching {
            authRepository.authorized { token ->
                userApi.getMyProfile(token).toDomain()
            }
        }.getOrNull()

    override suspend fun updateMyProfile(update: UserProfileUpdate): UserProfile? =
        authRepository.authorized { token ->
            userApi.updateMyProfile(token, update).toDomain()
        }

    override suspend fun updateMyStatus(update: UserStatusUpdate): UserProfile? =
        authRepository.authorized { token ->
            userApi.updateMyStatus(token, update).toDomain()
        }

    override suspend fun deleteProfileImage() {
        authRepository.authorized(userApi::deleteProfileImage)
    }

    override suspend fun searchUsers(criteria: UserSearchCriteria): UserSearchPage =
        authRepository.authorized { token ->
            val response = userApi.searchUsers(token, criteria)
            UserSearchPage(
                items = response.items.mapNotNull(UserApi.MyProfileResponse::toDomain),
                hasNext = response.hasNext,
                page = response.page,
                pageSize = response.pageSize,
                totalCount = response.totalCount,
            )
        }

    override suspend fun upsertUser(userId: Long, user: UserUpsert): UserProfile? =
        authRepository.authorized { token ->
            userApi.upsertUser(token, userId, user).toDomain()
        }

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

private fun UserApi.MyProfileResponse.toDomain(): UserProfile? {
    val userId = id ?: return null
    return UserProfile(
        id = userId,
        name = name.orEmpty(),
        email = email.orEmpty(),
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        github = githubId,
        studentRole = studentRole,
        studentNumber = studentNumber,
        major = major,
        specialty = specialty,
        description = description ?: accountDescription,
        roles = roles,
        sex = sex,
        status = status,
        statusMessage = statusMessage,
        statusExpiresAt = statusExpiresAt,
        accountDescription = accountDescription,
    )
}
