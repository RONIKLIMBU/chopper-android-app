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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.ChopperViewModel
import com.example.ui.components.ChopperCharacterHeroCard
import com.example.ui.components.ChopperRemindersView
import com.example.ui.components.ChopperVoiceCommandDialog
import com.example.ui.components.ChopperVoiceSettingsDialog
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

@Composable
fun ChopperApp(viewModel: ChopperViewModel) {
    val statusText by viewModel.chopperStatusText.collectAsState()
    val isVoiceMuted by viewModel.voiceManager.isVoiceMuted.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val currentReaction by viewModel.currentReaction.collectAsState()
    val githubVoiceRepoUrl by viewModel.githubVoiceRepoUrl.collectAsState()
    val voiceCommandState by viewModel.voiceCommandUiState.collectAsState()
    val reminders by viewModel.reminders.collectAsState()

    var showVoiceSettingsDialog by remember { mutableStateOf(false) }

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
            .testTag("chopper_main_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                // Interactive Tony Tony Chopper Character & Expressions Hero Card
                ChopperCharacterHeroCard(
                    currentReaction = currentReaction,
                    isSpeaking = isSpeaking,
                    isVoiceMuted = isVoiceMuted,
                    statusText = statusText,
                    githubVoiceRepoUrl = githubVoiceRepoUrl,
                    onTriggerReaction = { reaction ->
                        viewModel.triggerReaction(reaction)
                    },
                    onToggleVoiceMute = {
                        viewModel.voiceManager.toggleMute()
                    },
                    onStartVoiceCommand = {
                        viewModel.startVoiceCommandFlow(askFirst = true)
                    },
                    onOpenVoiceSettings = {
                        showVoiceSettingsDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fully Functional Chopper Reminder Engine
                ChopperRemindersView(
                    reminders = reminders,
                    onAddReminder = { title, targetTime, notes ->
                        viewModel.addReminder(title, targetTime, notes)
                    },
                    onQuickAddPreset = { preset ->
                        viewModel.quickAddReminder(preset)
                    },
                    onSnooze = { id, title, minutes ->
                        viewModel.snoozeReminder(id, title, minutes)
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
                    onTriggerAngryScold = {
                        viewModel.triggerReaction(com.example.voice.ChopperReaction.ANGRY_SCOLD)
                    },
                    onTestNotification = { type ->
                        viewModel.triggerNotificationTest(type)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Chopper Voice Command Dialog (Hands-free speech commands for reminders)
            ChopperVoiceCommandDialog(
                state = voiceCommandState,
                onClose = { viewModel.closeVoiceCommandFlow() },
                onAcceptCommand = { viewModel.acceptVoiceCommand(it) },
                onRetryListening = { viewModel.retryListening() }
            )

            // GitHub Voice & Audio Settings Dialog
            if (showVoiceSettingsDialog) {
                ChopperVoiceSettingsDialog(
                    currentRepoUrl = githubVoiceRepoUrl,
                    onSaveRepoUrl = { url ->
                        viewModel.setGithubVoiceRepoUrl(url)
                    },
                    onTestReaction = { reaction ->
                        viewModel.triggerReaction(reaction)
                    },
                    onDismiss = {
                        showVoiceSettingsDialog = false
                    }
                )
            }
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
