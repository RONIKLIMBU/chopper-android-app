package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.ChopperViewModel
import com.example.ui.components.ChopperChatView
import com.example.ui.components.ChopperHeader
import com.example.ui.components.ChopperRemindersView
import com.example.ui.components.ChopperTriageView
import com.example.ui.components.ChopperVoiceCommandDialog
import com.example.ui.theme.ChopperDoctorBlue
import com.example.ui.theme.ChopperPink
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ChopperViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChopperApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ChopperApp(viewModel: ChopperViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val statusText by viewModel.chopperStatusText.collectAsState()
    val isThinkingMode by viewModel.isThinkingModeEnabled.collectAsState()
    val isVoiceMuted by viewModel.voiceManager.isVoiceMuted.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isHandsFreeListening by viewModel.isHandsFreeWakeWordActive.collectAsState()
    val voiceCommandState by viewModel.voiceCommandUiState.collectAsState()
    val voiceId by viewModel.currentVoiceId.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val unhandledNotifsCount = notifications.count { it.status == "UNHANDLED" }
    val activeRemindersCount = reminders.count { it.status == "ACTIVE" }

    // Request permissions for Speech Recognition (RECORD_AUDIO) and Scheduled Alerts (POST_NOTIFICATIONS)
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permissions result handled gracefully */ }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chopper_main_screen"),
        topBar = {
            ChopperHeader(
                statusText = statusText,
                isThinkingMode = isThinkingMode,
                isVoiceMuted = isVoiceMuted,
                isSpeaking = isSpeaking,
                isHandsFreeListening = isHandsFreeListening,
                voiceId = voiceId,
                onToggleThinkingMode = { viewModel.toggleThinkingMode() },
                onToggleVoiceMute = { viewModel.voiceManager.toggleMute() },
                onToggleHandsFreeWakeWord = { viewModel.toggleHandsFreeWakeWord() },
                onTriggerWakeWord = { viewModel.triggerWakeWord() },
                onTriggerMorningRoutine = { viewModel.triggerMorningRoutine() },
                onTriggerLunchRoutine = { viewModel.triggerLunchRoutine() },
                onTriggerNightRoutine = { viewModel.triggerNightRoutine() },
                onTriggerWaterReminder = { viewModel.triggerDrinkingWaterCheck() },
                onTriggerCareNotification = { viewModel.triggerTakeCareCheck() },
                onTestVoice = { viewModel.testVoiceModel() },
                onStartVoiceCommand = { viewModel.startVoiceCommandFlow(askFirst = true) }
            )
        },
        bottomBar = {
            if (!WindowInsets.isImeVisible) {
                NavigationBar(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = {
                            Icon(Icons.Default.ChatBubble, contentDescription = "Chat & Care")
                        },
                        label = { Text("Chat & Care") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChopperPink,
                            selectedTextColor = ChopperPink,
                            indicatorColor = ChopperPink.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_chat")
                    )

                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = {
                            BadgedBox(badge = {
                                if (activeRemindersCount > 0) {
                                    Badge(containerColor = ChopperPink) {
                                        Text(activeRemindersCount.toString())
                                    }
                                }
                            }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Reminders")
                            }
                        },
                        label = { Text("2-Stage Reminders") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChopperPink,
                            selectedTextColor = ChopperPink,
                            indicatorColor = ChopperPink.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_reminders")
                    )

                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = {
                            BadgedBox(badge = {
                                if (unhandledNotifsCount > 0) {
                                    Badge(containerColor = ChopperDoctorBlue) {
                                        Text(unhandledNotifsCount.toString())
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Inbox, contentDescription = "Triage Inbox")
                            }
                        },
                        label = { Text("Inbox Triage") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChopperPink,
                            selectedTextColor = ChopperPink,
                            indicatorColor = ChopperPink.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_inbox")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            when (activeTab) {
                0 -> ChopperChatView(
                    messages = chatMessages,
                    isGenerating = isGenerating,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onPlayAudio = { viewModel.voiceManager.speak(it, overrideMute = true) },
                    onStartVoiceCommand = { viewModel.startVoiceCommandFlow(askFirst = true) }
                )
                1 -> ChopperRemindersView(
                    reminders = reminders,
                    onAddReminder = { title, targetTime, notes ->
                        viewModel.addReminder(title, targetTime, notes)
                    },
                    onMarkDone = { id, title ->
                        viewModel.markReminderDone(id, title)
                    },
                    onReschedule = { id, title, newTime ->
                        viewModel.rescheduleReminder(id, title, newTime)
                    },
                    onDeleteReminder = { id ->
                        viewModel.deleteReminder(id)
                    },
                    onTestNotification = { type ->
                        viewModel.triggerNotificationTest(type)
                    }
                )
                2 -> ChopperTriageView(
                    notifications = notifications,
                    onHandleAction = { notif, action ->
                        viewModel.handleNotificationAction(notif, action)
                    },
                    onSimulateIncoming = { type ->
                        viewModel.simulateIncoming(type)
                    }
                )
            }

            // Chopper Voice Command Dialog (Chopper speaks first, confirms understanding, then accepts voice commands)
            ChopperVoiceCommandDialog(
                state = voiceCommandState,
                onClose = { viewModel.closeVoiceCommandFlow() },
                onAcceptCommand = { viewModel.acceptVoiceCommand(it) },
                onRetryListening = { viewModel.retryListening() }
            )
        }
    }
}

// Retain Greeting for testing and backward compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Chopper") }
}
