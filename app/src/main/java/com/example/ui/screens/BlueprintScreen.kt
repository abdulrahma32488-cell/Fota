package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectPlan
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlotaViewModel
import com.example.ui.viewmodel.PlanningState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlueprintScreen(
    viewModel: FlotaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val plan by viewModel.pendingPlan.collectAsState()
    val planningState by viewModel.planningState.collectAsState()
    val planningPrompt by viewModel.planningPrompt.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLang == "UR"

    var editingPrompt by remember(planningPrompt) { mutableStateOf(planningPrompt) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isUrdu) "پروجیکٹ بلیو پرنٹ اور منصوبہ بندی" else "Project Blueprint & Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlotaTextPrimary
                        )
                        Text(
                            text = plan?.projectName ?: "Specification Review",
                            style = MaterialTheme.typography.labelSmall,
                            color = FlotaGreenDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.handleBack() },
                        modifier = Modifier.testTag("blueprint_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FlotaTextPrimary
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = FlotaGreenContainer
                    ) {
                        Text(
                            text = "v1.0 Plan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FlotaGreenDeep,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FlotaBackgroundWhite)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 4.dp,
                color = FlotaBackgroundWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Regenerate Plan Button
                    OutlinedButton(
                        onClick = {
                            viewModel.editBlueprintRequirements(editingPrompt)
                            viewModel.regenerateBlueprint()
                        },
                        enabled = !isGenerating && editingPrompt.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("regenerate_plan_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isUrdu) "دوبارہ بنائیں" else "Regenerate")
                    }

                    // Approve & Generate Code Button
                    Button(
                        onClick = {
                            viewModel.editBlueprintRequirements(editingPrompt)
                            viewModel.approvePlanAndBuild()
                        },
                        enabled = !isGenerating && plan != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("approve_plan_btn")
                    ) {
                        if (isGenerating && planningState == PlanningState.GENERATING_CODE) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isUrdu) "تیار ہو رہا ہے..." else "Building...")
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isUrdu) "منظور کریں اور کوڈ بنائیں" else "Approve & Build", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = FlotaBackgroundWhite,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Planning Status Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (planningState) {
                            PlanningState.ERROR -> MaterialTheme.colorScheme.errorContainer
                            PlanningState.GENERATING_PLAN, PlanningState.GENERATING_CODE -> FlotaGreenContainerSubtle
                            else -> FlotaGreenContainer
                        }
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (planningState == PlanningState.ERROR) MaterialTheme.colorScheme.error else FlotaGreenAccent
                        )
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("blueprint_status_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = FlotaGreenDark,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                if (planningState == PlanningState.ERROR) Icons.Default.Error else Icons.Default.Verified,
                                contentDescription = null,
                                tint = if (planningState == PlanningState.ERROR) MaterialTheme.colorScheme.error else FlotaGreenDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = when (planningState) {
                                    PlanningState.GENERATING_PLAN -> if (isUrdu) "منصوبہ بندی تیار ہو رہی ہے..." else "Formulating Blueprint Specifications..."
                                    PlanningState.GENERATING_CODE -> if (isUrdu) "کوڈ لکھا جا رہا ہے..." else "Generating Full-Stack Code Bundle..."
                                    PlanningState.ERROR -> if (isUrdu) "منصوبہ بندی میں غلطی" else "Planning Error"
                                    else -> if (isUrdu) "بلیو پرنٹ جائزہ کے لیے تیار ہے" else "Blueprint Ready For Review"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (planningState == PlanningState.ERROR) MaterialTheme.colorScheme.error else FlotaGreenDeep
                            )
                            Text(
                                text = if (isUrdu)
                                    "کوڈ تیار کرنے سے پہلے تمام صفحات، فیچرز اور آرکیٹیکچر کا جائزہ لیں یا ترمیم کریں۔"
                                else
                                    "Review information architecture, proposed pages, features, and tech stack prior to code generation.",
                                style = MaterialTheme.typography.labelSmall,
                                color = FlotaTextSecondary
                            )
                        }
                    }
                }
            }

            // Section 1: Executive Summary
            plan?.let { p ->
                item {
                    BlueprintSectionCard(
                        title = "📋 Executive Summary & Audience",
                        badge = "Overview"
                    ) {
                        Text(
                            text = p.projectName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = FlotaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = p.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FlotaTextSecondary,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = FlotaGreenDark, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Target Audience: ${p.targetAudience}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = FlotaGreenDeep
                            )
                        }
                    }
                }

                // Section 2: Information Architecture & Pages
                item {
                    BlueprintSectionCard(
                        title = "🌐 Pages & Information Architecture",
                        badge = "${p.requiredPages.size} Pages"
                    ) {
                        p.requiredPages.forEachIndexed { idx, page ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = FlotaGreenContainer,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${idx + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = FlotaGreenDeep
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = page,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = FlotaTextPrimary
                                )
                            }
                        }
                    }
                }

                // Section 3: Core Features & Functionality
                item {
                    BlueprintSectionCard(
                        title = "⚡ Core Features & Capabilities",
                        badge = "${p.coreFeatures.size} Features"
                    ) {
                        p.coreFeatures.forEach { feat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FlotaGreenDark, modifier = Modifier.size(18.dp))
                                Text(
                                    text = feat,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FlotaTextPrimary
                                )
                            }
                        }
                    }
                }

                // Section 4: Science Features (if applicable)
                if (p.scienceFeatures.isNotEmpty()) {
                    item {
                        BlueprintSectionCard(
                            title = "🧬 Specialized Domain / Science Skills",
                            badge = "Science Suite"
                        ) {
                            p.scienceFeatures.forEach { sFeat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Biotech, contentDescription = null, tint = FlotaGreenDark, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = sFeat,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = FlotaGreenDeep
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 5: Tech Stack & Architecture
                item {
                    BlueprintSectionCard(
                        title = "💻 Tech Stack & Architecture",
                        badge = if (p.requiresBackend) "Full-Stack" else "Frontend"
                    ) {
                        Text(
                            text = p.techStack,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = FlotaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FlotaSurfaceTinted,
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
                            ) {
                                Text(
                                    text = if (p.requiresBackend) "✓ Node.js REST API Included" else "✓ Static Web Bundle",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FlotaTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Section 6: Validation & Quality Assurance Plan
                item {
                    BlueprintSectionCard(
                        title = "🛡️ Validation & Quality Assurance Plan",
                        badge = "${p.validationChecks.size} Pre-flight Checks"
                    ) {
                        p.validationChecks.forEach { check ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = FlotaGreenDark, modifier = Modifier.size(16.dp))
                                Text(
                                    text = check,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FlotaTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Section 7: Edit Requirements Prompt (with Character Counter!)
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder)),
                    modifier = Modifier.fillMaxWidth().testTag("edit_blueprint_requirements_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isUrdu) "ضروریات میں ترمیم کریں" else "Edit Requirements & Prompt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FlotaTextPrimary
                            )

                            // Character Counter Indicator
                            Text(
                                text = "${editingPrompt.length} / ${FlotaViewModel.MAX_PROMPT_LENGTH}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (editingPrompt.length >= FlotaViewModel.MAX_PROMPT_LENGTH - 200)
                                    MaterialTheme.colorScheme.error
                                else
                                    FlotaTextTertiary,
                                modifier = Modifier.testTag("blueprint_char_counter")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editingPrompt,
                            onValueChange = {
                                if (it.length <= FlotaViewModel.MAX_PROMPT_LENGTH) {
                                    editingPrompt = it
                                    viewModel.editBlueprintRequirements(it)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = true,
                                imeAction = ImeAction.Default
                            ),
                            placeholder = { Text("Modify or refine prompt requirements...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp)
                                .testTag("blueprint_prompt_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlotaGreenPrimary,
                                unfocusedBorderColor = FlotaBorder,
                                focusedContainerColor = FlotaSurfaceTinted,
                                unfocusedContainerColor = FlotaSurfaceTinted
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BlueprintSectionCard(
    title: String,
    badge: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlotaTextPrimary
                )
                Surface(
                    shape = CircleShape,
                    color = FlotaGreenContainer
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FlotaGreenDeep,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
