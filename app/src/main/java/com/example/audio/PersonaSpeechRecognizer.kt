package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

private const val TAG = "PersonaSpeechRecognizer"

class PersonaSpeechRecognizer(
    context: Context,
    private val onListeningChanged: (Boolean) -> Unit,
    private val onPartialTranscript: (String) -> Unit,
    private val onFinalTranscript: (String) -> Unit,
    private val onRmsLevel: (Float) -> Unit,
    private val onError: (String) -> Unit
) {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun startListening(languageTag: String = "fa-IR") {
        if (!isAvailable) {
            onError("موتور تشخیص گفتار در این دستگاه فعال نیست.")
            return
        }
        stopListening()
        val instance = SpeechRecognizer.createSpeechRecognizer(appContext)
        recognizer = instance
        instance.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onListeningChanged(true)
            }

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                onRmsLevel(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                onListeningChanged(false)
                onRmsLevel(0f)
            }

            override fun onError(error: Int) {
                onListeningChanged(false)
                onRmsLevel(0f)
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "خطای ضبط صدا"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "دسترسی میکروفون داده نشده است"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "خطای شبکه در تشخیص گفتار"
                    SpeechRecognizer.ERROR_NO_MATCH -> "صدایی تشخیص داده نشد؛ دوباره بگویید"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "مهلت صحبت تمام شد"
                    else -> "خطای موتور تبدیل ویس به متن ($error)"
                }
                Log.w(TAG, "SpeechRecognizer error=$error")
                onError(message)
            }

            override fun onResults(results: Bundle?) {
                onListeningChanged(false)
                onRmsLevel(0f)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcript = matches?.firstOrNull().orEmpty().trim()
                if (transcript.isNotEmpty()) {
                    onFinalTranscript(transcript)
                } else {
                    onError("متنی از صدا استخراج نشد.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (partial.isNotBlank()) {
                    onPartialTranscript(partial)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        instance.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
        onListeningChanged(false)
        onRmsLevel(0f)
    }

    fun release() {
        stopListening()
    }
}
