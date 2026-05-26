package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.ProjectApi
import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.ProjectMember
import com.cowork.desktop.client.domain.model.ProjectRole

class DefaultProjectRepository(
    private val authRepository: AuthRepository,
    private val projectApi: ProjectApi,
) : ProjectRepository {

    override suspend fun getTeamProjects(teamId: Long): List<Project> =
        authorized { projectApi.getTeamProjects(it, teamId) }

    override suspend fun getMyProjects(): List<Project> =
        authorized { projectApi.getMyProjects(it) }

    override suspend fun getProject(projectId: Long): Project =
        authorized { projectApi.getProject(it, projectId) }

    override suspend fun createProject(teamId: Long, name: String, description: String?): Project =
        authorized { projectApi.createProject(it, teamId, name, description) }

    override suspend fun updateProject(
        projectId: Long,
        name: String?,
        description: String?,
        status: String?,
    ): Project =
        authorized { projectApi.updateProject(it, projectId, name, description, status) }

    override suspend fun deleteProject(projectId: Long) =
        authorized { projectApi.deleteProject(it, projectId) }

    override suspend fun getMembers(projectId: Long): List<ProjectMember> =
        authorized { projectApi.getMembers(it, projectId) }

    override suspend fun addMember(projectId: Long, userId: Long, role: ProjectRole): ProjectMember =
        authorized { projectApi.addMember(it, projectId, userId, role) }

    override suspend fun removeMember(projectId: Long, memberId: Long) =
        authorized { projectApi.removeMember(it, projectId, memberId) }

    override suspend fun reorderProjects(teamId: Long, orderedProjectIds: List<Long>): List<Project> =
        authorized { projectApi.reorderProjects(it, teamId, orderedProjectIds) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
