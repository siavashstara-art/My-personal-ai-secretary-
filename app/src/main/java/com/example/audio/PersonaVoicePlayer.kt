package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.AssistantPersona
import java.util.Locale

private const val TAG = "PersonaVoicePlayer"

class PersonaVoicePlayer(
    context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChanged(true)
                }

                override fun onDone(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    onSpeakingStateChanged(false)
                }
            })
        } else {
            Log.w(TAG, "TextToSpeech initialization failed with status=$status")
        }
    }

    fun speak(text: String, persona: AssistantPersona) {
        val engine = tts ?: return
        if (!isReady || text.isBlank()) return

        val containsPersian = text.any { it in '\u0600'..'\u06FF' }
        val targetLocale = if (containsPersian) Locale("fa", "IR") else Locale.US
        val langResult = engine.setLanguage(targetLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.US)
        }

        engine.setPitch(persona.voicePitch)
        engine.setSpeechRate(persona.voiceRate)

        val cleanedText = text
            .replace("*", "")
            .replace("#", "")
            .take(1500)

        engine.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "persona_${persona.id}")
    }

    fun stop() {
        tts?.stop()
        onSpeakingStateChanged(false)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
