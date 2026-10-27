package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FlotaDatabase
import com.example.data.repository.ProjectRepository
import com.example.generator.WebsiteGeneratorEngine
import com.example.generator.ZipExportManager
import com.example.model.*
import com.example.speech.SpeechHelper
import com.example.speech.VoiceState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class Screen {
    HOME,
    CONVERSATION,
    WORKSPACE,
    PROJECTS,
    SETTINGS,
    SCIENCE_PORTAL,
    BLUEPRINT
}

enum class WorkspaceTab {
    PREVIEW,
    CODE,
    VALIDATION,
    TESTS,
    DEPLOY,
    HISTORY
}

enum class ViewportMode {
    MOBILE,
    TABLET,
    DESKTOP
}

enum class PlanningState {
    IDLE,
    GENERATING_PLAN,
    PLAN_READY,
    GENERATING_CODE,
    APPROVED,
    ERROR
}

class FlotaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository = ProjectRepository(
        FlotaDatabase.getDatabase(application).projectDao()
    )

    val speechHelper = SpeechHelper(application)
    val voiceState: StateFlow<VoiceState> = speechHelper.voiceState

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen

    private val _screenStack = mutableListOf<Screen>()

    // Bilingual Support: "EN" or "UR"
    private val _currentLanguage = MutableStateFlow("EN")
    val currentLanguage: StateFlow<String> = _currentLanguage

    // Workspace & Project State
    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId

    val allProjects: StateFlow<List<Project>> = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeProject: StateFlow<Project?> = _selectedProjectId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeFiles: StateFlow<List<ProjectFile>> = _selectedProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getProjectFiles(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<ChatMessage>> = _selectedProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getChatMessages(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeSnapshots: StateFlow<List<VersionSnapshot>> = _selectedProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getSnapshots(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Workspace Sub-state
    private val _workspaceTab = MutableStateFlow(WorkspaceTab.PREVIEW)
    val workspaceTab: StateFlow<WorkspaceTab> = _workspaceTab

    private val _viewportMode = MutableStateFlow(ViewportMode.MOBILE)
    val viewportMode: StateFlow<ViewportMode> = _viewportMode

    private val _selectedFile = MutableStateFlow<ProjectFile?>(null)
    val selectedFile: StateFlow<ProjectFile?> = _selectedFile

    companion object {
        const val MAX_PROMPT_LENGTH = 2000
    }

    // UI Input State
    val draftPrompt = MutableStateFlow("")
    val conversationInput = MutableStateFlow("")
    val isGenerating = MutableStateFlow(false)

    // Planning & Blueprint State
    val planningState = MutableStateFlow(PlanningState.IDLE)
    val planningPrompt = MutableStateFlow("")

    // Validation & Tests State
    private val _validationSteps = MutableStateFlow<List<StepValidation>>(emptyList())
    val validationSteps: StateFlow<List<StepValidation>> = _validationSteps

    private val _automatedTests = MutableStateFlow<List<AutomatedTest>>(emptyList())
    val automatedTests: StateFlow<List<AutomatedTest>> = _automatedTests

    // Current Plan for review
    private val _pendingPlan = MutableStateFlow<ProjectPlan?>(null)
    val pendingPlan: StateFlow<ProjectPlan?> = _pendingPlan

    // Status / Toast Messages
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    init {
        // Initialize default sample projects if database is empty
        viewModelScope.launch {
            allProjects.first()
            if (allProjects.value.isEmpty()) {
                seedInitialProjects()
            }
        }
    }

    fun updateDraftPrompt(newText: String) {
        draftPrompt.value = newText
    }

    fun updateConversationInput(newText: String) {
        conversationInput.value = newText
    }

    fun appendDraftPrompt(spokenText: String) {
        val current = draftPrompt.value
        draftPrompt.value = if (current.isBlank()) {
            spokenText
        } else {
            "${current.trim()} $spokenText"
        }
    }

    fun appendConversationInput(spokenText: String) {
        val current = conversationInput.value
        conversationInput.value = if (current.isBlank()) {
            spokenText
        } else {
            "${current.trim()} $spokenText"
        }
    }

    private suspend fun seedInitialProjects() {
        val samplePrompt = "Build an autonomous BioScience and molecular genetics lab portal with DNA sequence analyzer, dilution calculator, and sterile protocol timer"
        repository.createProjectFromPrompt(samplePrompt)
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == "EN") "UR" else "EN"
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            _screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        if (_screenStack.isNotEmpty()) {
            _currentScreen.value = _screenStack.removeAt(_screenStack.size - 1)
            return true
        }
        if (_currentScreen.value != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            return true
        }
        return false
    }

    fun setWorkspaceTab(tab: WorkspaceTab) {
        _workspaceTab.value = tab
        if (tab == WorkspaceTab.VALIDATION && _validationSteps.value.isEmpty()) {
            runValidation()
        }
        if (tab == WorkspaceTab.TESTS && _automatedTests.value.isEmpty()) {
            runTests()
        }
    }

    fun setViewportMode(mode: ViewportMode) {
        _viewportMode.value = mode
    }

    fun selectFile(file: ProjectFile) {
        _selectedFile.value = file
    }

    fun openProject(project: Project, targetTab: WorkspaceTab = WorkspaceTab.PREVIEW) {
        _selectedProjectId.value = project.id
        _workspaceTab.value = targetTab
        _selectedFile.value = null
        _validationSteps.value = emptyList()
        _automatedTests.value = emptyList()
        navigateTo(Screen.WORKSPACE)
    }

    fun openProjectConversation(project: Project) {
        _selectedProjectId.value = project.id
        navigateTo(Screen.CONVERSATION)
    }

    fun generateBlueprint(prompt: String) {
        if (prompt.isBlank()) return
        planningPrompt.value = prompt
        planningState.value = PlanningState.GENERATING_PLAN
        isGenerating.value = true
        viewModelScope.launch {
            try {
                val plan = WebsiteGeneratorEngine.analyzeRequirements(prompt)
                _pendingPlan.value = plan
                planningState.value = PlanningState.PLAN_READY
                isGenerating.value = false
                navigateTo(Screen.BLUEPRINT)
            } catch (e: Exception) {
                isGenerating.value = false
                planningState.value = PlanningState.ERROR
                _statusMessage.value = "Failed to formulate plan: ${e.message}"
            }
        }
    }

    fun regenerateBlueprint() {
        val prompt = planningPrompt.value
        if (prompt.isBlank()) return
        generateBlueprint(prompt)
    }

    fun editBlueprintRequirements(newPrompt: String) {
        planningPrompt.value = newPrompt
    }

    fun approvePlanAndBuild() {
        val plan = _pendingPlan.value ?: return
        val prompt = planningPrompt.value.ifBlank { plan.projectName }

        planningState.value = PlanningState.GENERATING_CODE
        isGenerating.value = true
        viewModelScope.launch {
            try {
                val project = repository.createProjectFromPrompt(prompt)
                _selectedProjectId.value = project.id

                val files = repository.getProjectFilesSync(project.id)
                _validationSteps.value = repository.runProjectValidation(files)
                _automatedTests.value = repository.runProjectTests(files)

                draftPrompt.value = ""
                isGenerating.value = false
                planningState.value = PlanningState.APPROVED
                navigateTo(Screen.WORKSPACE)
            } catch (e: Exception) {
                isGenerating.value = false
                planningState.value = PlanningState.ERROR
                _statusMessage.value = "Error building website from plan: ${e.message}"
            }
        }
    }

    fun submitInitialPrompt(prompt: String) {
        generateBlueprint(prompt)
    }

    fun sendConversationalInstruction(instruction: String) {
        val projId = _selectedProjectId.value ?: return
        if (instruction.isBlank()) return

        isGenerating.value = true
        conversationInput.value = ""
        viewModelScope.launch {
            try {
                val summary = repository.applyConversationalEdit(projId, instruction)
                val files = repository.getProjectFilesSync(projId)
                _validationSteps.value = repository.runProjectValidation(files)
                _automatedTests.value = repository.runProjectTests(files)
                isGenerating.value = false
                _statusMessage.value = summary
            } catch (e: Exception) {
                isGenerating.value = false
                _statusMessage.value = "Failed to apply edit: ${e.message}"
            }
        }
    }

    fun runValidation() {
        val files = activeFiles.value
        if (files.isNotEmpty()) {
            _validationSteps.value = repository.runProjectValidation(files)
        }
    }

    fun runTests() {
        val files = activeFiles.value
        if (files.isNotEmpty()) {
            _automatedTests.value = repository.runProjectTests(files)
        }
    }

    fun exportProjectZip(): File? {
        val proj = activeProject.value ?: return null
        val files = activeFiles.value
        if (files.isEmpty()) return null

        val zipFile = ZipExportManager.createProjectZip(getApplication(), proj, files)
        if (zipFile != null) {
            ZipExportManager.shareProjectZip(getApplication(), zipFile)
            _statusMessage.value = "Source code exported successfully: ${zipFile.name}"
        } else {
            _statusMessage.value = "Failed to create ZIP package."
        }
        return zipFile
    }

    fun restoreVersion(snapshot: VersionSnapshot) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.rollbackToSnapshot(projId, snapshot)
            val files = repository.getProjectFilesSync(projId)
            _validationSteps.value = repository.runProjectValidation(files)
            _automatedTests.value = repository.runProjectTests(files)
            _statusMessage.value = "Restored to ${snapshot.label}"
        }
    }

    fun saveFileEdit(path: String, newContent: String) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.updateFileContent(projId, path, newContent)
            val files = repository.getProjectFilesSync(projId)
            _validationSteps.value = repository.runProjectValidation(files)
            _automatedTests.value = repository.runProjectTests(files)
            _statusMessage.value = "Saved changes to $path"
        }
    }

    fun deleteCurrentProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_selectedProjectId.value == projectId) {
                _selectedProjectId.value = null
                _currentScreen.value = Screen.HOME
            }
            _statusMessage.value = "Project deleted."
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
