package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChopperAntlerBrown
import com.example.ui.theme.ChopperDoctorBlue
import com.example.ui.theme.ChopperPink
import com.example.ui.theme.ChopperPinkContainer
import com.example.voice.ChopperReaction

@Composable
fun ChopperCharacterHeroCard(
    currentReaction: ChopperReaction,
    isSpeaking: Boolean,
    isVoiceMuted: Boolean,
    statusText: String,
    githubVoiceRepoUrl: String,
    onTriggerReaction: (ChopperReaction) -> Unit,
    onToggleVoiceMute: () -> Unit,
    onStartVoiceCommand: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chopper_animation")

    // Dance sway for chuckle
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    // Stomp bounce for angry
    val stompOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stomp"
    )

    // Speaking pulse
    val speechPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speech_pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chopper_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Quick Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking) ChopperPink else Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tony Tony Chopper",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ChopperPink.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Ship's Doctor 🩺",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChopperPink,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Voice Command
                    IconButton(
                        onClick = onStartVoiceCommand,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("chopper_voice_command_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Command",
                            tint = ChopperDoctorBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Voice Mute Toggle
                    IconButton(
                        onClick = onToggleVoiceMute,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("chopper_mute_btn")
                    ) {
                        Icon(
                            imageVector = if (isVoiceMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Toggle Audio",
                            tint = if (isVoiceMuted) Color.Gray else ChopperPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // GitHub Voice Repo Settings
                    IconButton(
                        onClick = onOpenVoiceSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("chopper_voice_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Voice & GitHub Repo Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Interactive Chopper Avatar and Dialogue Bubble
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                ChopperPinkContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    )
                    .clickable {
                        // Tapping Chopper cycles cute reactions!
                        val nextReaction = when (currentReaction) {
                            ChopperReaction.CHUCKLE_FLUSTERED -> ChopperReaction.ANGRY_SCOLD
                            ChopperReaction.ANGRY_SCOLD -> ChopperReaction.HIDING_PEEK
                            ChopperReaction.HIDING_PEEK -> ChopperReaction.COTTON_CANDY
                            ChopperReaction.COTTON_CANDY -> ChopperReaction.SHOCKED_DOCTOR
                            else -> ChopperReaction.CHUCKLE_FLUSTERED
                        }
                        onTriggerReaction(nextReaction)
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Chopper Character Visual
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, if (isSpeaking) ChopperPink else ChopperDoctorBlue.copy(alpha = 0.4f), CircleShape)
                        .scale(if (isSpeaking) speechPulse else 1f)
                        .rotate(if (currentReaction == ChopperReaction.CHUCKLE_FLUSTERED) swayAngle else 0f)
                        .offset(y = if (currentReaction == ChopperReaction.ANGRY_SCOLD) stompOffset.dp else 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (currentReaction) {
                        ChopperReaction.CHUCKLE_FLUSTERED -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌸", fontSize = 16.sp)
                                Text("🦌", fontSize = 34.sp)
                                Text("⁄(⁄ ⁄•⁄-⁄•⁄ ⁄)⁄", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ChopperPink)
                            }
                        }
                        ChopperReaction.ANGRY_SCOLD -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💢", fontSize = 16.sp)
                                Text("🦌", fontSize = 34.sp)
                                Text("(◣_◢)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                            }
                        }
                        ChopperReaction.HIDING_PEEK -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌲", fontSize = 14.sp)
                                Text("🙈", fontSize = 32.sp)
                                Text("Peek-a-boo!", fontSize = 8.sp, color = ChopperDoctorBlue)
                            }
                        }
                        ChopperReaction.SHOCKED_DOCTOR -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚡", fontSize = 16.sp)
                                Text("😱", fontSize = 32.sp)
                                Text("DOCTOR!!", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ChopperPink)
                            }
                        }
                        ChopperReaction.COTTON_CANDY -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✨", fontSize = 14.sp)
                                Text("🍭", fontSize = 34.sp)
                                Text("Yummy~", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ChopperPink)
                            }
                        }
                        ChopperReaction.MEDICINE_ALERT -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💊", fontSize = 16.sp)
                                Text("🦌", fontSize = 34.sp)
                                Text("Rx Time", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ChopperDoctorBlue)
                            }
                        }
                        ChopperReaction.WATER_ALERT -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💧", fontSize = 16.sp)
                                Text("🦌", fontSize = 34.sp)
                                Text("Drink H2O", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                            }
                        }
                        ChopperReaction.SLEEP_ALERT -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌙", fontSize = 16.sp)
                                Text("😴", fontSize = 32.sp)
                                Text("Zzz...", fontSize = 8.sp, color = Color(0xFF3949AB))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Speech Bubble
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentReaction.moodLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (currentReaction) {
                                ChopperReaction.ANGRY_SCOLD -> Color(0xFFD32F2F)
                                ChopperReaction.CHUCKLE_FLUSTERED, ChopperReaction.COTTON_CANDY -> ChopperPink
                                else -> ChopperDoctorBlue
                            }
                        )

                        if (isSpeaking) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = "Chopper Speaking",
                                    tint = ChopperPink,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Speaking...", fontSize = 10.sp, color = ChopperPink, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "\"${currentReaction.spokenLine}\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "👉 Tap Chopper or buttons below to trigger his funny anime reactions!",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Chopper Reactions Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReactionChip(
                    label = "🌸 Flustered Dance",
                    isSelected = currentReaction == ChopperReaction.CHUCKLE_FLUSTERED,
                    onClick = { onTriggerReaction(ChopperReaction.CHUCKLE_FLUSTERED) }
                )

                ReactionChip(
                    label = "💢 Angry Scold",
                    isSelected = currentReaction == ChopperReaction.ANGRY_SCOLD,
                    onClick = { onTriggerReaction(ChopperReaction.ANGRY_SCOLD) }
                )

                ReactionChip(
                    label = "🙈 Hide Wrong Way",
                    isSelected = currentReaction == ChopperReaction.HIDING_PEEK,
                    onClick = { onTriggerReaction(ChopperReaction.HIDING_PEEK) }
                )

                ReactionChip(
                    label = "🍭 Cotton Candy",
                    isSelected = currentReaction == ChopperReaction.COTTON_CANDY,
                    onClick = { onTriggerReaction(ChopperReaction.COTTON_CANDY) }
                )

                ReactionChip(
                    label = "😱 Doctor Panic!",
                    isSelected = currentReaction == ChopperReaction.SHOCKED_DOCTOR,
                    onClick = { onTriggerReaction(ChopperReaction.SHOCKED_DOCTOR) }
                )

                ReactionChip(
                    label = "💊 Medicine Check",
                    isSelected = currentReaction == ChopperReaction.MEDICINE_ALERT,
                    onClick = { onTriggerReaction(ChopperReaction.MEDICINE_ALERT) }
                )

                ReactionChip(
                    label = "💧 Hydration",
                    isSelected = currentReaction == ChopperReaction.WATER_ALERT,
                    onClick = { onTriggerReaction(ChopperReaction.WATER_ALERT) }
                )
            }
        }
    }
}

@Composable
private fun ReactionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isSelected) ChopperPink.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = if (isSelected) ChopperPink else MaterialTheme.colorScheme.onSurface
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = if (isSelected) ChopperPink else Color.Transparent
        )
    )
}
