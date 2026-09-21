package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.database.ChopperDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.TriageNotificationEntity
import com.example.data.repository.ChopperRepository
import com.example.notifications.ChopperNotificationManager
import com.example.ui.model.VoiceCommandStage
import com.example.ui.model.VoiceCommandUiState
import com.example.voice.ChopperSpeechRecognizer
import com.example.voice.ChopperVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChopperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChopperRepository
    val voiceManager = ChopperVoiceManager(application)
    val speechRecognizer = ChopperSpeechRecognizer(application)
    val notificationManager = ChopperNotificationManager(application)
    private val geminiService = GeminiService()

    val currentVoiceId: StateFlow<String> = voiceManager.currentVoiceId

    val reminders: StateFlow<List<ReminderEntity>>
    val notifications: StateFlow<List<TriageNotificationEntity>>
    val chatMessages: StateFlow<List<ChatMessageEntity>>

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isThinkingModeEnabled = MutableStateFlow(false)
    val isThinkingModeEnabled: StateFlow<Boolean> = _isThinkingModeEnabled.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Chat & Care, 1: Reminders, 2: Triage Inbox
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _chopperStatusText = MutableStateFlow("Ready for Boss! 🌸")
    val chopperStatusText: StateFlow<String> = _chopperStatusText.asStateFlow()

    val isHandsFreeWakeWordActive: StateFlow<Boolean>
        get() = speechRecognizer.isWakeWordListening

    private val _voiceCommandUiState = MutableStateFlow(VoiceCommandUiState())
    val voiceCommandUiState: StateFlow<VoiceCommandUiState> = _voiceCommandUiState.asStateFlow()

    init {
        val database = ChopperDatabase.getDatabase(application)
        repository = ChopperRepository(database.chopperDao())

        reminders = repository.reminders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        notifications = repository.notifications.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        chatMessages = repository.chatMessages.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Schedule recurring daily morning (7:00 AM) and bedtime check-ins
        notificationManager.scheduleDailyCheckIns()

        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            repository.ensurePermanentRemindersExist()
            // Schedule existing active reminders
            reminders.value.forEach { reminder ->
                if (reminder.status == "ACTIVE") {
                    notificationManager.scheduleReminderAlerts(reminder)
                }
            }
        }

        // Monitor speech recognizer state
        viewModelScope.launch {
            speechRecognizer.isListening.collect { listening ->
                _voiceCommandUiState.value = _voiceCommandUiState.value.copy(isListening = listening)
            }
        }
        viewModelScope.launch {
            speechRecognizer.recognizedText.collect { text ->
                if (text.isNotBlank()) {
                    _voiceCommandUiState.value = _voiceCommandUiState.value.copy(recognizedText = text)
                }
            }
        }
        viewModelScope.launch {
            speechRecognizer.rmsDb.collect { rms ->
                _voiceCommandUiState.value = _voiceCommandUiState.value.copy(rmsDb = rms)
            }
        }
        viewModelScope.launch {
            speechRecognizer.statusMessage.collect { msg ->
                if (msg != null && _voiceCommandUiState.value.isOpen) {
                    _voiceCommandUiState.value = _voiceCommandUiState.value.copy(statusText = msg)
                }
            }
        }
    }

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun toggleThinkingMode() {
        _isThinkingModeEnabled.value = !_isThinkingModeEnabled.value
    }

    // -------------------------------------------------------------
    // Voice Command Flow (Chopper asks and speaks first: "Do you understand me properly?")
    // -------------------------------------------------------------
    fun startVoiceCommandFlow(askFirst: Boolean = true) {
        val question = "Boss! Do you understand me properly? Chopper is ready to listen to your voice commands!"

        if (askFirst) {
            _voiceCommandUiState.value = VoiceCommandUiState(
                isOpen = true,
                stage = VoiceCommandStage.ASKING_UNDERSTANDING,
                chopperSpokenText = question,
                statusText = "Chopper speaking to Boss... 🦌",
                isListening = false
            )
            _chopperStatusText.value = "Chopper: Checking with Boss..."

            // Post initial question in chat
            viewModelScope.launch {
                repository.insertChatMessage(
                    ChatMessageEntity(
                        sender = "CHOPPER",
                        text = "Boss! Do you understand me properly? Chopper is ready to listen to your voice commands! 🌸"
                    )
                )
            }

            // Chopper speaks first, then automatically begins listening for Boss's command
            voiceManager.askUnderstandingCheck {
                startListeningForBossSpeech()
            }
        } else {
            _voiceCommandUiState.value = VoiceCommandUiState(
                isOpen = true,
                stage = VoiceCommandStage.LISTENING_TO_BOSS,
                chopperSpokenText = "Listening to Boss...",
                statusText = "Speak your command clearly, Boss! 🎙️",
                isListening = true
            )
            startListeningForBossSpeech()
        }
    }

    private fun startListeningForBossSpeech() {
        _voiceCommandUiState.value = _voiceCommandUiState.value.copy(
            stage = VoiceCommandStage.LISTENING_TO_BOSS,
            statusText = "Listening to Boss... 🎙️",
            isListening = true
        )
        _chopperStatusText.value = "Listening to Boss's voice... 🎙️"

        speechRecognizer.startListening { rawSpeech ->
            acceptVoiceCommand(rawSpeech)
        }
    }

    fun acceptVoiceCommand(commandText: String) {
        if (commandText.isBlank()) return

        speechRecognizer.stopListening()

        _voiceCommandUiState.value = _voiceCommandUiState.value.copy(
            stage = VoiceCommandStage.PROCESSING,
            recognizedText = commandText,
            statusText = "Processing command: \"$commandText\"... 🧠"
        )

        viewModelScope.launch {
            // Record Boss speech in chat
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "USER",
                    text = commandText
                )
            )

            val lower = commandText.lowercase().trim()
            var chopperReply = ""

            // Intent Handling
            when {
                // Boss confirms understanding
                lower in listOf("yes", "yeah", "yep", "i understand", "yes i understand", "yes chopper", "loud and clear", "i hear you", "understood") ||
                        lower.contains("understand") && (lower.contains("yes") || lower.contains("i do") || lower.contains("good")) -> {
                    chopperReply = "Yay, Boss! I'm so glad you understand me properly! 🌸 What would you like me to do? You can say: 'Remind me tomorrow', 'Start morning routine', 'Good night', or ask me any health or schedule question!"
                }

                // Boss didn't understand / asks to repeat
                lower in listOf("no", "nope", "i don't understand", "what", "repeat", "pardon", "say again") ||
                        lower.contains("don't understand") || lower.contains("repeat") -> {
                    chopperReply = "Understood, Boss! I am Tony Tony Chopper, your loyal doctor and schedule assistant! I'll speak extra clearly: whenever you are ready, give me a command or ask me to set a reminder!"
                }

                // Morning routine
                lower.contains("morning") || lower.contains("wake up") || lower.contains("check in") -> {
                    val upcomingCount = reminders.value.count { it.status == "ACTIVE" }
                    val unhandledNotifs = notifications.value.count { it.status == "UNHANDLED" }
                    val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                    chopperReply = "Good morning, Boss! ☀️ It's $timeString. You have $upcomingCount active schedule events and $unhandledNotifs communications waiting. Please drink a full glass of fresh water, Boss!"
                    _chopperStatusText.value = "Good Morning, Boss! ☀️"
                }

                // Night routine
                lower.contains("night") || lower.contains("sleep") || lower.contains("bedtime") -> {
                    chopperReply = "Boss, good night! 🌙 Dr. Chopper prescribes 8 hours of peaceful sleep to keep you energized for tomorrow's adventures! Sweet dreams!"
                    _chopperStatusText.value = "Good Night, Boss! 🌙"
                }

                // Reminder creation via voice: e.g. "remind me to call Zoro tomorrow"
                lower.startsWith("remind me to") || lower.startsWith("schedule") || lower.startsWith("add reminder") -> {
                    val parsedTitle = commandText
                        .replace(Regex("^(?i)(remind me to|schedule|add reminder for|add reminder)\\s*"), "")
                        .trim()
                        .ifEmpty { "Important Task from Boss" }

                    val targetTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L) // 24 hours from now
                    val newReminder = ReminderEntity(
                        title = parsedTitle,
                        targetTimestamp = targetTime,
                        remindDayBefore = true,
                        remindDayOf = true,
                        notes = "Scheduled via Boss voice command"
                    )
                    val insertedId = repository.insertReminder(newReminder)
                    notificationManager.scheduleReminderAlerts(newReminder.copy(id = insertedId))

                    val dateFormatted = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(targetTime))
                    chopperReply = "Right away, Boss! I've scheduled '$parsedTitle' for $dateFormatted and armed 2-stage Android notifications! 🌸"
                }

                // Check schedule status
                lower.contains("status") || lower.contains("reminders") || lower.contains("schedule") -> {
                    val upcoming = reminders.value.filter { it.status == "ACTIVE" }
                    chopperReply = if (upcoming.isEmpty()) {
                        "Boss, you currently have no active reminders! Your schedule is completely clear! 🌸"
                    } else {
                        val first = upcoming.first()
                        val formatted = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(first.targetTimestamp))
                        "Boss, you have ${upcoming.size} active reminders! The next one is '${first.title}' at $formatted!"
                    }
                }

                // Notification test
                lower.contains("test notification") || lower.contains("notification") -> {
                    notificationManager.showInstantNotification(
                        title = "🌸 Chopper Notification Test",
                        message = "Boss! Android Notification Manager is active and delivering timely alerts!"
                    )
                    chopperReply = "Boss! I just sent a test notification to your Android notification tray! Check the status bar!"
                }

                // General conversational voice query -> Gemini AI Chopper persona
                else -> {
                    val history = chatMessages.value.takeLast(4).map { it.sender to it.text }
                    val aiResponse = geminiService.generateResponse(
                        prompt = commandText,
                        enableThinkingMode = _isThinkingModeEnabled.value,
                        conversationHistory = history
                    )
                    chopperReply = aiResponse.replyText
                }
            }

            // Save Chopper reply in chat
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = chopperReply
                )
            )

            // Update voice dialog UI
            _voiceCommandUiState.value = _voiceCommandUiState.value.copy(
                stage = VoiceCommandStage.RESPONDING,
                chopperSpokenText = chopperReply,
                statusText = "Chopper responding to Boss! 🌸"
            )

            // Speak answer out loud
            voiceManager.speak(chopperReply) {
                _voiceCommandUiState.value = _voiceCommandUiState.value.copy(
                    stage = VoiceCommandStage.IDLE,
                    statusText = "Ready for Boss's next command! 🌸"
                )
            }
        }
    }

    fun closeVoiceCommandFlow() {
        speechRecognizer.stopListening()
        voiceManager.stopSpeaking()
        _voiceCommandUiState.value = VoiceCommandUiState(isOpen = false)
        _chopperStatusText.value = "Ready for Boss! 🌸"
    }

    fun retryListening() {
        startListeningForBossSpeech()
    }

    fun toggleHandsFreeWakeWord(enable: Boolean? = null) {
        val shouldEnable = enable ?: !speechRecognizer.isWakeWordListening.value
        if (shouldEnable) {
            speechRecognizer.startWakeWordListening {
                // When "Chopper" wake-word is detected via Android SpeechRecognizer:
                onWakeWordDetectedBySpeechRecognizer()
            }
            _chopperStatusText.value = "Listening for 'Chopper'... 🦌🎙️"
        } else {
            speechRecognizer.stopWakeWordListening()
            _chopperStatusText.value = "Ready for Boss! 🌸"
        }
    }

    private fun onWakeWordDetectedBySpeechRecognizer() {
        viewModelScope.launch {
            _chopperStatusText.value = "Wake-word detected! 🌸"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "USER",
                    text = "Chopper!"
                )
            )
            // Launch the voice command flow seamlessly so Boss can give their command hands-free!
            startVoiceCommandFlow(askFirst = true)
        }
    }

    fun triggerWakeWord() {
        viewModelScope.launch {
            val reply = "Yes, Boss! Chopper is here and ready! What do you need me to do? 🌸"
            _chopperStatusText.value = "Alert & Listening! 🩺"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "USER",
                    text = "Chopper!"
                )
            )
            startVoiceCommandFlow(askFirst = true)
        }
    }

    fun triggerMorningRoutine() {
        viewModelScope.launch {
            val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            val upcomingCount = reminders.value.count { it.status == "ACTIVE" }
            val unhandledNotifs = notifications.value.count { it.status == "UNHANDLED" }

            val reply = "Good morning, Boss! ☀️ It's $timeString! Did you sleep well? Doctor Chopper insists you drink some fresh water first! You currently have $upcomingCount active schedule events and $unhandledNotifs unhandled communications. Let's tackle today together, Boss!"

            _chopperStatusText.value = "Good Morning, Boss! ☀️"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = reply
                )
            )
            notificationManager.showDailyCheckInNotification("MORNING")
            voiceManager.speak(reply)
        }
    }

    fun triggerLunchRoutine() {
        viewModelScope.launch {
            val reply = "Boss! It's 2:00 PM! Time to step away from your tasks and enjoy a healthy lunch! Even the greatest captain needs nutrition! Doctor Chopper orders you to fuel up well! 🍱🌸"
            _chopperStatusText.value = "Lunch Time, Boss! 🍱"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = reply
                )
            )
            notificationManager.showDailyCheckInNotification("LUNCH")
            voiceManager.speak(reply)
        }
    }

    fun triggerDrinkingWaterCheck() {
        viewModelScope.launch {
            val reply = "Hydration check, Boss! 💧 Doctor Chopper reminds you to drink a glass of fresh water right now to keep your mind sharp and your body energized! 🦌🌸"
            _chopperStatusText.value = "Drink Water, Boss! 💧"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = reply
                )
            )
            notificationManager.showDailyCheckInNotification("WATER")
            voiceManager.speak(reply)
        }
    }

    fun triggerTakeCareCheck() {
        viewModelScope.launch {
            val reply = "Doctor Chopper care check-in! 🩺 Take a 2-minute breather, Boss! Roll your shoulders, look away from the screen, and take a deep breath. You're doing amazing, Boss! 🌸"
            _chopperStatusText.value = "Take Care, Boss! 🩺"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = reply
                )
            )
            notificationManager.showDailyCheckInNotification("CARE")
            voiceManager.speak(reply)
        }
    }

    fun testVoiceModel() {
        voiceManager.testCurrentVoice()
    }

    fun triggerNightRoutine() {
        viewModelScope.launch {
            val reply = "Boss, it's 11:00 PM! 🌙 Even the strongest pirate captains need their rest! As your doctor, I'm prescribing at least 8 hours of peaceful sleep. Let's wrap up and get some rest, Boss! Sweet dreams!"

            _chopperStatusText.value = "Good Night, Boss! 🌙"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = reply
                )
            )
            notificationManager.showDailyCheckInNotification("NIGHT")
            voiceManager.speak(reply)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userPrompt = text.trim()
        viewModelScope.launch {
            _isGenerating.value = true
            _chopperStatusText.value = if (_isThinkingModeEnabled.value) "Thinking Deeply (Gemini 3.1 Pro)... 🧠" else "Responding with care... 🌸"

            // Save user message
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "USER",
                    text = userPrompt
                )
            )

            // Gather history
            val history = chatMessages.value.takeLast(6).map { it.sender to it.text }

            val aiResponse = geminiService.generateResponse(
                prompt = userPrompt,
                enableThinkingMode = _isThinkingModeEnabled.value,
                conversationHistory = history
            )

            // Save Chopper reply
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = aiResponse.replyText,
                    isThinkingMode = aiResponse.isThinkingMode,
                    thinkingSummary = aiResponse.thinkingTrace
                )
            )

            _isGenerating.value = false
            _chopperStatusText.value = "Ready for Boss! 🌸"

            // Speak response via Chopper's anime voice
            voiceManager.speak(aiResponse.replyText)
        }
    }

    // Reminders & Scheduled Alerts
    fun addReminder(title: String, targetTimestamp: Long, notes: String = "") {
        viewModelScope.launch {
            val reminder = ReminderEntity(
                title = title,
                targetTimestamp = targetTimestamp,
                remindDayBefore = true,
                remindDayOf = true,
                notes = notes
            )
            val insertedId = repository.insertReminder(reminder)

            // Arm system notifications for Stage 1 (24h before) and Stage 2 (day of)
            notificationManager.scheduleReminderAlerts(reminder.copy(id = insertedId))

            val dateFormatted = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(targetTimestamp))
            val confirmation = "Boss! I've scheduled '$title' for $dateFormatted and armed 2-stage Android notifications (1 day before & day of event)! 🌸"

            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = confirmation
                )
            )
            voiceManager.speak(confirmation)
        }
    }

    fun markReminderDone(id: Long, title: String) {
        viewModelScope.launch {
            repository.updateReminderStatus(id, "COMPLETED")
            notificationManager.cancelReminderAlerts(id)
            val praise = "Awesome job, Boss! '$title' is marked as Done! ✅ That's another great victory for today!"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = praise
                )
            )
            voiceManager.speak(praise)
        }
    }

    fun rescheduleReminder(id: Long, title: String, newTimestamp: Long) {
        viewModelScope.launch {
            repository.rescheduleReminder(id, newTimestamp)
            notificationManager.cancelReminderAlerts(id)
            val updatedReminder = ReminderEntity(
                id = id,
                title = title,
                targetTimestamp = newTimestamp,
                remindDayBefore = true,
                remindDayOf = true
            )
            notificationManager.scheduleReminderAlerts(updatedReminder)

            val dateFormatted = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(newTimestamp))
            val confirmation = "Understood, Boss! I rescheduled '$title' to $dateFormatted and updated your notifications! 🔄"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = confirmation
                )
            )
            voiceManager.speak(confirmation)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            notificationManager.cancelReminderAlerts(id)
            repository.deleteReminder(id)
        }
    }

    fun triggerNotificationTest(type: String) {
        viewModelScope.launch {
            when (type) {
                "STAGE_1" -> {
                    notificationManager.showReminderNotification(
                        reminderId = 901L,
                        title = "Quarterly Strategy Review with Team",
                        stage = "STAGE_1",
                        notes = "Scheduled for tomorrow - Doctor Chopper reminder!"
                    )
                    voiceManager.speak("Boss! Tomorrow is your scheduled event! Chopper is reminding you one day before as promised! 🌸")
                }
                "STAGE_2" -> {
                    notificationManager.showReminderNotification(
                        reminderId = 902L,
                        title = "Health Check & Vitamin Restock",
                        stage = "STAGE_2",
                        notes = "Event happening today! Stay hydrated!"
                    )
                    voiceManager.speak("Boss! Today is the day for your scheduled event! Have you completed it or should I reschedule?")
                }
                "MORNING" -> {
                    notificationManager.showDailyCheckInNotification("MORNING")
                    triggerMorningRoutine()
                }
                "LUNCH" -> {
                    notificationManager.showDailyCheckInNotification("LUNCH")
                    triggerLunchRoutine()
                }
                "NIGHT" -> {
                    notificationManager.showDailyCheckInNotification("NIGHT")
                    triggerNightRoutine()
                }
                "WATER" -> {
                    notificationManager.showDailyCheckInNotification("WATER")
                    triggerDrinkingWaterCheck()
                }
                "CARE" -> {
                    notificationManager.showDailyCheckInNotification("CARE")
                    triggerTakeCareCheck()
                }
                "VOICE" -> {
                    testVoiceModel()
                }
                else -> notificationManager.showInstantNotification(
                    title = "🌸 Chopper Alert System",
                    message = "Boss! Timely updates for reminders and daily check-ins are active!"
                )
            }
        }
    }

    // Triage actions
    fun handleNotificationAction(notification: TriageNotificationEntity, action: String) {
        viewModelScope.launch {
            repository.updateNotificationStatus(notification.id, "HANDLED")
            val confirmation = "Command executed, Boss! For ${notification.sender}, I carried out: '$action'. Everything is handled smoothly! 🩺"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = confirmation
                )
            )
            voiceManager.speak(confirmation)
        }
    }

    fun simulateIncoming(type: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val notif = when (type) {
                "CALL" -> TriageNotificationEntity(
                    type = "CALL",
                    sender = "Sanji (Incoming Call)",
                    content = "Missed call: 'Boss, what should I prepare for tonight's dinner banquet?'",
                    timestamp = now,
                    chopperQuestion = "Boss! Sanji is calling about tonight's dinner menu! Should I ask him to make your favorite or call him back?",
                    suggestedActions = "Call Back, Text: Surprise me, Text: Make Meat & Cotton Candy",
                    status = "UNHANDLED"
                )
                "SMS" -> TriageNotificationEntity(
                    type = "SMS",
                    sender = "Usopp",
                    content = "Hey Boss! The new workshop inventions are ready for inspection!",
                    timestamp = now,
                    chopperQuestion = "Boss, you have a new text from Usopp about workshop inventions! Should I reply that you'll check it later?",
                    suggestedActions = "Reply: On my way, Reply: Check tomorrow, Remind later",
                    status = "UNHANDLED"
                )
                else -> TriageNotificationEntity(
                    type = "EMAIL",
                    sender = "Grand Fleet Operations",
                    content = "Monthly supply audit and weather forecast report for upcoming route.",
                    timestamp = now,
                    chopperQuestion = "Boss, an operational briefing email arrived. Would you like me to summarize it or archive it?",
                    suggestedActions = "Summarize Briefing, Archive, Mark Handled",
                    status = "UNHANDLED"
                )
            }
            repository.insertNotification(notif)
            val alert = "Boss! A new ${notif.type} alert just came in from ${notif.sender}! Head to the Inbox tab or tell me what to do!"
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = alert
                )
            )
            // Post a system notification as well
            notificationManager.showInstantNotification(
                title = "New ${notif.type}: ${notif.sender}",
                message = notif.chopperQuestion
            )
            voiceManager.speak(alert)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.cancel()
        voiceManager.shutdown()
    }
}
