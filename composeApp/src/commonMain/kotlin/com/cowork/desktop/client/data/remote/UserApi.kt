package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.UserProfileUpdate
import com.cowork.desktop.client.domain.model.UserSearchCriteria
import com.cowork.desktop.client.domain.model.UserStatusUpdate
import com.cowork.desktop.client.domain.model.UserUpsert
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class UserApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getMyProfile(accessToken: String): MyProfileResponse =
        client.get("$baseUrl/users/me") {
            bearerAuth(accessToken)
        }.bodyPayload()

    suspend fun updateMyProfile(
        accessToken: String,
        update: UserProfileUpdate,
    ): MyProfileResponse =
        client.patch("$baseUrl/users/me") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(update.toRequestBody())
        }.bodyPayload()

    suspend fun updateMyStatus(
        accessToken: String,
        update: UserStatusUpdate,
    ): MyProfileResponse =
        client.patch("$baseUrl/users/me/status") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(update.toRequestBody())
        }.bodyPayload()

    suspend fun deleteProfileImage(accessToken: String) {
        client.delete("$baseUrl/users/me/profile-image") {
            bearerAuth(accessToken)
        }
    }

    suspend fun generatePresignedUrl(accessToken: String, contentType: String): PresignedUploadResponse =
        client.post("$baseUrl/users/me/profile-image/presigned") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(PresignedUrlRequest(contentType = contentType))
        }.bodyPayload()

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
        }.bodyPayload()

    suspend fun searchUsers(
        accessToken: String,
        criteria: UserSearchCriteria = UserSearchCriteria(),
    ): UserSearchPageResponse {
        require(criteria.page >= 1) { "page는 1 이상이어야 합니다." }
        require(criteria.pageSize >= 1) { "pageSize는 1 이상이어야 합니다." }

        return client.get("$baseUrl/users/search") {
            bearerAuth(accessToken)
            criteria.name?.let { parameter("name", it) }
            criteria.nickname?.let { parameter("nickname", it) }
            criteria.major?.let { parameter("major", it) }
            criteria.studentRole?.let { parameter("student_role", it) }
            criteria.status?.let { parameter("status", it) }
            criteria.role?.let { parameter("role", it) }
            parameter("page", criteria.page)
            parameter("page_size", criteria.pageSize)
            parameter("sort_by", criteria.sortBy)
            parameter("sort_order", criteria.sortOrder.apiValue)
        }.bodyPayload()
    }

    suspend fun upsertUser(
        accessToken: String,
        userId: Long,
        user: UserUpsert,
    ): MyProfileResponse =
        client.put("$baseUrl/users/$userId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                UpsertUserRequest(
                    name = user.name,
                    email = user.email,
                    sex = user.sex,
                    major = user.major,
                    role = user.role,
                    githubId = user.githubId,
                    classNumber = user.classNumber,
                    grade = user.grade,
                    studentNumberInClass = user.studentNumberInClass,
                )
            )
        }.bodyPayload()

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
        @SerialName("status_message") val statusMessage: String? = null,
        @SerialName("status_expires_at") val statusExpiresAt: String? = null,
        @SerialName("profile_image_url") val profileImageUrl: String? = null,
    )

    @Serializable
    data class UserSearchPageResponse(
        val items: List<MyProfileResponse> = emptyList(),
        @SerialName("has_next") val hasNext: Boolean = false,
        val page: Int = 1,
        @SerialName("page_size") val pageSize: Int = 20,
        @SerialName("total_count") val totalCount: Long = 0,
    )

    @Serializable
    data class PresignedUploadResponse(
        @SerialName("upload_url") val uploadUrl: String,
        @SerialName("object_key") val objectKey: String,
    )

    @Serializable
    private data class UpsertUserRequest(
        val name: String,
        val email: String,
        val sex: String,
        val major: String,
        val role: String,
        @SerialName("github_id") val githubId: String?,
        @SerialName("class_number") val classNumber: Int?,
        val grade: Int?,
        @SerialName("student_number_in_class") val studentNumberInClass: Int?,
    )

    @Serializable
    private data class PresignedUrlRequest(
        @SerialName("content_type") val contentType: String,
    )

    @Serializable
    private data class ConfirmUploadRequest(
        @SerialName("object_key") val objectKey: String,
    )

    private suspend inline fun <reified T> HttpResponse.bodyPayload(): T {
        val response = body<JsonElement>()
        val payload = (response as? JsonObject)
            ?.get("data")
            ?.takeUnless { it is JsonNull }
            ?: response
        return responseJson.decodeFromJsonElement(payload)
    }

    private fun UserProfileUpdate.toRequestBody(): JsonObject = buildJsonObject {
        name?.let { put("name", JsonPrimitive(it)) }
        description?.let { put("description", JsonPrimitive(it)) }
        nickname?.let { put("nickname", JsonPrimitive(it)) }
        when {
            clearGithubId -> put("github_id", JsonNull)
            githubId != null -> put("github_id", JsonPrimitive(githubId))
        }
        roles?.let { values ->
            put("roles", JsonArray(values.map(::JsonPrimitive)))
        }
    }

    private fun UserStatusUpdate.toRequestBody(): JsonObject = buildJsonObject {
        put("status", JsonPrimitive(status))
        put("message", message?.let(::JsonPrimitive) ?: JsonNull)
        put("expiresAt", expiresAt?.let(::JsonPrimitive) ?: JsonNull)
    }

    private companion object {
        val responseJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
