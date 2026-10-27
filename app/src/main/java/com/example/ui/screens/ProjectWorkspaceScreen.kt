package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.WebViewPreview
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlotaViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.ViewportMode
import com.example.ui.viewmodel.WorkspaceTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectWorkspaceScreen(
    viewModel: FlotaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler {
        viewModel.handleBack()
    }

    val activeProject by viewModel.activeProject.collectAsState()
    val files by viewModel.activeFiles.collectAsState()
    val snapshots by viewModel.activeSnapshots.collectAsState()
    val currentTab by viewModel.workspaceTab.collectAsState()
    val viewportMode by viewModel.viewportMode.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val validationSteps by viewModel.validationSteps.collectAsState()
    val automatedTests by viewModel.automatedTests.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLang == "UR"

    // Default select index.html if no file selected
    LaunchedEffect(files) {
        if (selectedFile == null && files.isNotEmpty()) {
            val defaultFile = files.find { it.path == "index.html" } ?: files.first()
            viewModel.selectFile(defaultFile)
        }
    }

    // Editable content buffer
    var editingContent by remember(selectedFile) {
        mutableStateOf(selectedFile?.content ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = activeProject?.title ?: "Project Workspace",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FlotaTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = FlotaGreenContainer
                            ) {
                                Text(
                                    text = "v${activeProject?.versionNumber ?: 1}.0",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FlotaGreenDeep,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "${files.size} files • ${activeProject?.techStack ?: "HTML/CSS/JS"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FlotaTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.handleBack() },
                        modifier = Modifier.testTag("workspace_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FlotaTextPrimary
                        )
                    }
                },
                actions = {
                    // Chat Button to edit with conversation
                    IconButton(
                        onClick = {
                            activeProject?.let { viewModel.openProjectConversation(it) }
                        },
                        modifier = Modifier.testTag("open_chat_from_workspace_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat with Flota",
                            tint = FlotaGreenDark
                        )
                    }

                    // Export ZIP action
                    IconButton(
                        onClick = {
                            viewModel.exportProjectZip()
                        },
                        modifier = Modifier.testTag("export_zip_top_btn")
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Export ZIP",
                            tint = FlotaGreenDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FlotaBackgroundWhite)
            )
        },
        containerColor = FlotaBackgroundWhite,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Workspace Tab Selector (Scrollable Row)
            ScrollableTabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = FlotaBackgroundWhite,
                contentColor = FlotaGreenDark,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = FlotaBorderSubtle) },
                modifier = Modifier.testTag("workspace_tabs")
            ) {
                Tab(
                    selected = currentTab == WorkspaceTab.PREVIEW,
                    onClick = { viewModel.setWorkspaceTab(WorkspaceTab.PREVIEW) },
                    text = { Text(if (isUrdu) "لائیو پریویو" else "Live Preview", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_preview")
                )

                Tab(
                    selected = currentTab == WorkspaceTab.CODE,
                    onClick = { viewModel.setWorkspaceTab(WorkspaceTab.CODE) },
                    text = { Text(if (isUrdu) "سورس کوڈ" else "Source Code", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_code")
                )

                Tab(
                    selected = currentTab == WorkspaceTab.VALIDATION,
                    onClick = { viewModel.setWorkspaceTab(WorkspaceTab.VALIDATION) },
                    text = { Text(if (isUrdu) "توثیق (12 مراحل)" else "Validation (12)", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_validation")
                )

                Tab(
                    selected = currentTab == WorkspaceTab.TESTS,
                    onClick = { viewModel.setWorkspaceTab(WorkspaceTab.TESTS) },
                    text = { Text(if (isUrdu) "خودکار ٹیسٹس" else "Tests", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_tests")
                )

                Tab(
                    selected = currentTab == WorkspaceTab.DEPLOY,
                    onClick = { viewModel.setWorkspaceTab(WorkspaceTab.DEPLOY) },
                    text = { Text(if (isUrdu) "ڈپلائی اور ایکسپورٹ" else "Deploy & Export", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_deploy")
                )

                Tab(
                    selected = currentTab == WorkspaceTab.HISTORY,
                    onClick = { viewModel.setWorkspaceTab(WorkspaceTab.HISTORY) },
                    text = { Text(if (isUrdu) "ورژن ہسٹری" else "History", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentTab) {
                    WorkspaceTab.PREVIEW -> {
                        WebViewPreview(
                            files = files,
                            viewportMode = viewportMode,
                            onViewportChange = { viewModel.setViewportMode(it) }
                        )
                    }

                    WorkspaceTab.CODE -> {
                        SourceCodeTab(
                            files = files,
                            selectedFile = selectedFile,
                            editingContent = editingContent,
                            onFileSelect = { viewModel.selectFile(it) },
                            onContentChange = { editingContent = it },
                            onSave = { path, content ->
                                viewModel.saveFileEdit(path, content)
                            },
                            onCopy = { content ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Flota Source Code", content))
                                Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    WorkspaceTab.VALIDATION -> {
                        ValidationTab(
                            steps = validationSteps,
                            onRevalidate = { viewModel.runValidation() }
                        )
                    }

                    WorkspaceTab.TESTS -> {
                        AutomatedTestsTab(
                            tests = automatedTests,
                            onRerun = { viewModel.runTests() }
                        )
                    }

                    WorkspaceTab.DEPLOY -> {
                        DeployAndExportTab(
                            project = activeProject,
                            filesCount = files.size,
                            onExportZip = { viewModel.exportProjectZip() }
                        )
                    }

                    WorkspaceTab.HISTORY -> {
                        VersionHistoryTab(
                            snapshots = snapshots,
                            onRestore = { viewModel.restoreVersion(it) }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SOURCE CODE VIEWER & IN-PLACE EDITOR
// ==========================================
@Composable
fun SourceCodeTab(
    files: List<ProjectFile>,
    selectedFile: ProjectFile?,
    editingContent: String,
    onFileSelect: (ProjectFile) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: (String, String) -> Unit,
    onCopy: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // File Tree Tab Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(FlotaSurfaceTinted)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(files) { file ->
                val isSelected = selectedFile?.id == file.id
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) FlotaGreenContainer else FlotaSurfaceWhite,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) FlotaGreenDark else FlotaBorder
                        )
                    ),
                    modifier = Modifier
                        .clickable { onFileSelect(file) }
                        .testTag("file_tab_${file.path}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = when (file.fileType) {
                                "html" -> "🌐"
                                "css" -> "🎨"
                                "js" -> "⚡"
                                "json" -> "📦"
                                else -> "📄"
                            },
                            fontSize = 12.sp
                        )
                        Text(
                            text = file.path,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) FlotaGreenDeep else FlotaTextPrimary
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = FlotaBorder)

        // File Header & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${selectedFile?.path ?: ""} • ${selectedFile?.sizeBytes ?: 0} bytes",
                style = MaterialTheme.typography.labelSmall,
                color = FlotaTextSecondary,
                fontFamily = FontFamily.Monospace
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { onCopy(editingContent) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = FlotaTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = {
                        selectedFile?.let { onSave(it.path, editingContent) }
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Save", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Code Editor
        OutlinedTextField(
            value = editingContent,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxSize()
                .testTag("code_editor_field"),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp,
                color = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA)
            )
        )
    }
}

// ==========================================
// 12-STEP CODE VALIDATION TAB (PRD §12)
// ==========================================
@Composable
fun ValidationTab(
    steps: List<StepValidation>,
    onRevalidate: () -> Unit
) {
    val passedCount = steps.count { it.passed }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = FlotaGreenContainerSubtle),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaGreenAccent.copy(alpha = 0.5f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Validation Pipeline: $passedCount / 12 Passed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlotaGreenDeep
                        )
                        Text(
                            text = "Automated syntax, accessibility, responsive, and security checks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FlotaTextSecondary
                        )
                    }

                    Button(
                        onClick = onRevalidate,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark)
                    ) {
                        Text("Re-validate")
                    }
                }
            }
        }

        items(steps) { step ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (step.passed) FlotaGreenContainer else Color(0xFFFEE2E2),
                                CircleShape
                            )
                    ) {
                        Icon(
                            if (step.passed) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (step.passed) FlotaGreenDark else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Step ${step.stepIndex}: ${step.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = FlotaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = FlotaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF8FAFC)
                        ) {
                            Text(
                                text = step.details,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = FlotaTextSecondary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }

                        step.fixApplied?.let { fix ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✓ Recovery Patch: $fix",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = FlotaGreenDeep
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// AUTOMATED TEST SUITE TAB (PRD §13.2)
// ==========================================
@Composable
fun AutomatedTestsTab(
    tests: List<AutomatedTest>,
    onRerun: () -> Unit
) {
    val passedCount = tests.count { it.passed }
    val totalTime = tests.sumOf { it.durationMs }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Test Execution: $passedCount / ${tests.size} Passed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlotaTextPrimary
                        )
                        Text(
                            text = "Total Execution Duration: ${totalTime}ms • Zero Mocks",
                            style = MaterialTheme.typography.labelMedium,
                            color = FlotaTextSecondary
                        )
                    }

                    Button(
                        onClick = onRerun,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark)
                    ) {
                        Text("Run Tests")
                    }
                }
            }
        }

        items(tests) { test ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = test.testName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlotaTextPrimary
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (test.passed) FlotaGreenContainer else Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = if (test.passed) "PASSED (${test.durationMs}ms)" else "FAILED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (test.passed) FlotaGreenDeep else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Category: ${test.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlotaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = test.outputMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

// ==========================================
// DEPLOYMENT & ZIP EXPORT TAB (PRD §13.3 & §14)
// ==========================================
@Composable
fun DeployAndExportTab(
    project: Project?,
    filesCount: Int,
    onExportZip: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: One-Click ZIP Source Export
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FlotaGreenContainerSubtle),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaGreenAccent.copy(alpha = 0.5f)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = null,
                            tint = FlotaGreenDark,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Export Complete Source Code (.ZIP)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FlotaGreenDeep
                            )
                            Text(
                                text = "Packages $filesCount project files into a standard verified ZIP archive.",
                                style = MaterialTheme.typography.bodySmall,
                                color = FlotaTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Archive includes index.html, styles.css, app.js, server.js, package.json, and README.md with sanitized configurations (zero private secrets).",
                        style = MaterialTheme.typography.bodySmall,
                        color = FlotaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onExportZip,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark),
                        modifier = Modifier.fillMaxWidth().testTag("export_zip_action_btn")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download / Share Project ZIP", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 2: Hosting Provider Integrations (PRD §13.3)
        item {
            Text(
                text = "Hosting Provider Integrations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FlotaTextPrimary
            )
        }

        item {
            HostingProviderCard(
                providerName = "Firebase Hosting",
                status = "Provider API Ready",
                badgeColor = FlotaGreenContainer,
                badgeTextColor = FlotaGreenDeep,
                description = "Deploy single-page web app to Google's global CDN with automated SSL and custom domain mapping.",
                instructions = "Run: firebase deploy --only hosting"
            )
        }

        item {
            HostingProviderCard(
                providerName = "Vercel",
                status = "Provider API Ready",
                badgeColor = Color(0xFFF1F5F9),
                badgeTextColor = FlotaTextPrimary,
                description = "Edge network deployment with instant serverless API functions and automatic preview URLs.",
                instructions = "Run: vercel --prod"
            )
        }

        item {
            HostingProviderCard(
                providerName = "Netlify / Cloudflare Pages",
                status = "Provider API Ready",
                badgeColor = Color(0xFFF1F5F9),
                badgeTextColor = FlotaTextPrimary,
                description = "Fast git-backed continuous deployment with asset optimization and header rules.",
                instructions = "Run: netlify deploy --prod"
            )
        }
    }
}

@Composable
fun HostingProviderCard(
    providerName: String,
    status: String,
    badgeColor: Color,
    badgeTextColor: Color,
    description: String,
    instructions: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = providerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FlotaTextPrimary
                )
                Surface(
                    shape = CircleShape,
                    color = badgeColor
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = FlotaTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF8FAFC)
            ) {
                Text(
                    text = instructions,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = FlotaTextPrimary,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

// ==========================================
// VERSION HISTORY TAB (PRD §8.3 & §15)
// ==========================================
@Composable
fun VersionHistoryTab(
    snapshots: List<VersionSnapshot>,
    onRestore: (VersionSnapshot) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = FlotaGreenContainerSubtle),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaGreenAccent.copy(alpha = 0.4f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = FlotaGreenDark)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Safety Rollback: Flota creates version checkpoints before every major edit. Restore any past version with one click.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FlotaGreenDeep
                    )
                }
            }
        }

        if (snapshots.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Text("No snapshots recorded yet.", color = FlotaTextTertiary)
                }
            }
        } else {
            items(snapshots) { snap ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = snap.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FlotaTextPrimary
                                )
                                Text(
                                    text = dateFormat.format(Date(snap.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FlotaTextTertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = snap.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = FlotaTextSecondary
                            )
                        }

                        Button(
                            onClick = { onRestore(snap) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenContainer),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Restore",
                                color = FlotaGreenDeep,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
