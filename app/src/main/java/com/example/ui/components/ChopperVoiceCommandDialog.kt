package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.model.VoiceCommandStage
import com.example.ui.model.VoiceCommandUiState
import com.example.ui.theme.ChopperDoctorBlue
import com.example.ui.theme.ChopperGold
import com.example.ui.theme.ChopperPink
import com.example.ui.theme.ChopperPinkContainer

@Composable
fun ChopperVoiceCommandDialog(
    state: VoiceCommandUiState,
    onClose: () -> Unit,
    onAcceptCommand: (String) -> Unit,
    onRetryListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isOpen) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("chopper_voice_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🦌", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chopper Voice Command",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ChopperPink
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp).testTag("close_voice_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Mic / Speaker Avatar
                Box(
                    modifier = Modifier
                        .size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer pulsating ring
                    if (state.isListening || state.stage == VoiceCommandStage.ASKING_UNDERSTANDING || state.stage == VoiceCommandStage.RESPONDING) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    if (state.isListening) ChopperDoctorBlue.copy(alpha = 0.2f)
                                    else ChopperPink.copy(alpha = 0.2f)
                                )
                        )
                    }

                    // Inner main circle
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                when (state.stage) {
                                    VoiceCommandStage.ASKING_UNDERSTANDING -> ChopperPink
                                    VoiceCommandStage.LISTENING_TO_BOSS -> ChopperDoctorBlue
                                    VoiceCommandStage.PROCESSING -> ChopperGold
                                    VoiceCommandStage.RESPONDING -> ChopperPink
                                    else -> Color.Gray
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (state.stage) {
                                VoiceCommandStage.ASKING_UNDERSTANDING, VoiceCommandStage.RESPONDING -> Icons.AutoMirrored.Filled.VolumeUp
                                VoiceCommandStage.LISTENING_TO_BOSS -> Icons.Default.Mic
                                VoiceCommandStage.PROCESSING -> Icons.Default.Psychology
                                else -> Icons.Default.GraphicEq
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stage Title / Status
                Text(
                    text = when (state.stage) {
                        VoiceCommandStage.ASKING_UNDERSTANDING -> "Chopper is asking Boss..."
                        VoiceCommandStage.LISTENING_TO_BOSS -> "Listening to Boss..."
                        VoiceCommandStage.PROCESSING -> "Processing Boss's Command..."
                        VoiceCommandStage.RESPONDING -> "Chopper Responding!"
                        else -> "Voice Assistant Ready"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (state.stage) {
                        VoiceCommandStage.LISTENING_TO_BOSS -> ChopperDoctorBlue
                        VoiceCommandStage.PROCESSING -> Color(0xFFC07000)
                        else -> ChopperPink
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Spoken or Question Bubble
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ChopperPinkContainer.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (state.stage == VoiceCommandStage.ASKING_UNDERSTANDING) {
                            Text(
                                text = "🌸 \"Boss! Do you understand me properly? Chopper is ready to listen to your voice commands!\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else if (state.stage == VoiceCommandStage.LISTENING_TO_BOSS) {
                            Text(
                                text = if (state.recognizedText.isNotBlank())
                                    "\"${state.recognizedText}\""
                                else
                                    "Speak your command: e.g., 'Remind me tomorrow', 'Morning check-in', 'Yes I understand'...",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = if (state.recognizedText.isNotBlank()) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        } else {
                            Text(
                                text = state.chopperSpokenText.ifBlank { state.statusText },
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Response Chips / Buttons
                if (state.stage == VoiceCommandStage.ASKING_UNDERSTANDING) {
                    Text("Boss can also answer instantly:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAcceptCommand("Yes, I understand properly!") },
                            modifier = Modifier.weight(1f).testTag("quick_answer_yes"),
                            colors = ButtonDefaults.buttonColors(containerColor = ChopperPink)
                        ) {
                            Text("Yes, I understand! 🌸", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onAcceptCommand("Can you repeat that, Chopper?") },
                            modifier = Modifier.weight(1f).testTag("quick_answer_repeat")
                        ) {
                            Text("Repeat Please", fontSize = 12.sp)
                        }
                    }
                } else if (state.stage == VoiceCommandStage.LISTENING_TO_BOSS) {
                    Text("Quick Command Suggestions:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ElevatedAssistChip(
                            onClick = { onAcceptCommand("Remind me to buy medicine tomorrow morning") },
                            label = { Text("⏰ Remind me tomorrow", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        ElevatedAssistChip(
                            onClick = { onAcceptCommand("Start morning check-in") },
                            label = { Text("☀️ Morning Check-in", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ElevatedAssistChip(
                            onClick = { onAcceptCommand("Prescribe bedtime health check") },
                            label = { Text("🌙 Bedtime Routine", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        ElevatedAssistChip(
                            onClick = { onAcceptCommand("Test notification alert") },
                            label = { Text("🔔 Test Alert", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onRetryListening,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tap to Speak Again", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onRetryListening,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ChopperPink)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Give Another Command, Boss!", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
