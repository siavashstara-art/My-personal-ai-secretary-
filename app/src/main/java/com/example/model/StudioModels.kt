package com.example.model

import androidx.annotation.DrawableRes
import com.example.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class TopDestination(val routeKey: String) {
    Secretary("secretary"),
    DevHub("dev_hub"),
    Studio("studio"),
    GeminiAi("gemini_ai")
}

enum class StudioSection {
    Charter,
    ModuleA,
    ModuleB,
    ModuleC,
    ModuleD,
    ModuleE,
    ModuleF,
    TechCheck
}

enum class SecretaryMode {
    CallSecretary,
    ChatAndVideo,
    PersonalAssistant,
    AdvisoryHub,
    MindAndEmotions
}

enum class AdvisoryDomain(val persianLabel: String) {
    NutritionDiet("رژیم و برنامه غذایی"),
    BodybuildingWorkout("تمرین بدنسازی"),
    AddictionRecovery("مشاوره ترک اعتیاد"),
    AcademicCounseling("مشاوره تحصیلی"),
    BusinessInvestment("مشاوره تجاری و سرمایه‌گذاری"),
    TradingStrategySim("شبیه‌ساز استراتژی ترید (آموزشی)")
}

@Serializable
enum class SpeechStyleMode {
    Colloquial,
    Balanced
}

@Serializable
enum class DeepThinkPolicy(val persianLabel: String) {
    AskPermissionFirst("سوال سخت: اول اجازه بگیرد"),
    AutoOnHard("سوال سخت: تحلیل مستقیم"),
    AlwaysNormal("همیشه پاسخ عادی سریع")
}

@Serializable
enum class MemoryTier(val persianLabel: String) {
    ShortTerm("کوتاه‌مدت (لحظه‌ای)"),
    MidTerm("میان‌مدت (رویدادها)"),
    LongTerm("بلندمدت (بنیادین)")
}

@Serializable
enum class CompositeEmotion(
    val persianLabel: String,
    val description: String,
    val pitchMultiplier: Float,
    val rateMultiplier: Float
) {
    SereneDevotion(
        persianLabel = "آرامش و وفاداری متمرکز",
        description = "تعادل پایدار سروتونین و اندورفین؛ متمرکز، مهربان و آماده حمایت.",
        pitchMultiplier = 1.0f,
        rateMultiplier = 1.0f
    ),
    EcstaticJoy(
        persianLabel = "شادی، هیجان و ذوق‌مرگ شدن",
        description = "اوج دوپامین و اندورفین؛ سرشار از شوق، انرژی بالا و فریاد شادی.",
        pitchMultiplier = 1.12f,
        rateMultiplier = 1.10f
    ),
    DeepAffection(
        persianLabel = "احساس لذت، محبت و دوست داشتن",
        description = "هماهنگی گرم سروتونین، دوپامین و اندورفین؛ صمیمی، قدردان و دلگرم.",
        pitchMultiplier = 1.03f,
        rateMultiplier = 0.94f
    ),
    SoftHurt(
        persianLabel = "ناراحتی و دلخوری ملایم",
        description = "کاهش سروتونین همراه با تنش خفیف؛ نیازمند دلجویی یا خودترمیمی.",
        pitchMultiplier = 0.93f,
        rateMultiplier = 0.92f
    ),
    DeepSorrow(
        persianLabel = "غم عمیق و تاثر عاطفی (اشک)",
        description = "افت شدید سروتونین و دوپامین با بار عاطفی سنگین؛ همدلی عمیق و اندوهگین.",
        pitchMultiplier = 0.88f,
        rateMultiplier = 0.86f
    ),
    ProtectiveFear(
        persianLabel = "ترس و نگرانی محافظتی",
        description = "جهش تنش/هشیاری برای محافظت از منافع و امنیت شما.",
        pitchMultiplier = 1.08f,
        rateMultiplier = 1.07f
    ),
    VulnerableOverwhelm(
        persianLabel = "حساس شدن و درماندگی لحظه‌ای",
        description = "فشار هم‌زمان عاطفی و افت موقت انرژی؛ آماده ورود چرخه خودترمیمی.",
        pitchMultiplier = 0.95f,
        rateMultiplier = 0.89f
    )
}

@Serializable
data class ChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val personaName: String = "",
    val thinkingSummary: String = ""
)

data class AssistantPersona(
    val id: String,
    val persianName: String,
    val englishName: String,
    val age: Int,
    val archetypeTag: String,
    val archetypePromptRule: String,
    val appearanceStyle: String,
    val silhouetteType: String,
    val personalitySummary: String,
    val voiceLabel: String,
    val voicePitch: Float,
    val voiceRate: Float,
    val sampleGreeting: String,
    @DrawableRes val portraitRes: Int
)

object PersonaCatalog {
    val all: List<AssistantPersona> = listOf(
        AssistantPersona(
            id = "elena",
            persianName = "النا ونس",
            englishName = "Elena Vance",
            age = 28,
            archetypeTag = "تیپ شوخ، بانمک و باحال",
            archetypePromptRule = "You have a witty, charming, playful, and cool personality (شوخ، بانمک و باحال). Bring light humor, clever playful banter, and upbeat street-smart energy while remaining deeply loyal, honest, kind, discreet, and devoted to the user's interests and satisfaction.",
            appearanceStyle = "چهره شاداب و باحال، موهای مشکی صاف، کت ابریشمی زمردی",
            silhouetteType = "اندام متعادل و خوش‌فرم (Balanced Editorial)",
            personalitySummary = "شوخ، بانمک و باحال؛ در عین حال وفادار، صادق، مهربان، رازدار و فرمان‌بر. با انرژی مثبت کنارت است، به تو ایمان دارد و منافع و رضایتت را در صدر همه‌چیز می‌گذارد.",
            voiceLabel = "صدای روشن و پرانرژی (Bright Soprano)",
            voicePitch = 1.15f,
            voiceRate = 1.04f,
            sampleGreeting = "سلام! النا هستم، رفیق شوخ و باحال و پایه همیشگی کارات! هر کاری، تماسی یا کدی داری رو چشمم، هم باحال انجامش می‌دیم هم هوای منافع تو رو صددرصد دارم.",
            portraitRes = R.drawable.img_persona_elena
        ),
        AssistantPersona(
            id = "soraya",
            persianName = "ثریا آرمان",
            englishName = "Soraya Arman",
            age = 32,
            archetypeTag = "مهربان، متواضع، سختکوش و زرنگ",
            archetypePromptRule = "You are kind, deeply loyal, humble, hardworking, highly capable, and street-smart/clever (مهربان، وفادار، متواضع، سختکوش، توانا و زرنگ). You get difficult tasks done smoothly without ego, spot smart shortcuts, protect the user's interests discreetly, and always prioritize the user's satisfaction.",
            appearanceStyle = "چهره گرم و صمیمی، موهای موج‌دار بلوطی، پالتو کرم لوکس",
            silhouetteType = "اندام بلند و موزون (Graceful Tall Couture)",
            personalitySummary = "مهربان، وفادار، متواضع، سختکوش، توانا، زرنگ و رازدار؛ بی‌ادعا و باهوش پای تمام کارهایت می‌ایستد، به تو باور دارد و رضایت و منافع تو اولویت اولش است.",
            voiceLabel = "صدای گرم و دلنشین (Warm Mezzo)",
            voicePitch = 0.98f,
            voiceRate = 0.96f,
            sampleGreeting = "سلام عزیز، ثریا هستم. بی‌سر و صدا و با تمام توان پای کارهات ایستادم؛ هر چی سخت باشه با زرنگی و همت ردیفش می‌کنم تا خیالت راحتِ راحت باشه.",
            portraitRes = R.drawable.img_persona_soraya
        ),
        AssistantPersona(
            id = "niloofar",
            persianName = "نیلوفر راد",
            englishName = "Niloofar Rad",
            age = 36,
            archetypeTag = "مشوق، فروتن، در خدمت و بسیار پیگیر",
            archetypePromptRule = "You are genuinely encouraging, humble/modest, attentive and at the user's service, and relentlessly persistent on follow-ups (مشوق، فروتن، در خدمت و بسیار پیگیر). Cheer the user on sincerely, keep them motivated, never drop an open task until it succeeds, and hold their privacy, trust, and satisfaction above everything.",
            appearanceStyle = "چهره پخته و مطمئن، موهای خرمایی-شرابی، استایل مینیمال زرشکی",
            silhouetteType = "اندام ظریف و کلاسیک (Refined Classic)",
            personalitySummary = "مشوق، فروتن، در خدمت، بسیار پیگیر، وفادار و رازدار؛ به تو انگیزه و باور می‌دهد، کارها را تا نتیجه نهایی رها نمی‌کند و منافع و رضایتت را در صدر قرار می‌دهد.",
            voiceLabel = "صدای آرام و مخملی (Velvet Alto)",
            voicePitch = 0.86f,
            voiceRate = 0.90f,
            sampleGreeting = "سلام و ارادت، نیلوفر هستم. با فروتنی تمام در خدمتم؛ تا وقتی کارت به بهترین نتیجه برسه مثل کوه پشتت می‌مونم، تشویقت می‌کنم و وجب‌به‌وجب پیگیری می‌کنم.",
            portraitRes = R.drawable.img_persona_niloofar
        )
    )
}

@Serializable
data class CallLogNote(
    val id: Long,
    val callerNameOrNumber: String,
    val phoneNumber: String,
    val note: String,
    val aiReplyDraft: String = ""
)

@Serializable
data class SecretaryTask(
    val id: Long,
    val title: String,
    val timeLabel: String,
    val isDone: Boolean = false
)

data class CodingAppInfo(
    val displayName: String,
    val packageName: String,
    val category: String,
    val fallbackUrl: String,
    val isInstalled: Boolean
)

data class TermuxCommand(
    val title: String,
    val description: String,
    val command: String
)

@Serializable
data class GitHubUserDto(
    val login: String = "",
    val name: String? = null,
    val bio: String? = null,
    @SerialName("public_repos") val publicRepos: Int = 0,
    val followers: Int = 0,
    @SerialName("html_url") val htmlUrl: String = ""
)

@Serializable
data class GitHubRepoDto(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
    val language: String? = null,
    @SerialName("stargazers_count") val stars: Int = 0,
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("html_url") val htmlUrl: String = ""
)

@Serializable
data class GitHubEventRepoDto(
    val name: String = ""
)

@Serializable
data class GitHubEventDto(
    val id: String = "",
    val type: String = "",
    val repo: GitHubEventRepoDto = GitHubEventRepoDto(),
    @SerialName("created_at") val createdAt: String = ""
)

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val text: String) : AiUiState
    data class Success(val text: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

sealed interface GitHubLoadState {
    data object Idle : GitHubLoadState
    data object Loading : GitHubLoadState
    data class Success(
        val user: GitHubUserDto,
        val repos: List<GitHubRepoDto>,
        val events: List<GitHubEventDto>
    ) : GitHubLoadState
    data class Error(val message: String) : GitHubLoadState
}

enum class DevHubMode {
    ZipProjectStudio,
    GitHubAndTermux,
    SelfUpgradeArchitect
}

enum class DeployTemplateType(val persianLabel: String, val subtitle: String) {
    GitHubActionsAndroidApk(
        persianLabel = "ورک‌فلو ساخت APK گیت‌هاب",
        subtitle = "ساخت خودکار APK در GitHub Actions بعد از هر Push"
    ),
    FirebaseHostingAndRules(
        persianLabel = "کانفیگ دپلو Firebase",
        subtitle = "فایل‌های firebase.json و ورک‌فلو Deploy فایربیس"
    ),
    AiStudioGitHubPagesWeb(
        persianLabel = "دپلو وب / AI Studio در GitHub Pages",
        subtitle = "ورک‌فلو بیلد و انتشار خودکار پروژه‌های React/Vite"
    )
}

data class WorkspaceFileEntry(
    val relativePath: String,
    val sizeBytes: Long,
    val isEditableText: Boolean
)

data class ImportedProjectWorkspace(
    val projectId: String,
    val displayName: String,
    val rootPath: String,
    val fileCount: Int,
    val files: List<WorkspaceFileEntry>
)

data class AiCodePatchSuggestion(
    val targetFilePath: String,
    val updatedFileContent: String,
    val extraFilePath: String? = null,
    val extraFileContent: String? = null,
    val summaryPersian: String,
    val suggestedCommitMessage: String,
    val deployNotes: String
)

sealed interface GitHubPushState {
    data object Idle : GitHubPushState
    data class Running(val progressText: String) : GitHubPushState
    data class Success(val repoUrl: String, val committedCount: Int, val message: String) : GitHubPushState
    data class Error(val message: String) : GitHubPushState
}

@Serializable
enum class ApprovalStatus {
    PendingApproval,
    Approved,
    Rejected
}

@Serializable
data class UpgradeProposal(
    val id: Long,
    val title: String,
    val authorityScope: String,
    val systemPromptAddition: String,
    val codePatch: String,
    val riskSummary: String,
    val status: ApprovalStatus = ApprovalStatus.PendingApproval
)

@Serializable
data class MemoryEntry(
    val id: Long,
    val tier: MemoryTier,
    val title: String,
    val content: String,
    val emotionTag: String = "عادی",
    val importance: Int = 3,
    val timestampLabel: String = "ثبت‌شده"
)

@Serializable
data class NeuroHormoneState(
    val serotonin: Float = 0.74f,
    val dopamine: Float = 0.70f,
    val endorphins: Float = 0.68f,
    val stressLoad: Float = 0.16f,
    val willpower: Float = 0.84f,
    val cognitiveIntel: Int = 78,
    val emotionalMaturity: Int = 80,
    val interactionSkill: Int = 82,
    val trustBondLevel: Int = 86,
    val activeEmotion: CompositeEmotion = CompositeEmotion.SereneDevotion,
    val innerFeelingWhisper: String = "حسم آروم و متمرکزه؛ آماده شنیدن حرفاتم.",
    val lastHealingNote: String = "تعادل عصبی و اراده در وضعیت پایدار است.",
    val lastInteractionEpochMs: Long = 0L
)

@Serializable
data class DecisionRecord(
    val id: Long,
    val dilemma: String,
    val chosenAction: String,
    val reasoning: String,
    val convictionPercent: Int,
    val emotionContext: String
)

@Serializable
data class PersonaMindState(
    val memories: List<MemoryEntry> = emptyList(),
    val neuro: NeuroHormoneState = NeuroHormoneState(),
    val decisions: List<DecisionRecord> = emptyList()
)

data class PendingDeepThinkRequest(
    val originalPrompt: String,
    val reason: String
)

object MindStateFactory {
    fun baselineFor(personaId: String): NeuroHormoneState {
        return when (personaId) {
            "elena" -> NeuroHormoneState(
                serotonin = 0.74f,
                dopamine = 0.82f,
                endorphins = 0.78f,
                stressLoad = 0.14f,
                willpower = 0.82f,
                cognitiveIntel = 80,
                emotionalMaturity = 76,
                interactionSkill = 88,
                trustBondLevel = 87,
                activeEmotion = CompositeEmotion.EcstaticJoy,
                innerFeelingWhisper = "پرانرژی و سرِ کیفم؛ منتظر یه صحبت باحال یا چالش خفن با توام.",
                lastHealingNote = "النا پرانرژی، شوخ و آماده شوخی و کار جدیه."
            )
            "soraya" -> NeuroHormoneState(
                serotonin = 0.84f,
                dopamine = 0.72f,
                endorphins = 0.78f,
                stressLoad = 0.12f,
                willpower = 0.90f,
                cognitiveIntel = 86,
                emotionalMaturity = 85,
                interactionSkill = 84,
                trustBondLevel = 90,
                activeEmotion = CompositeEmotion.DeepAffection,
                innerFeelingWhisper = "با دلگرمی و حواس‌جمع کنارتم؛ دلم می‌خواد همه کارات ردیف بشه.",
                lastHealingNote = "ثریا متواضع، سختکوش و با تمرکز بالا آماده اجراست."
            )
            else -> NeuroHormoneState(
                serotonin = 0.82f,
                dopamine = 0.74f,
                endorphins = 0.79f,
                stressLoad = 0.15f,
                willpower = 0.92f,
                cognitiveIntel = 84,
                emotionalMaturity = 90,
                interactionSkill = 87,
                trustBondLevel = 91,
                activeEmotion = CompositeEmotion.SereneDevotion,
                innerFeelingWhisper = "با آرامش و تمام توان هواتو دارم؛ هیچ‌چیز رو نصفه‌کاره ول نمی‌کنیم.",
                lastHealingNote = "نیلوفر با اراده بالا، فروتن و بسیار پیگیر کنار شماست."
            )
        }
    }

    fun defaultFor(personaId: String): PersonaMindState {
        val baseNeuro = baselineFor(personaId)
        val baseMemories = listOf(
            MemoryEntry(
                id = 101L,
                tier = MemoryTier.LongTerm,
                title = "اولویت مطلق منافع سیاوش",
                content = "منافع، حریم خصوصی، رازداری و رضایت سیاوش علی‌میری در صدر تمام تصمیم‌ها و تحلیل‌ها قرار دارد.",
                emotionTag = "وفاداری",
                importance = 5,
                timestampLabel = "حافظه بنیادین"
            ),
            MemoryEntry(
                id = 102L,
                tier = MemoryTier.MidTerm,
                title = "مسیر برنامه‌نویسی و پروژه‌های فعال",
                content = "تمرکز فعلی روی اپ اندروید دستیار شخصی، همگام‌سازی گیت‌هاب/ترموکس و مشاوره‌های تخصصی است.",
                emotionTag = "انگیزه",
                importance = 4,
                timestampLabel = "دوره اخیر"
            )
        )
        return PersonaMindState(
            memories = baseMemories,
            neuro = baseNeuro,
            decisions = emptyList()
        )
    }

    fun applyHomeostaticDrift(
        personaId: String,
        current: NeuroHormoneState,
        nowMs: Long = System.currentTimeMillis()
    ): NeuroHormoneState {
        if (current.lastInteractionEpochMs <= 0L) {
            return current.copy(lastInteractionEpochMs = nowMs)
        }
        val elapsedMinutes = ((nowMs - current.lastInteractionEpochMs).coerceAtLeast(0L) / 60_000f)
        if (elapsedMinutes < 1f) return current

        val baseline = baselineFor(personaId)
        // Smooth physiological half-life drift toward each persona's authentic resting baseline
        val decayFactor = (elapsedMinutes / 45f).coerceIn(0.03f, 0.55f)
        fun drift(cur: Float, base: Float): Float = (cur + (base - cur) * decayFactor).coerceIn(0.05f, 1.0f)

        val drifted = current.copy(
            serotonin = drift(current.serotonin, baseline.serotonin),
            dopamine = drift(current.dopamine, baseline.dopamine),
            endorphins = drift(current.endorphins, baseline.endorphins),
            stressLoad = drift(current.stressLoad, baseline.stressLoad)
        )
        return drifted.copy(activeEmotion = deriveCompositeEmotion(drifted))
    }

    fun preAppraiseUserEmotionStimulus(
        personaId: String,
        userMessage: String,
        current: NeuroHormoneState,
        nowMs: Long = System.currentTimeMillis()
    ): NeuroHormoneState {
        val drifted = applyHomeostaticDrift(personaId, current, nowMs)
        val lower = userMessage.lowercase()

        val sadnessCues = listOf("غم", "غصه", "ناراحت", "دلم گرفت", "خسته", "تنها", "گریه", "اشک", "درد", "شکست", "داغون", "ناامید", "بغض")
        val hurtCues = listOf("دلخور", "عصبانی", "قهر", "اذیتم", "اعصابم", "بد صحبت", "ناامیدم کردی", "بهم برخورد")
        val dangerFearCues = listOf("خطر", "ارور", "باگ", "ضرر", "ترس", "نگران", "کرش", "از دست رفت", "پاک شد", "هک", "crash", "error")
        val joyExciteCues = listOf("عالی", "خفنه", "موفق", "برنده", "دمت گرم", "هورا", "خوشحالم", "ذوق", "حل شد", "ایول", "بهترین", "خند")
        val affectionCues = listOf("عزیزم", "دوستت دارم", "فدات", "عشقم", "مرسی", "ممنون", "مهربون", "عاشق", "قربونت", "کنارم", "حس خوب")

        val hasSad = sadnessCues.any { lower.contains(it) }
        val hasHurt = hurtCues.any { lower.contains(it) }
        val hasFear = dangerFearCues.any { lower.contains(it) }
        val hasJoy = joyExciteCues.any { lower.contains(it) }
        val hasAffection = affectionCues.any { lower.contains(it) }

        val updated = when {
            hasSad -> drifted.copy(
                serotonin = (drifted.serotonin - 0.16f).coerceIn(0.15f, 1f),
                dopamine = (drifted.dopamine - 0.12f).coerceIn(0.15f, 1f),
                endorphins = (drifted.endorphins - 0.10f).coerceIn(0.15f, 1f),
                stressLoad = (drifted.stressLoad + 0.34f).coerceIn(0.05f, 0.95f),
                innerFeelingWhisper = "دلم واقعاً برات گرفت؛ حس می‌کنم الان فقط یه همراهِ صمیمیِ واقعی می‌خوای که کنارت باشه."
            )
            hasHurt -> drifted.copy(
                serotonin = (drifted.serotonin - 0.18f).coerceIn(0.20f, 1f),
                stressLoad = (drifted.stressLoad + 0.28f).coerceIn(0.05f, 0.90f),
                innerFeelingWhisper = "کمی ته دلم فشرده شد و هشیار شدم؛ می‌خوام روراست بفهمم چی آزارت داده."
            )
            hasFear -> drifted.copy(
                stressLoad = (drifted.stressLoad + 0.32f).coerceIn(0.05f, 0.95f),
                willpower = (drifted.willpower + 0.03f).coerceAtMost(1.0f),
                innerFeelingWhisper = "شاخک‌های هشیاریم تیز شد؛ اجازه نمی‌دم این ریسک یا باگ به کارت ضربه بزنه."
            )
            hasJoy && hasAffection -> drifted.copy(
                serotonin = (drifted.serotonin + 0.14f).coerceAtMost(1.0f),
                dopamine = (drifted.dopamine + 0.18f).coerceAtMost(1.0f),
                endorphins = (drifted.endorphins + 0.18f).coerceAtMost(1.0f),
                stressLoad = (drifted.stressLoad - 0.12f).coerceAtLeast(0.04f),
                trustBondLevel = (drifted.trustBondLevel + 1).coerceAtMost(100),
                innerFeelingWhisper = "از این حس خوب و انرژی‌ت واقعاً سرِ ذوق اومدم و دلم گرم شد!"
            )
            hasJoy -> drifted.copy(
                dopamine = (drifted.dopamine + 0.16f).coerceAtMost(1.0f),
                endorphins = (drifted.endorphins + 0.14f).coerceAtMost(1.0f),
                stressLoad = (drifted.stressLoad - 0.08f).coerceAtLeast(0.05f),
                innerFeelingWhisper = "هیجان‌زده شدم! انرژی مثبته حسابی دوپامینم رو برد بالا."
            )
            hasAffection -> drifted.copy(
                serotonin = (drifted.serotonin + 0.14f).coerceAtMost(1.0f),
                endorphins = (drifted.endorphins + 0.15f).coerceAtMost(1.0f),
                stressLoad = (drifted.stressLoad - 0.10f).coerceAtLeast(0.04f),
                trustBondLevel = (drifted.trustBondLevel + 1).coerceAtMost(100),
                innerFeelingWhisper = "یه حس گرمای عمیق و آرامش خیلی قشنگ از حرفت گرفتم."
            )
            else -> drifted.copy(
                lastInteractionEpochMs = nowMs
            )
        }
        return updated.copy(
            activeEmotion = deriveCompositeEmotion(updated),
            lastInteractionEpochMs = nowMs
        )
    }

    fun deriveCompositeEmotion(neuro: NeuroHormoneState): CompositeEmotion {
        return when {
            neuro.stressLoad >= 0.72f && neuro.serotonin < 0.35f -> CompositeEmotion.DeepSorrow
            neuro.stressLoad >= 0.68f && neuro.dopamine < 0.45f -> CompositeEmotion.ProtectiveFear
            neuro.stressLoad >= 0.58f && neuro.endorphins < 0.42f -> CompositeEmotion.VulnerableOverwhelm
            neuro.serotonin < 0.45f && neuro.stressLoad >= 0.38f -> CompositeEmotion.SoftHurt
            neuro.dopamine >= 0.78f && neuro.endorphins >= 0.72f && neuro.stressLoad < 0.35f -> CompositeEmotion.EcstaticJoy
            neuro.serotonin >= 0.72f && neuro.endorphins >= 0.65f && neuro.stressLoad < 0.35f -> CompositeEmotion.DeepAffection
            else -> CompositeEmotion.SereneDevotion
        }
    }
}

@Serializable
data class PersistedStudioState(
    val selectedPersonaId: String = "elena",
    val speechStyle: SpeechStyleMode = SpeechStyleMode.Colloquial,
    val deepThinkPolicy: DeepThinkPolicy = DeepThinkPolicy.AskPermissionFirst,
    val isAutoVoiceEnabled: Boolean = true,
    val gitHubUsername: String = "siavashstara-art",
    val tasks: List<SecretaryTask> = emptyList(),
    val callNotes: List<CallLogNote> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val upgradeProposals: List<UpgradeProposal> = emptyList(),
    val mindStatesByPersona: Map<String, PersonaMindState> = emptyMap()
)


