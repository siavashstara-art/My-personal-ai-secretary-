package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.AssistantPersona
import com.example.model.CompositeEmotion
import java.util.Locale

private const val TAG = "PersonaVoicePlayer"

class PersonaVoicePlayer(
    context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    @Volatile
    private var activeFinalUtteranceId: String? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChanged(true)
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == null || utteranceId == activeFinalUtteranceId) {
                        onSpeakingStateChanged(false)
                    }
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

    fun speak(
        text: String,
        persona: AssistantPersona,
        emotion: CompositeEmotion = CompositeEmotion.SereneDevotion
    ) {
        val engine = tts ?: return
        if (!isReady || text.isBlank()) return

        val containsPersian = text.any { it in '\u0600'..'\u06FF' }
        val targetLocale = if (containsPersian) Locale("fa", "IR") else Locale.US
        val langResult = engine.setLanguage(targetLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.US)
        }

        val sanitized = sanitizeForNaturalSpeech(text).take(1800)
        if (sanitized.isBlank()) return

        val segments = splitIntoExpressiveSegments(sanitized)
        if (segments.isEmpty()) return

        val basePitch = (persona.voicePitch * emotion.pitchMultiplier).coerceIn(0.65f, 1.45f)
        val baseRate = (persona.voiceRate * emotion.rateMultiplier).coerceIn(0.75f, 1.35f)
        val sessionNonce = System.currentTimeMillis()
        val lastIndex = segments.lastIndex
        activeFinalUtteranceId = "persona_${persona.id}_${sessionNonce}_$lastIndex"

        segments.forEachIndexed { idx, seg ->
            val clausePitch = (basePitch + seg.pitchOffset).coerceIn(0.65f, 1.45f)
            val clauseRate = (baseRate + seg.rateOffset).coerceIn(0.75f, 1.35f)
            engine.setPitch(clausePitch)
            engine.setSpeechRate(clauseRate)

            val mode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val uttId = "persona_${persona.id}_${sessionNonce}_$idx"
            engine.speak(seg.spokenText, mode, null, uttId)

            if (idx < lastIndex && seg.pauseAfterMs > 0L) {
                engine.playSilentUtterance(
                    seg.pauseAfterMs,
                    TextToSpeech.QUEUE_ADD,
                    "pause_${sessionNonce}_$idx"
                )
            }
        }
    }

    private data class ExpressiveSegment(
        val spokenText: String,
        val pitchOffset: Float,
        val rateOffset: Float,
        val pauseAfterMs: Long
    )

    private fun sanitizeForNaturalSpeech(raw: String): String {
        return raw
            .replace(Regex("\\[THINK][\\s\\S]*?\\[/THINK]"), "")
            .replace("[ANSWER]", "")
            .replace("[/ANSWER]", "")
            .replace(Regex("https?://\\S+"), "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("—", "، ")
            .replace("•", "، ")
            .trim()
    }

    private fun splitIntoExpressiveSegments(text: String): List<ExpressiveSegment> {
        val rawChunks = text.split(Regex("(?<=[.!؟?\\n])\\s+|(?<=\\.\\.\\.)\\s*"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return rawChunks.map { chunk ->
            val hasExclamation = chunk.contains('!')
            val hasQuestion = chunk.contains('؟') || chunk.contains('?')
            val hasEllipsis = chunk.contains("...") || chunk.contains('…')

            val pitchOffset = when {
                hasExclamation -> 0.05f
                hasQuestion -> 0.04f
                hasEllipsis -> -0.03f
                else -> 0.0f
            }
            val rateOffset = when {
                hasExclamation -> 0.04f
                hasEllipsis -> -0.05f
                else -> 0.0f
            }
            val pauseMs = when {
                hasEllipsis -> 280L
                hasExclamation || hasQuestion -> 210L
                else -> 150L
            }
            ExpressiveSegment(
                spokenText = chunk,
                pitchOffset = pitchOffset,
                rateOffset = rateOffset,
                pauseAfterMs = pauseMs
            )
        }
    }

    fun stop() {
        activeFinalUtteranceId = null
        tts?.stop()
        onSpeakingStateChanged(false)
    }

    fun release() {
        activeFinalUtteranceId = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
