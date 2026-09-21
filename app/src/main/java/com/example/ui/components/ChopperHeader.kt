package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChopperAntlerBrown
import com.example.ui.theme.ChopperDoctorBlue
import com.example.ui.theme.ChopperPink

@Composable
fun ChopperHeader(
    statusText: String,
    isThinkingMode: Boolean,
    isVoiceMuted: Boolean,
    isSpeaking: Boolean,
    isHandsFreeListening: Boolean,
    voiceId: String = "e5e3a1d83d6f491db3c26b3929052b34",
    onToggleThinkingMode: () -> Unit,
    onToggleVoiceMute: () -> Unit,
    onToggleHandsFreeWakeWord: () -> Unit,
    onTriggerWakeWord: () -> Unit,
    onTriggerMorningRoutine: () -> Unit,
    onTriggerLunchRoutine: () -> Unit = {},
    onTriggerNightRoutine: () -> Unit,
    onTriggerWaterReminder: () -> Unit = {},
    onTriggerCareNotification: () -> Unit = {},
    onTestVoice: () -> Unit = {},
    onStartVoiceCommand: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chopper_header"),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Character badge & identity
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ChopperPink.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🦌",
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Chopper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ChopperDoctorBlue.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI Companion",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ChopperDoctorBlue
                                )
                            }
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSpeaking) ChopperPink else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSpeaking) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                // Controls: Voice Command, TTS Mute, Thinking Toggle, Wake Word
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onStartVoiceCommand,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("voice_command_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Talk to Chopper",
                            tint = ChopperDoctorBlue
                        )
                    }

                    IconButton(
                        onClick = onToggleVoiceMute,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("voice_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isVoiceMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isVoiceMuted) "Unmute Chopper Voice" else "Mute Chopper Voice",
                            tint = if (isVoiceMuted) Color.Gray else ChopperPink
                        )
                    }

                    IconButton(
                        onClick = onToggleThinkingMode,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("thinking_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Toggle Gemini 3.1 Pro Thinking Mode",
                            tint = if (isThinkingMode) ChopperDoctorBlue else Color.Gray
                        )
                    }

                    // Wake word hands-free listening toggle / trigger button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isHandsFreeListening) Color(0xFF4CAF50)
                                else ChopperPink
                            )
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = onToggleHandsFreeWakeWord,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("wake_word_button")
                        ) {
                            Text(
                                text = if (isHandsFreeListening) "Listening 🦌" else "Chopper!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isThinkingMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ChopperDoctorBlue.copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = ChopperDoctorBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Thinking Mode Active: gemini-3.1-pro-preview (ThinkingLevel: HIGH)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChopperDoctorBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Routine & Voice Bar (Horizontally scrollable for all permanent routines)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice Model Chip
                AssistChip(
                    onClick = onTestVoice,
                    label = { Text("Voice: ${voiceId.take(8)}... (Active)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.testTag("voice_model_chip"),
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = ChopperPink,
                        containerColor = ChopperPink.copy(alpha = 0.12f)
                    )
                )

                AssistChip(
                    onClick = onTriggerMorningRoutine,
                    label = { Text("7:00 AM Morning", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.testTag("morning_routine_chip"),
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = Color(0xFFFFA000)
                    )
                )

                AssistChip(
                    onClick = onTriggerLunchRoutine,
                    label = { Text("2:00 PM Lunch", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.testTag("lunch_routine_chip"),
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = Color(0xFF2E7D32)
                    )
                )

                AssistChip(
                    onClick = onTriggerNightRoutine,
                    label = { Text("11:00 PM Night", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.testTag("night_routine_chip"),
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = Color(0xFF5C6BC0)
                    )
                )

                AssistChip(
                    onClick = onTriggerWaterReminder,
                    label = { Text("Drink Water 💧", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.testTag("water_routine_chip"),
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = Color(0xFF0288D1)
                    )
                )

                AssistChip(
                    onClick = onTriggerCareNotification,
                    label = { Text("Take Care 🩺", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.testTag("care_routine_chip"),
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = ChopperPink
                    )
                )

                AssistChip(
                    onClick = onStartVoiceCommand,
                    label = { Text("Voice Check 🎙️", fontSize = 11.sp) },
                    modifier = Modifier.testTag("voice_check_chip")
                )

                AssistChip(
                    onClick = onTriggerWakeWord,
                    label = { Text("Call Name", fontSize = 11.sp) },
                    leadingIcon = {
                        Text("🦌", fontSize = 11.sp)
                    },
                    modifier = Modifier.testTag("call_name_chip")
                )
            }
        }
    }
}
