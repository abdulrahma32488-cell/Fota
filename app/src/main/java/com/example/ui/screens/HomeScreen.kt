package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.model.ProjectCategory
import com.example.ui.components.VoiceInputDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlotaViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.WorkspaceTab

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FlotaViewModel,
    modifier: Modifier = Modifier
) {
    val draftPrompt by viewModel.draftPrompt.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLang == "UR"

    var showVoiceDialog by remember { mutableStateOf(false) }

    if (showVoiceDialog) {
        VoiceInputDialog(
            speechHelper = viewModel.speechHelper,
            isUrdu = isUrdu,
            onDismiss = { showVoiceDialog = false },
            onConfirmText = { spoken ->
                viewModel.appendDraftPrompt(spoken)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FlotaGreenContainer)
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Flota Logo",
                                tint = FlotaGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Flota",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = FlotaTextPrimary
                        )
                        Surface(
                            shape = CircleShape,
                            color = FlotaGreenContainer,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "AI Studio",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FlotaGreenDeep,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Language switcher button
                    OutlinedButton(
                        onClick = { viewModel.toggleLanguage() },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("lang_toggle_btn")
                    ) {
                        Text(
                            text = if (isUrdu) "English" else "اردو",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlotaGreenDark
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.SCIENCE_PORTAL) },
                        modifier = Modifier.testTag("science_skills_nav_btn")
                    ) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = "Science Skills",
                            tint = FlotaGreenDark
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                        modifier = Modifier.testTag("settings_nav_btn")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = FlotaTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FlotaBackgroundWhite
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = FlotaBackgroundWhite,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* already home */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(if (isUrdu) "ہوم" else "Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlotaGreenDark,
                        selectedTextColor = FlotaGreenDark,
                        indicatorColor = FlotaGreenContainer
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.PROJECTS) },
                    icon = { Icon(Icons.Default.FolderOpen, contentDescription = "Projects") },
                    label = { Text(if (isUrdu) "پروجیکٹس" else "Projects") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlotaGreenDark,
                        selectedTextColor = FlotaGreenDark,
                        indicatorColor = FlotaGreenContainer
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.SCIENCE_PORTAL) },
                    icon = { Icon(Icons.Default.Biotech, contentDescription = "BioScience") },
                    label = { Text(if (isUrdu) "سائنس ٹولز" else "Science") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlotaGreenDark,
                        selectedTextColor = FlotaGreenDark,
                        indicatorColor = FlotaGreenContainer
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text(if (isUrdu) "سیٹنگز" else "Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlotaGreenDark,
                        selectedTextColor = FlotaGreenDark,
                        indicatorColor = FlotaGreenContainer
                    )
                )
            }
        },
        containerColor = FlotaBackgroundWhite,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Welcome Greeting
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isUrdu) "آج آپ کیا بنانا چاہیں گے؟" else "What would you like to build today?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlotaTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isUrdu)
                            "اپنی ویب سائٹ کو آسان زبان میں بیان کریں یا بولیں۔ فلوٹا مکمل کوڈ، ڈیزائن، ٹیسٹنگ اور لائیو پریویو فراہم کرے گا۔"
                        else
                            "Describe your website in plain English or Urdu. Flota plans, writes the frontend & backend code, runs automated tests, and displays a live interactive preview.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FlotaTextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            // Message Composer Card (PRD §7.3)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("message_composer_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = draftPrompt,
                            onValueChange = {
                                if (it.length <= FlotaViewModel.MAX_PROMPT_LENGTH) {
                                    viewModel.updateDraftPrompt(it)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = true,
                                imeAction = ImeAction.Default
                            ),
                            placeholder = {
                                Text(
                                    text = if (isUrdu)
                                        "مثال: ایک جدید بائیو سائنس ریسرچ پورٹل بنائیں جس میں ڈی این اے اینالائزر اور مالیکیولر ڈائیلیوشن کیلکولیٹر ہو..."
                                    else
                                        "e.g. Build a modern BioScience research portal with DNA sequence analyzer, dilution calculator, and protocol runner...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FlotaTextTertiary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 110.dp)
                                .testTag("home_prompt_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlotaGreenPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = FlotaSurfaceTinted,
                                unfocusedContainerColor = FlotaSurfaceTinted
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Voice Microphone Button
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { showVoiceDialog = true },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(FlotaGreenContainer, CircleShape)
                                        .testTag("voice_mic_btn")
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Voice Input",
                                        tint = FlotaGreenDark,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Character Counter Label
                                Text(
                                    text = "${draftPrompt.length} / ${FlotaViewModel.MAX_PROMPT_LENGTH}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (draftPrompt.length >= FlotaViewModel.MAX_PROMPT_LENGTH - 200)
                                        MaterialTheme.colorScheme.error
                                    else
                                        FlotaTextTertiary,
                                    modifier = Modifier.testTag("home_char_counter")
                                )
                            }

                            // Processing or Submit Button
                            if (isGenerating) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .background(FlotaGreenContainer, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = FlotaGreenDark,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = if (isUrdu) "تیار کیا جا رہا ہے..." else "Generating Code...",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FlotaGreenDeep
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.submitInitialPrompt(draftPrompt)
                                    },
                                    enabled = draftPrompt.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark),
                                    modifier = Modifier.testTag("submit_prompt_btn")
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "Build Website",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isUrdu) "ویب سائٹ بنائیں" else "Build Website",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Starter Shortcut Cards (PRD §7.3 + Explicit BioScience requirement)
            item {
                Text(
                    text = if (isUrdu) "شروع کرنے کے لیے شارٹ کٹس" else "Starter Blueprints & Skills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlotaTextPrimary
                )
            }

            // Highlighted BioScience Skills Card
            item {
                StarterActionCard(
                    title = if (isUrdu) "🧬 بائیو سائنس ریسرچ پورٹل" else "🧬 BioScience & Molecular Lab Portal",
                    description = if (isUrdu)
                        "ڈی این اے / آر این اے سیکوینس اینالائزر، ڈائیلیوشن فارمولا (C1V1 = C2V2)، اور لیب پروٹوکول ٹائمر کے ساتھ"
                    else
                        "Complete lab suite with DNA/RNA sequence analysis, molarity dilution stoichiometry, and sterile protocol timer.",
                    tag = "Specialized Science Skill",
                    icon = Icons.Default.Science,
                    highlighted = true,
                    onClick = {
                        viewModel.draftPrompt.value = "Build an autonomous BioScience and molecular genetics lab portal with DNA sequence analyzer, dilution calculator, and sterile protocol timer"
                    }
                )
            }

            item {
                StarterActionCard(
                    title = if (isUrdu) "💼 کاروباری ویب سائٹ بنائیں" else "💼 Create a Business Website",
                    description = if (isUrdu)
                        "خدمات، قیمتوں کے پیکجز، کلائنٹ ریویوز اور رابطہ فارم کے ساتھ پروفیشنل ویب سائٹ"
                    else
                        "Professional enterprise website with services, pricing tables, testimonials, and verified lead capture.",
                    tag = "Enterprise",
                    icon = Icons.Default.Business,
                    onClick = {
                        viewModel.draftPrompt.value = "Create a modern enterprise consulting website for Nexus Solutions with services, client testimonials, pricing plans, and contact inquiry form"
                    }
                )
            }

            item {
                StarterActionCard(
                    title = if (isUrdu) "🛍️ آن لائن سٹور تیار کریں" else "🛍️ Build an Online Store",
                    description = if (isUrdu)
                        "پروڈکٹ کیٹلاگ، کارٹ بیج، چیک آؤٹ اور آرڈر کنفرمیشن کے ساتھ مکمل ای کامرس سٹور"
                    else
                        "Complete storefront with product catalog filters, interactive cart badge, checkout modal, and order flow.",
                    tag = "E-Commerce",
                    icon = Icons.Default.ShoppingCart,
                    onClick = {
                        viewModel.draftPrompt.value = "Build a modern lifestyle clothing and accessories online store with filterable catalog, shopping cart, and instant checkout"
                    }
                )
            }

            item {
                StarterActionCard(
                    title = if (isUrdu) "🎨 پورٹ فولیو ویب سائٹ بنائیں" else "🎨 Create a Portfolio",
                    description = if (isUrdu)
                        "پراجیکٹس شوکیس، مہارتوں کے ٹیگز، ٹائم لائن اور پرسنل بائیو کے ساتھ شاندار پورٹ فولیو"
                    else
                        "Showcase projects, core competencies, experience milestones, and collaboration inquiry form.",
                    tag = "Portfolio",
                    icon = Icons.Default.Person,
                    onClick = {
                        viewModel.draftPrompt.value = "Create a clean developer and designer portfolio with featured projects, skill radar tags, and interactive contact form"
                    }
                )
            }

            // Recent Projects
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUrdu) "حالیہ پروجیکٹس" else "Recent Projects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlotaTextPrimary
                    )

                    if (allProjects.isNotEmpty()) {
                        TextButton(onClick = { viewModel.navigateTo(Screen.PROJECTS) }) {
                            Text(
                                text = if (isUrdu) "تمام دیکھیں" else "View All",
                                color = FlotaGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (allProjects.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = if (isUrdu) "ابھی تک کوئی پروجیکٹ نہیں بنایا گیا۔ اوپر دیے گئے پرامپٹ سے شروع کریں!" else "No projects created yet. Start with a prompt above!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FlotaTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(allProjects.take(3)) { proj ->
                    ProjectQuickItem(
                        project = proj,
                        onOpenWorkspace = { viewModel.openProject(proj, WorkspaceTab.PREVIEW) },
                        onOpenCode = { viewModel.openProject(proj, WorkspaceTab.CODE) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StarterActionCard(
    title: String,
    description: String,
    tag: String,
    icon: ImageVector,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) FlotaGreenContainerSubtle else FlotaSurfaceWhite
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (highlighted) FlotaGreenAccent.copy(alpha = 0.5f) else FlotaBorder
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("starter_card_${tag.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (highlighted) FlotaGreenContainer else FlotaSurfaceMuted,
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (highlighted) FlotaGreenDark else FlotaTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FlotaTextPrimary
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (highlighted) FlotaGreenPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (highlighted) FlotaGreenDeep else FlotaTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = FlotaTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = FlotaTextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProjectQuickItem(
    project: Project,
    onOpenWorkspace: () -> Unit,
    onOpenCode: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenWorkspace)
            .testTag("project_quick_item_${project.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = when (project.category) {
                            ProjectCategory.BIOSCIENCE -> "🧬"
                            ProjectCategory.ECOMMERCE -> "🛍️"
                            ProjectCategory.PORTFOLIO -> "🎨"
                            else -> "💼"
                        },
                        fontSize = 16.sp
                    )
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlotaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FlotaGreenContainer
                    ) {
                        Text(
                            text = "v${project.versionNumber}.0",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FlotaGreenDeep,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.filesCount} files • ${project.techStack}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FlotaTextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onOpenCode,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = "Source Code",
                        tint = FlotaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenWorkspace,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Live Preview",
                        tint = FlotaGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
