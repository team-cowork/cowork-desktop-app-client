package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.ProjectApi
import com.cowork.desktop.client.domain.model.GithubApproveResult
import com.cowork.desktop.client.domain.model.GithubMergeResult
import com.cowork.desktop.client.domain.model.GithubPullRequest
import com.cowork.desktop.client.domain.model.GithubPullRequestBoard
import com.cowork.desktop.client.domain.model.GithubPullRequestFile
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
        clearDescription: Boolean,
    ): Project =
        authorized { projectApi.updateProject(it, projectId, name, description, status, clearDescription) }

    override suspend fun deleteProject(projectId: Long) =
        authorized { projectApi.deleteProject(it, projectId) }

    override suspend fun getMembers(projectId: Long): List<ProjectMember> =
        authorized { projectApi.getMembers(it, projectId) }

    override suspend fun addMember(projectId: Long, userId: Long, role: ProjectRole): ProjectMember =
        authorized { projectApi.addMember(it, projectId, userId, role) }

    override suspend fun removeMember(projectId: Long, memberId: Long) =
        authorized { projectApi.removeMember(it, projectId, memberId) }

    override suspend fun updateMemberRole(projectId: Long, memberId: Long, role: ProjectRole): ProjectMember =
        authorized { projectApi.updateMemberRole(it, projectId, memberId, role) }

    override suspend fun linkGithubRepository(projectId: Long, repositoryUrl: String): Project =
        authorized { projectApi.linkGithubRepository(it, projectId, repositoryUrl) }

    override suspend fun unlinkGithubRepository(projectId: Long): Project =
        authorized { projectApi.unlinkGithubRepository(it, projectId) }

    override suspend fun getGithubPullRequests(projectId: Long): GithubPullRequestBoard =
        authorized { projectApi.getGithubPullRequests(it, projectId) }

    override suspend fun getGithubPullRequest(projectId: Long, prNumber: Int): GithubPullRequest =
        authorized { projectApi.getGithubPullRequest(it, projectId, prNumber) }

    override suspend fun getGithubPullRequestFiles(projectId: Long, prNumber: Int): List<GithubPullRequestFile> =
        authorized { projectApi.getGithubPullRequestFiles(it, projectId, prNumber) }

    override suspend fun mergeGithubPullRequest(projectId: Long, prNumber: Int): GithubMergeResult =
        authorized { projectApi.mergeGithubPullRequest(it, projectId, prNumber) }

    override suspend fun approveGithubPullRequest(projectId: Long, prNumber: Int): GithubApproveResult =
        authorized { projectApi.approveGithubPullRequest(it, projectId, prNumber) }

    override suspend fun reorderProjects(teamId: Long, orderedProjectIds: List<Long>): List<Project> =
        authorized { projectApi.reorderProjects(it, teamId, orderedProjectIds) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
