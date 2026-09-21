package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class ChopperReaction(
    val id: String,
    val spokenLine: String,
    val audioFileName: String,
    val moodLabel: String,
    val subtitle: String
) {
    CHUCKLE_FLUSTERED(
        id = "chuckle",
        spokenLine = "Shut up, you jerk! Calling me great doesn't make me happy at all, you idiot! Ehehehe!",
        audioFileName = "chopper_chuckle.mp3",
        moodLabel = "Dancing & Chuckling",
        subtitle = "Kono yarō! Complimenting me won't make me happy! (Dances happily)"
    ),
    ANGRY_SCOLD(
        id = "angry",
        spokenLine = "Baka-yaro! Did you take your medicine?! Don't you dare collapse from overwork! Listen to your doctor, idiot!",
        audioFileName = "chopper_angry.mp3",
        moodLabel = "Angry Doctor Scolding",
        subtitle = "Hey! Don't push yourself too hard! Take your medicine right now!"
    ),
    HIDING_PEEK(
        id = "hiding",
        spokenLine = "You can't see me, right?! Ehehe, I'm completely hidden behind this wall! Wait, am I facing the wrong way?!",
        audioFileName = "chopper_hide.mp3",
        moodLabel = "Hiding the Wrong Way",
        subtitle = "Peeking out from behind a pillar backwards... 'You can't see me!'"
    ),
    SHOCKED_DOCTOR(
        id = "shocked",
        spokenLine = "Gyaaaah! Doctor! Someone call a doctor right now!! Wait a minute... I AM THE DOCTOR!!",
        audioFileName = "chopper_shocked.mp3",
        moodLabel = "Shocked Doctor",
        subtitle = "Doctor! Doctor! ...Wait, I'M the doctor!"
    ),
    COTTON_CANDY(
        id = "cotton_candy",
        spokenLine = "Cotton candy! Yay! You finished your reminder! Cotton Candy Lover Chopper approves!",
        audioFileName = "chopper_cotton_candy.mp3",
        moodLabel = "Cotton Candy Delight",
        subtitle = "Mmm cotton candy! Great job finishing your reminder!"
    ),
    MEDICINE_ALERT(
        id = "medicine",
        spokenLine = "Doctor Chopper prescription! Take your medicine and drink a glass of water right now! No skipping!",
        audioFileName = "chopper_medicine.mp3",
        moodLabel = "Medicine Time",
        subtitle = "Doctor's orders: Take your pills with water right away!"
    ),
    WATER_ALERT(
        id = "water",
        spokenLine = "Hydration check! Your body is losing water! Drink a fresh cup of water right now!",
        audioFileName = "chopper_water.mp3",
        moodLabel = "Hydration Check",
        subtitle = "Drink 250ml of clean water! Doctor's orders!"
    ),
    SLEEP_ALERT(
        id = "sleep",
        spokenLine = "Hey, it's late! Even pirates need proper rest to stay strong! Go to sleep, baka!",
        audioFileName = "chopper_sleep.mp3",
        moodLabel = "Rest & Sleep",
        subtitle = "Rest is the best medicine! Time to sleep, boss!"
    )
}

class ChopperVoiceManager(private val context: Context) {

    companion object {
        const val DEFAULT_VOICE_ID = "e5e3a1d83d6f491db3c26b3929052b34"
        private const val PREFS_NAME = "chopper_voice_prefs"
        private const val KEY_GITHUB_REPO_URL = "github_voice_repo_url"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var mediaPlayer: MediaPlayer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isVoiceMuted = MutableStateFlow(false)
    val isVoiceMuted: StateFlow<Boolean> = _isVoiceMuted.asStateFlow()

    private val _currentVoiceId = MutableStateFlow(DEFAULT_VOICE_ID)
    val currentVoiceId: StateFlow<String> = _currentVoiceId.asStateFlow()

    private val _currentReaction = MutableStateFlow(ChopperReaction.CHUCKLE_FLUSTERED)
    val currentReaction: StateFlow<ChopperReaction> = _currentReaction.asStateFlow()

    private val _githubVoiceRepoUrl = MutableStateFlow(
        prefs.getString(KEY_GITHUB_REPO_URL, "") ?: ""
    )
    val githubVoiceRepoUrl: StateFlow<String> = _githubVoiceRepoUrl.asStateFlow()

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

        // High-pitched, cheerful, anime-inspired delivery (Tony Tony Chopper style)
        engine.setPitch(1.42f)
        engine.setSpeechRate(1.10f)
    }

    fun setVoiceId(newVoiceId: String) {
        _currentVoiceId.value = newVoiceId
        tts?.let { engine ->
            if (isTtsReady) {
                applyVoiceConfiguration(engine, newVoiceId)
            }
        }
    }

    fun setGithubVoiceRepoUrl(url: String) {
        val trimmed = url.trim()
        _githubVoiceRepoUrl.value = trimmed
        prefs.edit().putString(KEY_GITHUB_REPO_URL, trimmed).apply()
        Log.i("ChopperVoiceManager", "Updated GitHub Voice URL: $trimmed")
    }

    /**
     * Trigger Chopper emotional reaction with voice/sound and animation state.
     */
    fun triggerReaction(reaction: ChopperReaction, overrideMute: Boolean = false, onDone: (() -> Unit)? = null) {
        _currentReaction.value = reaction
        val baseUrl = _githubVoiceRepoUrl.value.trim()

        if (baseUrl.isNotEmpty() && !_isVoiceMuted.value) {
            // Attempt to stream / play audio clip from configured GitHub / raw URL
            val audioUrl = if (baseUrl.endsWith("/")) {
                "$baseUrl${reaction.audioFileName}"
            } else {
                "$baseUrl/${reaction.audioFileName}"
            }
            playRemoteAudioOrFallback(audioUrl, reaction.spokenLine, overrideMute, onDone)
        } else {
            // Standard high-pitch anime TTS recitation
            speak(reaction.spokenLine, overrideMute = overrideMute, onDone = onDone)
        }
    }

    private fun playRemoteAudioOrFallback(audioUrl: String, fallbackText: String, overrideMute: Boolean, onDone: (() -> Unit)?) {
        try {
            stopAudio()
            _isSpeaking.value = true
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener {
                    _isSpeaking.value = false
                    stopAudio()
                    onDone?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w("ChopperVoiceManager", "MediaPlayer error: $what, $extra. Falling back to TTS.")
                    _isSpeaking.value = false
                    stopAudio()
                    speak(fallbackText, overrideMute = overrideMute, onDone = onDone)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.w("ChopperVoiceManager", "Failed to init MediaPlayer for URL: $audioUrl. Falling back to TTS.", e)
            _isSpeaking.value = false
            speak(fallbackText, overrideMute = overrideMute, onDone = onDone)
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignored
        } finally {
            mediaPlayer = null
        }
    }

    fun testCurrentVoice() {
        triggerReaction(ChopperReaction.CHUCKLE_FLUSTERED, overrideMute = true)
    }

    fun toggleMute() {
        val newMute = !_isVoiceMuted.value
        _isVoiceMuted.value = newMute
        if (newMute) {
            stopSpeaking()
            stopAudio()
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

        stopAudio()
        currentOnDoneCallback = onDone

        val sanitized = text.replace(Regex("[\\p{So}\\p{Cn}]"), " ")
            .replace("*", "")
            .trim()

        _isSpeaking.value = true
        val utteranceId = "chopper_utterance_${System.currentTimeMillis()}"
        tts?.speak(sanitized, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun askUnderstandingCheck(onDone: () -> Unit) {
        val question = "Boss! Do you understand me properly? Doctor Chopper is ready to take your voice reminder commands!"
        speak(question, overrideMute = true, onDone = onDone)
    }

    fun stopSpeaking() {
        tts?.stop()
        stopAudio()
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
