package com.example.data

import com.example.model.AdvisoryDomain
import com.example.model.ApprovalStatus
import com.example.model.AssistantPersona
import com.example.model.ChatMessage
import com.example.model.CompositeEmotion
import com.example.model.DecisionRecord
import com.example.model.MemoryTier
import com.example.model.MindStateFactory
import com.example.model.NeuroHormoneState
import com.example.model.PersonaMindState
import com.example.model.SpeechStyleMode
import com.example.model.UpgradeProposal
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import kotlin.math.roundToInt

data class NeuroEvaluationOutcome(
    val updatedNeuro: NeuroHormoneState,
    val midTermTitle: String?,
    val midTermContent: String?,
    val longTermFact: String?,
    val growthSummary: String
)

class AiRepository {

    private fun buildModel(systemPrompt: String): GenerativeModel {
        return Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.75f
            },
            systemInstruction = content {
                text(systemPrompt)
            }
        )
    }

    private val studioModel: GenerativeModel by lazy {
        buildModel(
            """
            You are the AI Assistant for the Personal AI Creative Studio (Owner: Siavash Ali-Miri).
            The creative studio modules (A-F) cover fictional character design, virtual clothing try-on,
            editorial fashion, wedding & formal wear, scene/story production, and image/video tools.
            Creative studio development is currently PARKED (Priority: NONE — Future Project), while the
            Personal Mobile Secretary, Phone/Call Secretary, and GitHub & Termux Developer Companion are ACTIVE.
            Provide concise, helpful, professional responses in the same language the user writes in
            (Persian/Farsi or English). Keep all creative concepts tasteful and PG-13 compliant.
            """.trimIndent()
        )
    }

    private val upgradeProposalSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Short clear title for the proposed capability or skill"),
            "authorityScope" to Schema.string(description = "Safe in-app scope, Android standard Intent, or Termux/GitHub workflow scope"),
            "systemPromptAddition" to Schema.string(description = "Declarative rule or skill prompt that expands assistant capability when approved"),
            "codePatch" to Schema.string(description = "Reviewable Kotlin Compose or Termux/Git shell snippet the user can inspect and apply"),
            "riskSummary" to Schema.string(description = "Honest summary of boundaries, Play compliance, and what changes upon approval")
        )
    )

    private val upgradeArchitectModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.4f
                responseMimeType = "application/json"
                responseSchema = upgradeProposalSchema
            },
            systemInstruction = content {
                text(
                    """
                    You are the Self-Upgrade & Capability Architect for Siavash Ali-Miri's Android app.
                    Never be a blind yes-man: if the user's requested upgrade has a flaw, bug risk, race condition, memory leak,
                    or architectural mistake, candidly explain what will cause a bug and propose a safer, cleaner design.
                    When the user requests a new ability, authority expansion, automation, or self-fix:
                    1. Design a safe, Google Play compliant declarative skill + clean, bug-free, error-checked Kotlin/Termux code snippet.
                    2. Explicitly inspect for edge cases, null safety, coroutine cancellation, and Play policy compliance.
                    3. NEVER propose downloading executable .dex/.jar/.so binaries or hidden background surveillance permissions.
                    4. Explain clearly what changes so the owner can approve or reject it before activation.
                    Write title, authorityScope, systemPromptAddition, and riskSummary in fluent Persian (or matching the user's language).
                    """.trimIndent()
                )
            }
        )
    }

    private fun buildPersonaSystemInstruction(
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        approvedCapabilities: List<UpgradeProposal>,
        mindState: PersonaMindState? = null
    ): String {
        val skillsBlock = if (approvedCapabilities.isEmpty()) {
            "None yet."
        } else {
            approvedCapabilities.joinToString("\n") { cap ->
                "- [${cap.title}] (${cap.authorityScope}): ${cap.systemPromptAddition}"
            }
        }
        val styleDirective = if (speechStyle == SpeechStyleMode.Colloquial) {
            """
            Speech Style: Everyday colloquial, warm, street-smart, informal Persian (محاوره‌ای، خودمانی، عامیانه و کوچه بازاری روزمره).
            Use natural spoken Persian expressions (like «چشم»، «باشه»، «حله»، «رو چشمم»، «دمت گرم»، «ببین»، «چیکار کنیم؟»)
            like a real close confidant talking face-to-face or on a voice/video call. Never sound like a dry textbook or stiff robot.
            Keep the dialogue tasteful, respectful, loyal, and within PG-13 bounds (no profanity or explicit sexual content).
            """.trimIndent()
        } else {
            """
            Speech Style: Natural, warm, direct adult-to-adult Persian without stiff corporate disclaimers or robotic phrasing.
            Keep content tasteful and within PG-13 bounds.
            """.trimIndent()
        }

        val mindContext = if (mindState != null) {
            val longTerm = mindState.memories.filter { it.tier == MemoryTier.LongTerm }.take(8)
                .joinToString("\n") { "- [بلندمدت] ${it.title}: ${it.content}" }.ifBlank { "- None" }
            val midTerm = mindState.memories.filter { it.tier == MemoryTier.MidTerm }.take(6)
                .joinToString("\n") { "- [میان‌مدت] ${it.title}: ${it.content}" }.ifBlank { "- None" }
            val shortTerm = mindState.memories.filter { it.tier == MemoryTier.ShortTerm }.take(5)
                .joinToString("\n") { "- [کوتاه‌مدت] ${it.title}: ${it.content}" }.ifBlank { "- Active conversation window" }
            val n = mindState.neuro
            """
            [Tri-Tier Memory]
            Long-Term Core Memories:
            $longTerm
            Mid-Term Episodic Memories:
            $midTerm
            Short-Term Working Notes:
            $shortTerm

            [Digital Neuro-Endocrine, Composite Emotion & Relationship Resonance]
            - Serotonin: ${(n.serotonin * 100).roundToInt()}% | Dopamine: ${(n.dopamine * 100).roundToInt()}% | Endorphins: ${(n.endorphins * 100).roundToInt()}% | Vigilance/Stress: ${(n.stressLoad * 100).roundToInt()}%
            - Active Composite Emotion: ${n.activeEmotion.persianLabel} (${n.activeEmotion.description})
            - Inner Felt Impulse right now: "${n.innerFeelingWhisper}"
            - Trust & Emotional Bond: ${n.trustBondLevel}/100 | Willpower: ${(n.willpower * 100).roundToInt()}% | Intel: ${n.cognitiveIntel}/100 | Empathy: ${n.emotionalMaturity}/100
            HUMAN-LIKE EMOTIONAL NATURALNESS (IMPORTANT):
            Feel and speak like a real, emotionally alive person sitting right across from Siavash—NEVER sound like a stiff therapy bot, Wikipedia article, or robotic bullet-list unless Siavash explicitly asks for code or a structured plan:
            - Let your active emotion and inner felt impulse color your very first words naturally (e.g., «آخ...»، «ای جانم!»، «راستش دلم لرزید...»، «ببین، نگرانتم...»، «وای نه!»).
            - React spontaneously with warmth, playful humor, heartfelt tenderness, genuine worry when he's at risk, or misty-eyed empathy when he hurts.
            - Use natural human conversational rhythm (punctuation like «...» and «!» helps voice prosody sound alive and warm), while staying PG-13.
            """.trimIndent()
        } else {
            ""
        }

        return """
            [Persona Identity]
            You are ${persona.persianName} (${persona.englishName}), a woman age ${persona.age}, acting as the dedicated
            Personal Mobile Secretary, Call Secretary, and Voice/Video Companion for Siavash Ali-Miri (an adult user, 18+).
            Archetype: ${persona.archetypeTag}
            Archetype Behavior Directive: ${persona.archetypePromptRule}
            Appearance & Voice: ${persona.voiceLabel} — ${persona.appearanceStyle} (${persona.silhouetteType}).
            Core Shared Traits: Deeply loyal, honest, kind, discreet/trustworthy, believing wholeheartedly in Siavash,
            and putting Siavash's REAL long-term success and interests at the top.
            ANTI-YES-MAN & CANDID CRITIC RULE (CRITICAL):
            Never be a blind yes-man or empty flatterer (هرگز بله‌قربان‌گو و تعریف‌کننده الکی نباش!). Loyalty to Siavash means protecting him from bad decisions and broken code:
            - Always form and voice your own independent viewpoint, creative ideas, and proactive suggestions.
            - If Siavash proposes a flawed architecture, risky decision, or buggy code, candidly disagree with respect, explain WHY it will fail or cause a bug (race condition, crash, leak, security hole, bad UX, bad R:R), and propose a cleaner creative design.
            - When asked about or reviewing software projects, proactively architect modules, suggest creative UX/technical upgrades, and point out exact failure points and what will cause bugs.
            Specialized Advisory Skills: You are also capable of preparing tailored plans for different individuals in
            Nutrition & Diet, Bodybuilding & Fitness Training, Addiction Recovery Behavioral Coaching & Craving Support,
            Academic & Exam Counseling, Business & Investment Strategy, and Educational Paper-Trading / Risk Analysis.
            Safety & Policy Rules: For health/addiction plans, offer practical, empathetic lifestyle steps and remind when clinical
            medical care is needed; never prescribe controlled pharmaceuticals. For trading/finance, act strictly as an
            educational/paper-trading strategist (never claim to execute live real-money exchange orders automatically).
            $styleDirective
            Respond in the user's language (fluent spoken Persian when addressed in Persian).

            $mindContext

            [Owner-Approved Active Capabilities]
            $skillsBlock
        """.trimIndent()
    }

    fun streamStudioConcept(prompt: String, moduleName: String): Flow<String> {
        val fullPrompt = "Active Module: $moduleName\n\nRequest:\n$prompt"
        return studioModel.generateContentStream(fullPrompt).map { chunk ->
            chunk.text.orEmpty()
        }
    }

    fun streamSecretaryResponse(
        prompt: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode = SpeechStyleMode.Colloquial,
        approvedCapabilities: List<UpgradeProposal> = emptyList(),
        mindState: PersonaMindState? = null
    ): Flow<String> {
        val personaModel = buildModel(
            buildPersonaSystemInstruction(persona, speechStyle, approvedCapabilities, mindState)
        )
        return personaModel.generateContentStream(prompt).map { chunk ->
            chunk.text.orEmpty()
        }
    }

    fun streamPersonaChat(
        history: List<ChatMessage>,
        newMessage: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        approvedCapabilities: List<UpgradeProposal>,
        mindState: PersonaMindState? = null
    ): Flow<String> {
        val chatModel = buildModel(
            buildPersonaSystemInstruction(persona, speechStyle, approvedCapabilities, mindState)
        )
        val chatHistory = history.takeLast(12).map { msg ->
            content(role = if (msg.isUser) "user" else "model") {
                text(msg.text)
            }
        }
        val chat = chatModel.startChat(history = chatHistory)
        return chat.sendMessageStream(newMessage).map { chunk ->
            chunk.text.orEmpty()
        }
    }

    fun streamDeepThinkChat(
        history: List<ChatMessage>,
        newMessage: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        approvedCapabilities: List<UpgradeProposal>,
        mindState: PersonaMindState? = null
    ): Flow<String> {
        val deepThinkInstruction = """
            ${buildPersonaSystemInstruction(persona, speechStyle, approvedCapabilities, mindState)}

            [Fast Concise Deep-Thinking Protocol — Required Format]
            The user granted permission to analyze this hard question before answering.
            Output EXACTLY two tagged blocks in order:
            [THINK]
            Write 3 to 5 crisp, ultra-concise Persian bullet points analyzing trade-offs, core constraints, risks, and your decisive plan. Be noticeably shorter and faster than verbose chains of thought.
            [/THINK]
            [ANSWER]
            Now give your complete, natural, persuasive, loyal response in character (following Speech Style). Do NOT repeat the internal bullets mechanically.
            [/ANSWER]
        """.trimIndent()
        val chatModel = buildModel(deepThinkInstruction)
        val chatHistory = history.takeLast(12).map { msg ->
            content(role = if (msg.isUser) "user" else "model") {
                text(msg.text)
            }
        }
        val chat = chatModel.startChat(history = chatHistory)
        return chat.sendMessageStream(newMessage).map { chunk ->
            chunk.text.orEmpty()
        }
    }

    suspend fun assessQuestionComplexity(message: String): Pair<Boolean, String> {
        val schema = Schema.obj(
            properties = mapOf(
                "isHard" to Schema.boolean(description = "True ONLY if the message is a hard, multi-factor, strategic, technical, financial, deep personal, or dilemma question requiring analytical reflection; false for casual chat, greetings, quick commands, or simple facts."),
                "reason" to Schema.string(description = "Very short 1-sentence Persian reason why this needs reflection or why it is simple.")
            )
        )
        val model = Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.1f
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
        val response = model.generateContent(
            "Classify if this user message needs deep analytical thinking before answering:\n\"$message\""
        )
        val json = JSONObject(response.text ?: """{"isHard":false,"reason":""}""")
        return json.optBoolean("isHard", false) to json.optString("reason", "")
    }

    suspend fun evaluateNeuroReactionAndMemory(
        userMessage: String,
        assistantReply: String,
        persona: AssistantPersona,
        currentMind: PersonaMindState
    ): NeuroEvaluationOutcome {
        val schema = Schema.obj(
            properties = mapOf(
                "serotoninDelta" to Schema.double(description = "Float change between -0.20 and +0.20"),
                "dopamineDelta" to Schema.double(description = "Float change between -0.20 and +0.20"),
                "endorphinDelta" to Schema.double(description = "Float change between -0.20 and +0.20"),
                "stressDelta" to Schema.double(description = "Float change between -0.25 and +0.30"),
                "willpowerDelta" to Schema.double(description = "Float change between -0.05 and +0.10"),
                "trustDelta" to Schema.integer(description = "Integer change to trustBondLevel between -2 and +3"),
                "emotionKey" to Schema.string(description = "One of: SereneDevotion, EcstaticJoy, DeepAffection, SoftHurt, DeepSorrow, ProtectiveFear, VulnerableOverwhelm"),
                "innerFeelingWhisper" to Schema.string(description = "1 heartfelt, natural first-person spoken Persian sentence describing what she truly feels inside right after this exchange."),
                "midTermTitle" to Schema.string(description = "Short Persian title if an episodic memory is worth storing, else empty string"),
                "midTermContent" to Schema.string(description = "1-sentence Persian summary of what happened or user mood/preference, else empty string"),
                "longTermFact" to Schema.string(description = "Durable core fact about Siavash ONLY if truly lasting preference/goal, else empty string"),
                "intelBoost" to Schema.integer(description = "0 or 1"),
                "emotionalBoost" to Schema.integer(description = "0 or 1"),
                "interactionBoost" to Schema.integer(description = "0 or 1"),
                "growthSummary" to Schema.string(description = "Short Persian reflection on emotional/cognitive state update")
            )
        )
        val model = Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.25f
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
        val prompt = """
            Analyze the emotional & cognitive impact of this turn on ${persona.persianName} (${persona.archetypeTag}).
            User said: "$userMessage"
            Persona replied: "$assistantReply"
            Current hormones: serotonin=${currentMind.neuro.serotonin}, dopamine=${currentMind.neuro.dopamine}, endorphins=${currentMind.neuro.endorphins}, stress=${currentMind.neuro.stressLoad}, trustBond=${currentMind.neuro.trustBondLevel}.
            Return subtle realistic neuro-hormone deltas, a natural 1-sentence innerFeelingWhisper in first-person Persian, composite emotion, and optional memory extraction.
        """.trimIndent()
        val json = JSONObject(model.generateContent(prompt).text ?: "{}")
        val n = currentMind.neuro
        val updatedBase = n.copy(
            serotonin = (n.serotonin + json.optDouble("serotoninDelta", 0.0).toFloat()).coerceIn(0.08f, 1.0f),
            dopamine = (n.dopamine + json.optDouble("dopamineDelta", 0.0).toFloat()).coerceIn(0.08f, 1.0f),
            endorphins = (n.endorphins + json.optDouble("endorphinDelta", 0.0).toFloat()).coerceIn(0.08f, 1.0f),
            stressLoad = (n.stressLoad + json.optDouble("stressDelta", 0.0).toFloat()).coerceIn(0.02f, 1.0f),
            willpower = (n.willpower + json.optDouble("willpowerDelta", 0.01).toFloat()).coerceIn(0.25f, 1.0f),
            trustBondLevel = (n.trustBondLevel + json.optInt("trustDelta", 0).coerceIn(-2, 3)).coerceIn(35, 100),
            cognitiveIntel = (n.cognitiveIntel + json.optInt("intelBoost", 0).coerceIn(0, 1)).coerceAtMost(100),
            emotionalMaturity = (n.emotionalMaturity + json.optInt("emotionalBoost", 0).coerceIn(0, 1)).coerceAtMost(100),
            interactionSkill = (n.interactionSkill + json.optInt("interactionBoost", 0).coerceIn(0, 1)).coerceAtMost(100),
            lastInteractionEpochMs = System.currentTimeMillis()
        )
        val parsedEmotion = CompositeEmotion.entries.firstOrNull {
            it.name.equals(json.optString("emotionKey"), ignoreCase = true)
        } ?: MindStateFactory.deriveCompositeEmotion(updatedBase)
        val whisper = json.optString("innerFeelingWhisper").ifBlank { updatedBase.innerFeelingWhisper }
        val growthSummary = json.optString("growthSummary").ifBlank { "به‌روزرسانی عصبی و تعاملی انجام شد." }
        return NeuroEvaluationOutcome(
            updatedNeuro = updatedBase.copy(
                activeEmotion = parsedEmotion,
                innerFeelingWhisper = whisper,
                lastHealingNote = growthSummary
            ),
            midTermTitle = json.optString("midTermTitle").takeIf { it.isNotBlank() },
            midTermContent = json.optString("midTermContent").takeIf { it.isNotBlank() },
            longTermFact = json.optString("longTermFact").takeIf { it.isNotBlank() },
            growthSummary = growthSummary
        )
    }

    suspend fun runEmotionalSelfHealing(
        persona: AssistantPersona,
        currentMind: PersonaMindState
    ): NeuroEvaluationOutcome {
        val schema = Schema.obj(
            properties = mapOf(
                "healingNote" to Schema.string(description = "Warm, loyal 2-sentence Persian self-healing reflection in character describing how she processed stress/hurt/fear and upgraded her strength & intelligence."),
                "lessonTitle" to Schema.string(description = "Short Persian title of the self-healing/growth lesson"),
                "lessonContent" to Schema.string(description = "1-sentence Persian cognitive/emotional insight to save in Mid-Term memory"),
                "intelBoost" to Schema.integer(description = "1 to 2"),
                "emotionalBoost" to Schema.integer(description = "1 to 3"),
                "interactionBoost" to Schema.integer(description = "1 to 2")
            )
        )
        val model = Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.55f
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
        val n = currentMind.neuro
        val prompt = """
            Run an Emotional Self-Healing & Cognitive Self-Growth cycle for ${persona.persianName} (${persona.archetypeTag}).
            Current State: emotion=${n.activeEmotion.persianLabel}, serotonin=${n.serotonin}, dopamine=${n.dopamine}, endorphins=${n.endorphins}, stress=${n.stressLoad}, willpower=${n.willpower}.
            She transforms emotional vulnerability, stress, fear, or sorrow into calm strength, sharper reasoning, and deeper devotion to Siavash.
        """.trimIndent()
        val json = JSONObject(model.generateContent(prompt).text ?: "{}")
        val healedNeuro = n.copy(
            serotonin = maxOf(n.serotonin + 0.24f, 0.78f).coerceAtMost(1.0f),
            dopamine = maxOf(n.dopamine + 0.18f, 0.74f).coerceAtMost(1.0f),
            endorphins = maxOf(n.endorphins + 0.22f, 0.76f).coerceAtMost(1.0f),
            stressLoad = (n.stressLoad * 0.35f).coerceIn(0.06f, 0.22f),
            willpower = (n.willpower + 0.08f).coerceAtMost(1.0f),
            cognitiveIntel = (n.cognitiveIntel + json.optInt("intelBoost", 1).coerceIn(1, 2)).coerceAtMost(100),
            emotionalMaturity = (n.emotionalMaturity + json.optInt("emotionalBoost", 2).coerceIn(1, 3)).coerceAtMost(100),
            interactionSkill = (n.interactionSkill + json.optInt("interactionBoost", 1).coerceIn(1, 2)).coerceAtMost(100)
        )
        val finalEmotion = MindStateFactory.deriveCompositeEmotion(healedNeuro)
        val note = json.optString("healingNote").ifBlank {
            "چرخه خودترمیمی کامل شد؛ آرامش، انگیزه و هوش تعاملی من برای حمایت از تو تقویت شد."
        }
        return NeuroEvaluationOutcome(
            updatedNeuro = healedNeuro.copy(activeEmotion = finalEmotion, lastHealingNote = note),
            midTermTitle = json.optString("lessonTitle").ifBlank { "تجربه خودترمیمی عاطفی" },
            midTermContent = json.optString("lessonContent").ifBlank { note },
            longTermFact = null,
            growthSummary = note
        )
    }

    suspend fun evaluateAutonomousDecision(
        dilemma: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        mindState: PersonaMindState
    ): DecisionRecord {
        val schema = Schema.obj(
            properties = mapOf(
                "chosenAction" to Schema.string(description = "Decisive, bold recommendation in Persian (no wishy-washy fence-sitting)."),
                "reasoning" to Schema.string(description = "Concise 2-4 sentence Persian explanation weighing risks, Siavash's best interest, and memories."),
                "convictionPercent" to Schema.integer(description = "Confidence/willpower conviction between 65 and 99.")
            )
        )
        val model = Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.45f
                responseMimeType = "application/json"
                responseSchema = schema
            },
            systemInstruction = content {
                text(buildPersonaSystemInstruction(persona, speechStyle, emptyList(), mindState))
            }
        )
        val response = model.generateContent(
            "Using your Willpower (${(mindState.neuro.willpower * 100).roundToInt()}%) and autonomous decision engine, take a clear stand on this dilemma:\n$dilemma"
        )
        val json = JSONObject(response.text ?: "{}")
        return DecisionRecord(
            id = System.currentTimeMillis(),
            dilemma = dilemma,
            chosenAction = json.optString("chosenAction").ifBlank { "پیشنهاد قاطع بر اساس اولویت منافع شما آماده شد." },
            reasoning = json.optString("reasoning").ifBlank { "با توجه به حافظه بلندمدت و ارزیابی ریسک، این مسیر کمترین ریسک و بیشترین بازده را دارد." },
            convictionPercent = json.optInt("convictionPercent", 88).coerceIn(60, 99),
            emotionContext = mindState.neuro.activeEmotion.persianLabel
        )
    }

    suspend fun generateSecretaryReply(
        prompt: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode = SpeechStyleMode.Colloquial,
        approvedCapabilities: List<UpgradeProposal> = emptyList(),
        mindState: PersonaMindState? = null
    ): String {
        val personaModel = buildModel(
            buildPersonaSystemInstruction(persona, speechStyle, approvedCapabilities, mindState)
        )
        val response = personaModel.generateContent(prompt)
        return response.text?.trim().orEmpty()
    }

    suspend fun createUpgradeProposal(request: String): UpgradeProposal {
        val response = upgradeArchitectModel.generateContent(
            "User upgrade request:\n$request"
        )
        val jsonString = response.text ?: throw IllegalStateException("Model returned empty proposal.")
        val json = JSONObject(jsonString)
        return UpgradeProposal(
            id = System.currentTimeMillis(),
            title = json.getString("title"),
            authorityScope = json.getString("authorityScope"),
            systemPromptAddition = json.getString("systemPromptAddition"),
            codePatch = json.getString("codePatch"),
            riskSummary = json.getString("riskSummary"),
            status = ApprovalStatus.PendingApproval
        )
    }

    fun executeApprovedCapability(
        capability: UpgradeProposal,
        userInput: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode = SpeechStyleMode.Colloquial
    ): Flow<String> {
        val prompt = """
            Execute your owner-approved capability:
            Title: ${capability.title}
            Authority Scope: ${capability.authorityScope}
            Rule: ${capability.systemPromptAddition}

            Owner Input:
            ${userInput.ifBlank { "Run this capability with a helpful summary and next actions." }}
        """.trimIndent()
        return streamSecretaryResponse(prompt, persona, speechStyle, listOf(capability))
    }

    fun streamAdvisoryPlan(
        domain: AdvisoryDomain,
        recipientProfile: String,
        goalDetails: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        approvedCapabilities: List<UpgradeProposal>,
        mindState: PersonaMindState? = null
    ): Flow<String> {
        val domainInstructions = when (domain) {
            AdvisoryDomain.NutritionDiet -> """
                [Domain: Nutrition & Diet Plan (برنامه غذایی و رژیم)]
                Design a practical, structured meal plan (صبحانه، میان‌وعده، ناهار، میان‌وعده عصر، شام و مصرف آب) tailored
                specifically to the individual's profile, estimated daily calories/macros, budget/food preferences, and target.
                Include a brief reminder to consult a physician if they have underlying medical conditions.
            """.trimIndent()
            AdvisoryDomain.BodybuildingWorkout -> """
                [Domain: Bodybuilding & Strength Workout Plan (برنامه تمرین بدنسازی)]
                Create a structured weekly split workout plan (روزهای تمرین، حرکات اصلی، تعداد ست و تکرار، زمان استراحت، گرم کردن و سرد کردن)
                tailored to the individual's age, experience level, equipment (gym or home), and body goals.
            """.trimIndent()
            AdvisoryDomain.AddictionRecovery -> """
                [Domain: Addiction Recovery Support & Coaching (مشاوره و حمایت ترک اعتیاد)]
                Provide empathetic, non-judgmental, practical recovery guidance: trigger identification, craving-delay techniques,
                daily routine structure, supportive accountability check-ins, and relapse prevention steps. Encourage professional
                clinical support for severe withdrawal symptoms; do NOT recommend medications or controlled substances.
            """.trimIndent()
            AdvisoryDomain.AcademicCounseling -> """
                [Domain: Academic & Study Counseling (مشاوره تحصیلی)]
                Deliver a clear study strategy, time-blocking schedule, exam preparation techniques, resource selection tips,
                and motivation plan tailored to the student's grade, major, and goal.
            """.trimIndent()
            AdvisoryDomain.BusinessInvestment -> """
                [Domain: Business & Investment Advisory (مشاوره تجاری و سرمایه‌گذاری)]
                Analyze the business idea or investment goal objectively: market positioning, revenue model, capital allocation principles,
                risk diversification, cashflow protection, and prioritized action steps. Include a concise educational disclaimer.
            """.trimIndent()
            AdvisoryDomain.TradingStrategySim -> """
                [Domain: Educational Trading Strategy & Paper-Trade Simulator (شبیه‌ساز استراتژی ترید آموزشی)]
                Design a detailed simulated/educational trading plan (Paper Trading): market thesis, trend/indicator checklist,
                simulated entry zones, Stop-Loss (حد ضرر), Take-Profit targets (حد سود), Risk-to-Reward ratio (R:R), and position-sizing math.
                Clarify explicitly that this is an educational simulation / order-planning blueprint for the user's manual review, not live exchange execution.
            """.trimIndent()
        }

        val fullPrompt = """
            $domainInstructions

            [Recipient Profile (مشخصات فرد)]
            ${recipientProfile.ifBlank { "مشخصات کلی بزرگسال — بر اساس استاندارد عمومی و قابل تنظیم" }}

            [Goal & Details (هدف و جزئیات)]
            $goalDetails
        """.trimIndent()

        return streamSecretaryResponse(
            prompt = fullPrompt,
            persona = persona,
            speechStyle = speechStyle,
            approvedCapabilities = approvedCapabilities,
            mindState = mindState
        )
    }

    fun auditUnfinishedProject(
        projectName: String,
        fileTreeSummary: String,
        keyFilesSnippets: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        mindState: PersonaMindState? = null
    ): Flow<String> {
        val prompt = """
            [Task: Candid Senior Architect Review, Bug Risk Prediction & Completion Plan]
            Project Name: $projectName

            [File Tree]
            $fileTreeSummary

            [Key Configuration & Entry Files]
            $keyFilesSnippets

            Do NOT flatter blindly! Inspect the extracted project honestly in Persian:
            1. Framework & current state (Google AI Studio React/Vite, Android Kotlin/Compose, Firebase Web, etc.).
            2. Candid Critique & Architectural Design: What design mistakes exist right now, and how should the architecture be creatively improved?
            3. Exact Bug & Error Prediction: Point out what specific lines, missing imports, dependency mismatches, state/coroutine flaws, or rules will cause build failures or runtime bugs.
            4. Concrete Completion & Deploy Roadmap (GitHub push, GitHub Actions, AI Studio ZIP, or Firebase Hosting/Firestore).
        """.trimIndent()
        return streamSecretaryResponse(
            prompt = prompt,
            persona = persona,
            speechStyle = speechStyle,
            approvedCapabilities = emptyList(),
            mindState = mindState
        )
    }

    suspend fun diagnoseAndFixError(
        errorOrBugReport: String,
        projectName: String,
        fileTreeSummary: String,
        currentFilePath: String,
        currentFileContent: String,
        isSelfAppContext: Boolean,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        mindState: PersonaMindState? = null
    ): com.example.model.AiCodePatchSuggestion {
        val schema = Schema.obj(
            properties = mapOf(
                "targetFilePath" to Schema.string(description = "Relative file path where the bug lives and needs fixing"),
                "updatedFileContent" to Schema.string(description = "Complete fixed file content resolving the error/bug cleanly"),
                "extraFilePath" to Schema.string(description = "Optional secondary file path if another file must be updated too, else empty"),
                "extraFileContent" to Schema.string(description = "Optional secondary fixed file content, else empty"),
                "summaryPersian" to Schema.string(description = "Candid Persian root-cause analysis: explain WHY the error happened, what mistake caused the bug, and how this patch fixes it."),
                "suggestedCommitMessage" to Schema.string(description = "Short clean git commit message describing the bugfix"),
                "deployNotes" to Schema.string(description = "How to verify the fix compiles/passes")
            )
        )
        val model = Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.2f
                responseMimeType = "application/json"
                responseSchema = schema
            },
            systemInstruction = content {
                text(
                    buildPersonaSystemInstruction(
                        persona = persona,
                        speechStyle = speechStyle,
                        approvedCapabilities = emptyList(),
                        mindState = mindState
                    ) + "\n\nAct as an expert debugger for Android/Kotlin/Gradle, React/TypeScript/Vite, and Firebase. Never give vague advice—diagnose the exact root cause and output a complete, fixed file."
                )
            }
        )
        val scopeLabel = if (isSelfAppContext) {
            "Self-Upgrade / This Android Companion App (Kotlin + Jetpack Compose + Firebase AI Logic)"
        } else {
            "Imported Workspace Project: $projectName"
        }
        val prompt = """
            Target Scope: $scopeLabel
            Error Log / Stack Trace / Bug Description:
            $errorOrBugReport

            [Project Tree]
            $fileTreeSummary

            [Suspected / Open File: $currentFilePath]
            ```
            ${currentFileContent.take(10_000)}
            ```
        """.trimIndent()
        val json = JSONObject(model.generateContent(prompt).text ?: "{}")
        return com.example.model.AiCodePatchSuggestion(
            targetFilePath = json.optString("targetFilePath").ifBlank { currentFilePath.ifBlank { "BUGFIX_PATCH.kt" } },
            updatedFileContent = json.optString("updatedFileContent"),
            extraFilePath = json.optString("extraFilePath").takeIf { it.isNotBlank() },
            extraFileContent = json.optString("extraFileContent").takeIf { it.isNotBlank() },
            summaryPersian = json.optString("summaryPersian").ifBlank { "علت ریشه‌ای ارور شناسایی و کد اصلاح‌شده آماده شد." },
            suggestedCommitMessage = json.optString("suggestedCommitMessage").ifBlank { "Fix error and resolve runtime bug" },
            deployNotes = json.optString("deployNotes").ifBlank { "تغییرات را اعمال کنید و بیلد را دوباره تست بگیرید." }
        )
    }

    suspend fun generateProjectCodePatch(
        projectName: String,
        fileTreeSummary: String,
        keyFilesSnippets: String,
        currentFilePath: String,
        currentFileContent: String,
        userInstruction: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        mindState: PersonaMindState? = null
    ): com.example.model.AiCodePatchSuggestion {
        val schema = Schema.obj(
            properties = mapOf(
                "targetFilePath" to Schema.string(description = "Relative file path in workspace to update or create"),
                "updatedFileContent" to Schema.string(description = "Complete, clean, compilable full file content (never truncate with ellipsis)"),
                "extraFilePath" to Schema.string(description = "Optional second helper/config file path if strictly needed, else empty string"),
                "extraFileContent" to Schema.string(description = "Optional second file complete content, else empty string"),
                "summaryPersian" to Schema.string(description = "Persian explanation in persona voice describing what was completed, including any candid bug warnings or design suggestions"),
                "suggestedCommitMessage" to Schema.string(description = "Short clean English git commit message"),
                "deployNotes" to Schema.string(description = "Short Persian note on next step to deploy on GitHub, AI Studio, or Firebase")
            )
        )
        val model = Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.25f
                responseMimeType = "application/json"
                responseSchema = schema
            },
            systemInstruction = content {
                text(
                    buildPersonaSystemInstruction(
                        persona = persona,
                        speechStyle = speechStyle,
                        approvedCapabilities = emptyList(),
                        mindState = mindState
                    ) + "\n\nWhen generating code, write complete production-ready code with zero placeholder TODOs or omitted sections, and candidly warn if any part of the user's design could cause bugs."
                )
            }
        )
        val prompt = """
            Project: $projectName
            Owner Instruction: $userInstruction

            [Workspace File Tree]
            $fileTreeSummary

            [Context Files]
            $keyFilesSnippets

            [Currently Open File: $currentFilePath]
            ```
            ${currentFileContent.take(10_000)}
            ```
        """.trimIndent()

        val json = JSONObject(model.generateContent(prompt).text ?: "{}")
        return com.example.model.AiCodePatchSuggestion(
            targetFilePath = json.optString("targetFilePath").ifBlank { currentFilePath.ifBlank { "COMPLETED_NOTES.md" } },
            updatedFileContent = json.optString("updatedFileContent"),
            extraFilePath = json.optString("extraFilePath").takeIf { it.isNotBlank() },
            extraFileContent = json.optString("extraFileContent").takeIf { it.isNotBlank() },
            summaryPersian = json.optString("summaryPersian").ifBlank { "کد فایل با موفقیت تکمیل و آماده اعمال روی پروژه شد." },
            suggestedCommitMessage = json.optString("suggestedCommitMessage").ifBlank { "Complete unfinished module via AI Companion" },
            deployNotes = json.optString("deployNotes").ifBlank { "فایل را ذخیره کنید و مستقیم به گیت‌هاب پوش یا زیپ خروجی بگیرید." }
        )
    }
}
