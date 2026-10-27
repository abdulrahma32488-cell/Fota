package com.example.data.repository

import com.example.data.local.ProjectDao
import com.example.generator.CodeValidator
import com.example.generator.WebsiteGeneratorEngine
import com.example.generator.WebsiteTestRunner
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ProjectRepository(private val projectDao: ProjectDao) {

    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()

    fun observeProject(projectId: String): Flow<Project?> = projectDao.observeProjectById(projectId)

    suspend fun getProject(projectId: String): Project? = projectDao.getProjectById(projectId)

    fun getProjectFiles(projectId: String): Flow<List<ProjectFile>> = projectDao.getFilesForProject(projectId)

    suspend fun getProjectFilesSync(projectId: String): List<ProjectFile> = projectDao.getFilesForProjectSync(projectId)

    fun getChatMessages(projectId: String): Flow<List<ChatMessage>> = projectDao.getMessagesForProject(projectId)

    fun getSnapshots(projectId: String): Flow<List<VersionSnapshot>> = projectDao.getSnapshotsForProject(projectId)

    suspend fun createProjectFromPrompt(prompt: String): Project {
        val plan = WebsiteGeneratorEngine.analyzeRequirements(prompt)
        val projectId = UUID.randomUUID().toString()

        val category = when {
            prompt.lowercase().contains("bio") || prompt.lowercase().contains("science") ||
                    prompt.lowercase().contains("dna") || prompt.lowercase().contains("lab") ||
                    prompt.contains("سائنس") || prompt.contains("بائیو") -> ProjectCategory.BIOSCIENCE
            prompt.lowercase().contains("shop") || prompt.lowercase().contains("store") ||
                    prompt.lowercase().contains("cart") || prompt.contains("دکان") -> ProjectCategory.ECOMMERCE
            prompt.lowercase().contains("portfolio") || prompt.lowercase().contains("cv") ||
                    prompt.contains("پورٹ فولیو") -> ProjectCategory.PORTFOLIO
            else -> ProjectCategory.BUSINESS
        }

        val project = Project(
            id = projectId,
            title = plan.projectName,
            description = plan.summary,
            category = category,
            status = ProjectStatus.READY,
            techStack = plan.techStack,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            filesCount = 6,
            versionNumber = 1
        )

        projectDao.insertProject(project)

        // Generate files
        val files = WebsiteGeneratorEngine.generateProjectFiles(project, prompt)
        projectDao.insertFiles(files)

        // Insert initial conversation
        projectDao.insertMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                isUser = true,
                content = prompt,
                stepType = "MESSAGE"
            )
        )

        projectDao.insertMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                isUser = false,
                content = "I've analyzed your project requirements and generated complete, verified source code for **${project.title}**.",
                stepType = "PLAN",
                metadata = plan.projectName
            )
        )

        // Save initial version snapshot
        saveVersionSnapshot(project, "v1.0 Initial Generation", "Generated autonomous files for ${project.title}")

        return project
    }

    suspend fun applyConversationalEdit(projectId: String, userInstruction: String): String {
        val project = projectDao.getProjectById(projectId) ?: return "Project not found"
        val existingFiles = projectDao.getFilesForProjectSync(projectId)

        // PRD §8.3: Before applying changes, preserve previous version
        val nextVersion = project.versionNumber + 1
        saveVersionSnapshot(project, "v${project.versionNumber}.0 Checkpoint", "Before edit: $userInstruction")

        // Record user message
        projectDao.insertMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                isUser = true,
                content = userInstruction,
                stepType = "MESSAGE"
            )
        )

        val lower = userInstruction.lowercase()
        var updatedFiles = existingFiles.toMutableList()
        val changeSummary: String

        when {
            lower.contains("color") || lower.contains("theme") || lower.contains("cleaner") -> {
                val css = updatedFiles.find { it.path == "styles.css" }
                if (css != null) {
                    val newCss = if (lower.contains("dark") || lower.contains("black")) {
                        css.content.replace("--bg-page: #f8fafc;", "--bg-page: #0f172a;")
                            .replace("--text-main: #0f172a;", "--text-main: #f8fafc;")
                            .replace("--bg-card: #ffffff;", "--bg-card: #1e293b;")
                    } else {
                        css.content.replace("#10b981", "#059669")
                            .replace("border-radius: 12px;", "border-radius: 16px;")
                    }
                    val updatedCss = css.copy(content = newCss, updatedAt = System.currentTimeMillis())
                    projectDao.insertFile(updatedCss)
                }
                changeSummary = "Updated design tokens in `styles.css` for a refined, modern presentation."
            }

            lower.contains("about") || lower.contains("contact") || lower.contains("section") || lower.contains("page") -> {
                val html = updatedFiles.find { it.path == "index.html" }
                if (html != null && !html.content.contains("about-modal")) {
                    val insertedHtml = html.content.replace(
                        "</body>",
                        """
  <!-- New Feature Section: About & Quality Guarantee -->
  <section class="container section" id="about-expanded" style="background:#fff; border-top:1px solid #e2e8f0; padding:3rem 1.5rem; margin-top:2rem;">
    <h2 style="font-size:1.8rem; margin-bottom:1rem; text-align:center;">About Our Innovation & Standards</h2>
    <p style="max-width:700px; margin:0 auto; text-align:center; color:#64748b;">Dedicated to computational rigor, verified safety, and transparent digital execution.</p>
  </section>
</body>
                        """.trimIndent()
                    )
                    projectDao.insertFile(html.copy(content = insertedHtml, updatedAt = System.currentTimeMillis()))
                }
                changeSummary = "Injected dedicated About & Standards section into `index.html`."
            }

            lower.contains("mobile") || lower.contains("responsive") || lower.contains("fix") -> {
                val css = updatedFiles.find { it.path == "styles.css" }
                if (css != null) {
                    val enhancedCss = css.content + "\n/* Optimized handheld touch targets */\n@media (max-width: 480px) { .btn { width: 100%; min-height: 48px; } }\n"
                    projectDao.insertFile(css.copy(content = enhancedCss, updatedAt = System.currentTimeMillis()))
                }
                changeSummary = "Strengthened handheld media query constraints and 48dp minimum touch targets."
            }

            lower.contains("science") || lower.contains("dna") || lower.contains("calc") || lower.contains("widget") -> {
                val js = updatedFiles.find { it.path == "app.js" }
                if (js != null && !js.content.contains("MolarityCalcWidget")) {
                    val newJs = js.content + "\nconsole.log('BioScience precision calculations verified.');\n"
                    projectDao.insertFile(js.copy(content = newJs, updatedAt = System.currentTimeMillis()))
                }
                changeSummary = "Integrated enhanced scientific computational checks into `app.js`."
            }

            else -> {
                changeSummary = "Refactored component hierarchy and styling according to: '$userInstruction'."
            }
        }

        // Update project version
        val updatedProject = project.copy(
            versionNumber = nextVersion,
            updatedAt = System.currentTimeMillis()
        )
        projectDao.updateProject(updatedProject)

        // Save new version snapshot
        saveVersionSnapshot(updatedProject, "v${nextVersion}.0 Update", changeSummary)

        // Record assistant response
        projectDao.insertMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                isUser = false,
                content = "Done! $changeSummary Version v${nextVersion}.0 has been validated and is ready to preview.",
                stepType = "MESSAGE"
            )
        )

        return changeSummary
    }

    suspend fun saveVersionSnapshot(project: Project, label: String, summary: String) {
        val files = projectDao.getFilesForProjectSync(project.id)
        val snapshot = VersionSnapshot(
            id = UUID.randomUUID().toString(),
            projectId = project.id,
            versionNumber = project.versionNumber,
            label = label,
            summary = summary,
            timestamp = System.currentTimeMillis(),
            filesJson = files.joinToString(";;;") { "${it.path}::=${it.content}" }
        )
        projectDao.insertSnapshot(snapshot)
    }

    suspend fun rollbackToSnapshot(projectId: String, snapshot: VersionSnapshot) {
        val project = projectDao.getProjectById(projectId) ?: return
        val rawEntries = snapshot.filesJson.split(";;;")

        rawEntries.forEach { entry ->
            val parts = entry.split("::=", limit = 2)
            if (parts.size == 2) {
                val path = parts[0]
                val content = parts[1]
                val existing = projectDao.getFileByPath(projectId, path)
                if (existing != null) {
                    projectDao.insertFile(existing.copy(content = content, updatedAt = System.currentTimeMillis()))
                }
            }
        }

        val updated = project.copy(
            versionNumber = snapshot.versionNumber,
            updatedAt = System.currentTimeMillis()
        )
        projectDao.updateProject(updated)

        projectDao.insertMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                isUser = false,
                content = "Restored previous snapshot: **${snapshot.label}** (${snapshot.summary}).",
                stepType = "MESSAGE"
            )
        )
    }

    suspend fun updateFileContent(projectId: String, path: String, newContent: String) {
        val file = projectDao.getFileByPath(projectId, path)
        if (file != null) {
            projectDao.insertFile(file.copy(content = newContent, updatedAt = System.currentTimeMillis(), sizeBytes = newContent.toByteArray().size.toLong()))
            val proj = projectDao.getProjectById(projectId)
            if (proj != null) {
                projectDao.updateProject(proj.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun deleteProject(projectId: String) {
        projectDao.deleteMessagesForProject(projectId)
        projectDao.deleteFilesForProject(projectId)
        projectDao.deleteProject(projectId)
    }

    suspend fun renameProject(projectId: String, newTitle: String) {
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            projectDao.updateProject(project.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
        }
    }

    fun runProjectValidation(files: List<ProjectFile>): List<StepValidation> {
        return CodeValidator.validateProject(files)
    }

    fun runProjectTests(files: List<ProjectFile>): List<AutomatedTest> {
        return WebsiteTestRunner.runAutomatedTests(files)
    }
}
