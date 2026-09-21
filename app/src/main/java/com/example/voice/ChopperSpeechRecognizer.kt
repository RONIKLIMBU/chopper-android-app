package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class ChopperSpeechRecognizer(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>("Ready for voice commands")
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isWakeWordListening = MutableStateFlow(false)
    val isWakeWordListening: StateFlow<Boolean> = _isWakeWordListening.asStateFlow()

    private var onFinalResultCallback: ((String) -> Unit)? = null
    private var onWakeWordDetectedCallback: (() -> Unit)? = null
    private var isContinuousWakeWordActive = false
    private var isRecognizingCommand = false

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Start continuous hands-free detection for "Chopper", "Hey Chopper", or "Doctor Chopper".
     * Automatically restarts listening loop on silence/timeout until disabled or when a wake-word is triggered.
     */
    fun startWakeWordListening(onWakeWordDetected: () -> Unit) {
        if (!isAvailable) {
            _statusMessage.value = "Speech recognition service unavailable on device"
            return
        }

        onWakeWordDetectedCallback = onWakeWordDetected
        isContinuousWakeWordActive = true
        isRecognizingCommand = false
        _isWakeWordListening.value = true
        _statusMessage.value = "Listening for 'Chopper' wake-word... 🦌🎙️"

        listenForWakeWordLoop()
    }

    private fun listenForWakeWordLoop() {
        if (!isContinuousWakeWordActive || isRecognizingCommand) return

        mainHandler.post {
            stopListeningInternal()

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            if (isContinuousWakeWordActive && !isRecognizingCommand) {
                                _isWakeWordListening.value = true
                                _isListening.value = false
                                _statusMessage.value = "Listening for 'Chopper'... Say 'Chopper' anytime! 🌸"
                            }
                        }

                        override fun onBeginningOfSpeech() {}

                        override fun onRmsChanged(rmsdB: Float) {
                            if (isContinuousWakeWordActive && !isRecognizingCommand) {
                                _rmsDb.value = rmsdB
                            }
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {}

                        override fun onError(error: Int) {
                            // On timeouts or no match during wake-word detection, automatically restart listening loop
                            if (isContinuousWakeWordActive && !isRecognizingCommand) {
                                mainHandler.postDelayed({
                                    if (isContinuousWakeWordActive && !isRecognizingCommand) {
                                        listenForWakeWordLoop()
                                    }
                                }, 300)
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            var detected = false
                            if (!matches.isNullOrEmpty()) {
                                for (phrase in matches) {
                                    if (containsWakeWord(phrase)) {
                                        detected = true
                                        break
                                    }
                                }
                            }

                            if (detected) {
                                Log.i("ChopperSpeech", "Wake-word 'Chopper' detected in speech!")
                                _statusMessage.value = "Wake-word detected! 'Chopper!' 🦌✨"
                                stopListeningInternal()
                                _isWakeWordListening.value = false
                                onWakeWordDetectedCallback?.invoke()
                            } else if (isContinuousWakeWordActive && !isRecognizingCommand) {
                                // Restart listening loop for next utterance
                                mainHandler.postDelayed({
                                    if (isContinuousWakeWordActive && !isRecognizingCommand) {
                                        listenForWakeWordLoop()
                                    }
                                }, 200)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                for (phrase in matches) {
                                    if (containsWakeWord(phrase)) {
                                        Log.i("ChopperSpeech", "Wake-word detected in partial results: $phrase")
                                        _statusMessage.value = "Wake-word detected! 'Chopper!' 🦌✨"
                                        stopListeningInternal()
                                        _isWakeWordListening.value = false
                                        onWakeWordDetectedCallback?.invoke()
                                        return
                                    }
                                }
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("ChopperSpeech", "Error in wake-word detection loop", e)
                if (isContinuousWakeWordActive && !isRecognizingCommand) {
                    mainHandler.postDelayed({
                        if (isContinuousWakeWordActive && !isRecognizingCommand) {
                            listenForWakeWordLoop()
                        }
                    }, 1000)
                }
            }
        }
    }

    private fun containsWakeWord(text: String): Boolean {
        val lower = text.lowercase(Locale.getDefault())
        // Recognize "chopper", "hey chopper", "hi chopper", "doctor chopper", "dr chopper", "choper", etc.
        return lower.contains("chopper") ||
                lower.contains("choper") ||
                lower.contains("chopar") ||
                lower.contains("hey chopper") ||
                lower.contains("hi chopper") ||
                lower.contains("dr chopper") ||
                lower.contains("doctor chopper")
    }

    fun stopWakeWordListening() {
        isContinuousWakeWordActive = false
        _isWakeWordListening.value = false
        _statusMessage.value = "Wake-word detection paused"
        mainHandler.post {
            if (!isRecognizingCommand) {
                stopListeningInternal()
            }
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        if (!isAvailable) {
            _statusMessage.value = "Speech recognition service unavailable on device"
            return
        }

        isRecognizingCommand = true
        onFinalResultCallback = onResult
        _recognizedText.value = ""
        _statusMessage.value = "Listening to Boss... 🎙️"

        mainHandler.post {
            stopListeningInternal()

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            _statusMessage.value = "Listening... Speak to Chopper!"
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            _rmsDb.value = rmsdB
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _statusMessage.value = "Processing Boss's voice..."
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            val errorDesc = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
                                SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                                SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that, Boss! Please try again."
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                                SpeechRecognizer.ERROR_SERVER -> "Server error"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                                else -> "Voice recognition issue ($error)"
                            }
                            _statusMessage.value = errorDesc
                            Log.w("ChopperSpeech", "Speech recognition error: $errorDesc ($error)")
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val text = matches[0]
                                _recognizedText.value = text
                                _statusMessage.value = "Received: \"$text\""
                                onFinalResultCallback?.invoke(text)
                            } else {
                                _statusMessage.value = "No speech recognized"
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                _recognizedText.value = matches[0]
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                _statusMessage.value = "Failed to start listening: ${e.message}"
                Log.e("ChopperSpeech", "Error starting speech listener", e)
            }
        }
    }

    private fun stopListeningInternal() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("ChopperSpeech", "Error stopping speech listener", e)
        } finally {
            speechRecognizer = null
        }
    }

    fun stopListening() {
        _isListening.value = false
        isRecognizingCommand = false
        mainHandler.post {
            stopListeningInternal()
            if (isContinuousWakeWordActive) {
                listenForWakeWordLoop()
            }
        }
    }

    fun cancel() {
        _isListening.value = false
        isRecognizingCommand = false
        isContinuousWakeWordActive = false
        _isWakeWordListening.value = false
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignored
            } finally {
                speechRecognizer = null
            }
        }
    }
}
