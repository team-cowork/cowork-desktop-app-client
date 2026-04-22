package com.cowork.app_client.data.repository

import com.cowork.app_client.data.remote.UserApi
import com.cowork.app_client.domain.model.UserProfile

interface UserRepository {
    suspend fun getMyProfile(): UserProfile?
    suspend fun uploadProfileImage(bytes: ByteArray, contentType: String): Boolean
}

class DefaultUserRepository(
    private val authRepository: AuthRepository,
    private val userApi: UserApi,
) : UserRepository {

    override suspend fun getMyProfile(): UserProfile? {
        val token = authRepository.getStoredTokens()?.accessToken ?: return null
        return runCatching {
            val response = userApi.getMyProfile(token)
            val id = response.id ?: return null
            UserProfile(
                id = id,
                name = response.name ?: "",
                email = response.email ?: "",
                nickname = response.nickname,
                profileImageUrl = response.profileImageUrl,
                github = response.github,
                studentRole = response.studentRole,
                studentNumber = response.studentNumber,
                major = response.major,
                specialty = response.specialty,
                description = response.description ?: response.accountDescription,
                roles = response.roles,
            )
        }.getOrNull()
    }

    override suspend fun uploadProfileImage(bytes: ByteArray, contentType: String): Boolean {
        val token = authRepository.getStoredTokens()?.accessToken ?: return false
        return runCatching {
            val presigned = userApi.generatePresignedUrl(token, contentType)
            userApi.putBytesToS3(presigned.uploadUrl, bytes, contentType)
            userApi.confirmUpload(token, presigned.objectKey)
        }.isSuccess
    }
}
