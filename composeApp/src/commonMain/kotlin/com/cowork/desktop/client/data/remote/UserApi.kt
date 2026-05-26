package com.cowork.desktop.client.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.content.ByteArrayContent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UserApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getMyProfile(accessToken: String): MyProfileResponse =
        client.get("$baseUrl/users/me") {
            bearerAuth(accessToken)
        }.body<ApiResponse<MyProfileResponse>>().data ?: MyProfileResponse()

    suspend fun generatePresignedUrl(accessToken: String, contentType: String): PresignedUploadResponse =
        client.post("$baseUrl/users/me/profile-image/presigned") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(PresignedUrlRequest(contentType = contentType))
        }.body<ApiResponse<PresignedUploadResponse>>().data
            ?: throw IllegalStateException("프리사인드 URL 발급 실패")

    suspend fun confirmUpload(accessToken: String, objectKey: String) {
        client.post("$baseUrl/users/me/profile-image/confirm") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ConfirmUploadRequest(objectKey = objectKey))
        }
    }

    suspend fun getUserProfile(accessToken: String, userId: Long): MyProfileResponse =
        client.get("$baseUrl/users/$userId") {
            bearerAuth(accessToken)
        }.body<ApiResponse<MyProfileResponse>>().data ?: MyProfileResponse()

    suspend fun putBytesToS3(uploadUrl: String, bytes: ByteArray, contentType: String) {
        client.put(uploadUrl) {
            setBody(ByteArrayContent(bytes, ContentType.parse(contentType)))
        }
    }

    @Serializable
    data class MyProfileResponse(
        val id: Long? = null,
        val name: String? = null,
        val email: String? = null,
        val sex: String? = null,
        @SerialName("github_id") val githubId: String? = null,
        @SerialName("account_description") val accountDescription: String? = null,
        @SerialName("student_role") val studentRole: String? = null,
        @SerialName("student_number") val studentNumber: String? = null,
        val major: String? = null,
        val specialty: String? = null,
        val status: String? = null,
        val nickname: String? = null,
        val roles: List<String> = emptyList(),
        val description: String? = null,
        @SerialName("profile_image_url") val profileImageUrl: String? = null,
    )

    @Serializable
    data class PresignedUploadResponse(
        @SerialName("upload_url") val uploadUrl: String,
        @SerialName("object_key") val objectKey: String,
    )

    @Serializable
    private data class PresignedUrlRequest(
        @SerialName("content_type") val contentType: String,
    )

    @Serializable
    private data class ConfirmUploadRequest(
        @SerialName("object_key") val objectKey: String,
    )
}
