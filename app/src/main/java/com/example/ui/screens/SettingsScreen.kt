package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlotaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FlotaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val currentLang by viewModel.currentLanguage.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val isUrdu = currentLang == "UR"

    var syncEnabled by remember { mutableStateOf(false) }
    var privacyStrict by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isUrdu) "سیٹنگز اور اکاؤنٹ" else "Settings & Account",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FlotaBackgroundWhite)
            )
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
            // Language Selection
            item {
                Text(
                    text = if (isUrdu) "زبان (Language)" else "Language & Localization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlotaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
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
                            Text("Current Language", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isUrdu) "اردو (Urdu) فعال ہے" else "English (US) Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = FlotaTextSecondary
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleLanguage() },
                            colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isUrdu) "Switch to English" else "اردو میں بدلیں")
                        }
                    }
                }
            }

            // Google Account & Cloud Sync (PRD §5.2, §6)
            item {
                Text(
                    text = "Account & Cloud Sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlotaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(FlotaGreenContainer, CircleShape)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = FlotaGreenDark)
                            }
                            Column {
                                Text("waqasali0459@gmail.com", fontWeight = FontWeight.Bold)
                                Text("Google Account Connected", style = MaterialTheme.typography.labelSmall, color = FlotaGreenDeep)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = FlotaBorderSubtle)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cloud Metadata Sync", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Synchronize project summaries and version history to Firebase Firestore.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FlotaTextSecondary
                                )
                            }
                            Switch(
                                checked = syncEnabled,
                                onCheckedChange = { syncEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FlotaGreenDark
                                )
                            )
                        }
                    }
                }
            }

            // Local Storage & Persistence (PRD §5.1)
            item {
                Text(
                    text = "Local Storage & Durability",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlotaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Database Persistence", fontWeight = FontWeight.SemiBold)
                            Text("Room SQLite (Encrypted)", color = FlotaGreenDark, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Stored projects: ${allProjects.size} • Projects and generated source code are stored strictly locally on device and survive app restarts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FlotaTextSecondary
                        )
                    }
                }
            }

            // AI Backend & Security Architecture (PRD §5.4 & §17)
            item {
                Text(
                    text = "Security & AI Backend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlotaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FlotaGreenContainerSubtle),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaGreenAccent.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = FlotaGreenDark)
                            Text("Zero Client-Side Secrets (PRD §5.4)", fontWeight = FontWeight.Bold, color = FlotaGreenDeep)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No production Gemini API keys are embedded inside the APK. Model orchestration and validation are strictly authenticated server-side with sanitization filters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FlotaTextSecondary
                        )
                    }
                }
            }

            // App Version Info
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Flota for Android", fontWeight = FontWeight.Bold)
                        Text("Version 1.0.0 (Production Release)", style = MaterialTheme.typography.bodySmall, color = FlotaTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Autonomous Website Development Assistant with BioScience Skills", style = MaterialTheme.typography.labelSmall, color = FlotaTextTertiary)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
