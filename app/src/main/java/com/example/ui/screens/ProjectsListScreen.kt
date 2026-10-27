package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.model.ProjectCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlotaViewModel
import com.example.ui.viewmodel.Screen
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.example.ui.viewmodel.WorkspaceTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(
    viewModel: FlotaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val allProjects by viewModel.allProjects.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLang == "UR"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ProjectCategory?>(null) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    // Deletion confirmation dialog
    projectToDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text(if (isUrdu) "پروجیکٹ ڈیلیٹ کریں؟" else "Delete Project?") },
            text = {
                Text(
                    if (isUrdu)
                        "کیا آپ واقعی '${proj.title}' اور اس کی تمام فائلیں ڈیلیٹ کرنا چاہتے ہیں؟ یہ عمل واپس نہیں ہو سکتا۔"
                    else
                        "Are you sure you want to permanently delete '${proj.title}' and its generated files? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCurrentProject(proj.id)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isUrdu) "ڈیلیٹ کریں" else "Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { projectToDelete = null }) {
                    Text(if (isUrdu) "منسوخ کریں" else "Cancel")
                }
            }
        )
    }

    val filteredProjects = allProjects.filter { proj ->
        val matchesQuery = proj.title.contains(searchQuery, ignoreCase = true) ||
                proj.description.contains(searchQuery, ignoreCase = true)
        val matchesCat = selectedCategory == null || proj.category == selectedCategory
        matchesQuery && matchesCat
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isUrdu) "تمام پروجیکٹس" else "Saved Projects",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FlotaTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FlotaTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.HOME) }) {
                        Icon(Icons.Default.Add, contentDescription = "New Project", tint = FlotaGreenDark)
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
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                placeholder = {
                    Text(
                        if (isUrdu) "پروجیکٹس میں تلاش کریں..." else "Search projects by name or category...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FlotaTextTertiary
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = FlotaTextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = FlotaTextTertiary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlotaGreenPrimary,
                    unfocusedBorderColor = FlotaBorder,
                    focusedContainerColor = FlotaSurfaceTinted,
                    unfocusedContainerColor = FlotaSurfaceTinted
                )
            )

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${allProjects.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlotaGreenContainer,
                            selectedLabelColor = FlotaGreenDeep
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = selectedCategory == ProjectCategory.BIOSCIENCE,
                        onClick = { selectedCategory = ProjectCategory.BIOSCIENCE },
                        label = { Text("🧬 BioScience") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlotaGreenContainer,
                            selectedLabelColor = FlotaGreenDeep
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = selectedCategory == ProjectCategory.BUSINESS,
                        onClick = { selectedCategory = ProjectCategory.BUSINESS },
                        label = { Text("💼 Business") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlotaGreenContainer,
                            selectedLabelColor = FlotaGreenDeep
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = selectedCategory == ProjectCategory.ECOMMERCE,
                        onClick = { selectedCategory = ProjectCategory.ECOMMERCE },
                        label = { Text("🛍️ Store") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlotaGreenContainer,
                            selectedLabelColor = FlotaGreenDeep
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = selectedCategory == ProjectCategory.PORTFOLIO,
                        onClick = { selectedCategory = ProjectCategory.PORTFOLIO },
                        label = { Text("🎨 Portfolio") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlotaGreenContainer,
                            selectedLabelColor = FlotaGreenDeep
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Projects List
            if (filteredProjects.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FolderOff,
                            contentDescription = null,
                            tint = FlotaTextTertiary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isUrdu) "کوئی پروجیکٹ نہیں ملا" else "No matching projects found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlotaTextPrimary
                        )
                        Text(
                            text = if (isUrdu) "نیا پروجیکٹ بنانے کے لیے ہوم اسکرین پر جائیں" else "Create a project from the Home screen",
                            style = MaterialTheme.typography.bodySmall,
                            color = FlotaTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProjects) { proj ->
                        ProjectItemCard(
                            project = proj,
                            formattedDate = dateFormat.format(Date(proj.updatedAt)),
                            onOpenPreview = { viewModel.openProject(proj, WorkspaceTab.PREVIEW) },
                            onOpenCode = { viewModel.openProject(proj, WorkspaceTab.CODE) },
                            onOpenChat = { viewModel.openProjectConversation(proj) },
                            onDelete = { projectToDelete = proj }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectItemCard(
    project: Project,
    formattedDate: String,
    onOpenPreview: () -> Unit,
    onOpenCode: () -> Unit,
    onOpenChat: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPreview)
            .testTag("project_item_${project.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (project.category) {
                            ProjectCategory.BIOSCIENCE -> "🧬"
                            ProjectCategory.ECOMMERCE -> "🛍️"
                            ProjectCategory.PORTFOLIO -> "🎨"
                            else -> "💼"
                        },
                        fontSize = 20.sp
                    )
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlotaTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = FlotaGreenContainer
                ) {
                    Text(
                        text = "v${project.versionNumber}.0",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FlotaGreenDeep,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodySmall,
                color = FlotaTextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updated $formattedDate • ${project.filesCount} files",
                    style = MaterialTheme.typography.labelSmall,
                    color = FlotaTextTertiary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onOpenChat, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat",
                            tint = FlotaTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onOpenCode, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = "Code",
                            tint = FlotaTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onOpenPreview, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Preview",
                            tint = FlotaGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
