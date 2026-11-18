package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AssistantPersona
import com.example.model.CompositeEmotion
import com.example.model.MemoryTier
import com.example.model.PersonaMindState
import kotlin.math.roundToInt

@Composable
fun MindAndEmotionSection(
    selectedPersona: AssistantPersona,
    mindState: PersonaMindState,
    healingLoading: Boolean,
    decisionLoading: Boolean,
    neuroSyncLoading: Boolean,
    isSpeaking: Boolean,
    onTriggerEmotion: (CompositeEmotion) -> Unit,
    onRunSelfHealing: () -> Unit,
    onConsolidateMemory: () -> Unit,
    onAddManualMemory: (MemoryTier, String, String) -> Unit,
    onPromoteMemory: (Long) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onEvaluateDecision: (String) -> Unit,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMemoryTier by remember { mutableStateOf<MemoryTier?>(null) }
    var showAddMemoryForm by remember { mutableStateOf(false) }
    var manualMemTier by remember { mutableStateOf(MemoryTier.LongTerm) }
    var manualMemTitle by remember { mutableStateOf("") }
    var manualMemContent by remember { mutableStateOf("") }
    var decisionDilemmaInput by remember { mutableStateOf("") }

    val neuro = mindState.neuro

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Composite Emotion, Willpower & Self-Growth Card
        StudioCard(borderColor = MaterialTheme.colorScheme.primary) {
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
                        imageVector = Icons.Filled.SelfImprovement,
                        contentDescription = "Mind & Willpower",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "اراده، خودترمیمی و تکامل (${selectedPersona.persianName})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = neuro.activeEmotion.persianLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                StatusPill(
                    label = if (neuroSyncLoading) "همگام‌سازی عصبی..." else "اراده ${(neuro.willpower * 100).roundToInt()}%",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = neuro.activeEmotion.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            NeuroProgressRow(
                label = "ذخیره اراده و پایداری تصمیم (Willpower)",
                value = neuro.willpower,
                trackColor = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GrowthMetricChip("هوش تحلیلی", neuro.cognitiveIntel, Modifier.weight(1f))
                GrowthMetricChip("بلوغ عاطفی", neuro.emotionalMaturity, Modifier.weight(1f))
                GrowthMetricChip("مهارت تعامل", neuro.interactionSkill, Modifier.weight(1f))
                GrowthMetricChip("پیوند عاطفی", neuro.trustBondLevel, Modifier.weight(1f))
            }

            if (neuro.innerFeelingWhisper.isNotBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "حس درونی لحظه‌ای: «${neuro.innerFeelingWhisper}»",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = neuro.lastHealingNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
            }

            Button(
                onClick = onRunSelfHealing,
                enabled = !healingLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("run_self_healing_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                if (healingLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                } else {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (healingLoading) "در حال اجرای چرخه خودترمیمی و ارتقا..."
                    else "خودترمیمی عاطفی و ارتقای هوش و تعامل"
                )
            }
        }

        // 2. Digital Neuro-Endocrine & 7 Composite Emotions Network
        StudioCard {
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
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Neuro-Hormones",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "شبیه‌ساز عصبی هورمون‌ها و احساسات ترکیبی",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusPill(
                    label = "DIGITAL SIM",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Text(
                text = "این متغیرهای عصبی مستقیماً لحن گفتار Gemini و زیربم/سرعت صدای TTS شخصیت را در لحظه تغییر می‌دهند (شبیه‌سازی نرم‌افزاری، نه شیمی زیستی).",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            NeuroProgressRow("سروتونین (آرامش و ثبات خلق)", neuro.serotonin, MaterialTheme.colorScheme.tertiary)
            NeuroProgressRow("دوپامین (انگیزه، شوق و هیجان)", neuro.dopamine, MaterialTheme.colorScheme.primary)
            NeuroProgressRow("اندورفین (لذت عاطفی و نشاط)", neuro.endorphins, MaterialTheme.colorScheme.secondary)
            NeuroProgressRow("بار تنش و هشیاری محافظتی", neuro.stressLoad, MaterialTheme.colorScheme.error)

            Text(
                text = "تحریک و تجربه مستقیم احساس ترکیبی (روی لحن و صدا اثر می‌گذارد):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompositeEmotion.entries.forEach { emotion ->
                    FilterChip(
                        selected = neuro.activeEmotion == emotion,
                        onClick = { onTriggerEmotion(emotion) },
                        label = { Text(emotion.persianLabel) },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("emotion_chip_${emotion.name}")
                    )
                }
            }
        }

        // 3. Autonomous Willpower & Decision Engine
        StudioCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Gavel,
                    contentDescription = "Decision Engine",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "موتور اراده و تصمیم‌گیری قاطع (${selectedPersona.persianName})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedTextField(
                value = decisionDilemmaInput,
                onValueChange = { decisionDilemmaInput = it },
                label = { Text("یک دوراهی یا تصمیم سخت بنویس تا با اراده و حافظه نظر قاطع بدهد...") },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("decision_dilemma_input")
            )

            Button(
                onClick = { onEvaluateDecision(decisionDilemmaInput) },
                enabled = !decisionLoading && decisionDilemmaInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("evaluate_decision_button")
            ) {
                if (decisionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(if (decisionLoading) "در حال سنجش ریسک و اراده..." else "تصمیم‌گیری قاطع و استدلال")
            }

            if (mindState.decisions.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                mindState.decisions.take(5).forEach { record ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusPill(
                                    label = "قطعیت اراده ${record.convictionPercent}%",
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                IconButton(
                                    onClick = {
                                        if (isSpeaking) onStopSpeaking()
                                        else onSpeakText("${record.chosenAction}. ${record.reasoning}")
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                        contentDescription = "Speak Decision"
                                    )
                                }
                            }
                            Text(
                                text = "موضوع: ${record.dilemma}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = record.chosenAction,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = record.reasoning,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 4. Tri-Tier Memory Architecture (Short-Term, Mid-Term, Long-Term)
        StudioCard {
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
                        imageVector = Icons.Filled.Memory,
                        contentDescription = "Tri-Tier Memory",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "حافظه سه‌لایه (کوتاه‌مدت، میان‌مدت، بلندمدت)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusPill(
                    label = "${mindState.memories.size} خاطره",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMemoryTier == null,
                    onClick = { selectedMemoryTier = null },
                    label = { Text("همه (${mindState.memories.size})") },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
                MemoryTier.entries.forEach { tier ->
                    val count = mindState.memories.count { it.tier == tier }
                    FilterChip(
                        selected = selectedMemoryTier == tier,
                        onClick = { selectedMemoryTier = tier },
                        label = { Text("${tier.persianLabel} ($count)") },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("memory_filter_${tier.name}")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConsolidateMemory,
                    enabled = !neuroSyncLoading,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("consolidate_memory_button")
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("تحکیم مکالمه")
                }
                OutlinedButton(
                    onClick = { showAddMemoryForm = !showAddMemoryForm },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("toggle_add_memory_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ثبت خاطره دستی")
                }
            }

            AnimatedVisibility(visible = showAddMemoryForm) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MemoryTier.entries.forEach { tier ->
                            FilterChip(
                                selected = manualMemTier == tier,
                                onClick = { manualMemTier = tier },
                                label = { Text(tier.persianLabel) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                    OutlinedTextField(
                        value = manualMemTitle,
                        onValueChange = { manualMemTitle = it },
                        label = { Text("عنوان خاطره یا اصل مهم") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_memory_title_input")
                    )
                    OutlinedTextField(
                        value = manualMemContent,
                        onValueChange = { manualMemContent = it },
                        label = { Text("جزئیات خاطره که شخصیت همیشه یادش بماند...") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_memory_content_input")
                    )
                    Button(
                        onClick = {
                            onAddManualMemory(manualMemTier, manualMemTitle, manualMemContent)
                            manualMemTitle = ""
                            manualMemContent = ""
                            showAddMemoryForm = false
                        },
                        enabled = manualMemTitle.isNotBlank() && manualMemContent.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("save_manual_memory_button")
                    ) {
                        Text("ذخیره در حافظه ${selectedPersona.persianName}")
                    }
                }
            }

            val visibleMemories = if (selectedMemoryTier == null) {
                mindState.memories
            } else {
                mindState.memories.filter { it.tier == selectedMemoryTier }
            }

            visibleMemories.forEach { entry ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusPill(
                                label = "${entry.tier.persianLabel} • ${entry.emotionTag}",
                                containerColor = when (entry.tier) {
                                    MemoryTier.LongTerm -> MaterialTheme.colorScheme.primaryContainer
                                    MemoryTier.MidTerm -> MaterialTheme.colorScheme.secondaryContainer
                                    MemoryTier.ShortTerm -> MaterialTheme.colorScheme.tertiaryContainer
                                },
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                            Row {
                                if (entry.tier != MemoryTier.LongTerm) {
                                    IconButton(onClick = { onPromoteMemory(entry.id) }) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowUpward,
                                            contentDescription = "Promote to Long-Term Memory",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                IconButton(onClick = { onDeleteMemory(entry.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete Memory",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = entry.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NeuroProgressRow(
    label: String,
    value: Float,
    trackColor: Color
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "neuro_progress"
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${(animatedValue * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = trackColor
            )
        }
        LinearProgressIndicator(
            progress = { animatedValue },
            color = trackColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun GrowthMetricChip(
    label: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score/100",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
