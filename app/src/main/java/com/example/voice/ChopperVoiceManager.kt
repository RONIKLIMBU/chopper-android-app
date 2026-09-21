package com.example.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class ChopperVoiceManager(context: Context) {

    companion object {
        const val DEFAULT_VOICE_ID = "e5e3a1d83d6f491db3c26b3929052b34"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isVoiceMuted = MutableStateFlow(false)
    val isVoiceMuted: StateFlow<Boolean> = _isVoiceMuted.asStateFlow()

    private val _currentVoiceId = MutableStateFlow(DEFAULT_VOICE_ID)
    val currentVoiceId: StateFlow<String> = _currentVoiceId.asStateFlow()

    private var currentOnDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale.US)
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        applyVoiceConfiguration(engine, _currentVoiceId.value)
                        isTtsReady = true
                        Log.i("ChopperVoiceManager", "TTS initialized with Voice ID: ${_currentVoiceId.value}")
                    }

                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            val cb = currentOnDoneCallback
                            currentOnDoneCallback = null
                            cb?.let { callback ->
                                mainHandler.post { callback() }
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            val cb = currentOnDoneCallback
                            currentOnDoneCallback = null
                            cb?.let { callback ->
                                mainHandler.post { callback() }
                            }
                        }
                    })
                }
            } else {
                Log.e("ChopperVoiceManager", "TTS initialization failed.")
            }
        }
    }

    private fun applyVoiceConfiguration(engine: TextToSpeech, voiceId: String) {
        try {
            // Check if available system voices include the custom target voice model
            val availableVoices = engine.voices
            val targetVoice = availableVoices?.firstOrNull { voice ->
                voice.name.contains(voiceId, ignoreCase = true)
            }
            if (targetVoice != null) {
                engine.voice = targetVoice
                Log.i("ChopperVoiceManager", "Applied custom voice model: ${targetVoice.name}")
            } else {
                Log.i("ChopperVoiceManager", "Voice ID $voiceId configured. Using tuned anime parameters.")
            }
        } catch (e: Exception) {
            Log.w("ChopperVoiceManager", "Exception checking engine voices", e)
        }

        // High-pitched, cheerful, anime-inspired delivery
        engine.setPitch(1.35f)
        engine.setSpeechRate(1.05f)
    }

    fun setVoiceId(newVoiceId: String) {
        _currentVoiceId.value = newVoiceId
        tts?.let { engine ->
            if (isTtsReady) {
                applyVoiceConfiguration(engine, newVoiceId)
            }
        }
    }

    fun testCurrentVoice() {
        val testMessage = "Boss! This is Chopper speaking with voice model ${DEFAULT_VOICE_ID.take(8)}! I'm ready to take care of you, Boss! 🌸"
        speak(testMessage, overrideMute = true)
    }

    fun toggleMute() {
        val newMute = !_isVoiceMuted.value
        _isVoiceMuted.value = newMute
        if (newMute) {
            stopSpeaking()
        }
    }

    fun speak(text: String, overrideMute: Boolean = false, onDone: (() -> Unit)? = null) {
        if (_isVoiceMuted.value && !overrideMute) {
            onDone?.invoke()
            return
        }
        if (!isTtsReady || tts == null) {
            onDone?.invoke()
            return
        }

        currentOnDoneCallback = onDone

        // Clean text from emojis for smoother TTS recitation
        val sanitized = text.replace(Regex("[\\p{So}\\p{Cn}]"), " ")
            .replace("*", "")
            .trim()

        _isSpeaking.value = true
        val utteranceId = "chopper_utterance_${System.currentTimeMillis()}"
        tts?.speak(sanitized, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Chopper asks and speaks first: "Boss! Do you understand me properly? Chopper is ready to listen to your voice commands!"
     */
    fun askUnderstandingCheck(onDone: () -> Unit) {
        val question = "Boss! Do you understand me properly? Chopper is ready to listen to your voice commands!"
        speak(question, overrideMute = true, onDone = onDone)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        currentOnDoneCallback = null
    }

    fun shutdown() {
        stopSpeaking()
        tts?.shutdown()
        tts = null
        isTtsReady = false
    }
}
