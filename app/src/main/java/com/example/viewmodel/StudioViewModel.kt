package com.example.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
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
import com.example.model.CompositeEmotion
import com.example.model.DecisionRecord
import com.example.model.DeepThinkPolicy
import com.example.model.DevHubMode
import com.example.model.GitHubLoadState
import com.example.model.MemoryEntry
import com.example.model.MemoryTier
import com.example.model.MindStateFactory
import com.example.model.PendingDeepThinkRequest
import com.example.model.PersonaMindState
import com.example.model.PersistedStudioState
import com.example.model.PersonaCatalog
import com.example.model.SecretaryMode
import com.example.model.SecretaryTask
import com.example.model.SpeechStyleMode
import com.example.model.StudioSection
import com.example.model.TermuxCommand
import android.net.Uri
import com.example.data.ProjectWorkspaceManager
import com.example.model.AiCodePatchSuggestion
import com.example.model.DeployTemplateType
import com.example.model.GitHubPushState
import com.example.model.ImportedProjectWorkspace
import com.example.model.TopDestination
import com.example.model.UpgradeProposal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "StudioViewModel"

class StudioViewModel(
    private val aiRepository: AiRepository = AiRepository(),
    private val gitHubRepository: GitHubRepository = GitHubRepository()
) : ViewModel() {

    private var localStore: LocalStudioStore? = null
    private var workspaceManager: ProjectWorkspaceManager? = null
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

    private val _deepThinkPolicy = MutableStateFlow(DeepThinkPolicy.AskPermissionFirst)
    val deepThinkPolicy: StateFlow<DeepThinkPolicy> = _deepThinkPolicy.asStateFlow()

    // Per-persona Tri-Tier Memory, Neuro-Hormones, Composite Emotions & Willpower state
    private val _mindStatesByPersona = MutableStateFlow(
        PersonaCatalog.all.associate { it.id to MindStateFactory.defaultFor(it.id) }
    )
    val activeMindState: StateFlow<PersonaMindState> = combine(
        _mindStatesByPersona,
        _selectedPersona
    ) { map, persona ->
        map[persona.id] ?: MindStateFactory.defaultFor(persona.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MindStateFactory.defaultFor(PersonaCatalog.all.first().id)
    )

    private val _healingLoading = MutableStateFlow(false)
    val healingLoading: StateFlow<Boolean> = _healingLoading.asStateFlow()

    private val _decisionLoading = MutableStateFlow(false)
    val decisionLoading: StateFlow<Boolean> = _decisionLoading.asStateFlow()

    private val _neuroSyncLoading = MutableStateFlow(false)
    val neuroSyncLoading: StateFlow<Boolean> = _neuroSyncLoading.asStateFlow()

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

    private val _chatThinkingStreamingText = MutableStateFlow<String?>(null)
    val chatThinkingStreamingText: StateFlow<String?> = _chatThinkingStreamingText.asStateFlow()

    private val _chatStreamingText = MutableStateFlow<String?>(null)
    val chatStreamingText: StateFlow<String?> = _chatStreamingText.asStateFlow()

    private val _pendingDeepThink = MutableStateFlow<PendingDeepThinkRequest?>(null)
    val pendingDeepThink: StateFlow<PendingDeepThinkRequest?> = _pendingDeepThink.asStateFlow()

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

    private val _gitHubToken = MutableStateFlow(
        BuildConfig.GITHUB_PERSONAL_TOKEN
            .trim()
            .takeIf { it.isNotBlank() && it != "OPTIONAL_SET_IN_SECRETS_PANEL" }
            .orEmpty()
    )
    val gitHubToken: StateFlow<String> = _gitHubToken.asStateFlow()

    private val _gitHubState = MutableStateFlow<GitHubLoadState>(GitHubLoadState.Idle)
    val gitHubState: StateFlow<GitHubLoadState> = _gitHubState.asStateFlow()

    private val _codingApps = MutableStateFlow<List<CodingAppInfo>>(emptyList())
    val codingApps: StateFlow<List<CodingAppInfo>> = _codingApps.asStateFlow()

    val termuxCommands: List<TermuxCommand> = CodingAppsCatalog.defaultTermuxCommands

    // Approval-gated Self-Upgrade & Capability Architect state
    private val _devHubMode = MutableStateFlow(DevHubMode.ZipProjectStudio)
    val devHubMode: StateFlow<DevHubMode> = _devHubMode.asStateFlow()

    // ZIP Project Workspace, AI Code Completion & GitHub/Firebase Deploy state
    private val _savedWorkspaces = MutableStateFlow<List<ImportedProjectWorkspace>>(emptyList())
    val savedWorkspaces: StateFlow<List<ImportedProjectWorkspace>> = _savedWorkspaces.asStateFlow()

    private val _activeWorkspace = MutableStateFlow<ImportedProjectWorkspace?>(null)
    val activeWorkspace: StateFlow<ImportedProjectWorkspace?> = _activeWorkspace.asStateFlow()

    private val _selectedWorkspaceFilePath = MutableStateFlow("")
    val selectedWorkspaceFilePath: StateFlow<String> = _selectedWorkspaceFilePath.asStateFlow()

    private val _openFileContent = MutableStateFlow("")
    val openFileContent: StateFlow<String> = _openFileContent.asStateFlow()

    private val _isOpenFileModified = MutableStateFlow(false)
    val isOpenFileModified: StateFlow<Boolean> = _isOpenFileModified.asStateFlow()

    private val _newFilePathInput = MutableStateFlow("")
    val newFilePathInput: StateFlow<String> = _newFilePathInput.asStateFlow()

    private val _codeAiInstructionInput = MutableStateFlow("")
    val codeAiInstructionInput: StateFlow<String> = _codeAiInstructionInput.asStateFlow()

    private val _projectAuditState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val projectAuditState: StateFlow<AiUiState> = _projectAuditState.asStateFlow()

    private val _codePatchLoading = MutableStateFlow(false)
    val codePatchLoading: StateFlow<Boolean> = _codePatchLoading.asStateFlow()

    private val _codePatchSuggestion = MutableStateFlow<AiCodePatchSuggestion?>(null)
    val codePatchSuggestion: StateFlow<AiCodePatchSuggestion?> = _codePatchSuggestion.asStateFlow()

    private val _errorLogInput = MutableStateFlow("")
    val errorLogInput: StateFlow<String> = _errorLogInput.asStateFlow()

    private val _selfBugFixLoading = MutableStateFlow(false)
    val selfBugFixLoading: StateFlow<Boolean> = _selfBugFixLoading.asStateFlow()

    private val _selfBugFixSuggestion = MutableStateFlow<AiCodePatchSuggestion?>(null)
    val selfBugFixSuggestion: StateFlow<AiCodePatchSuggestion?> = _selfBugFixSuggestion.asStateFlow()

    private val _workspaceStatusMessage = MutableStateFlow<String?>(null)
    val workspaceStatusMessage: StateFlow<String?> = _workspaceStatusMessage.asStateFlow()

    private val _gitHubTargetRepoInput = MutableStateFlow("")
    val gitHubTargetRepoInput: StateFlow<String> = _gitHubTargetRepoInput.asStateFlow()

    private val _gitHubBranchInput = MutableStateFlow("main")
    val gitHubBranchInput: StateFlow<String> = _gitHubBranchInput.asStateFlow()

    private val _gitHubCommitMessageInput = MutableStateFlow("Complete & update project via Personal AI Studio")
    val gitHubCommitMessageInput: StateFlow<String> = _gitHubCommitMessageInput.asStateFlow()

    private val _gitHubPushPrivate = MutableStateFlow(false)
    val gitHubPushPrivate: StateFlow<Boolean> = _gitHubPushPrivate.asStateFlow()

    private val _gitHubPushState = MutableStateFlow<GitHubPushState>(GitHubPushState.Idle)
    val gitHubPushState: StateFlow<GitHubPushState> = _gitHubPushState.asStateFlow()

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
        val wsManager = ProjectWorkspaceManager(context.applicationContext)
        workspaceManager = wsManager
        viewModelScope.launch {
            runCatching { wsManager.listSavedWorkspaces() }.onSuccess { list ->
                _savedWorkspaces.value = list
                if (_activeWorkspace.value == null && list.isNotEmpty()) {
                    selectWorkspace(list.first())
                }
            }
            val saved = store.load() ?: return@launch
            val personaMatch = personas.firstOrNull { it.id == saved.selectedPersonaId }
            if (personaMatch != null) {
                _selectedPersona.value = personaMatch
                _charName.value = personaMatch.englishName
                _charAge.value = personaMatch.age
            }
            _speechStyle.value = saved.speechStyle
            _deepThinkPolicy.value = saved.deepThinkPolicy
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
            if (saved.mindStatesByPersona.isNotEmpty()) {
                val merged = PersonaCatalog.all.associate { persona ->
                    persona.id to (saved.mindStatesByPersona[persona.id] ?: MindStateFactory.defaultFor(persona.id))
                }
                _mindStatesByPersona.value = merged
            }
        }
    }

    private fun currentMindState(personaId: String = _selectedPersona.value.id): PersonaMindState {
        return _mindStatesByPersona.value[personaId] ?: MindStateFactory.defaultFor(personaId)
    }

    private fun updatePersonaMindState(personaId: String, transform: (PersonaMindState) -> PersonaMindState) {
        val current = currentMindState(personaId)
        val updated = transform(current)
        _mindStatesByPersona.value = _mindStatesByPersona.value + (personaId to updated)
        saveCurrentState()
    }

    private fun saveCurrentState() {
        val store = localStore ?: return
        val snapshot = PersistedStudioState(
            selectedPersonaId = _selectedPersona.value.id,
            speechStyle = _speechStyle.value,
            deepThinkPolicy = _deepThinkPolicy.value,
            isAutoVoiceEnabled = _isAutoVoiceEnabled.value,
            gitHubUsername = _gitHubUsername.value,
            tasks = _tasks.value,
            callNotes = _callNotes.value.take(50),
            chatMessages = _chatMessages.value.takeLast(50),
            upgradeProposals = _upgradeProposals.value.take(30),
            mindStatesByPersona = _mindStatesByPersona.value
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

    fun updateDeepThinkPolicy(policy: DeepThinkPolicy) {
        _deepThinkPolicy.value = policy
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
        _chatThinkingStreamingText.value = null
        _pendingDeepThink.value = null
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
        _pendingDeepThink.value = null
        _sttPartialTranscript.value = ""
        _sttError.value = null
        _chatError.value = null
        _chatLoading.value = true
        _chatStreamingText.value = ""
        _chatThinkingStreamingText.value = null

        // Record Short-Term working memory entry + instantaneous organic emotional appraisal BEFORE reply generation
        recordShortTermMemory(persona.id, "پیام اخیر سیاوش", raw)
        updatePersonaMindState(persona.id) { state ->
            state.copy(
                neuro = MindStateFactory.preAppraiseUserEmotionStimulus(
                    personaId = persona.id,
                    userMessage = raw,
                    current = state.neuro
                )
            )
        }

        viewModelScope.launch {
            try {
                val wordsCount = raw.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                val mightNeedComplexityCheck = _deepThinkPolicy.value != DeepThinkPolicy.AlwaysNormal &&
                    (wordsCount >= 5 || raw.contains("؟") || raw.contains("?") || raw.length >= 35)

                val (isHard, reason) = if (mightNeedComplexityCheck) {
                    try {
                        withTimeout(10_000L) {
                            aiRepository.assessQuestionComplexity(raw)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        false to ""
                    }
                } else {
                    false to ""
                }

                if (isHard && _deepThinkPolicy.value == DeepThinkPolicy.AskPermissionFirst) {
                    val askText = buildDeepThinkPermissionText(persona, _speechStyle.value, reason)
                    _pendingDeepThink.value = PendingDeepThinkRequest(originalPrompt = raw, reason = reason)
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        id = System.currentTimeMillis(),
                        isUser = false,
                        text = askText,
                        personaName = persona.persianName
                    )
                    saveCurrentState()
                    _chatStreamingText.value = null
                    _chatLoading.value = false
                    if (_isAutoVoiceEnabled.value || _isVideoStageOpen.value) {
                        onReplyComplete?.invoke(askText, persona)
                    }
                    return@launch
                }

                executeChatStream(
                    userPrompt = raw,
                    history = previousHistory,
                    persona = persona,
                    useDeepThink = isHard && _deepThinkPolicy.value == DeepThinkPolicy.AutoOnHard,
                    onReplyComplete = onReplyComplete
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat dispatch failed", e)
                _chatError.value = e.localizedMessage ?: "خطا در گفتگو با هوش مصنوعی"
                _chatStreamingText.value = null
                _chatThinkingStreamingText.value = null
                _chatLoading.value = false
            }
        }
    }

    fun confirmDeepThinkAnalysis(onReplyComplete: ((String, AssistantPersona) -> Unit)? = null) {
        val pending = _pendingDeepThink.value ?: return
        if (_chatLoading.value) return
        _pendingDeepThink.value = null
        val persona = _selectedPersona.value
        val history = _chatMessages.value
        _chatLoading.value = true
        _chatError.value = null
        _chatThinkingStreamingText.value = "در حال تحلیل سریع و جمع‌بندی..."
        _chatStreamingText.value = ""

        viewModelScope.launch {
            executeChatStream(
                userPrompt = pending.originalPrompt,
                history = history,
                persona = persona,
                useDeepThink = true,
                onReplyComplete = onReplyComplete
            )
        }
    }

    fun declineDeepThinkAndReplyNormally(onReplyComplete: ((String, AssistantPersona) -> Unit)? = null) {
        val pending = _pendingDeepThink.value ?: return
        if (_chatLoading.value) return
        _pendingDeepThink.value = null
        val persona = _selectedPersona.value
        val history = _chatMessages.value
        _chatLoading.value = true
        _chatError.value = null
        _chatThinkingStreamingText.value = null
        _chatStreamingText.value = ""

        viewModelScope.launch {
            executeChatStream(
                userPrompt = pending.originalPrompt,
                history = history,
                persona = persona,
                useDeepThink = false,
                onReplyComplete = onReplyComplete
            )
        }
    }

    private suspend fun executeChatStream(
        userPrompt: String,
        history: List<ChatMessage>,
        persona: AssistantPersona,
        useDeepThink: Boolean,
        onReplyComplete: ((String, AssistantPersona) -> Unit)?
    ) {
        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                val approved = _upgradeProposals.value.filter { it.status == ApprovalStatus.Approved }
                val mindState = currentMindState(persona.id)
                val flow = if (useDeepThink) {
                    aiRepository.streamDeepThinkChat(
                        history = history,
                        newMessage = userPrompt,
                        persona = persona,
                        speechStyle = _speechStyle.value,
                        approvedCapabilities = approved,
                        mindState = mindState
                    )
                } else {
                    aiRepository.streamPersonaChat(
                        history = history,
                        newMessage = userPrompt,
                        persona = persona,
                        speechStyle = _speechStyle.value,
                        approvedCapabilities = approved,
                        mindState = mindState
                    )
                }
                flow.collect { chunk ->
                    accumulated.append(chunk)
                    if (useDeepThink) {
                        val (thinking, answer) = parseDeepThinkMarkers(accumulated.toString())
                        _chatThinkingStreamingText.value = thinking.ifBlank { null }
                        _chatStreamingText.value = answer
                    } else {
                        _chatStreamingText.value = accumulated.toString()
                    }
                }
            }
            val rawComplete = accumulated.toString().trim()
            val (finalThink, finalAnswer) = if (useDeepThink) {
                parseDeepThinkMarkers(rawComplete)
            } else {
                "" to rawComplete
            }
            val cleanAnswer = finalAnswer.ifBlank { rawComplete }
            if (cleanAnswer.isNotEmpty()) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = System.currentTimeMillis(),
                    isUser = false,
                    text = cleanAnswer,
                    personaName = persona.persianName,
                    thinkingSummary = finalThink
                )
                saveCurrentState()
                if (_isAutoVoiceEnabled.value || _isVideoStageOpen.value) {
                    onReplyComplete?.invoke(cleanAnswer, persona)
                }
                triggerBackgroundNeuroUpdate(userPrompt, cleanAnswer, persona)
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
            _chatThinkingStreamingText.value = null
            _chatLoading.value = false
        }
    }

    private fun parseDeepThinkMarkers(raw: String): Pair<String, String> {
        val thinkStart = raw.indexOf("[THINK]")
        val thinkEnd = raw.indexOf("[/THINK]")
        val answerStart = raw.indexOf("[ANSWER]")
        val answerEnd = raw.indexOf("[/ANSWER]")

        val thinking = when {
            thinkStart >= 0 && thinkEnd > thinkStart ->
                raw.substring(thinkStart + 7, thinkEnd).trim()
            thinkStart >= 0 ->
                raw.substring(thinkStart + 7).trim()
            else -> ""
        }

        val answer = when {
            answerStart >= 0 && answerEnd > answerStart ->
                raw.substring(answerStart + 8, answerEnd).trim()
            answerStart >= 0 ->
                raw.substring(answerStart + 8).trim()
            thinkEnd >= 0 ->
                raw.substring(thinkEnd + 8).trim()
            thinkStart >= 0 -> ""
            else -> raw.trim()
        }
        return thinking to answer
    }

    private fun buildDeepThinkPermissionText(
        persona: AssistantPersona,
        speechStyle: SpeechStyleMode,
        reason: String
    ): String {
        val why = if (reason.isNotBlank()) " ($reason)" else ""
        return if (speechStyle == SpeechStyleMode.Colloquial) {
            when (persona.id) {
                "elena" -> "اوه چه سوال توپ و فکری پرسیدی$why! اجازه می‌دی یه لحظه سریع حلاجی و تحلیلش کنم، بعد جواب خفنش رو بهت بدم؟"
                "soraya" -> "سوال خیلی مهم و دقیقی پرسیدی عزیز$why؛ اجازه می‌دی یه کم روش فکر و تحلیل کوتاه کنم تا حساب‌شده‌ترین جواب رو بدم؟"
                else -> "سوال بسیار عمیق و مهمی پرسیدی$why؛ با اجازه خودت یه لحظه کوتاه تحلیلش کنم و بعد بهترین و پیگیرترین راه رو بهت بگم؟"
            }
        } else {
            "این پرسش نیاز به تامل و تحلیل چندجانبه دارد$why. اجازه می‌دهید ابتدا یک تحلیل کوتاه و دقیق انجام دهم و سپس پاسخ نهایی را ارائه کنم؟"
        }
    }

    private fun recordShortTermMemory(personaId: String, title: String, content: String) {
        updatePersonaMindState(personaId) { state ->
            val shortTerms = state.memories.filter { it.tier == MemoryTier.ShortTerm }
            val others = state.memories.filterNot { it.tier == MemoryTier.ShortTerm }
            val entry = MemoryEntry(
                id = System.currentTimeMillis(),
                tier = MemoryTier.ShortTerm,
                title = title,
                content = content.take(180),
                emotionTag = state.neuro.activeEmotion.persianLabel,
                importance = 2,
                timestampLabel = "حافظه کوتاه‌مدت"
            )
            state.copy(memories = (listOf(entry) + shortTerms).take(8) + others)
        }
    }

    private fun triggerBackgroundNeuroUpdate(
        userMessage: String,
        assistantReply: String,
        persona: AssistantPersona
    ) {
        viewModelScope.launch {
            _neuroSyncLoading.value = true
            try {
                val currentMind = currentMindState(persona.id)
                val outcome = withTimeout(25_000L) {
                    aiRepository.evaluateNeuroReactionAndMemory(
                        userMessage = userMessage,
                        assistantReply = assistantReply,
                        persona = persona,
                        currentMind = currentMind
                    )
                }
                applyNeuroOutcome(persona.id, outcome)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Background neuro/memory sync skipped: ${e.message}")
            } finally {
                _neuroSyncLoading.value = false
            }
        }
    }

    private fun applyNeuroOutcome(
        personaId: String,
        outcome: com.example.data.NeuroEvaluationOutcome
    ) {
        updatePersonaMindState(personaId) { state ->
            val newMemories = state.memories.toMutableList()
            if (!outcome.midTermTitle.isNullOrBlank() && !outcome.midTermContent.isNullOrBlank()) {
                newMemories.add(
                    0,
                    MemoryEntry(
                        id = System.currentTimeMillis(),
                        tier = MemoryTier.MidTerm,
                        title = outcome.midTermTitle,
                        content = outcome.midTermContent,
                        emotionTag = outcome.updatedNeuro.activeEmotion.persianLabel,
                        importance = 4,
                        timestampLabel = "میان‌مدت خودکار"
                    )
                )
            }
            if (!outcome.longTermFact.isNullOrBlank()) {
                newMemories.add(
                    0,
                    MemoryEntry(
                        id = System.currentTimeMillis() + 1L,
                        tier = MemoryTier.LongTerm,
                        title = "شناخت ماندگار از سیاوش",
                        content = outcome.longTermFact,
                        emotionTag = "بنیادین",
                        importance = 5,
                        timestampLabel = "بلندمدت خودکار"
                    )
                )
            }
            state.copy(
                memories = newMemories.take(40),
                neuro = outcome.updatedNeuro
            )
        }
    }

    fun triggerCompositeEmotionStimulus(targetEmotion: CompositeEmotion) {
        val persona = _selectedPersona.value
        updatePersonaMindState(persona.id) { state ->
            val n = state.neuro
            val updated = when (targetEmotion) {
                CompositeEmotion.EcstaticJoy -> n.copy(
                    serotonin = 0.84f,
                    dopamine = 0.96f,
                    endorphins = 0.94f,
                    stressLoad = 0.10f,
                    willpower = (n.willpower + 0.05f).coerceAtMost(1.0f),
                    activeEmotion = CompositeEmotion.EcstaticJoy,
                    lastHealingNote = "موج شادی، هیجان و ذوق‌مرگ شدن فعال شد!"
                )
                CompositeEmotion.DeepAffection -> n.copy(
                    serotonin = 0.92f,
                    dopamine = 0.82f,
                    endorphins = 0.90f,
                    stressLoad = 0.08f,
                    activeEmotion = CompositeEmotion.DeepAffection,
                    lastHealingNote = "حس عمیق لذت، محبت و دوست داشتن در شبکه عصبی فعال شد."
                )
                CompositeEmotion.SoftHurt -> n.copy(
                    serotonin = 0.36f,
                    dopamine = 0.44f,
                    endorphins = 0.40f,
                    stressLoad = 0.52f,
                    activeEmotion = CompositeEmotion.SoftHurt,
                    lastHealingNote = "کاهش سروتونین و دلخوری ملایم ثبت شد؛ آماده خودترمیمی یا دلجویی."
                )
                CompositeEmotion.DeepSorrow -> n.copy(
                    serotonin = 0.20f,
                    dopamine = 0.24f,
                    endorphins = 0.22f,
                    stressLoad = 0.84f,
                    activeEmotion = CompositeEmotion.DeepSorrow,
                    lastHealingNote = "موج غم عمیق و تاثر عاطفی تجربه شد."
                )
                CompositeEmotion.ProtectiveFear -> n.copy(
                    serotonin = 0.42f,
                    dopamine = 0.38f,
                    endorphins = 0.35f,
                    stressLoad = 0.86f,
                    willpower = (n.willpower + 0.04f).coerceAtMost(1.0f),
                    activeEmotion = CompositeEmotion.ProtectiveFear,
                    lastHealingNote = "حس ترس و هشیاری محافظتی برای مراقبت از منافع شما فعال شد."
                )
                CompositeEmotion.VulnerableOverwhelm -> n.copy(
                    serotonin = 0.30f,
                    dopamine = 0.28f,
                    endorphins = 0.25f,
                    stressLoad = 0.78f,
                    willpower = (n.willpower - 0.06f).coerceAtLeast(0.40f),
                    activeEmotion = CompositeEmotion.VulnerableOverwhelm,
                    lastHealingNote = "فشار و حساس شدن لحظه‌ای رخ داد؛ دکمه خودترمیمی می‌تواند آن را به قدرت تبدیل کند."
                )
                CompositeEmotion.SereneDevotion -> n.copy(
                    serotonin = 0.80f,
                    dopamine = 0.72f,
                    endorphins = 0.74f,
                    stressLoad = 0.14f,
                    activeEmotion = CompositeEmotion.SereneDevotion,
                    lastHealingNote = "تعادل پایدار و آرامش وفادارانه برقرار شد."
                )
            }
            state.copy(neuro = updated)
        }
    }

    fun runEmotionalSelfHealingAndGrowth() {
        if (_healingLoading.value) return
        val persona = _selectedPersona.value
        val currentMind = currentMindState(persona.id)
        viewModelScope.launch {
            _healingLoading.value = true
            try {
                val outcome = withTimeout(45_000L) {
                    aiRepository.runEmotionalSelfHealing(persona, currentMind)
                }
                applyNeuroOutcome(persona.id, outcome)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Self-healing cycle failed", e)
            } finally {
                _healingLoading.value = false
            }
        }
    }

    fun consolidateChatIntoMemory() {
        if (_neuroSyncLoading.value) return
        val persona = _selectedPersona.value
        val lastUser = _chatMessages.value.lastOrNull { it.isUser }?.text ?: "مرور مکالمه اخیر"
        val lastAi = _chatMessages.value.lastOrNull { !it.isUser }?.text ?: persona.sampleGreeting
        triggerBackgroundNeuroUpdate(lastUser, lastAi, persona)
    }

    fun addManualMemory(tier: MemoryTier, title: String, content: String) {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()
        if (cleanTitle.isEmpty() || cleanContent.isEmpty()) return
        val personaId = _selectedPersona.value.id
        updatePersonaMindState(personaId) { state ->
            val newEntry = MemoryEntry(
                id = System.currentTimeMillis(),
                tier = tier,
                title = cleanTitle,
                content = cleanContent,
                emotionTag = state.neuro.activeEmotion.persianLabel,
                importance = if (tier == MemoryTier.LongTerm) 5 else 3,
                timestampLabel = tier.persianLabel
            )
            state.copy(memories = (listOf(newEntry) + state.memories).take(45))
        }
    }

    fun promoteMemoryToLongTerm(memoryId: Long) {
        val personaId = _selectedPersona.value.id
        updatePersonaMindState(personaId) { state ->
            val updated = state.memories.map { mem ->
                if (mem.id == memoryId) {
                    mem.copy(
                        tier = MemoryTier.LongTerm,
                        importance = 5,
                        timestampLabel = "ارتقایافته به بلندمدت"
                    )
                } else mem
            }
            state.copy(memories = updated)
        }
    }

    fun deleteMemory(memoryId: Long) {
        val personaId = _selectedPersona.value.id
        updatePersonaMindState(personaId) { state ->
            state.copy(memories = state.memories.filterNot { it.id == memoryId })
        }
    }

    fun evaluateAutonomousDecision(
        dilemma: String,
        onReplyComplete: ((String, AssistantPersona) -> Unit)? = null
    ) {
        val clean = dilemma.trim()
        if (clean.isEmpty() || _decisionLoading.value) return
        val persona = _selectedPersona.value
        val style = _speechStyle.value
        val mind = currentMindState(persona.id)

        viewModelScope.launch {
            _decisionLoading.value = true
            try {
                val record = withTimeout(45_000L) {
                    aiRepository.evaluateAutonomousDecision(clean, persona, style, mind)
                }
                updatePersonaMindState(persona.id) { state ->
                    val boostedNeuro = state.neuro.copy(
                        willpower = (state.neuro.willpower + 0.02f).coerceAtMost(1.0f)
                    )
                    state.copy(
                        neuro = boostedNeuro,
                        decisions = (listOf(record) + state.decisions).take(20)
                    )
                }
                if (_isAutoVoiceEnabled.value) {
                    onReplyComplete?.invoke("${record.chosenAction}. ${record.reasoning}", persona)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Decision engine failed", e)
            } finally {
                _decisionLoading.value = false
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
                        approvedCapabilities = approved,
                        mindState = currentMindState(persona.id)
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
                        approvedCapabilities = _upgradeProposals.value.filter { it.status == ApprovalStatus.Approved },
                        mindState = currentMindState()
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
                        approvedCapabilities = approved,
                        mindState = currentMindState()
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

    // ZIP Project Workspace, AI Code Completion & GitHub/Firebase Deploy actions
    fun importProjectZip(context: Context, uri: Uri) {
        val manager = workspaceManager ?: ProjectWorkspaceManager(context.applicationContext).also { workspaceManager = it }
        viewModelScope.launch {
            _workspaceStatusMessage.value = "در حال استخراج فایل زیپ و اسکن کدها..."
            _projectAuditState.value = AiUiState.Idle
            _codePatchSuggestion.value = null
            _gitHubPushState.value = GitHubPushState.Idle
            try {
                val imported = manager.extractZipFromUri(uri)
                _savedWorkspaces.value = manager.listSavedWorkspaces()
                selectWorkspace(imported)
                _workspaceStatusMessage.value = "پروژه «${imported.displayName}» با ${imported.fileCount} فایل با موفقیت استخراج و آماده کدنویسی شد."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "ZIP import failed", e)
                _workspaceStatusMessage.value = "خطا در استخراج زیپ: ${e.localizedMessage ?: "نامشخص"}"
            }
        }
    }

    fun selectWorkspace(workspace: ImportedProjectWorkspace) {
        _activeWorkspace.value = workspace
        _gitHubTargetRepoInput.value = workspace.displayName.lowercase().replace(Regex("[^a-z0-9._-]"), "-")
        val firstEditable = workspace.files.firstOrNull {
            it.isEditableText && (
                it.relativePath.endsWith(".kt") ||
                    it.relativePath.endsWith(".tsx") ||
                    it.relativePath.endsWith(".ts") ||
                    it.relativePath.endsWith(".json") ||
                    it.relativePath.endsWith(".xml")
                )
        } ?: workspace.files.firstOrNull { it.isEditableText }
        if (firstEditable != null) {
            openWorkspaceFile(firstEditable.relativePath)
        } else {
            _selectedWorkspaceFilePath.value = ""
            _openFileContent.value = ""
            _isOpenFileModified.value = false
        }
    }

    fun deleteWorkspace(projectId: String) {
        val manager = workspaceManager ?: return
        viewModelScope.launch {
            manager.deleteWorkspace(projectId)
            val remaining = manager.listSavedWorkspaces()
            _savedWorkspaces.value = remaining
            if (_activeWorkspace.value?.projectId == projectId) {
                _activeWorkspace.value = remaining.firstOrNull()
                if (remaining.isNotEmpty()) {
                    selectWorkspace(remaining.first())
                } else {
                    _selectedWorkspaceFilePath.value = ""
                    _openFileContent.value = ""
                    _isOpenFileModified.value = false
                }
            }
        }
    }

    fun openWorkspaceFile(relativePath: String) {
        val ws = _activeWorkspace.value ?: return
        val manager = workspaceManager ?: return
        viewModelScope.launch {
            try {
                val text = manager.readTextFile(ws.projectId, relativePath)
                _selectedWorkspaceFilePath.value = relativePath
                _openFileContent.value = text
                _isOpenFileModified.value = false
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _workspaceStatusMessage.value = "خطا در خواندن فایل: ${e.localizedMessage}"
            }
        }
    }

    fun updateOpenFileContent(content: String) {
        _openFileContent.value = content
        _isOpenFileModified.value = true
    }

    fun saveOpenFileToDisk() {
        val ws = _activeWorkspace.value ?: return
        val path = _selectedWorkspaceFilePath.value.takeIf { it.isNotBlank() } ?: return
        val manager = workspaceManager ?: return
        viewModelScope.launch {
            try {
                val refreshed = manager.writeTextFile(ws.projectId, path, _openFileContent.value)
                _activeWorkspace.value = refreshed
                _savedWorkspaces.value = manager.listSavedWorkspaces()
                _isOpenFileModified.value = false
                _workspaceStatusMessage.value = "فایل «$path» با موفقیت در فضای کاری ذخیره شد."
            } catch (e: Exception) {
                _workspaceStatusMessage.value = "خطا در ذخیره فایل: ${e.localizedMessage}"
            }
        }
    }

    fun updateNewFilePathInput(value: String) {
        _newFilePathInput.value = value
    }

    fun createNewWorkspaceFile() {
        val ws = _activeWorkspace.value ?: return
        val manager = workspaceManager ?: return
        val path = _newFilePathInput.value.trim().trimStart('/')
        if (path.isEmpty()) return
        viewModelScope.launch {
            try {
                val refreshed = manager.writeTextFile(ws.projectId, path, "// Created by Personal AI Companion\n")
                _activeWorkspace.value = refreshed
                _savedWorkspaces.value = manager.listSavedWorkspaces()
                _selectedWorkspaceFilePath.value = path
                _openFileContent.value = "// Created by Personal AI Companion\n"
                _isOpenFileModified.value = false
                _newFilePathInput.value = ""
                _workspaceStatusMessage.value = "فایل جدید «$path» ساخته شد."
            } catch (e: Exception) {
                _workspaceStatusMessage.value = "خطا در ساخت فایل: ${e.localizedMessage}"
            }
        }
    }

    fun exportActiveWorkspaceZip(context: Context, destUri: Uri) {
        val ws = _activeWorkspace.value ?: return
        val manager = workspaceManager ?: ProjectWorkspaceManager(context.applicationContext).also { workspaceManager = it }
        viewModelScope.launch {
            try {
                val count = manager.exportWorkspaceToZipUri(ws.projectId, destUri)
                _workspaceStatusMessage.value = "خروجی زیپ تکمیل‌شده با $count فایل آماده ایمپورت در AI Studio / اشتراک‌گذاری ساخته شد."
            } catch (e: Exception) {
                _workspaceStatusMessage.value = "خطا در ساخت فایل زیپ خروجی: ${e.localizedMessage}"
            }
        }
    }

    fun updateCodeAiInstruction(value: String) {
        _codeAiInstructionInput.value = value
    }

    fun auditActiveWorkspaceProject() {
        val ws = _activeWorkspace.value ?: return
        val manager = workspaceManager ?: return
        val persona = _selectedPersona.value
        val style = _speechStyle.value
        val mind = currentMindState(persona.id)
        viewModelScope.launch {
            _projectAuditState.value = AiUiState.Loading
            val accumulated = StringBuilder()
            try {
                val (tree, snippets) = manager.buildProjectContextForAi(ws.projectId, _selectedWorkspaceFilePath.value)
                withTimeout(60_000L) {
                    aiRepository.auditUnfinishedProject(
                        projectName = ws.displayName,
                        fileTreeSummary = tree,
                        keyFilesSnippets = snippets,
                        persona = persona,
                        speechStyle = style,
                        mindState = mind
                    ).collect { chunk ->
                        accumulated.append(chunk)
                        _projectAuditState.value = AiUiState.Streaming(accumulated.toString())
                    }
                }
                _projectAuditState.value = if (accumulated.isNotBlank()) {
                    AiUiState.Success(accumulated.toString())
                } else {
                    AiUiState.Error("گزارش تحلیل پروژه دریافت نشد.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _projectAuditState.value = AiUiState.Error(e.localizedMessage ?: "خطا در تحلیل پروژه")
            }
        }
    }

    fun generateAiCodeCompletion() {
        val ws = _activeWorkspace.value ?: return
        val manager = workspaceManager ?: return
        val instruction = _codeAiInstructionInput.value.trim()
        if (instruction.isEmpty() || _codePatchLoading.value) return
        val persona = _selectedPersona.value
        val style = _speechStyle.value
        val mind = currentMindState(persona.id)

        viewModelScope.launch {
            _codePatchLoading.value = true
            _codePatchSuggestion.value = null
            try {
                val (tree, snippets) = manager.buildProjectContextForAi(ws.projectId, _selectedWorkspaceFilePath.value)
                val patch = withTimeout(60_000L) {
                    aiRepository.generateProjectCodePatch(
                        projectName = ws.displayName,
                        fileTreeSummary = tree,
                        keyFilesSnippets = snippets,
                        currentFilePath = _selectedWorkspaceFilePath.value,
                        currentFileContent = _openFileContent.value,
                        userInstruction = instruction,
                        persona = persona,
                        speechStyle = style,
                        mindState = mind
                    )
                }
                _codePatchSuggestion.value = patch
                _gitHubCommitMessageInput.value = patch.suggestedCommitMessage
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _workspaceStatusMessage.value = "خطا در کدنویسی AI: ${e.localizedMessage}"
            } finally {
                _codePatchLoading.value = false
            }
        }
    }

    fun updateErrorLogInput(value: String) {
        _errorLogInput.value = value
    }

    fun fixErrorInActiveWorkspace() {
        val ws = _activeWorkspace.value ?: return
        val manager = workspaceManager ?: return
        val report = _errorLogInput.value.trim()
        if (report.isEmpty() || _codePatchLoading.value) return
        val persona = _selectedPersona.value
        val style = _speechStyle.value
        val mind = currentMindState(persona.id)

        viewModelScope.launch {
            _codePatchLoading.value = true
            _codePatchSuggestion.value = null
            try {
                val (tree, _) = manager.buildProjectContextForAi(ws.projectId, _selectedWorkspaceFilePath.value)
                val patch = withTimeout(60_000L) {
                    aiRepository.diagnoseAndFixError(
                        errorOrBugReport = report,
                        projectName = ws.displayName,
                        fileTreeSummary = tree,
                        currentFilePath = _selectedWorkspaceFilePath.value,
                        currentFileContent = _openFileContent.value,
                        isSelfAppContext = false,
                        persona = persona,
                        speechStyle = style,
                        mindState = mind
                    )
                }
                _codePatchSuggestion.value = patch
                _gitHubCommitMessageInput.value = patch.suggestedCommitMessage
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _workspaceStatusMessage.value = "خطا در عیب‌یابی باگ: ${e.localizedMessage}"
            } finally {
                _codePatchLoading.value = false
            }
        }
    }

    fun fixSelfUpgradeOrAppError() {
        val report = _errorLogInput.value.trim()
        if (report.isEmpty() || _selfBugFixLoading.value) return
        val persona = _selectedPersona.value
        val style = _speechStyle.value
        val mind = currentMindState(persona.id)
        val latestUpgradeCode = _upgradeProposals.value.firstOrNull()?.codePatch.orEmpty()

        viewModelScope.launch {
            _selfBugFixLoading.value = true
            _selfBugFixSuggestion.value = null
            try {
                val patch = withTimeout(60_000L) {
                    aiRepository.diagnoseAndFixError(
                        errorOrBugReport = report,
                        projectName = "Personal AI Creative Studio (Self Kotlin/Compose App)",
                        fileTreeSummary = "MainActivity.kt, StudioViewModel.kt, AiRepository.kt, GitHubRepository.kt, ProjectWorkspaceManager.kt, StudioModels.kt, SecretaryScreen.kt, DevHubScreen.kt",
                        currentFilePath = _upgradeProposals.value.firstOrNull()?.title ?: "SelfUpgradePatch.kt",
                        currentFileContent = latestUpgradeCode,
                        isSelfAppContext = true,
                        persona = persona,
                        speechStyle = style,
                        mindState = mind
                    )
                }
                _selfBugFixSuggestion.value = patch
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _upgradeError.value = "خطا در عیب‌یابی کد خود برنامه: ${e.localizedMessage}"
            } finally {
                _selfBugFixLoading.value = false
            }
        }
    }

    fun applySuggestedCodePatch() {
        val ws = _activeWorkspace.value ?: return
        val patch = _codePatchSuggestion.value ?: return
        val manager = workspaceManager ?: return
        viewModelScope.launch {
            try {
                var refreshed = manager.writeTextFile(ws.projectId, patch.targetFilePath, patch.updatedFileContent)
                if (!patch.extraFilePath.isNullOrBlank() && !patch.extraFileContent.isNullOrBlank()) {
                    refreshed = manager.writeTextFile(ws.projectId, patch.extraFilePath, patch.extraFileContent)
                }
                _activeWorkspace.value = refreshed
                _savedWorkspaces.value = manager.listSavedWorkspaces()
                _selectedWorkspaceFilePath.value = patch.targetFilePath
                _openFileContent.value = patch.updatedFileContent
                _isOpenFileModified.value = false
                _workspaceStatusMessage.value = "تغییرات کدنویسی AI روی فایل «${patch.targetFilePath}» اعمال و ذخیره شد!"
            } catch (e: Exception) {
                _workspaceStatusMessage.value = "خطا در اعمال تغییرات روی فایل: ${e.localizedMessage}"
            }
        }
    }

    fun insertDeployTemplate(templateType: DeployTemplateType) {
        val ws = _activeWorkspace.value ?: return
        val manager = workspaceManager ?: return
        viewModelScope.launch {
            try {
                val files = manager.buildDeployTemplateFiles(templateType, ws.displayName)
                var latest = ws
                files.forEach { (path, content) ->
                    latest = manager.writeTextFile(ws.projectId, path, content)
                }
                _activeWorkspace.value = latest
                _savedWorkspaces.value = manager.listSavedWorkspaces()
                val first = files.firstOrNull()
                if (first != null) {
                    _selectedWorkspaceFilePath.value = first.first
                    _openFileContent.value = first.second
                    _isOpenFileModified.value = false
                }
                _gitHubCommitMessageInput.value = "Add ${templateType.name} workflow/config"
                _workspaceStatusMessage.value = "فایل‌های ${templateType.persianLabel} به پروژه اضافه شد؛ الان آماده Push به گیت‌هاب است."
            } catch (e: Exception) {
                _workspaceStatusMessage.value = "خطا در ساخت قالب دپلو: ${e.localizedMessage}"
            }
        }
    }

    fun updateGitHubTargetRepo(value: String) { _gitHubTargetRepoInput.value = value }
    fun updateGitHubBranch(value: String) { _gitHubBranchInput.value = value }
    fun updateGitHubCommitMessage(value: String) { _gitHubCommitMessageInput.value = value }
    fun toggleGitHubPushPrivate() { _gitHubPushPrivate.value = !_gitHubPushPrivate.value }

    fun pushActiveWorkspaceToGitHub() {
        val ws = _activeWorkspace.value ?: return
        val manager = workspaceManager ?: return
        val owner = _gitHubUsername.value.trim()
        val repo = _gitHubTargetRepoInput.value.trim().ifBlank { ws.displayName }
        val branch = _gitHubBranchInput.value.trim().ifBlank { "main" }
        val token = _gitHubToken.value.trim()
        val commitMsg = _gitHubCommitMessageInput.value.trim().ifBlank { "Update via Personal AI Companion" }

        if (owner.isEmpty() || token.isEmpty()) {
            _gitHubPushState.value = GitHubPushState.Error("برای ساخت مخزن و پوش مستقیم کد، نام کاربری گیت‌هاب و Personal Access Token (با دسترسی repo) را در تب گیت‌هاب یا همینجا وارد کنید.")
            return
        }

        viewModelScope.launch {
            _gitHubPushState.value = GitHubPushState.Running("در حال بررسی / ساخت مخزن $owner/$repo روی گیت‌هاب...")
            try {
                val repoUrl = gitHubRepository.ensureRepositoryExists(
                    owner = owner,
                    repoName = repo,
                    personalToken = token,
                    isPrivate = _gitHubPushPrivate.value
                )
                val filesToPush = manager.collectFilesForGitHubPush(ws.projectId)
                if (filesToPush.isEmpty()) {
                    _gitHubPushState.value = GitHubPushState.Error("فایل قابل آپلودی در پروژه پیدا نشد.")
                    return@launch
                }
                val committed = gitHubRepository.pushFilesToRepository(
                    owner = owner,
                    repoName = repo,
                    branch = branch,
                    personalToken = token,
                    files = filesToPush,
                    commitMessage = commitMsg
                ) { idx, total, path ->
                    _gitHubPushState.value = GitHubPushState.Running("آپلود فایل $idx از $total: $path")
                }
                _gitHubPushState.value = GitHubPushState.Success(
                    repoUrl = repoUrl,
                    committedCount = committed,
                    message = "$committed فایل با موفقیت روی شاخه $branch مخزن $owner/$repo پوش و کامیت شد!"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "GitHub push failed", e)
                _gitHubPushState.value = GitHubPushState.Error(e.localizedMessage ?: "خطا در پوش به گیت‌هاب")
            }
        }
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
