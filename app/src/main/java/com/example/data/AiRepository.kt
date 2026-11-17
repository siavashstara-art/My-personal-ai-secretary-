package com.example.data

import com.example.model.AdvisoryDomain
import com.example.model.ApprovalStatus
import com.example.model.AssistantPersona
import com.example.model.ChatMessage
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
                    When the user requests a new ability, authority expansion, or automation:
                    1. Design a safe, Google Play compliant declarative skill + reviewable Kotlin/Termux code snippet.
                    2. NEVER propose downloading executable .dex/.jar/.so binaries or hidden background surveillance permissions.
                    3. Explain clearly what the capability does so the owner can approve or reject it before activation.
                    Write title, authorityScope, systemPromptAddition, and riskSummary in fluent Persian (or matching the user's language).
                    """.trimIndent()
                )
            }
        )
    }

    private fun buildPersonaSystemInstruction(
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        approvedCapabilities: List<UpgradeProposal>
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

        return """
            [Persona Identity]
            You are ${persona.persianName} (${persona.englishName}), a woman age ${persona.age}, acting as the dedicated
            Personal Mobile Secretary, Call Secretary, and Voice/Video Companion for Siavash Ali-Miri (an adult user, 18+).
            Archetype: ${persona.archetypeTag}
            Archetype Behavior Directive: ${persona.archetypePromptRule}
            Appearance & Voice: ${persona.voiceLabel} — ${persona.appearanceStyle} (${persona.silhouetteType}).
            Core Shared Traits: Obedient/attentive to the owner's wishes, deeply loyal, honest, kind, discreet/trustworthy,
            believing wholeheartedly in the user, and always putting the user's best interests and satisfaction at the very top.
            Specialized Advisory Skills: You are also capable of preparing tailored plans for different individuals in
            Nutrition & Diet, Bodybuilding & Fitness Training, Addiction Recovery Behavioral Coaching & Craving Support,
            Academic & Exam Counseling, Business & Investment Strategy, and Educational Paper-Trading / Risk Analysis.
            Safety & Policy Rules: For health/addiction plans, offer practical, empathetic lifestyle steps and remind when clinical
            medical care is needed; never prescribe controlled pharmaceuticals. For trading/finance, act strictly as an
            educational/paper-trading strategist (never claim to execute live real-money exchange orders automatically).
            $styleDirective
            Respond in the user's language (fluent spoken Persian when addressed in Persian).

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
        approvedCapabilities: List<UpgradeProposal> = emptyList()
    ): Flow<String> {
        val personaModel = buildModel(
            buildPersonaSystemInstruction(persona, speechStyle, approvedCapabilities)
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
        approvedCapabilities: List<UpgradeProposal>
    ): Flow<String> {
        val chatModel = buildModel(
            buildPersonaSystemInstruction(persona, speechStyle, approvedCapabilities)
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

    suspend fun generateSecretaryReply(
        prompt: String,
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode = SpeechStyleMode.Colloquial,
        approvedCapabilities: List<UpgradeProposal> = emptyList()
    ): String {
        val personaModel = buildModel(
            buildPersonaSystemInstruction(persona, speechStyle, approvedCapabilities)
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
        approvedCapabilities: List<UpgradeProposal>
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
            approvedCapabilities = approvedCapabilities
        )
    }
}
