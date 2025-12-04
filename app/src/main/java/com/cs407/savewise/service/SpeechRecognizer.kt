package com.cs407.savewise.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.cs407.savewise.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel


class SpeechRecognizerHelper(
    context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    // 🔔 Called when we auto-stop because of silence (for the UI button)
    private val onAutoStop: (() -> Unit)? = null
) {

    private val recognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val prefs = UserPreferencesRepository(context.applicationContext)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var isListening: Boolean = false
    private var lastVoiceTime: Long = 0L
    private var silenceJob: Job? = null
    private var autoStopEnabled: Boolean = false

    init {
        recognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {
                // user started talking
                lastVoiceTime = SystemClock.elapsedRealtime()
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Treat > 2 dB as "some voice" to refresh the timer
                if (autoStopEnabled && isListening && rmsdB > 2f) {
                    lastVoiceTime = SystemClock.elapsedRealtime()
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                // results / error will follow
            }

            override fun onError(error: Int) {
                stopSilenceWatcher()
                isListening = false
                onError("Error: $error")
            }

            override fun onResults(results: Bundle?) {
                stopSilenceWatcher()
                isListening = false

                val data = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.trim()

                if (!data.isNullOrEmpty()) {
                    onResult(data)
                } else {
                    onError("Nothing detected")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Any partial text → we heard something, refresh timer
                if (autoStopEnabled && isListening) {
                    lastVoiceTime = SystemClock.elapsedRealtime()
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    /**
     * Start recognition.
     * Reads the "auto recording" setting from UserPreferences and,
     * if enabled, starts a watcher that auto-stops after 2s of silence.
     */
    fun start() {
        if (isListening) return

        isListening = true
        lastVoiceTime = SystemClock.elapsedRealtime()

        // Kick off listening immediately
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer.startListening(intent)

        // Start silence watcher (reads autoRecording from prefs)
        startSilenceWatcher()
    }

    fun stop() {
        if (!isListening) return
        isListening = false
        stopSilenceWatcher()
        recognizer.stopListening()
    }

    fun destroy() {
        isListening = false
        stopSilenceWatcher()
        scope.cancel()
        recognizer.destroy()
    }

    // ----------------- internal helpers -----------------

    private fun startSilenceWatcher() {
        stopSilenceWatcher()

        silenceJob = scope.launch {
            // Read current setting from the same prefs MeViewModel uses :contentReference[oaicite:1]{index=1}
            autoStopEnabled = prefs.preferencesFlow
                .map { it.autoRecording }
                .first()

            if (!autoStopEnabled) return@launch

            while (isListening) {
                delay(250L) // check about 4x per second

                val idleMs = SystemClock.elapsedRealtime() - lastVoiceTime
                if (idleMs >= 2000L) { // 2 seconds of silence
                    // Stop recognizer
                    isListening = false
                    recognizer.stopListening()

                    // Tell UI to stop the button animation
                    onAutoStop?.invoke()
                    break
                }
            }
        }
    }

    private fun stopSilenceWatcher() {
        silenceJob?.cancel()
        silenceJob = null
    }
}
