package com.cowork.desktop.client.domain.model

data class UserProfile(
    val id: Long,
    val name: String,
    val email: String,
    val nickname: String?,
    val profileImageUrl: String?,
    val github: String?,
    val studentRole: String?,
    val studentNumber: String?,
    val major: String?,
    val specialty: String?,
    val description: String?,
    val roles: List<String>,
    val sex: String? = null,
    val status: String? = null,
    val statusMessage: String? = null,
    val statusExpiresAt: String? = null,
    val accountDescription: String? = null,
)

data class UserProfileUpdate(
    val name: String? = null,
    val description: String? = null,
    val nickname: String? = null,
    val githubId: String? = null,
    val clearGithubId: Boolean = false,
    val roles: List<String>? = null,
)

data class UserStatusUpdate(
    val status: String,
    val message: String? = null,
    val expiresAt: String? = null,
)

data class UserSearchCriteria(
    val name: String? = null,
    val nickname: String? = null,
    val major: String? = null,
    val studentRole: String? = null,
    val status: String? = null,
    val role: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: String = "id",
    val sortOrder: UserSortOrder = UserSortOrder.Ascending,
)

enum class UserSortOrder(val apiValue: String) {
    Ascending("asc"),
    Descending("desc"),
}

data class UserSearchPage(
    val items: List<UserProfile>,
    val hasNext: Boolean,
    val page: Int,
    val pageSize: Int,
    val totalCount: Long,
)

data class UserUpsert(
    val name: String,
    val email: String,
    val sex: String,
    val major: String,
    val role: String,
    val githubId: String? = null,
    val classNumber: Int? = null,
    val grade: Int? = null,
    val studentNumberInClass: Int? = null,
)
