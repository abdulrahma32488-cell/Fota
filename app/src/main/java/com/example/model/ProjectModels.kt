package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProjectCategory {
    BUSINESS,
    ECOMMERCE,
    PORTFOLIO,
    BIOSCIENCE,
    CUSTOM
}

enum class ProjectStatus {
    PLANNING,
    GENERATING,
    VALIDATING,
    READY,
    DEPLOYED,
    ERROR
}

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: ProjectCategory,
    val status: ProjectStatus = ProjectStatus.READY,
    val techStack: String = "HTML5, CSS3, JavaScript (ES6+)",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isFavorite: Boolean = false,
    val previewUrl: String? = null,
    val versionNumber: Int = 1,
    val filesCount: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "project_files")
data class ProjectFile(
    @PrimaryKey val id: String,
    val projectId: String,
    val path: String,
    val content: String,
    val fileType: String, // "html", "css", "js", "json", "md"
    val sizeBytes: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val projectId: String,
    val isUser: Boolean,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stepType: String = "MESSAGE", // "MESSAGE", "PLAN", "VALIDATION", "TEST", "ERROR"
    val metadata: String? = null
)

@Entity(tableName = "version_snapshots")
data class VersionSnapshot(
    @PrimaryKey val id: String,
    val projectId: String,
    val versionNumber: Int,
    val label: String,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val filesJson: String // serialized files snapshot for quick restoration
)

data class StepValidation(
    val stepIndex: Int,
    val title: String,
    val description: String,
    val passed: Boolean,
    val details: String,
    val fixApplied: String? = null
)

data class AutomatedTest(
    val testName: String,
    val category: String,
    val passed: Boolean,
    val durationMs: Long,
    val outputMessage: String
)

data class ProjectPlan(
    val projectName: String,
    val summary: String,
    val targetAudience: String,
    val requiredPages: List<String>,
    val coreFeatures: List<String>,
    val scienceFeatures: List<String> = emptyList(),
    val techStack: String,
    val requiresBackend: Boolean,
    val validationChecks: List<String>
)
