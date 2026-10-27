package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlotaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SciencePortalScreen(
    viewModel: FlotaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val currentLang by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLang == "UR"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isUrdu) "🧬 بائیو سائنس سکلز سوٹ" else "🧬 BioScience Skills Suite",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = FlotaTextPrimary
                        )
                        Surface(
                            shape = CircleShape,
                            color = FlotaGreenContainer
                        ) {
                            Text(
                                text = "Science Mode",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FlotaGreenDeep,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
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
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FlotaGreenContainerSubtle),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaGreenAccent.copy(alpha = 0.5f)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (isUrdu) "کمپیوٹیشنل بائیولوجی اور لیب ٹولز" else "Computational Biology & Laboratory Engineering",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlotaGreenDeep
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isUrdu)
                                "فلوٹا میں بائیو سائنس کے خصوصی الگورتھمز شامل ہیں: ڈی این اے ٹرانسلیشن، مولیریٹی کیلکولیشن، اور اسٹرائل لیبارٹری پروٹوکولز۔"
                            else
                                "Flota is equipped with specialized bioscience algorithms that generate production-grade research portals, molecular calculators, and clinical biotech sites.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FlotaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.draftPrompt.value = "Build an autonomous BioScience and molecular genetics lab portal with DNA sequence analyzer, dilution calculator, and sterile protocol timer"
                                viewModel.submitInitialPrompt(viewModel.draftPrompt.value)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark),
                            modifier = Modifier.fillMaxWidth().testTag("generate_science_portal_btn")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isUrdu) "ابھی بائیو سائنس پورٹل بنائیں" else "Generate Full BioScience Web App",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (isUrdu) "دستیاب سائنس مہارتیں" else "Active Science Modules & Capabilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlotaTextPrimary
                )
            }

            item {
                ScienceModuleCard(
                    icon = "🧬",
                    title = "Genomic Sequence & Codon Translation",
                    description = "Accurate nucleotide sequence parser that extracts GC content, melting temperature (Tm) via salt-adjusted equations, complementary strands, reverse complements, and codon-to-amino-acid translation using standard NCBI genetic code tables.",
                    features = listOf("Wallace Rule Tm", "GC% Calculation", "Amino Acid Codon Map", "RNA/DNA Conversion")
                )
            }

            item {
                ScienceModuleCard(
                    icon = "🧪",
                    title = "Solution Dilution & Buffer Stoichiometry",
                    description = "Interactive stoichiometry engine solving the classic laboratory buffer dilution equation (C₁ × V₁ = C₂ × V₂). Computes required stock aliquots, solvent volumes, and unit conversions (M, mM, µM, X buffers).",
                    features = listOf("C1V1 = C2V2 Equation", "Buffer Multipliers (10X to 1X)", "Volume Normalization (mL, µL, L)", "Solvent Subtraction")
                )
            }

            item {
                ScienceModuleCard(
                    icon = "⏱️",
                    title = "Sterile Laboratory Protocol Runner",
                    description = "Time-synchronized experiment assistant preloaded with verified protocols for Standard Taq PCR Amplification, Agarose Gel Electrophoresis, and Alkaline Lysis Plasmid Minipreps with step-by-step checklist.",
                    features = listOf("Thermocycler Cycle Steps", "Gel Casting Timers", "Lysis Precision Inversions", "Completion Sound Alerts")
                )
            }

            item {
                ScienceModuleCard(
                    icon = "📚",
                    title = "PubMed / NCBI Biomedical Indexer",
                    description = "Curated scientific research publication feed enabling peer-reviewed literature discovery, DOI citations, and abstract filtering for molecular biology and CRISPR therapeutics.",
                    features = listOf("Abstract Retrieval", "Journal Citation Metadata", "Keyword Search Filtering", "Author Attribution")
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ScienceModuleCard(
    icon: String,
    title: String,
    description: String,
    features: List<String>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FlotaSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, fontSize = 22.sp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlotaTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = FlotaTextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                features.take(2).forEach { feat ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FlotaSurfaceTinted,
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder))
                    ) {
                        Text(
                            text = "✓ $feat",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = FlotaGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
