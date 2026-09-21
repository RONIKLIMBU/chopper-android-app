package com.example.ui.model

enum class VoiceCommandStage {
    IDLE,
    ASKING_UNDERSTANDING, // Chopper speaks first: "Boss! Do you understand me properly? Chopper is ready to listen to your voice commands!"
    LISTENING_TO_BOSS,     // Chopper is listening to Boss's voice command
    PROCESSING,           // Interpreting voice command
    RESPONDING            // Chopper answers Boss
}

data class VoiceCommandUiState(
    val isOpen: Boolean = false,
    val stage: VoiceCommandStage = VoiceCommandStage.IDLE,
    val recognizedText: String = "",
    val chopperSpokenText: String = "",
    val statusText: String = "",
    val isListening: Boolean = false,
    val rmsDb: Float = 0f
)
