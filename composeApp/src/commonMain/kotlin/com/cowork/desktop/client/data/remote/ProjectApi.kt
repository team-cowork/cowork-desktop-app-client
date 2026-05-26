package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.ProjectMember
import com.cowork.desktop.client.domain.model.ProjectRole
import com.cowork.desktop.client.domain.model.ProjectStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

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
            }.body<ApiResponse<PageResponse<ProjectResponse>>>().data
        }.map(ProjectResponse::toDomain)

    suspend fun getMyProjects(accessToken: String): List<Project> =
        fetchAllPages { page ->
            client.get("$baseUrl/projects/me") {
                bearerAuth(accessToken)
                parameter("size", PAGE_SIZE)
                parameter("page", page)
            }.body<ApiResponse<PageResponse<ProjectResponse>>>().data
        }.map(ProjectResponse::toDomain)

    suspend fun getProject(accessToken: String, projectId: Long): Project =
        client.get("$baseUrl/projects/$projectId") {
            bearerAuth(accessToken)
        }.body<ApiResponse<ProjectResponse>>().data?.toDomain()
            ?: error("프로젝트 조회 응답에 data가 없습니다")

    suspend fun createProject(accessToken: String, teamId: Long, name: String, description: String?): Project =
        client.post("$baseUrl/projects") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateProjectRequest(teamId = teamId, name = name, description = description))
        }.body<ApiResponse<ProjectResponse>>().data?.toDomain()
            ?: error("프로젝트 생성 응답에 data가 없습니다")

    suspend fun updateProject(
        accessToken: String,
        projectId: Long,
        name: String? = null,
        description: String? = null,
        status: String? = null,
    ): Project =
        client.patch("$baseUrl/projects/$projectId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(UpdateProjectRequest(name = name, description = description, status = status))
        }.body<ApiResponse<ProjectResponse>>().data?.toDomain()
            ?: error("프로젝트 수정 응답에 data가 없습니다")

    suspend fun deleteProject(accessToken: String, projectId: Long) {
        client.delete("$baseUrl/projects/$projectId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun getMembers(accessToken: String, projectId: Long): List<ProjectMember> =
        client.get("$baseUrl/projects/$projectId/members") {
            bearerAuth(accessToken)
        }.body<ApiResponse<List<ProjectMemberResponse>>>().data.orEmpty().map(ProjectMemberResponse::toDomain)

    suspend fun addMember(accessToken: String, projectId: Long, userId: Long, role: ProjectRole): ProjectMember =
        client.post("$baseUrl/projects/$projectId/members") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(AddProjectMemberRequest(userId = userId, role = role.toApiValue()))
        }.body<ApiResponse<ProjectMemberResponse>>().data?.toDomain()
            ?: error("프로젝트 멤버 추가 응답에 data가 없습니다")

    suspend fun removeMember(accessToken: String, projectId: Long, memberId: Long) {
        client.delete("$baseUrl/projects/$projectId/members/$memberId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun reorderProjects(accessToken: String, teamId: Long, orderedProjectIds: List<Long>): List<Project> =
        client.patch("$baseUrl/teams/$teamId/projects/reorder") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ReorderProjectsRequest(orderedProjectIds))
        }.body<ApiResponse<List<ProjectResponse>>>().data.orEmpty().map(ProjectResponse::toDomain)

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
    private data class UpdateProjectRequest(val name: String?, val description: String?, val status: String?)

    @Serializable
    private data class AddProjectMemberRequest(val userId: Long, val role: String)

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
    ) {
        fun toDomain(): Project = Project(
            id = id,
            teamId = teamId,
            name = name,
            description = description,
            status = status.toProjectStatus(),
            createdBy = createdBy,
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
