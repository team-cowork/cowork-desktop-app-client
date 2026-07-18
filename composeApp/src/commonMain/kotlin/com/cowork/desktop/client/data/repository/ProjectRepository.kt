package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.GithubApproveResult
import com.cowork.desktop.client.domain.model.GithubMergeResult
import com.cowork.desktop.client.domain.model.GithubPullRequest
import com.cowork.desktop.client.domain.model.GithubPullRequestBoard
import com.cowork.desktop.client.domain.model.GithubPullRequestFile
import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.ProjectMember
import com.cowork.desktop.client.domain.model.ProjectRole

interface ProjectRepository {
    suspend fun getTeamProjects(teamId: Long): List<Project>
    suspend fun getMyProjects(): List<Project>
    suspend fun getProject(projectId: Long): Project
    suspend fun createProject(teamId: Long, name: String, description: String?): Project
    suspend fun updateProject(
        projectId: Long,
        name: String? = null,
        description: String? = null,
        status: String? = null,
        clearDescription: Boolean = false,
    ): Project
    suspend fun deleteProject(projectId: Long)
    suspend fun getMembers(projectId: Long): List<ProjectMember>
    suspend fun addMember(projectId: Long, userId: Long, role: ProjectRole): ProjectMember
    suspend fun removeMember(projectId: Long, memberId: Long)
    suspend fun updateMemberRole(projectId: Long, memberId: Long, role: ProjectRole): ProjectMember
    suspend fun linkGithubRepository(projectId: Long, repositoryUrl: String): Project
    suspend fun unlinkGithubRepository(projectId: Long): Project
    suspend fun getGithubPullRequests(projectId: Long): GithubPullRequestBoard
    suspend fun getGithubPullRequest(projectId: Long, prNumber: Int): GithubPullRequest
    suspend fun getGithubPullRequestFiles(projectId: Long, prNumber: Int): List<GithubPullRequestFile>
    suspend fun mergeGithubPullRequest(projectId: Long, prNumber: Int): GithubMergeResult
    suspend fun approveGithubPullRequest(projectId: Long, prNumber: Int): GithubApproveResult
    suspend fun reorderProjects(teamId: Long, orderedProjectIds: List<Long>): List<Project>
}
