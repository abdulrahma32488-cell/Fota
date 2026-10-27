package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.speech.SpeechHelper
import com.example.speech.VoiceState
import com.example.ui.theme.FlotaGreenDark
import com.example.ui.theme.FlotaGreenPrimary
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun VoiceInputDialog(
    speechHelper: SpeechHelper,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmText: (String) -> Unit
) {
    val context = LocalContext.current
    val voiceState by speechHelper.voiceState.collectAsState()
    var recognizedText by remember { mutableStateOf("") }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionDenied = false
            speechHelper.startListening(if (isUrdu) "ur-PK" else "en-US")
        } else {
            permissionDenied = true
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            speechHelper.startListening(if (isUrdu) "ur-PK" else "en-US")
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(voiceState) {
        if (voiceState is VoiceState.Success) {
            recognizedText = (voiceState as VoiceState.Success).text
        }
    }

    // Pulse animation during listening
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(onDismissRequest = {
        speechHelper.stopListening()
        onDismiss()
    }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("voice_input_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUrdu) "آوازی ہدایت" else "Voice Input",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            speechHelper.stopListening()
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (permissionDenied) {
                    Icon(
                        Icons.Default.MicOff,
                        contentDescription = "Permission Denied",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isUrdu)
                            "آواز کی شناخت کے لیے مائیکروفون کی اجازت درکار ہے۔ براہ کرم اجازت دیں یا ٹیکسٹ ان پٹ استعمال کریں۔"
                        else
                            "Microphone permission is required for voice input. Please grant permission or use keyboard text input.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark)
                    ) {
                        Text(if (isUrdu) "اجازت دیں" else "Grant Permission")
                    }
                } else {
                    // Microphone Visualizer
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(90.dp)
                    ) {
                        if (voiceState is VoiceState.Listening) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .scale(pulseScale)
                                    .background(FlotaGreenPrimary.copy(alpha = 0.2f), CircleShape)
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .background(FlotaGreenPrimary, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Listening",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when (voiceState) {
                            is VoiceState.Listening -> if (isUrdu) "سن رہے ہیں... اپنی ویب سائٹ کی ضروریات بولیں" else "Listening... Describe your website"
                            is VoiceState.Success -> if (isUrdu) "پہچان مکمل ہو گئی" else "Speech recognized"
                            is VoiceState.Error -> (voiceState as VoiceState.Error).message
                            else -> if (isUrdu) "تیار ہیں" else "Ready"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (voiceState is VoiceState.Error) MaterialTheme.colorScheme.error else FlotaGreenDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Editable Transcript
                    OutlinedTextField(
                        value = recognizedText,
                        onValueChange = { recognizedText = it },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrectEnabled = true,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                            .testTag("voice_transcript_field"),
                        placeholder = {
                            Text(if (isUrdu) "بولی گئی عبارت یہاں ظاہر ہو گی..." else "Recognized speech will appear here...")
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                speechHelper.startListening(if (isUrdu) "ur-PK" else "en-US")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isUrdu) "دوبارہ سنیں" else "Listen Again")
                        }

                        Button(
                            onClick = {
                                speechHelper.stopListening()
                                onConfirmText(recognizedText)
                                onDismiss()
                            },
                            enabled = recognizedText.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FlotaGreenDark)
                        ) {
                            Text(if (isUrdu) "استعمال کریں" else "Use Speech")
                        }
                    }
                }
            }
        }
    }
}
