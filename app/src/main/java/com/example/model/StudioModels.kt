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
    AdvisoryHub
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
data class ChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val personaName: String = ""
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
    GitHubAndTermux,
    SelfUpgradeArchitect
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
data class PersistedStudioState(
    val selectedPersonaId: String = "elena",
    val speechStyle: SpeechStyleMode = SpeechStyleMode.Colloquial,
    val isAutoVoiceEnabled: Boolean = true,
    val gitHubUsername: String = "siavashstara-art",
    val tasks: List<SecretaryTask> = emptyList(),
    val callNotes: List<CallLogNote> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val upgradeProposals: List<UpgradeProposal> = emptyList()
)


