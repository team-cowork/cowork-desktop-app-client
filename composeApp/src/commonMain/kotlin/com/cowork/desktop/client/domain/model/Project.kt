package com.cowork.desktop.client.domain.model

data class Project(
    val id: Long,
    val teamId: Long,
    val name: String,
    val description: String?,
    val status: ProjectStatus,
    val createdBy: Long,
    val position: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val memberCount: Long? = null,
    val githubRepoUrl: String? = null,
)

enum class ProjectStatus {
    Active,
    Archived,
    Unknown,
}

data class ProjectMember(
    val id: Long,
    val projectId: Long,
    val userId: Long,
    val role: ProjectRole,
)

enum class ProjectRole {
    Owner,
    Editor,
    Viewer,
    Unknown,
}

data class GithubPullRequestBoard(
    val draft: List<GithubPullRequestSummary>,
    val inReview: List<GithubPullRequestSummary>,
)

data class GithubPullRequestSummary(
    val number: Int,
    val title: String,
    val author: String,
    val state: String,
    val isDraft: Boolean,
    val isMerged: Boolean,
    val htmlUrl: String,
    val labels: List<String>,
    val createdAt: String,
    val updatedAt: String,
)

data class GithubPullRequest(
    val number: Int,
    val title: String,
    val body: String?,
    val author: String,
    val state: String,
    val mergeable: Boolean?,
    val mergeableState: String,
    val reviewDecision: String?,
    val headRef: String,
    val baseRef: String,
    val htmlUrl: String,
)

data class GithubPullRequestFile(
    val filename: String,
    val status: String,
    val additions: Int,
    val deletions: Int,
    val patch: String?,
)

data class GithubMergeResult(
    val alreadyMerged: Boolean,
    val prUrl: String,
    val prNumber: Int,
)

data class GithubApproveResult(
    val prUrl: String,
    val prNumber: Int,
)
