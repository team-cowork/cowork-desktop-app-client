package com.cowork.app_client.data.remote

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
        val github: String? = null,
        val accountDescription: String? = null,
        val studentRole: String? = null,
        val studentNumber: String? = null,
        val major: String? = null,
        val specialty: String? = null,
        val status: String? = null,
        val nickname: String? = null,
        val roles: List<String> = emptyList(),
        val description: String? = null,
        @SerialName("profileImageUrl")
        val profileImageUrl: String? = null,
    )

    @Serializable
    data class PresignedUploadResponse(
        val uploadUrl: String,
        val objectKey: String,
    )

    @Serializable
    private data class PresignedUrlRequest(val contentType: String)

    @Serializable
    private data class ConfirmUploadRequest(val objectKey: String)
}
