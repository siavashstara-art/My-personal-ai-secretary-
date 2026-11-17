package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.model.AiUiState
import com.example.ui.components.AndroidIntentActions
import com.example.ui.components.StatusPill
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioDropdownSelector

@Composable
fun GeminiStudioScreen(
    selectedModule: String,
    prompt: String,
    aiState: AiUiState,
    onModuleSelected: (String) -> Unit,
    onPromptChange: (String) -> Unit,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBusy = aiState is AiUiState.Loading || aiState is AiUiState.Streaming

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StudioCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GEMINI AI INTEGRATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Studio Concept & Prompt Generator",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                StatusPill(
                    label = "GEMINI 3.8 FLASH",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            StudioDropdownSelector(
                label = "Target Studio Module",
                selectedValue = selectedModule,
                options = listOf(
                    "Module A — Fictional Character Studio",
                    "Module B — Virtual Clothing Try-On",
                    "Module C — Fashion & Editorial Studio",
                    "Module D — Wedding & Formal Wear Studio",
                    "Module E — Scene & Story Studio",
                    "Module F — Basic Image & Video Tools",
                    "Personal & Phone Secretary",
                    "GitHub & Termux Coding Assistant",
                    "General Studio Architecture & Technical Evaluation"
                ),
                onOptionSelected = onModuleSelected,
                testTag = "ai_module_dropdown"
            )

            Text(
                text = "Quick Action Presets",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {
                        onPromptChange("Design a complete fictional adult character persona with detailed facial features, clothing, and aesthetic prompt tags.")
                    },
                    label = { Text("Character Persona") },
                    modifier = Modifier.testTag("preset_character_persona")
                )
                AssistChip(
                    onClick = {
                        onPromptChange("Evaluate the technical challenges of preserving lace and fabric textures during virtual clothing try-on.")
                    },
                    label = { Text("Try-On Technical Audit") },
                    modifier = Modifier.testTag("preset_tryon_audit")
                )
                AssistChip(
                    onClick = {
                        onPromptChange("یک متن منشی تلفنی فارسی و انگلیسی برای زمانی که در حال کدنویسی هستم بنویس.")
                    },
                    label = { Text("متن منشی تلفنی") },
                    modifier = Modifier.testTag("preset_call_secretary")
                )
                AssistChip(
                    onClick = {
                        onPromptChange("یک اسکریپت Termux برای بررسی وضعیت ریپوزیتوری‌های GitHub و push خودکار با پیام مرتب بساز.")
                    },
                    label = { Text("اسکریپت Termux/Git") },
                    modifier = Modifier.testTag("preset_termux_git")
                )
            }

            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                label = { Text("Prompt / Inquiry for Gemini Studio Assistant") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_prompt_input")
            )

            Button(
                onClick = onGenerateClick,
                enabled = !isBusy && prompt.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("ai_generate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(if (isBusy) "Generating with Gemini..." else "Generate AI Concept")
            }
        }

        when (aiState) {
            AiUiState.Idle -> Unit
            AiUiState.Loading -> {
                StudioCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Connecting to Firebase AI Logic (Gemini 3.8 Flash)...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            is AiUiState.Streaming, is AiUiState.Success -> {
                val outputText = if (aiState is AiUiState.Streaming) aiState.text else (aiState as AiUiState.Success).text
                StudioCard(borderColor = MaterialTheme.colorScheme.secondary) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Gemini AI Studio Output",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { AndroidIntentActions.copyToClipboard(context, "Gemini Output", outputText) },
                                modifier = Modifier.testTag("ai_copy_output_button")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Output")
                            }
                            IconButton(
                                onClick = { AndroidIntentActions.shareText(context, "Gemini Studio Output", outputText) },
                                modifier = Modifier.testTag("ai_share_output_button")
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share Output")
                            }
                        }
                    }
                    Text(
                        text = outputText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            is AiUiState.Error -> {
                StudioCard(borderColor = MaterialTheme.colorScheme.error) {
                    Text(
                        text = "Generation Notice: ${aiState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
