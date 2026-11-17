package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.PersonaSpeechRecognizer
import com.example.audio.PersonaVoicePlayer
import com.example.firebase.configureAppCheck
import com.example.model.TopDestination
import com.example.ui.components.StudioBannerHeader
import com.example.ui.screens.CreativeStudioScreen
import com.example.ui.screens.DevHubScreen
import com.example.ui.screens.GeminiStudioScreen
import com.example.ui.screens.SecretaryScreen
import com.example.ui.theme.PersonalAICreativeStudioTheme
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val useNavRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

            PersonalAICreativeStudioTheme {
                StudioAppRoot(useNavRail = useNavRail)
            }
        }
    }
}

private data class NavItem(
    val destination: TopDestination,
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun StudioAppRoot(
    useNavRail: Boolean,
    studioViewModel: StudioViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        studioViewModel.initializeLocalStore(context)
    }
    val selectedPersona by studioViewModel.selectedPersona.collectAsStateWithLifecycle()
    val currentDestination by studioViewModel.currentDestination.collectAsStateWithLifecycle()
    val studioSection by studioViewModel.studioSection.collectAsStateWithLifecycle()

    var isSpeaking by remember { mutableStateOf(false) }
    val voicePlayer = remember(context) {
        PersonaVoicePlayer(context) { speaking -> isSpeaking = speaking }
    }
    val speechRecognizer = remember(context) {
        PersonaSpeechRecognizer(
            context = context,
            onListeningChanged = { listening -> studioViewModel.onSttListeningChanged(listening) },
            onPartialTranscript = { partial -> studioViewModel.onSttPartialTranscript(partial) },
            onFinalTranscript = { transcript ->
                studioViewModel.sendChatMessage(overrideText = transcript) { reply, persona ->
                    voicePlayer.speak(reply, persona)
                }
            },
            onRmsLevel = { rms -> studioViewModel.onSttRmsLevel(rms) },
            onError = { err -> studioViewModel.onSttError(err) }
        )
    }
    DisposableEffect(voicePlayer, speechRecognizer) {
        onDispose {
            voicePlayer.release()
            speechRecognizer.release()
        }
    }

    // Secretary states
    val secretaryMode by studioViewModel.secretaryMode.collectAsStateWithLifecycle()
    val speechStyle by studioViewModel.speechStyle.collectAsStateWithLifecycle()
    val chatMessages by studioViewModel.chatMessages.collectAsStateWithLifecycle()
    val chatInput by studioViewModel.chatInput.collectAsStateWithLifecycle()
    val chatStreamingText by studioViewModel.chatStreamingText.collectAsStateWithLifecycle()
    val chatLoading by studioViewModel.chatLoading.collectAsStateWithLifecycle()
    val chatError by studioViewModel.chatError.collectAsStateWithLifecycle()
    val isVideoStageOpen by studioViewModel.isVideoStageOpen.collectAsStateWithLifecycle()
    val isAutoVoiceEnabled by studioViewModel.isAutoVoiceEnabled.collectAsStateWithLifecycle()
    val isListening by studioViewModel.isListening.collectAsStateWithLifecycle()
    val rmsLevel by studioViewModel.rmsLevel.collectAsStateWithLifecycle()
    val sttPartialTranscript by studioViewModel.sttPartialTranscript.collectAsStateWithLifecycle()
    val sttError by studioViewModel.sttError.collectAsStateWithLifecycle()
    val callScenario by studioViewModel.callScenario.collectAsStateWithLifecycle()
    val callerName by studioViewModel.callerNameInput.collectAsStateWithLifecycle()
    val callerPhone by studioViewModel.callerPhoneInput.collectAsStateWithLifecycle()
    val callNoteInput by studioViewModel.callNoteInput.collectAsStateWithLifecycle()
    val callNotes by studioViewModel.callNotes.collectAsStateWithLifecycle()
    val personalQuestion by studioViewModel.personalQuestion.collectAsStateWithLifecycle()
    val newTaskInput by studioViewModel.newTaskInput.collectAsStateWithLifecycle()
    val tasks by studioViewModel.tasks.collectAsStateWithLifecycle()
    val secretaryAiState by studioViewModel.secretaryAiState.collectAsStateWithLifecycle()
    val advisoryDomain by studioViewModel.advisoryDomain.collectAsStateWithLifecycle()
    val advisoryProfileInput by studioViewModel.advisoryProfileInput.collectAsStateWithLifecycle()
    val advisoryGoalInput by studioViewModel.advisoryGoalInput.collectAsStateWithLifecycle()
    val advisoryAiState by studioViewModel.advisoryAiState.collectAsStateWithLifecycle()

    // GitHub & Coding Apps + Self-Upgrade states
    val devHubMode by studioViewModel.devHubMode.collectAsStateWithLifecycle()
    val gitHubUsername by studioViewModel.gitHubUsername.collectAsStateWithLifecycle()
    val gitHubToken by studioViewModel.gitHubToken.collectAsStateWithLifecycle()
    val gitHubState by studioViewModel.gitHubState.collectAsStateWithLifecycle()
    val codingApps by studioViewModel.codingApps.collectAsStateWithLifecycle()
    val upgradeRequestInput by studioViewModel.upgradeRequestInput.collectAsStateWithLifecycle()
    val upgradeProposals by studioViewModel.upgradeProposals.collectAsStateWithLifecycle()
    val upgradeProposalLoading by studioViewModel.upgradeProposalLoading.collectAsStateWithLifecycle()
    val upgradeError by studioViewModel.upgradeError.collectAsStateWithLifecycle()
    val capabilityRunInput by studioViewModel.capabilityRunInput.collectAsStateWithLifecycle()
    val capabilityRunState by studioViewModel.capabilityRunState.collectAsStateWithLifecycle()

    // Creative Studio states
    val charName by studioViewModel.charName.collectAsStateWithLifecycle()
    val charAge by studioViewModel.charAge.collectAsStateWithLifecycle()
    val charStyle by studioViewModel.charStyle.collectAsStateWithLifecycle()
    val charCategory by studioViewModel.charCategory.collectAsStateWithLifecycle()
    val tryOnModel by studioViewModel.tryOnModel.collectAsStateWithLifecycle()
    val tryOnClothing by studioViewModel.tryOnClothing.collectAsStateWithLifecycle()
    val bridalGownStyle by studioViewModel.bridalGownStyle.collectAsStateWithLifecycle()
    val groomSuitStyle by studioViewModel.groomSuitStyle.collectAsStateWithLifecycle()
    val sceneType by studioViewModel.sceneType.collectAsStateWithLifecycle()
    val lightingSetup by studioViewModel.lightingSetup.collectAsStateWithLifecycle()

    // Gemini AI Studio states
    val aiModuleSelection by studioViewModel.aiModuleSelection.collectAsStateWithLifecycle()
    val aiPrompt by studioViewModel.aiPrompt.collectAsStateWithLifecycle()
    val studioAiState by studioViewModel.studioAiState.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavItem(TopDestination.Secretary, R.string.nav_secretary, Icons.Filled.SupportAgent, "nav_secretary"),
        NavItem(TopDestination.DevHub, R.string.nav_dev_hub, Icons.Filled.Code, "nav_dev_hub"),
        NavItem(TopDestination.Studio, R.string.nav_studio, Icons.Filled.Layers, "nav_studio"),
        NavItem(TopDestination.GeminiAi, R.string.nav_ai, Icons.Filled.AutoAwesome, "nav_gemini_ai")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!useNavRail) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    navItems.forEach { item ->
                        val label = stringResource(id = item.labelRes)
                        NavigationBarItem(
                            selected = currentDestination == item.destination,
                            onClick = { studioViewModel.selectDestination(item.destination) },
                            icon = { Icon(item.icon, contentDescription = label) },
                            label = { Text(label) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (useNavRail) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    navItems.forEach { item ->
                        val label = stringResource(id = item.labelRes)
                        NavigationRailItem(
                            selected = currentDestination == item.destination,
                            onClick = { studioViewModel.selectDestination(item.destination) },
                            icon = { Icon(item.icon, contentDescription = label) },
                            label = { Text(label) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StudioBannerHeader()

                    when (currentDestination) {
                        TopDestination.Secretary -> {
                            SecretaryScreen(
                                personas = studioViewModel.personas,
                                selectedPersona = selectedPersona,
                                isSpeaking = isSpeaking,
                                isListening = isListening,
                                rmsLevel = rmsLevel,
                                isSpeechRecognizerAvailable = speechRecognizer.isAvailable,
                                sttPartialTranscript = sttPartialTranscript,
                                sttError = sttError,
                                mode = secretaryMode,
                                speechStyle = speechStyle,
                                chatMessages = chatMessages,
                                chatInput = chatInput,
                                chatStreamingText = chatStreamingText,
                                chatLoading = chatLoading,
                                chatError = chatError,
                                isVideoStageOpen = isVideoStageOpen,
                                isAutoVoiceEnabled = isAutoVoiceEnabled,
                                callScenario = callScenario,
                                callerName = callerName,
                                callerPhone = callerPhone,
                                callNoteInput = callNoteInput,
                                callNotes = callNotes,
                                personalQuestion = personalQuestion,
                                newTaskInput = newTaskInput,
                                tasks = tasks,
                                aiState = secretaryAiState,
                                advisoryDomain = advisoryDomain,
                                advisoryProfileInput = advisoryProfileInput,
                                advisoryGoalInput = advisoryGoalInput,
                                advisoryAiState = advisoryAiState,
                                onSelectPersona = studioViewModel::selectPersona,
                                onSpeakText = { text, persona -> voicePlayer.speak(text, persona) },
                                onStopSpeaking = { voicePlayer.stop() },
                                onStartVoiceListening = { speechRecognizer.startListening() },
                                onStopVoiceListening = { speechRecognizer.stopListening() },
                                onVoiceTranscriptFallback = { transcript ->
                                    studioViewModel.sendChatMessage(overrideText = transcript) { reply, persona ->
                                        voicePlayer.speak(reply, persona)
                                    }
                                },
                                onSetSttError = studioViewModel::onSttError,
                                onModeChange = studioViewModel::selectSecretaryMode,
                                onSpeechStyleChange = studioViewModel::updateSpeechStyle,
                                onChatInputChange = studioViewModel::updateChatInput,
                                onSendChatMessage = {
                                    studioViewModel.sendChatMessage { reply, persona ->
                                        voicePlayer.speak(reply, persona)
                                    }
                                },
                                onClearChat = studioViewModel::clearChatHistory,
                                onSetVideoStageOpen = studioViewModel::setVideoStageOpen,
                                onToggleAutoVoice = studioViewModel::toggleAutoVoice,
                                onCallScenarioChange = studioViewModel::updateCallScenario,
                                onCallerNameChange = studioViewModel::updateCallerName,
                                onCallerPhoneChange = studioViewModel::updateCallerPhone,
                                onCallNoteChange = studioViewModel::updateCallNoteInput,
                                onGenerateGreeting = studioViewModel::generateCallGreetingScript,
                                onLogCallAndDraftReply = studioViewModel::logCallAndDraftReply,
                                onPersonalQuestionChange = studioViewModel::updatePersonalQuestion,
                                onAskPersonalSecretary = studioViewModel::askPersonalSecretary,
                                onNewTaskChange = studioViewModel::updateNewTaskInput,
                                onAddTask = studioViewModel::addTask,
                                onToggleTask = studioViewModel::toggleTask,
                                onDeleteTask = studioViewModel::deleteTask,
                                onSelectAdvisoryDomain = studioViewModel::selectAdvisoryDomain,
                                onAdvisoryProfileChange = studioViewModel::updateAdvisoryProfile,
                                onAdvisoryGoalChange = studioViewModel::updateAdvisoryGoal,
                                onApplyAdvisoryPreset = studioViewModel::applyAdvisoryPreset,
                                onGenerateAdvisoryPlan = {
                                    studioViewModel.generateAdvisoryPlan { plan, persona ->
                                        voicePlayer.speak(plan, persona)
                                    }
                                }
                            )
                        }

                        TopDestination.DevHub -> {
                            DevHubScreen(
                                devHubMode = devHubMode,
                                username = gitHubUsername,
                                token = gitHubToken,
                                gitHubState = gitHubState,
                                codingApps = codingApps,
                                termuxCommands = studioViewModel.termuxCommands,
                                upgradeRequestInput = upgradeRequestInput,
                                upgradeProposals = upgradeProposals,
                                upgradeProposalLoading = upgradeProposalLoading,
                                upgradeError = upgradeError,
                                capabilityRunInput = capabilityRunInput,
                                capabilityRunState = capabilityRunState,
                                onSelectDevHubMode = studioViewModel::selectDevHubMode,
                                onUsernameChange = studioViewModel::updateGitHubUsername,
                                onTokenChange = studioViewModel::updateGitHubToken,
                                onLoadGitHub = studioViewModel::loadGitHubActivity,
                                onRefreshCodingApps = { studioViewModel.refreshCodingApps(context) },
                                onAskAiForDevScript = { prompt ->
                                    studioViewModel.sendPromptToGeminiStudio(
                                        "GitHub & Termux Coding Assistant",
                                        prompt
                                    )
                                },
                                onUpgradeRequestChange = studioViewModel::updateUpgradeRequestInput,
                                onRequestUpgradeProposal = studioViewModel::requestUpgradeProposal,
                                onApproveProposal = studioViewModel::approveUpgradeProposal,
                                onRejectProposal = studioViewModel::rejectUpgradeProposal,
                                onCapabilityRunInputChange = studioViewModel::updateCapabilityRunInput,
                                onRunApprovedCapability = studioViewModel::runApprovedCapability
                            )
                        }

                        TopDestination.Studio -> {
                            CreativeStudioScreen(
                                section = studioSection,
                                personas = studioViewModel.personas,
                                selectedPersona = selectedPersona,
                                charName = charName,
                                charAge = charAge,
                                charStyle = charStyle,
                                charCategory = charCategory,
                                tryOnModel = tryOnModel,
                                tryOnClothing = tryOnClothing,
                                bridalGownStyle = bridalGownStyle,
                                groomSuitStyle = groomSuitStyle,
                                sceneType = sceneType,
                                lightingSetup = lightingSetup,
                                onSelectPersona = studioViewModel::selectPersona,
                                onSelectSection = studioViewModel::selectStudioSection,
                                onCharNameChange = studioViewModel::updateCharName,
                                onCharAgeChange = studioViewModel::updateCharAge,
                                onCharStyleChange = studioViewModel::updateCharStyle,
                                onCharCategoryChange = studioViewModel::updateCharCategory,
                                onTryOnModelChange = studioViewModel::updateTryOnModel,
                                onTryOnClothingChange = studioViewModel::updateTryOnClothing,
                                onBridalGownChange = studioViewModel::updateBridalGownStyle,
                                onGroomSuitChange = studioViewModel::updateGroomSuitStyle,
                                onSceneTypeChange = studioViewModel::updateSceneType,
                                onLightingSetupChange = studioViewModel::updateLightingSetup,
                                onSendPromptToAi = studioViewModel::sendPromptToGeminiStudio
                            )
                        }

                        TopDestination.GeminiAi -> {
                            GeminiStudioScreen(
                                selectedModule = aiModuleSelection,
                                prompt = aiPrompt,
                                aiState = studioAiState,
                                onModuleSelected = studioViewModel::updateAiModuleSelection,
                                onPromptChange = studioViewModel::updateAiPrompt,
                                onGenerateClick = studioViewModel::generateStudioAiConcept
                            )
                        }
                    }
                }
            }
        }
    }
}
