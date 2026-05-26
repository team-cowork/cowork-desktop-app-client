package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.ProjectMember
import com.cowork.desktop.client.domain.model.ProjectRole

interface ProjectRepository {
    suspend fun getTeamProjects(teamId: Long): List<Project>
    suspend fun getMyProjects(): List<Project>
    suspend fun getProject(projectId: Long): Project
    suspend fun createProject(teamId: Long, name: String, description: String?): Project
    suspend fun updateProject(projectId: Long, name: String? = null, description: String? = null, status: String? = null): Project
    suspend fun deleteProject(projectId: Long)
    suspend fun getMembers(projectId: Long): List<ProjectMember>
    suspend fun addMember(projectId: Long, userId: Long, role: ProjectRole): ProjectMember
    suspend fun removeMember(projectId: Long, memberId: Long)
    suspend fun reorderProjects(teamId: Long, orderedProjectIds: List<Long>): List<Project>
}
