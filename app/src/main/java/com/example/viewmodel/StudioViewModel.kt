package com.example.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiRepository
import com.example.data.CodingAppsCatalog
import com.example.data.GitHubRepository
import com.example.data.LocalStudioStore
import com.example.model.AdvisoryDomain
import com.example.model.AiUiState
import com.example.model.ApprovalStatus
import com.example.model.AssistantPersona
import com.example.model.CallLogNote
import com.example.model.ChatMessage
import com.example.model.CodingAppInfo
import com.example.model.DevHubMode
import com.example.model.GitHubLoadState
import com.example.model.PersistedStudioState
import com.example.model.PersonaCatalog
import com.example.model.SecretaryMode
import com.example.model.SecretaryTask
import com.example.model.SpeechStyleMode
import com.example.model.StudioSection
import com.example.model.TermuxCommand
import com.example.model.TopDestination
import com.example.model.UpgradeProposal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "StudioViewModel"

class StudioViewModel(
    private val aiRepository: AiRepository = AiRepository(),
    private val gitHubRepository: GitHubRepository = GitHubRepository()
) : ViewModel() {

    private var localStore: LocalStudioStore? = null
    private var storeInitialized = false

    val personas: List<AssistantPersona> = PersonaCatalog.all

    private val _selectedPersona = MutableStateFlow(PersonaCatalog.all.first())
    val selectedPersona: StateFlow<AssistantPersona> = _selectedPersona.asStateFlow()

    // Top-level navigation
    private val _currentDestination = MutableStateFlow(TopDestination.Secretary)
    val currentDestination: StateFlow<TopDestination> = _currentDestination.asStateFlow()

    // Creative Studio sub-navigation (Charter vs Modules A-F vs Tech)
    private val _studioSection = MutableStateFlow(StudioSection.Charter)
    val studioSection: StateFlow<StudioSection> = _studioSection.asStateFlow()

    // Secretary mode & state
    private val _secretaryMode = MutableStateFlow(SecretaryMode.CallSecretary)
    val secretaryMode: StateFlow<SecretaryMode> = _secretaryMode.asStateFlow()

    private val _speechStyle = MutableStateFlow(SpeechStyleMode.Colloquial)
    val speechStyle: StateFlow<SpeechStyleMode> = _speechStyle.asStateFlow()

    // Live Chat, Voice-to-Chat (STT), Chat-to-Voice (TTS) & Video Chat state
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = 1L,
                isUser = false,
                text = PersonaCatalog.all.first().sampleGreeting,
                personaName = PersonaCatalog.all.first().persianName
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _chatStreamingText = MutableStateFlow<String?>(null)
    val chatStreamingText: StateFlow<String?> = _chatStreamingText.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _isVideoStageOpen = MutableStateFlow(false)
    val isVideoStageOpen: StateFlow<Boolean> = _isVideoStageOpen.asStateFlow()

    private val _isAutoVoiceEnabled = MutableStateFlow(true)
    val isAutoVoiceEnabled: StateFlow<Boolean> = _isAutoVoiceEnabled.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _sttPartialTranscript = MutableStateFlow("")
    val sttPartialTranscript: StateFlow<String> = _sttPartialTranscript.asStateFlow()

    private val _sttError = MutableStateFlow<String?>(null)
    val sttError: StateFlow<String?> = _sttError.asStateFlow()

    private val _callScenario = MutableStateFlow("در حال کدنویسی / جلسه فنی (Busy Coding)")
    val callScenario: StateFlow<String> = _callScenario.asStateFlow()

    private val _callerNameInput = MutableStateFlow("")
    val callerNameInput: StateFlow<String> = _callerNameInput.asStateFlow()

    private val _callerPhoneInput = MutableStateFlow("")
    val callerPhoneInput: StateFlow<String> = _callerPhoneInput.asStateFlow()

    private val _callNoteInput = MutableStateFlow("")
    val callNoteInput: StateFlow<String> = _callNoteInput.asStateFlow()

    private val _callNotes = MutableStateFlow<List<CallLogNote>>(emptyList())
    val callNotes: StateFlow<List<CallLogNote>> = _callNotes.asStateFlow()

    private val _secretaryAiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val secretaryAiState: StateFlow<AiUiState> = _secretaryAiState.asStateFlow()

    // Multi-domain Advisory & Strategy Hub state
    private val _advisoryDomain = MutableStateFlow(AdvisoryDomain.NutritionDiet)
    val advisoryDomain: StateFlow<AdvisoryDomain> = _advisoryDomain.asStateFlow()

    private val _advisoryProfileInput = MutableStateFlow("")
    val advisoryProfileInput: StateFlow<String> = _advisoryProfileInput.asStateFlow()

    private val _advisoryGoalInput = MutableStateFlow("")
    val advisoryGoalInput: StateFlow<String> = _advisoryGoalInput.asStateFlow()

    private val _advisoryAiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val advisoryAiState: StateFlow<AiUiState> = _advisoryAiState.asStateFlow()

    private val _personalQuestion = MutableStateFlow("")
    val personalQuestion: StateFlow<String> = _personalQuestion.asStateFlow()

    private val _newTaskInput = MutableStateFlow("")
    val newTaskInput: StateFlow<String> = _newTaskInput.asStateFlow()

    private val _tasks = MutableStateFlow(
        listOf(
            SecretaryTask(1L, "بررسی فعالیت‌های جدید GitHub و همگام‌سازی در Termux", "امروز"),
            SecretaryTask(2L, "پاسخ به تماس‌های از دست‌رفته و پیگیری کارها", "امروز")
        )
    )
    val tasks: StateFlow<List<SecretaryTask>> = _tasks.asStateFlow()

    // GitHub & Coding Apps Hub state
    private val _gitHubUsername = MutableStateFlow("siavashstara-art")
    val gitHubUsername: StateFlow<String> = _gitHubUsername.asStateFlow()

    private val _gitHubToken = MutableStateFlow("")
    val gitHubToken: StateFlow<String> = _gitHubToken.asStateFlow()

    private val _gitHubState = MutableStateFlow<GitHubLoadState>(GitHubLoadState.Idle)
    val gitHubState: StateFlow<GitHubLoadState> = _gitHubState.asStateFlow()

    private val _codingApps = MutableStateFlow<List<CodingAppInfo>>(emptyList())
    val codingApps: StateFlow<List<CodingAppInfo>> = _codingApps.asStateFlow()

    val termuxCommands: List<TermuxCommand> = CodingAppsCatalog.defaultTermuxCommands

    // Approval-gated Self-Upgrade & Capability Architect state
    private val _devHubMode = MutableStateFlow(DevHubMode.GitHubAndTermux)
    val devHubMode: StateFlow<DevHubMode> = _devHubMode.asStateFlow()

    private val _upgradeRequestInput = MutableStateFlow("")
    val upgradeRequestInput: StateFlow<String> = _upgradeRequestInput.asStateFlow()

    private val _upgradeProposals = MutableStateFlow<List<UpgradeProposal>>(emptyList())
    val upgradeProposals: StateFlow<List<UpgradeProposal>> = _upgradeProposals.asStateFlow()

    private val _upgradeProposalLoading = MutableStateFlow(false)
    val upgradeProposalLoading: StateFlow<Boolean> = _upgradeProposalLoading.asStateFlow()

    private val _upgradeError = MutableStateFlow<String?>(null)
    val upgradeError: StateFlow<String?> = _upgradeError.asStateFlow()

    private val _capabilityRunInput = MutableStateFlow("")
    val capabilityRunInput: StateFlow<String> = _capabilityRunInput.asStateFlow()

    private val _capabilityRunState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val capabilityRunState: StateFlow<AiUiState> = _capabilityRunState.asStateFlow()

    // Creative Studio interactive parameters (from imported React app)
    private val _charName = MutableStateFlow("Elena Vance")
    val charName: StateFlow<String> = _charName.asStateFlow()

    private val _charAge = MutableStateFlow(28)
    val charAge: StateFlow<Int> = _charAge.asStateFlow()

    private val _charStyle = MutableStateFlow("Cinematic Editorial")
    val charStyle: StateFlow<String> = _charStyle.asStateFlow()

    private val _charCategory = MutableStateFlow("Formal & Evening Wear")
    val charCategory: StateFlow<String> = _charCategory.asStateFlow()

    private val _tryOnModel = MutableStateFlow("Model #04 (Studio Fit)")
    val tryOnModel: StateFlow<String> = _tryOnModel.asStateFlow()

    private val _tryOnClothing = MutableStateFlow("Silk Satin Slip Dress - Emerald Green with Lace Trim")
    val tryOnClothing: StateFlow<String> = _tryOnClothing.asStateFlow()

    private val _bridalGownStyle = MutableStateFlow("A-Line Lace Chantilly Gown")
    val bridalGownStyle: StateFlow<String> = _bridalGownStyle.asStateFlow()

    private val _groomSuitStyle = MutableStateFlow("Tailored Navy Tuxedo with Satin Lapel")
    val groomSuitStyle: StateFlow<String> = _groomSuitStyle.asStateFlow()

    private val _sceneType = MutableStateFlow("Wedding Reception Grand Entrance")
    val sceneType: StateFlow<String> = _sceneType.asStateFlow()

    private val _lightingSetup = MutableStateFlow("Warm Golden Hour & Soft Rim Light")
    val lightingSetup: StateFlow<String> = _lightingSetup.asStateFlow()

    // Gemini AI Studio tab state
    private val _aiModuleSelection = MutableStateFlow("Module A — Fictional Character Studio")
    val aiModuleSelection: StateFlow<String> = _aiModuleSelection.asStateFlow()

    private val _aiPrompt = MutableStateFlow("")
    val aiPrompt: StateFlow<String> = _aiPrompt.asStateFlow()

    private val _studioAiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val studioAiState: StateFlow<AiUiState> = _studioAiState.asStateFlow()

    fun initializeLocalStore(context: Context) {
        if (storeInitialized) return
        storeInitialized = true
        val store = LocalStudioStore(context)
        localStore = store
        viewModelScope.launch {
            val saved = store.load() ?: return@launch
            val personaMatch = personas.firstOrNull { it.id == saved.selectedPersonaId }
            if (personaMatch != null) {
                _selectedPersona.value = personaMatch
                _charName.value = personaMatch.englishName
                _charAge.value = personaMatch.age
            }
            _speechStyle.value = saved.speechStyle
            _isAutoVoiceEnabled.value = saved.isAutoVoiceEnabled
            if (saved.gitHubUsername.isNotBlank()) {
                _gitHubUsername.value = saved.gitHubUsername
            }
            if (saved.tasks.isNotEmpty()) {
                _tasks.value = saved.tasks
            }
            if (saved.callNotes.isNotEmpty()) {
                _callNotes.value = saved.callNotes
            }
            if (saved.chatMessages.isNotEmpty()) {
                _chatMessages.value = saved.chatMessages
            }
            if (saved.upgradeProposals.isNotEmpty()) {
                _upgradeProposals.value = saved.upgradeProposals
            }
        }
    }

    private fun saveCurrentState() {
        val store = localStore ?: return
        val snapshot = PersistedStudioState(
            selectedPersonaId = _selectedPersona.value.id,
            speechStyle = _speechStyle.value,
            isAutoVoiceEnabled = _isAutoVoiceEnabled.value,
            gitHubUsername = _gitHubUsername.value,
            tasks = _tasks.value,
            callNotes = _callNotes.value.take(50),
            chatMessages = _chatMessages.value.takeLast(50),
            upgradeProposals = _upgradeProposals.value.take(30)
        )
        viewModelScope.launch {
            store.save(snapshot)
        }
    }

    fun selectPersona(persona: AssistantPersona) {
        val changed = _selectedPersona.value.id != persona.id
        _selectedPersona.value = persona
        _charName.value = persona.englishName
        _charAge.value = persona.age
        if (changed) {
            _chatMessages.value = _chatMessages.value + ChatMessage(
                id = System.currentTimeMillis(),
                isUser = false,
                text = persona.sampleGreeting,
                personaName = persona.persianName
            )
            saveCurrentState()
        }
    }

    fun selectDestination(dest: TopDestination) {
        _currentDestination.value = dest
    }

    fun selectStudioSection(section: StudioSection) {
        _studioSection.value = section
    }

    fun selectSecretaryMode(mode: SecretaryMode) {
        _secretaryMode.value = mode
    }

    fun updateSpeechStyle(style: SpeechStyleMode) {
        _speechStyle.value = style
        saveCurrentState()
    }

    fun updateChatInput(value: String) {
        _chatInput.value = value
    }

    fun clearChatHistory() {
        val current = _selectedPersona.value
        _chatMessages.value = listOf(
            ChatMessage(
                id = System.currentTimeMillis(),
                isUser = false,
                text = current.sampleGreeting,
                personaName = current.persianName
            )
        )
        _chatError.value = null
        _chatStreamingText.value = null
        saveCurrentState()
    }

    fun setVideoStageOpen(open: Boolean) {
        _isVideoStageOpen.value = open
        if (open) {
            _secretaryMode.value = SecretaryMode.ChatAndVideo
        }
    }

    fun toggleAutoVoice() {
        _isAutoVoiceEnabled.value = !_isAutoVoiceEnabled.value
        saveCurrentState()
    }

    fun onSttListeningChanged(listening: Boolean) {
        _isListening.value = listening
        if (listening) {
            _sttError.value = null
        }
    }

    fun onSttPartialTranscript(partial: String) {
        _sttPartialTranscript.value = partial
    }

    fun onSttRmsLevel(level: Float) {
        _rmsLevel.value = level
    }

    fun onSttError(error: String?) {
        _sttError.value = error
        _sttPartialTranscript.value = ""
    }

    fun sendChatMessage(
        overrideText: String? = null,
        onReplyComplete: ((String, AssistantPersona) -> Unit)? = null
    ) {
        val raw = (overrideText ?: _chatInput.value).trim()
        if (raw.isEmpty() || _chatLoading.value) return

        val persona = _selectedPersona.value
        val previousHistory = _chatMessages.value
        val userMessage = ChatMessage(
            id = System.currentTimeMillis(),
            isUser = true,
            text = raw
        )
        _chatMessages.value = previousHistory + userMessage
        if (overrideText == null) {
            _chatInput.value = ""
        }
        _sttPartialTranscript.value = ""
        _sttError.value = null
        _chatError.value = null
        _chatLoading.value = true
        _chatStreamingText.value = ""

        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    val approved = _upgradeProposals.value.filter { it.status == ApprovalStatus.Approved }
                    aiRepository.streamPersonaChat(
                        history = previousHistory,
                        newMessage = raw,
                        persona = persona,
                        speechStyle = _speechStyle.value,
                        approvedCapabilities = approved
                    ).collect { chunk ->
                        accumulated.append(chunk)
                        _chatStreamingText.value = accumulated.toString()
                    }
                }
                val finalReply = accumulated.toString().trim()
                if (finalReply.isNotEmpty()) {
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        id = System.currentTimeMillis(),
                        isUser = false,
                        text = finalReply,
                        personaName = persona.persianName
                    )
                    saveCurrentState()
                    if (_isAutoVoiceEnabled.value || _isVideoStageOpen.value) {
                        onReplyComplete?.invoke(finalReply, persona)
                    }
                } else {
                    _chatError.value = "پاسخی از مدل دریافت نشد."
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Persona chat timed out", e)
                _chatError.value = "زمان پاسخ‌گویی به پایان رسید. دوباره تلاش کنید."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Persona chat failed", e)
                _chatError.value = e.localizedMessage ?: "خطا در گفتگو با هوش مصنوعی"
            } finally {
                _chatStreamingText.value = null
                _chatLoading.value = false
            }
        }
    }

    fun updateCallScenario(value: String) {
        _callScenario.value = value
    }

    fun updateCallerName(value: String) {
        _callerNameInput.value = value
    }

    fun updateCallerPhone(value: String) {
        _callerPhoneInput.value = value
    }

    fun updateCallNoteInput(value: String) {
        _callNoteInput.value = value
    }

    fun updatePersonalQuestion(value: String) {
        _personalQuestion.value = value
    }

    fun selectAdvisoryDomain(domain: AdvisoryDomain) {
        _advisoryDomain.value = domain
    }

    fun updateAdvisoryProfile(value: String) {
        _advisoryProfileInput.value = value
    }

    fun updateAdvisoryGoal(value: String) {
        _advisoryGoalInput.value = value
    }

    fun applyAdvisoryPreset(profile: String, goal: String) {
        _advisoryProfileInput.value = profile
        _advisoryGoalInput.value = goal
    }

    fun generateAdvisoryPlan(onReplyComplete: ((String, AssistantPersona) -> Unit)? = null) {
        val goal = _advisoryGoalInput.value.trim()
        val profile = _advisoryProfileInput.value.trim()
        if (goal.isEmpty() && profile.isEmpty()) return

        val domain = _advisoryDomain.value
        val persona = _selectedPersona.value
        val style = _speechStyle.value
        val approved = _upgradeProposals.value.filter { it.status == ApprovalStatus.Approved }

        viewModelScope.launch {
            _advisoryAiState.value = AiUiState.Loading
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    aiRepository.streamAdvisoryPlan(
                        domain = domain,
                        recipientProfile = profile,
                        goalDetails = goal.ifEmpty { "یک برنامه کامل و کاربردی با توجه به مشخصات ارائه بده." },
                        persona = persona,
                        speechStyle = style,
                        approvedCapabilities = approved
                    ).collect { chunk ->
                        accumulated.append(chunk)
                        _advisoryAiState.value = AiUiState.Streaming(accumulated.toString())
                    }
                }
                val finalPlan = accumulated.toString().trim()
                if (finalPlan.isNotEmpty()) {
                    _advisoryAiState.value = AiUiState.Success(finalPlan)
                    if (_isAutoVoiceEnabled.value) {
                        onReplyComplete?.invoke(finalPlan, persona)
                    }
                } else {
                    _advisoryAiState.value = AiUiState.Error("برنامه یا مشاوره‌ای دریافت نشد.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Advisory AI timed out", e)
                _advisoryAiState.value = AiUiState.Error("زمان آماده‌سازی برنامه به پایان رسید. دوباره تلاش کنید.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Advisory AI failed", e)
                _advisoryAiState.value = AiUiState.Error(e.localizedMessage ?: "خطا در آماده‌سازی مشاوره")
            }
        }
    }

    fun updateNewTaskInput(value: String) {
        _newTaskInput.value = value
    }

    fun addTask() {
        val title = _newTaskInput.value.trim()
        if (title.isEmpty()) return
        val nextId = (_tasks.value.maxOfOrNull { it.id } ?: 0L) + 1L
        _tasks.value = listOf(SecretaryTask(nextId, title, "جدید")) + _tasks.value
        _newTaskInput.value = ""
        saveCurrentState()
    }

    fun toggleTask(id: Long) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(isDone = !task.isDone) else task
        }
        saveCurrentState()
    }

    fun deleteTask(id: Long) {
        _tasks.value = _tasks.value.filterNot { it.id == id }
        saveCurrentState()
    }

    fun generateCallGreetingScript() {
        val scenario = _callScenario.value
        val prompt = """
            به عنوان منشی تلفنی هوشمند سیاوش علی‌میری، برای وضعیت فعلی «$scenario»:
            ۱. یک متن خوش‌آمدگویی کوتاه و محترمانه برای منشی تلفنی (Voicemail Greeting) به فارسی بنویس.
            ۲. یک متن کوتاه پیامک پاسخ سریع (Missed-call SMS Reply) به فارسی و یک نسخه انگلیسی بنویس.
            متن‌ها واضح، حرفه‌ای و آماده کپی یا ارسال باشند.
        """.trimIndent()
        runSecretaryStream(prompt)
    }

    fun logCallAndDraftReply() {
        val name = _callerNameInput.value.trim().ifEmpty { "مخاطب ناشناس" }
        val phone = _callerPhoneInput.value.trim()
        val note = _callNoteInput.value.trim()
        if (note.isEmpty() && phone.isEmpty()) return

        val entryId = System.currentTimeMillis()
        val initialEntry = CallLogNote(
            id = entryId,
            callerNameOrNumber = name,
            phoneNumber = phone,
            note = note.ifEmpty { "تماس از دست‌رفته" },
            aiReplyDraft = "در حال تهیه پاسخ پیشنهادی با هوش مصنوعی..."
        )
        _callNotes.value = listOf(initialEntry) + _callNotes.value
        _callerNameInput.value = ""
        _callerPhoneInput.value = ""
        _callNoteInput.value = ""

        viewModelScope.launch {
            try {
                val reply = withTimeout(60_000L) {
                    aiRepository.generateSecretaryReply(
                        prompt = """
                        منشی تلفنی: کاربر «$name» (شماره: ${phone.ifEmpty { "نامشخص" }}) با این یادداشت تماس گرفته است: «${initialEntry.note}».
                        یک پاسخ پیامکی کوتاه و صمیمی به فارسی و یک مورد پیگیری (Action Item) پیشنهاد بده.
                        """.trimIndent(),
                        persona = _selectedPersona.value,
                        speechStyle = _speechStyle.value,
                        approvedCapabilities = _upgradeProposals.value.filter { it.status == ApprovalStatus.Approved }
                    )
                }
                _callNotes.value = _callNotes.value.map { item ->
                    if (item.id == entryId) item.copy(aiReplyDraft = reply.ifEmpty { "پاسخ آماده نشد." }) else item
                }
                saveCurrentState()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to draft call reply", e)
                _callNotes.value = _callNotes.value.map { item ->
                    if (item.id == entryId) {
                        item.copy(aiReplyDraft = "خطا در ارتباط با Gemini: ${e.localizedMessage ?: "نامشخص"}")
                    } else item
                }
                saveCurrentState()
            }
        }
    }

    fun askPersonalSecretary() {
        val question = _personalQuestion.value.trim()
        if (question.isEmpty()) return
        runSecretaryStream(question)
    }

    private fun runSecretaryStream(prompt: String) {
        viewModelScope.launch {
            _secretaryAiState.value = AiUiState.Loading
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    val approved = _upgradeProposals.value.filter { it.status == ApprovalStatus.Approved }
                    aiRepository.streamSecretaryResponse(
                        prompt = prompt,
                        persona = _selectedPersona.value,
                        speechStyle = _speechStyle.value,
                        approvedCapabilities = approved
                    ).collect { chunk ->
                        accumulated.append(chunk)
                        _secretaryAiState.value = AiUiState.Streaming(accumulated.toString())
                    }
                }
                if (accumulated.isNotBlank()) {
                    _secretaryAiState.value = AiUiState.Success(accumulated.toString())
                } else {
                    _secretaryAiState.value = AiUiState.Error("پاسخی از مدل دریافت نشد.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Secretary AI timed out", e)
                _secretaryAiState.value = AiUiState.Error("زمان پاسخ‌گویی به پایان رسید. لطفاً دوباره تلاش کنید.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Secretary AI failed", e)
                _secretaryAiState.value = AiUiState.Error(e.localizedMessage ?: "خطا در اجرای هوش مصنوعی")
            }
        }
    }

    // GitHub & Coding Apps actions
    fun updateGitHubUsername(value: String) {
        _gitHubUsername.value = value
        saveCurrentState()
    }

    fun updateGitHubToken(value: String) {
        _gitHubToken.value = value
    }

    fun refreshCodingApps(context: Context) {
        _codingApps.value = CodingAppsCatalog.detectInstalledApps(context)
    }

    fun loadGitHubActivity() {
        val username = _gitHubUsername.value.trim()
        if (username.isEmpty()) {
            _gitHubState.value = GitHubLoadState.Error("نام کاربری گیت‌هاب را وارد کنید.")
            return
        }
        viewModelScope.launch {
            _gitHubState.value = GitHubLoadState.Loading
            try {
                val snapshot = gitHubRepository.fetchUserActivity(username, _gitHubToken.value)
                _gitHubState.value = GitHubLoadState.Success(
                    user = snapshot.user,
                    repos = snapshot.repos,
                    events = snapshot.events
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "GitHub fetch failed", e)
                _gitHubState.value = GitHubLoadState.Error(e.localizedMessage ?: "خطا در دریافت اطلاعات گیت‌هاب")
            }
        }
    }

    // Approval-Gated Self-Upgrade & Capability Architect actions
    fun selectDevHubMode(mode: DevHubMode) {
        _devHubMode.value = mode
    }

    fun updateUpgradeRequestInput(value: String) {
        _upgradeRequestInput.value = value
    }

    fun updateCapabilityRunInput(value: String) {
        _capabilityRunInput.value = value
    }

    fun requestUpgradeProposal() {
        val request = _upgradeRequestInput.value.trim()
        if (request.isEmpty()) return
        viewModelScope.launch {
            _upgradeProposalLoading.value = true
            _upgradeError.value = null
            try {
                val proposal = withTimeout(60_000L) {
                    aiRepository.createUpgradeProposal(request)
                }
                _upgradeProposals.value = listOf(proposal) + _upgradeProposals.value
                _upgradeRequestInput.value = ""
                saveCurrentState()
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Upgrade proposal timed out", e)
                _upgradeError.value = "زمان طراحی پیشنهاد ارتقا به پایان رسید."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Upgrade proposal failed", e)
                _upgradeError.value = e.localizedMessage ?: "خطا در طراحی پیشنهاد ارتقا"
            } finally {
                _upgradeProposalLoading.value = false
            }
        }
    }

    fun approveUpgradeProposal(id: Long) {
        _upgradeProposals.value = _upgradeProposals.value.map { proposal ->
            if (proposal.id == id) proposal.copy(status = ApprovalStatus.Approved) else proposal
        }
        saveCurrentState()
    }

    fun rejectUpgradeProposal(id: Long) {
        _upgradeProposals.value = _upgradeProposals.value.map { proposal ->
            if (proposal.id == id) proposal.copy(status = ApprovalStatus.Rejected) else proposal
        }
        saveCurrentState()
    }

    fun runApprovedCapability(proposal: UpgradeProposal) {
        if (proposal.status != ApprovalStatus.Approved) return
        val input = _capabilityRunInput.value.trim()
        viewModelScope.launch {
            _capabilityRunState.value = AiUiState.Loading
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    aiRepository.executeApprovedCapability(
                        capability = proposal,
                        userInput = input,
                        persona = _selectedPersona.value,
                        speechStyle = _speechStyle.value
                    ).collect { chunk ->
                            accumulated.append(chunk)
                            _capabilityRunState.value = AiUiState.Streaming(accumulated.toString())
                        }
                }
                if (accumulated.isNotBlank()) {
                    _capabilityRunState.value = AiUiState.Success(accumulated.toString())
                } else {
                    _capabilityRunState.value = AiUiState.Error("خروجی از قابلیت تاییدشده دریافت نشد.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Capability run timed out", e)
                _capabilityRunState.value = AiUiState.Error("زمان اجرای قابلیت به پایان رسید.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Capability run failed", e)
                _capabilityRunState.value = AiUiState.Error(e.localizedMessage ?: "خطا در اجرای قابلیت")
            }
        }
    }

    // Studio state setters
    fun updateCharName(value: String) { _charName.value = value }
    fun updateCharAge(value: Int) { _charAge.value = value.coerceIn(18, 99) }
    fun updateCharStyle(value: String) { _charStyle.value = value }
    fun updateCharCategory(value: String) { _charCategory.value = value }
    fun updateTryOnModel(value: String) { _tryOnModel.value = value }
    fun updateTryOnClothing(value: String) { _tryOnClothing.value = value }
    fun updateBridalGownStyle(value: String) { _bridalGownStyle.value = value }
    fun updateGroomSuitStyle(value: String) { _groomSuitStyle.value = value }
    fun updateSceneType(value: String) { _sceneType.value = value }
    fun updateLightingSetup(value: String) { _lightingSetup.value = value }

    fun updateAiModuleSelection(value: String) { _aiModuleSelection.value = value }
    fun updateAiPrompt(value: String) { _aiPrompt.value = value }

    fun sendPromptToGeminiStudio(moduleName: String, prompt: String) {
        _aiModuleSelection.value = moduleName
        _aiPrompt.value = prompt
        _currentDestination.value = TopDestination.GeminiAi
    }

    fun generateStudioAiConcept() {
        val prompt = _aiPrompt.value.trim()
        if (prompt.isEmpty()) return
        val moduleName = _aiModuleSelection.value
        viewModelScope.launch {
            _studioAiState.value = AiUiState.Loading
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    aiRepository.streamStudioConcept(prompt, moduleName).collect { chunk ->
                        accumulated.append(chunk)
                        _studioAiState.value = AiUiState.Streaming(accumulated.toString())
                    }
                }
                if (accumulated.isNotBlank()) {
                    _studioAiState.value = AiUiState.Success(accumulated.toString())
                } else {
                    _studioAiState.value = AiUiState.Error("Model returned an empty response.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Studio AI timed out", e)
                _studioAiState.value = AiUiState.Error("Generation timed out after 60 seconds. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Studio AI failed", e)
                _studioAiState.value = AiUiState.Error(e.localizedMessage ?: "AI generation failed.")
            }
        }
    }
}
