package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.AdvisoryDomain
import com.example.model.AiUiState
import com.example.model.AssistantPersona
import com.example.model.CallLogNote
import com.example.model.ChatMessage
import com.example.model.SecretaryMode
import com.example.model.SecretaryTask
import com.example.model.SpeechStyleMode
import com.example.ui.components.AdvisoryHubSection
import com.example.ui.components.AndroidIntentActions
import com.example.ui.components.StatusPill
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioDropdownSelector
import com.example.ui.components.VideoChatStage
import androidx.compose.material.icons.filled.Psychology

@Composable
fun SecretaryScreen(
    personas: List<AssistantPersona>,
    selectedPersona: AssistantPersona,
    isSpeaking: Boolean,
    isListening: Boolean,
    rmsLevel: Float,
    isSpeechRecognizerAvailable: Boolean,
    sttPartialTranscript: String,
    sttError: String?,
    mode: SecretaryMode,
    speechStyle: SpeechStyleMode,
    chatMessages: List<ChatMessage>,
    chatInput: String,
    chatStreamingText: String?,
    chatLoading: Boolean,
    chatError: String?,
    isVideoStageOpen: Boolean,
    isAutoVoiceEnabled: Boolean,
    callScenario: String,
    callerName: String,
    callerPhone: String,
    callNoteInput: String,
    callNotes: List<CallLogNote>,
    personalQuestion: String,
    newTaskInput: String,
    tasks: List<SecretaryTask>,
    aiState: AiUiState,
    advisoryDomain: AdvisoryDomain,
    advisoryProfileInput: String,
    advisoryGoalInput: String,
    advisoryAiState: AiUiState,
    onSelectPersona: (AssistantPersona) -> Unit,
    onSpeakText: (String, AssistantPersona) -> Unit,
    onStopSpeaking: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onVoiceTranscriptFallback: (String) -> Unit,
    onSetSttError: (String?) -> Unit,
    onModeChange: (SecretaryMode) -> Unit,
    onSpeechStyleChange: (SpeechStyleMode) -> Unit,
    onChatInputChange: (String) -> Unit,
    onSendChatMessage: () -> Unit,
    onClearChat: () -> Unit,
    onSetVideoStageOpen: (Boolean) -> Unit,
    onToggleAutoVoice: () -> Unit,
    onCallScenarioChange: (String) -> Unit,
    onCallerNameChange: (String) -> Unit,
    onCallerPhoneChange: (String) -> Unit,
    onCallNoteChange: (String) -> Unit,
    onGenerateGreeting: () -> Unit,
    onLogCallAndDraftReply: () -> Unit,
    onPersonalQuestionChange: (String) -> Unit,
    onAskPersonalSecretary: () -> Unit,
    onNewTaskChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onToggleTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onSelectAdvisoryDomain: (AdvisoryDomain) -> Unit,
    onAdvisoryProfileChange: (String) -> Unit,
    onAdvisoryGoalChange: (String) -> Unit,
    onApplyAdvisoryPreset: (String, String) -> Unit,
    onGenerateAdvisoryPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pendingMicStart by remember { mutableStateOf(false) }

    val fallbackSpeechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val transcript = matches?.firstOrNull().orEmpty().trim()
            if (transcript.isNotEmpty()) {
                onVoiceTranscriptFallback(transcript)
            }
        }
    }

    val launchVoiceInput = remember(isSpeechRecognizerAvailable, isListening) {
        {
            if (isListening) {
                onStopVoiceListening()
            } else if (isSpeechRecognizerAvailable) {
                onStartVoiceListening()
            } else {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "صحبت کنید تا به چت تبدیل شود...")
                }
                try {
                    fallbackSpeechIntentLauncher.launch(intent)
                } catch (_: Exception) {
                    onSetSttError("سرویس تشخیص گفتار سیستم روی این دستگاه در دسترس نیست؛ می‌توانید متن تایپ کنید.")
                }
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingMicStart) {
            pendingMicStart = false
            launchVoiceInput()
        } else if (!granted) {
            pendingMicStart = false
            onSetSttError("دسترسی میکروفون داده نشد؛ برای ویس به چت دسترسی میکروفون لازم است.")
        }
    }

    val requestOrToggleMic = {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMic) {
            launchVoiceInput()
        } else {
            pendingMicStart = true
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 3 Female Personas Selector (Ages 27-37, 3 Appearances/Silhouettes, 3 Voices, 3 Distinct Archetypes)
        PersonaCompanionSection(
            personas = personas,
            selectedPersona = selectedPersona,
            isSpeaking = isSpeaking,
            onSelectPersona = onSelectPersona,
            onSpeakSample = { persona -> onSpeakText(persona.sampleGreeting, persona) },
            onStopSpeaking = onStopSpeaking
        )

        // Tone / Speech Style selector (Colloquial Street-Smart Adult vs Warm Balanced)
        SpeechStyleCard(
            speechStyle = speechStyle,
            onSpeechStyleChange = onSpeechStyleChange
        )

        // Mode switcher: Phone Secretary vs Chat/Voice/Video vs Personal Assistant
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = mode == SecretaryMode.CallSecretary,
                onClick = { onModeChange(SecretaryMode.CallSecretary) },
                label = { Text("منشی تلفنی") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.PhoneInTalk,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("secretary_mode_call")
            )
            FilterChip(
                selected = mode == SecretaryMode.ChatAndVideo,
                onClick = { onModeChange(SecretaryMode.ChatAndVideo) },
                label = { Text("چت، ویس و ویدیو") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("secretary_mode_chat_video")
            )
            FilterChip(
                selected = mode == SecretaryMode.PersonalAssistant,
                onClick = { onModeChange(SecretaryMode.PersonalAssistant) },
                label = { Text("منشی شخصی") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("secretary_mode_personal")
            )
            FilterChip(
                selected = mode == SecretaryMode.AdvisoryHub,
                onClick = { onModeChange(SecretaryMode.AdvisoryHub) },
                label = { Text("مشاوره و برنامه‌ریزی") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("secretary_mode_advisory")
            )
        }

        when (mode) {
            SecretaryMode.CallSecretary -> {
                StudioCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "منشی تلفنی هوشمند (${selectedPersona.persianName})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StatusPill(
                            label = "PLAY COMPLIANT",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Text(
                        text = "تولید متن منشی تلفنی، پاسخ پیامکی تماس‌های از دست‌رفته، و پیگیری تماس‌ها با لحن ${selectedPersona.persianName} (${selectedPersona.archetypeTag}) و تایید مستقیم شما در شماره‌گیر و پیامک سیستم.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    StudioDropdownSelector(
                        label = "وضعیت فعلی تماس (Call Scenario)",
                        selectedValue = callScenario,
                        options = listOf(
                            "در حال کدنویسی / جلسه فنی (Busy Coding)",
                            "جلسه کاری مهم (In a Meeting)",
                            "ساعات استراحت / غیرکاری (After Hours)",
                            "رانندگی یا عدم دسترسی موقت (Driving / Unavailable)"
                        ),
                        onOptionSelected = onCallScenarioChange,
                        testTag = "call_scenario_dropdown"
                    )

                    Button(
                        onClick = onGenerateGreeting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("generate_call_greeting_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ساخت متن منشی تلفنی با صدای ${selectedPersona.persianName}")
                    }
                }

                StudioCard {
                    Text(
                        text = "ثبت تماس و پیشنهاد پاسخ خودکار",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = callerName,
                        onValueChange = onCallerNameChange,
                        label = { Text("نام تماس‌گیرنده") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("caller_name_input")
                    )

                    OutlinedTextField(
                        value = callerPhone,
                        onValueChange = onCallerPhoneChange,
                        label = { Text("شماره تلفن (برای شماره‌گیری یا پیامک)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("caller_phone_input")
                    )

                    OutlinedTextField(
                        value = callNoteInput,
                        onValueChange = onCallNoteChange,
                        label = { Text("موضوع تماس یا پیام صوتی") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("call_note_input")
                    )

                    Button(
                        onClick = onLogCallAndDraftReply,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("log_call_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ثبت در دفترچه منشی و تولید پاسخ با AI")
                    }

                    if (callNotes.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        callNotes.forEach { entry ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${entry.callerNameOrNumber} ${if (entry.phoneNumber.isNotBlank()) "(${entry.phoneNumber})" else ""}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = entry.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = entry.aiReplyDraft,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (entry.phoneNumber.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = { AndroidIntentActions.openDialer(context, entry.phoneNumber) },
                                                modifier = Modifier.heightIn(min = 48.dp)
                                            ) {
                                                Icon(Icons.Filled.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("تماس")
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    AndroidIntentActions.openSmsComposer(
                                                        context,
                                                        entry.phoneNumber,
                                                        entry.aiReplyDraft
                                                    )
                                                },
                                                modifier = Modifier.heightIn(min = 48.dp)
                                            ) {
                                                Icon(Icons.Filled.Sms, contentDescription = "SMS", modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("پیامک")
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                AndroidIntentActions.copyToClipboard(
                                                    context,
                                                    "Reply Draft",
                                                    entry.aiReplyDraft
                                                )
                                            },
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        ) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("کپی")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                SecretaryAiOutputCard(
                    aiState = aiState,
                    selectedPersona = selectedPersona,
                    isSpeaking = isSpeaking,
                    onSpeakText = { text -> onSpeakText(text, selectedPersona) },
                    onStopSpeaking = onStopSpeaking
                )
            }

            SecretaryMode.ChatAndVideo -> {
                ChatAndVideoSection(
                    selectedPersona = selectedPersona,
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    rmsLevel = rmsLevel,
                    isSpeechRecognizerAvailable = isSpeechRecognizerAvailable,
                    sttPartialTranscript = sttPartialTranscript,
                    sttError = sttError,
                    chatMessages = chatMessages,
                    chatInput = chatInput,
                    chatStreamingText = chatStreamingText,
                    chatLoading = chatLoading,
                    chatError = chatError,
                    isVideoStageOpen = isVideoStageOpen,
                    isAutoVoiceEnabled = isAutoVoiceEnabled,
                    onToggleMic = requestOrToggleMic,
                    onSpeakMessage = { msgText -> onSpeakText(msgText, selectedPersona) },
                    onStopSpeaking = onStopSpeaking,
                    onChatInputChange = onChatInputChange,
                    onSendChatMessage = onSendChatMessage,
                    onClearChat = onClearChat,
                    onSetVideoStageOpen = onSetVideoStageOpen,
                    onToggleAutoVoice = onToggleAutoVoice
                )
            }

            SecretaryMode.PersonalAssistant -> {
                StudioCard {
                    Text(
                        text = "منشی شخصی موبایل (${selectedPersona.persianName})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "برنامه‌ریزی روزانه، نگارش پیام‌های صمیمی یا رسمی و پیگیری کارها با همراهی ${selectedPersona.persianName} (${selectedPersona.archetypeTag})، همراه با ثبت مستقیم رویداد در تقویم گوشی.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = personalQuestion,
                        onValueChange = onPersonalQuestionChange,
                        label = { Text("درخواست از منشی شخصی (مثلاً: برنامه امروز من را بچین)") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_secretary_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAskPersonalSecretary,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("ask_personal_secretary_button")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("اجرا با Gemini")
                        }
                        OutlinedButton(
                            onClick = {
                                AndroidIntentActions.openCalendarReminder(
                                    context,
                                    title = "یادآور منشی شخصی",
                                    description = personalQuestion.ifBlank { "بررسی کارهای امروز و گیت‌هاب" }
                                )
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("add_calendar_event_button")
                        ) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Calendar", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("تقویم")
                        }
                    }
                }

                StudioCard {
                    Text(
                        text = "لیست کارهای منشی شخصی",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTaskInput,
                            onValueChange = onNewTaskChange,
                            label = { Text("افزودن کار جدید...") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("new_task_input")
                        )
                        Button(
                            onClick = onAddTask,
                            modifier = Modifier
                                .heightIn(min = 52.dp)
                                .testTag("add_task_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add task")
                        }
                    }

                    tasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onToggleTask(task.id) }) {
                                Icon(
                                    imageVector = if (task.isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = "Toggle task",
                                    tint = if (task.isDone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDeleteTask(task.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete task",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                SecretaryAiOutputCard(
                    aiState = aiState,
                    selectedPersona = selectedPersona,
                    isSpeaking = isSpeaking,
                    onSpeakText = { text -> onSpeakText(text, selectedPersona) },
                    onStopSpeaking = onStopSpeaking
                )
            }

            SecretaryMode.AdvisoryHub -> {
                AdvisoryHubSection(
                    selectedPersona = selectedPersona,
                    advisoryDomain = advisoryDomain,
                    advisoryProfileInput = advisoryProfileInput,
                    advisoryGoalInput = advisoryGoalInput,
                    advisoryAiState = advisoryAiState,
                    isSpeaking = isSpeaking,
                    onSelectDomain = onSelectAdvisoryDomain,
                    onProfileChange = onAdvisoryProfileChange,
                    onGoalChange = onAdvisoryGoalChange,
                    onApplyPreset = onApplyAdvisoryPreset,
                    onGeneratePlan = onGenerateAdvisoryPlan,
                    onSpeakText = { text -> onSpeakText(text, selectedPersona) },
                    onStopSpeaking = onStopSpeaking
                )
            }
        }
    }
}

@Composable
private fun SpeechStyleCard(
    speechStyle: SpeechStyleMode,
    onSpeechStyleChange: (SpeechStyleMode) -> Unit
) {
    StudioCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "لحن تعامل بزرگسال و گفت‌وگو",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تنظیم لحن واقعی و غیررباتیک (عامیانه / کوچه بازاری روزمره یا صمیمی و روان)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                label = "ADULT 18+ PG-13",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = speechStyle == SpeechStyleMode.Colloquial,
                onClick = { onSpeechStyleChange(SpeechStyleMode.Colloquial) },
                label = { Text("کوچه بازاری و عامیانه") },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("speech_style_colloquial")
            )
            FilterChip(
                selected = speechStyle == SpeechStyleMode.Balanced,
                onClick = { onSpeechStyleChange(SpeechStyleMode.Balanced) },
                label = { Text("صمیمی و روان (غیررسمی)") },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("speech_style_balanced")
            )
        }
    }
}

@Composable
private fun ChatAndVideoSection(
    selectedPersona: AssistantPersona,
    isSpeaking: Boolean,
    isListening: Boolean,
    rmsLevel: Float,
    isSpeechRecognizerAvailable: Boolean,
    sttPartialTranscript: String,
    sttError: String?,
    chatMessages: List<ChatMessage>,
    chatInput: String,
    chatStreamingText: String?,
    chatLoading: Boolean,
    chatError: String?,
    isVideoStageOpen: Boolean,
    isAutoVoiceEnabled: Boolean,
    onToggleMic: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onChatInputChange: (String) -> Unit,
    onSendChatMessage: () -> Unit,
    onClearChat: () -> Unit,
    onSetVideoStageOpen: (Boolean) -> Unit,
    onToggleAutoVoice: () -> Unit
) {
    val context = LocalContext.current
    val lastPersonaReply = chatStreamingText?.takeIf { it.isNotBlank() }
        ?: chatMessages.lastOrNull { !it.isUser }?.text.orEmpty()

    // Dual Voice Engines + Video Call Control Card
    StudioCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "موتور دوطرفه ویس + تماس تصویری زنده",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${selectedPersona.persianName} (${selectedPersona.archetypeTag})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onClearChat,
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reset chat history")
            }
        }

        // Engine Status Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusPill(
                label = if (isListening) "STT: در حال گوش دادن..." else if (isSpeechRecognizerAvailable) "ویس به چت (STT): آماده" else "ویس به چت: سیستم",
                containerColor = if (isListening) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isListening) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusPill(
                label = if (isSpeaking) "TTS: در حال پخش صدا" else "چت به ویس (TTS): فعال",
                containerColor = if (isSpeaking) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSpeaking) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isAutoVoiceEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = "Auto voice playback",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "پخش خودکار ویس برای جواب‌ها (TTS)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Switch(
                checked = isAutoVoiceEnabled,
                onCheckedChange = { onToggleAutoVoice() },
                modifier = Modifier.testTag("auto_voice_switch")
            )
        }

        Button(
            onClick = { onSetVideoStageOpen(!isVideoStageOpen) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("toggle_video_call_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isVideoStageOpen) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isVideoStageOpen) Icons.Filled.VideocamOff else Icons.Filled.Videocam,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isVideoStageOpen) "بستن تماس تصویری و ادامه چت متنی/ویس"
                else "شروع تماس تصویری زنده با ${selectedPersona.persianName}"
            )
        }
    }

    if (isVideoStageOpen) {
        VideoChatStage(
            persona = selectedPersona,
            lastPersonaReply = lastPersonaReply,
            partialTranscript = sttPartialTranscript,
            isListening = isListening,
            isSpeaking = isSpeaking,
            rmsLevel = rmsLevel,
            isAutoVoiceEnabled = isAutoVoiceEnabled,
            onToggleMic = onToggleMic,
            onToggleAutoVoice = onToggleAutoVoice,
            onStopSpeaking = onStopSpeaking,
            onEndVideoCall = { onSetVideoStageOpen(false) }
        )
    }

    // Chat Transcript Cards
    StudioCard {
        Text(
            text = "گفتگوی زنده (چت، ویس به چت و چت به ویس)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            chatMessages.forEach { msg ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (msg.isUser) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        if (msg.isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!msg.isUser) {
                                    Image(
                                        painter = painterResource(id = selectedPersona.portraitRes),
                                        contentDescription = msg.personaName,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    text = if (msg.isUser) "شما (سیاوش)" else msg.personaName.ifBlank { selectedPersona.persianName },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        if (isSpeaking) onStopSpeaking() else onSpeakMessage(msg.text)
                                    },
                                    modifier = Modifier.testTag("chat_speak_message_${msg.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                        contentDescription = "Speak message via TTS",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { AndroidIntentActions.copyToClipboard(context, "Chat Message", msg.text) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy chat message",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (chatStreamingText != null && chatStreamingText.isNotBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${selectedPersona.persianName} در حال نوشتن...",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = chatStreamingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else if (chatLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text(
                        text = "${selectedPersona.persianName} الان جواب می‌ده...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (sttPartialTranscript.isNotBlank()) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "در حال شنیدن (ویس به چت): $sttPartialTranscript",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
            }
        }

        if (sttError != null) {
            Text(
                text = sttError,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (chatError != null) {
            Text(
                text = chatError,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        // Voice-to-Chat Mic + Text Input + Send Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledIconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("chat_voice_mic_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isListening) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Filled.Mic else Icons.Filled.MicOff,
                    contentDescription = "Voice to Chat Input"
                )
            }

            OutlinedTextField(
                value = chatInput,
                onValueChange = onChatInputChange,
                label = { Text("پیام متنی یا با میکروفون صحبت کن...") },
                maxLines = 3,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input")
            )

            FilledIconButton(
                onClick = onSendChatMessage,
                enabled = !chatLoading && chatInput.isNotBlank(),
                modifier = Modifier
                    .size(52.dp)
                    .testTag("chat_send_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send Chat Message"
                )
            }
        }
    }
}

@Composable
private fun PersonaCompanionSection(
    personas: List<AssistantPersona>,
    selectedPersona: AssistantPersona,
    isSpeaking: Boolean,
    onSelectPersona: (AssistantPersona) -> Unit,
    onSpeakSample: (AssistantPersona) -> Unit,
    onStopSpeaking: () -> Unit
) {
    StudioCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "انتخاب شخصیت، تیپ رفتاری، چهره و صدا",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            StatusPill(
                label = "۳ تیپ فعال",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            personas.forEach { persona ->
                val isSelected = persona.id == selectedPersona.id
                Surface(
                    modifier = Modifier
                        .width(228.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onSelectPersona(persona) }
                        .testTag("persona_card_${persona.id}"),
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = persona.portraitRes),
                                contentDescription = persona.persianName,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = persona.persianName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "سن: ${persona.age} سال • بانو",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text(
                            text = persona.archetypeTag,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = persona.silhouetteType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = persona.voiceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Active Persona Details + Voice Test
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = selectedPersona.portraitRes),
                        contentDescription = selectedPersona.persianName,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedPersona.persianName} (${selectedPersona.age} ساله) • ${selectedPersona.archetypeTag}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = selectedPersona.appearanceStyle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = selectedPersona.personalitySummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isSpeaking) onStopSpeaking() else onSpeakSample(selectedPersona)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("persona_voice_preview_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.RecordVoiceOver,
                            contentDescription = "Voice Preview",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isSpeaking) "توقف پخش صدا"
                            else "پخش نمونه صدای ${selectedPersona.persianName}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecretaryAiOutputCard(
    aiState: AiUiState,
    selectedPersona: AssistantPersona,
    isSpeaking: Boolean,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit
) {
    val context = LocalContext.current
    when (aiState) {
        AiUiState.Idle -> Unit
        AiUiState.Loading -> {
            StudioCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text(
                        text = "${selectedPersona.persianName} در حال آماده‌سازی پاسخ...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        is AiUiState.Streaming, is AiUiState.Success -> {
            val text = if (aiState is AiUiState.Streaming) aiState.text else (aiState as AiUiState.Success).text
            StudioCard(borderColor = MaterialTheme.colorScheme.secondary) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "پاسخ ${selectedPersona.persianName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Row {
                        IconButton(
                            onClick = { if (isSpeaking) onStopSpeaking() else onSpeakText(text) },
                            modifier = Modifier.testTag("secretary_speak_output_button")
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                contentDescription = "Speak response"
                            )
                        }
                        IconButton(onClick = { AndroidIntentActions.copyToClipboard(context, "Secretary AI", text) }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy response")
                        }
                        IconButton(onClick = { AndroidIntentActions.shareText(context, "Secretary Output", text) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share response")
                        }
                    }
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        is AiUiState.Error -> {
            StudioCard(borderColor = MaterialTheme.colorScheme.error) {
                Text(
                    text = "خطا: ${aiState.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

