package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.components.VoiceInputDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlotaViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.WorkspaceTab
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: FlotaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val activeProject by viewModel.activeProject.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val conversationInput by viewModel.conversationInput.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isUrdu = currentLang == "UR"

    var showVoiceDialog by remember { mutableStateOf(false) }

    if (showVoiceDialog) {
        VoiceInputDialog(
            speechHelper = viewModel.speechHelper,
            isUrdu = isUrdu,
            onDismiss = { showVoiceDialog = false },
            onConfirmText = { text ->
                viewModel.appendConversationInput(text)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeProject?.title ?: "Flota Conversation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlotaTextPrimary
                        )
                        Text(
                            text = if (isGenerating) "AI Autonomous Assistant • Working..." else "AI Autonomous Assistant • Ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isGenerating) FlotaGreenDark else FlotaTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.handleBack() },
                        modifier = Modifier.testTag("conversation_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FlotaTextPrimary
                        )
                    }
                },
                actions = {
                    activeProject?.let { proj ->
                        Button(
                            onClick = { viewModel.openProject(proj, WorkspaceTab.PREVIEW) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenContainer),
                            modifier = Modifier.testTag("open_workspace_from_chat_btn")
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                tint = FlotaGreenDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isUrdu) "پریویو" else "Preview",
                                color = FlotaGreenDeep,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FlotaBackgroundWhite)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FlotaBackgroundWhite)
                    .imePadding()
            ) {
                // Quick Suggestion Chips (PRD §8.3)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val suggestions = if (isUrdu) {
                        listOf(
                            "ڈیزائن کو زیادہ صاف ستھرا بنائیں",
                            "ہوم پیج پر About Us سیکشن شامل کریں",
                            "موبائل سکرین کا لے آؤٹ ٹھیک کریں",
                            "سائنس اور ڈی این اے کیلکولیٹر ٹول شامل کریں",
                            "تمام خودکار ٹیسٹس چلائیں"
                        )
                    } else {
                        listOf(
                            "Make the design cleaner",
                            "Add an About Us section",
                            "Fix mobile layout touch targets",
                            "Add DNA sequence translation widget",
                            "Verify all automated tests"
                        )
                    }

                    items(suggestions) { sug ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = FlotaSurfaceTinted,
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder)),
                            modifier = Modifier
                                .clickable {
                                    viewModel.conversationInput.value = sug
                                }
                        ) {
                            Text(
                                text = sug,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = FlotaTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = FlotaBorderSubtle)

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showVoiceDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(FlotaGreenContainer, CircleShape)
                            .testTag("chat_voice_mic_btn")
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = FlotaGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    OutlinedTextField(
                        value = conversationInput,
                        onValueChange = {
                            if (it.length <= FlotaViewModel.MAX_PROMPT_LENGTH) {
                                viewModel.updateConversationInput(it)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrectEnabled = true,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (conversationInput.isNotBlank() && !isGenerating) {
                                    viewModel.sendConversationalInstruction(conversationInput)
                                }
                            }
                        ),
                        placeholder = {
                            Text(
                                if (isUrdu) "تبدیلی یا سوال لکھیں..." else "Suggest edits or ask Flota...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FlotaTextTertiary
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 120.dp)
                            .testTag("chat_text_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlotaGreenPrimary,
                            unfocusedBorderColor = FlotaBorder,
                            focusedContainerColor = FlotaSurfaceTinted,
                            unfocusedContainerColor = FlotaSurfaceTinted
                        )
                    )

                    IconButton(
                        onClick = {
                            viewModel.sendConversationalInstruction(conversationInput)
                        },
                        enabled = conversationInput.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (conversationInput.isNotBlank() && !isGenerating) FlotaGreenDark else Color(0xFFCBD5E1),
                                CircleShape
                            )
                            .testTag("chat_send_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (conversationInput.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${conversationInput.length} / ${FlotaViewModel.MAX_PROMPT_LENGTH}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (conversationInput.length >= FlotaViewModel.MAX_PROMPT_LENGTH - 200)
                                MaterialTheme.colorScheme.error
                            else
                                FlotaTextTertiary,
                            modifier = Modifier.testTag("chat_char_counter")
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            reverseLayout = false
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Info Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FlotaGreenContainerSubtle),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaGreenAccent.copy(alpha = 0.4f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = FlotaGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isUrdu)
                                "تمام ترامیم سے قبل خودکار طور پر نیا ورژن محفوظ کیا جاتا ہے تاکہ آپ کبھی بھی پرانے ورژن پر واپس جا سکیں۔"
                            else
                                "Prior to destructive changes, checkpoints are preserved so you can restore earlier versions at any time.",
                            style = MaterialTheme.typography.labelSmall,
                            color = FlotaGreenDeep
                        )
                    }
                }
            }

            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    onViewWorkspace = {
                        activeProject?.let { proj ->
                            viewModel.openProject(proj, WorkspaceTab.PREVIEW)
                        }
                    }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = FlotaGreenDark,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = if (isUrdu) "فلوٹا کوڈ کی توثیق اور فائلیں اپڈیٹ کر رہا ہے..." else "Flota is generating files, verifying syntax, and running tests...",
                            style = MaterialTheme.typography.bodySmall,
                            color = FlotaTextSecondary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onViewWorkspace: () -> Unit
) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(FlotaGreenContainer)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Flota Assistant",
                    tint = FlotaGreenDark,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) FlotaGreenDark else FlotaSurfaceWhite,
            border = if (!isUser) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlotaBorder)) else null,
            shadowElevation = if (isUser) 1.dp else 2.dp,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else FlotaTextPrimary,
                    lineHeight = 20.sp
                )

                if (message.stepType == "PLAN" && !isUser) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onViewWorkspace,
                        colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenContainer),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = FlotaGreenDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Live Preview",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlotaGreenDeep
                        )
                    }
                }
            }
        }
    }
}
