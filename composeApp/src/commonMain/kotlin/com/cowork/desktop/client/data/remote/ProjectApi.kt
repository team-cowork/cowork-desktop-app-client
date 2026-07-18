package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.GithubApproveResult
import com.cowork.desktop.client.domain.model.GithubMergeResult
import com.cowork.desktop.client.domain.model.GithubPullRequest
import com.cowork.desktop.client.domain.model.GithubPullRequestBoard
import com.cowork.desktop.client.domain.model.GithubPullRequestFile
import com.cowork.desktop.client.domain.model.GithubPullRequestSummary
import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.ProjectMember
import com.cowork.desktop.client.domain.model.ProjectRole
import com.cowork.desktop.client.domain.model.ProjectStatus
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProjectApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getTeamProjects(accessToken: String, teamId: Long): List<Project> =
        fetchAllPages { page ->
            client.get("$baseUrl/projects") {
                bearerAuth(accessToken)
                parameter("teamId", teamId)
                parameter("size", PAGE_SIZE)
                parameter("page", page)
            }.bodyPayload<PageResponse<ProjectResponse>>()
        }.map(ProjectResponse::toDomain)

    suspend fun getMyProjects(accessToken: String): List<Project> =
        fetchAllPages { page ->
            client.get("$baseUrl/projects/me") {
                bearerAuth(accessToken)
                parameter("size", PAGE_SIZE)
                parameter("page", page)
            }.bodyPayload<PageResponse<ProjectResponse>>()
        }.map(ProjectResponse::toDomain)

    suspend fun getProject(accessToken: String, projectId: Long): Project =
        client.get("$baseUrl/projects/$projectId") {
            bearerAuth(accessToken)
        }.bodyPayload<ProjectResponse>().toDomain()

    suspend fun createProject(accessToken: String, teamId: Long, name: String, description: String?): Project =
        client.post("$baseUrl/projects") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateProjectRequest(teamId = teamId, name = name, description = description))
        }.bodyPayload<ProjectResponse>().toDomain()

    suspend fun updateProject(
        accessToken: String,
        projectId: Long,
        name: String? = null,
        description: String? = null,
        status: String? = null,
        clearDescription: Boolean = false,
    ): Project =
        client.patch("$baseUrl/projects/$projectId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                name?.let { put("name", it) }
                if (description != null) put("description", description)
                else if (clearDescription) put("description", "")
                status?.let { put("status", it) }
            })
        }.bodyPayload<ProjectResponse>().toDomain()

    suspend fun deleteProject(accessToken: String, projectId: Long) {
        client.delete("$baseUrl/projects/$projectId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun getMembers(accessToken: String, projectId: Long): List<ProjectMember> =
        client.get("$baseUrl/projects/$projectId/members") {
            bearerAuth(accessToken)
        }.bodyPayload<List<ProjectMemberResponse>>().map(ProjectMemberResponse::toDomain)

    suspend fun addMember(accessToken: String, projectId: Long, userId: Long, role: ProjectRole): ProjectMember =
        client.post("$baseUrl/projects/$projectId/members") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(AddProjectMemberRequest(userId = userId, role = role.toApiValue()))
        }.bodyPayload<ProjectMemberResponse>().toDomain()

    suspend fun removeMember(accessToken: String, projectId: Long, memberId: Long) {
        client.delete("$baseUrl/projects/$projectId/members/$memberId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun updateMemberRole(
        accessToken: String,
        projectId: Long,
        memberId: Long,
        role: ProjectRole,
    ): ProjectMember = client.patch("$baseUrl/projects/$projectId/members/$memberId") {
        bearerAuth(accessToken)
        contentType(ContentType.Application.Json)
        setBody(UpdateProjectMemberRoleRequest(role.toApiValue()))
    }.bodyPayload<ProjectMemberResponse>().toDomain()

    suspend fun linkGithubRepository(accessToken: String, projectId: Long, repositoryUrl: String): Project =
        client.put("$baseUrl/projects/$projectId/github-repo") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(LinkGithubRepositoryRequest(repositoryUrl))
        }.bodyPayload<ProjectResponse>().toDomain()

    suspend fun unlinkGithubRepository(accessToken: String, projectId: Long): Project =
        client.delete("$baseUrl/projects/$projectId/github-repo") {
            bearerAuth(accessToken)
        }.bodyPayload<ProjectResponse>().toDomain()

    suspend fun getGithubPullRequests(accessToken: String, projectId: Long): GithubPullRequestBoard =
        client.get("$baseUrl/projects/$projectId/github/pulls") {
            bearerAuth(accessToken)
        }.bodyPayload<GithubPullRequestBoardResponse>().toDomain()

    suspend fun getGithubPullRequest(accessToken: String, projectId: Long, prNumber: Int): GithubPullRequest =
        client.get("$baseUrl/projects/$projectId/github/pulls/$prNumber") {
            bearerAuth(accessToken)
        }.bodyPayload<GithubPullRequestResponse>().toDomain()

    suspend fun getGithubPullRequestFiles(
        accessToken: String,
        projectId: Long,
        prNumber: Int,
    ): List<GithubPullRequestFile> =
        client.get("$baseUrl/projects/$projectId/github/pulls/$prNumber/files") {
            bearerAuth(accessToken)
        }.bodyPayload<List<GithubPullRequestFileResponse>>()
            .map(GithubPullRequestFileResponse::toDomain)

    suspend fun mergeGithubPullRequest(
        accessToken: String,
        projectId: Long,
        prNumber: Int,
    ): GithubMergeResult = client.post("$baseUrl/projects/$projectId/github/pulls/$prNumber/merge") {
        bearerAuth(accessToken)
    }.bodyPayload<GithubMergeResultResponse>().toDomain()

    suspend fun approveGithubPullRequest(
        accessToken: String,
        projectId: Long,
        prNumber: Int,
    ): GithubApproveResult = client.post("$baseUrl/projects/$projectId/github/pulls/$prNumber/approve") {
        bearerAuth(accessToken)
    }.bodyPayload<GithubApproveResultResponse>().toDomain()

    suspend fun reorderProjects(accessToken: String, teamId: Long, orderedProjectIds: List<Long>): List<Project> =
        client.patch("$baseUrl/teams/$teamId/projects/reorder") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ReorderProjectsRequest(orderedProjectIds))
        }.bodyPayload<List<ProjectResponse>>().map(ProjectResponse::toDomain)

    private companion object {
        const val PAGE_SIZE = 50
    }

    private suspend fun <T> fetchAllPages(fetch: suspend (page: Int) -> PageResponse<T>?): List<T> {
        val result = mutableListOf<T>()
        var page = 0
        while (true) {
            val pageData = fetch(page) ?: break
            result.addAll(pageData.content)
            if (page >= pageData.totalPages - 1) break
            page++
        }
        return result
    }

    @Serializable
    private data class CreateProjectRequest(val teamId: Long, val name: String, val description: String?)

    @Serializable
    private data class AddProjectMemberRequest(val userId: Long, val role: String)

    @Serializable
    private data class UpdateProjectMemberRoleRequest(val role: String)

    @Serializable
    private data class LinkGithubRepositoryRequest(val githubRepoUrl: String)

    @Serializable
    private data class ReorderProjectsRequest(val orderedProjectIds: List<Long>)

    @Serializable
    private data class ProjectResponse(
        val id: Long,
        val teamId: Long,
        val name: String,
        val description: String? = null,
        val status: String,
        val createdBy: Long,
        val position: Int = 0,
        val createdAt: String? = null,
        val updatedAt: String? = null,
        val memberCount: Long? = null,
        val githubRepoUrl: String? = null,
    ) {
        fun toDomain(): Project = Project(
            id = id,
            teamId = teamId,
            name = name,
            description = description,
            status = status.toProjectStatus(),
            createdBy = createdBy,
            position = position,
            createdAt = createdAt,
            updatedAt = updatedAt,
            memberCount = memberCount,
            githubRepoUrl = githubRepoUrl,
        )
    }

    @Serializable
    private data class ProjectMemberResponse(
        val id: Long,
        val projectId: Long,
        val userId: Long,
        val role: String,
    ) {
        fun toDomain(): ProjectMember = ProjectMember(
            id = id,
            projectId = projectId,
            userId = userId,
            role = role.toProjectRole(),
        )
    }

    @Serializable
    private data class GithubPullRequestBoardResponse(
        val draft: List<GithubPullRequestSummaryResponse> = emptyList(),
        val inReview: List<GithubPullRequestSummaryResponse> = emptyList(),
    ) {
        fun toDomain() = GithubPullRequestBoard(
            draft = draft.map(GithubPullRequestSummaryResponse::toDomain),
            inReview = inReview.map(GithubPullRequestSummaryResponse::toDomain),
        )
    }

    @Serializable
    private data class GithubPullRequestSummaryResponse(
        val number: Int,
        val title: String,
        val author: String,
        val state: String,
        val draft: Boolean,
        val merged: Boolean,
        val htmlUrl: String,
        val labels: List<String> = emptyList(),
        val createdAt: String,
        val updatedAt: String,
    ) {
        fun toDomain() = GithubPullRequestSummary(
            number = number,
            title = title,
            author = author,
            state = state,
            isDraft = draft,
            isMerged = merged,
            htmlUrl = htmlUrl,
            labels = labels,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    @Serializable
    private data class GithubPullRequestResponse(
        val number: Int,
        val title: String,
        val body: String? = null,
        val author: String,
        val state: String,
        val mergeable: Boolean? = null,
        val mergeableState: String,
        val reviewDecision: String? = null,
        val headRef: String,
        val baseRef: String,
        val htmlUrl: String,
    ) {
        fun toDomain() = GithubPullRequest(
            number = number,
            title = title,
            body = body,
            author = author,
            state = state,
            mergeable = mergeable,
            mergeableState = mergeableState,
            reviewDecision = reviewDecision,
            headRef = headRef,
            baseRef = baseRef,
            htmlUrl = htmlUrl,
        )
    }

    @Serializable
    private data class GithubPullRequestFileResponse(
        val filename: String,
        val status: String,
        val additions: Int,
        val deletions: Int,
        val patch: String? = null,
    ) {
        fun toDomain() = GithubPullRequestFile(filename, status, additions, deletions, patch)
    }

    @Serializable
    private data class GithubMergeResultResponse(
        val alreadyMerged: Boolean,
        val prUrl: String,
        val prNumber: Int,
    ) {
        fun toDomain() = GithubMergeResult(alreadyMerged, prUrl, prNumber)
    }

    @Serializable
    private data class GithubApproveResultResponse(
        val prUrl: String,
        val prNumber: Int,
    ) {
        fun toDomain() = GithubApproveResult(prUrl, prNumber)
    }

}

private fun String.toProjectStatus(): ProjectStatus = when (uppercase()) {
    "ACTIVE" -> ProjectStatus.Active
    "ARCHIVED" -> ProjectStatus.Archived
    else -> ProjectStatus.Unknown
}

private fun String.toProjectRole(): ProjectRole = when (uppercase()) {
    "OWNER" -> ProjectRole.Owner
    "EDITOR" -> ProjectRole.Editor
    "VIEWER" -> ProjectRole.Viewer
    else -> ProjectRole.Unknown
}

private fun ProjectRole.toApiValue(): String = when (this) {
    ProjectRole.Owner -> "OWNER"
    ProjectRole.Editor -> "EDITOR"
    ProjectRole.Viewer -> "VIEWER"
    ProjectRole.Unknown -> "VIEWER"
}
