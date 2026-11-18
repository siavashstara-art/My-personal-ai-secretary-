package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AiCodePatchSuggestion
import com.example.model.AiUiState
import com.example.model.AssistantPersona
import com.example.model.DeployTemplateType
import com.example.model.GitHubPushState
import com.example.model.ImportedProjectWorkspace

@Composable
fun ZipProjectWorkspaceSection(
    selectedPersona: AssistantPersona,
    savedWorkspaces: List<ImportedProjectWorkspace>,
    activeWorkspace: ImportedProjectWorkspace?,
    selectedFilePath: String,
    openFileContent: String,
    isOpenFileModified: Boolean,
    newFilePathInput: String,
    codeAiInstruction: String,
    projectAuditState: AiUiState,
    codePatchLoading: Boolean,
    codePatchSuggestion: AiCodePatchSuggestion?,
    errorLogInput: String,
    workspaceStatusMessage: String?,
    gitHubUsername: String,
    gitHubToken: String,
    gitHubTargetRepo: String,
    gitHubBranch: String,
    gitHubCommitMessage: String,
    gitHubPushPrivate: Boolean,
    gitHubPushState: GitHubPushState,
    onImportZipUri: (android.net.Uri) -> Unit,
    onExportZipUri: (android.net.Uri) -> Unit,
    onSelectWorkspace: (ImportedProjectWorkspace) -> Unit,
    onDeleteWorkspace: (String) -> Unit,
    onOpenFile: (String) -> Unit,
    onOpenFileContentChange: (String) -> Unit,
    onSaveOpenFile: () -> Unit,
    onNewFilePathChange: (String) -> Unit,
    onCreateNewFile: () -> Unit,
    onCodeAiInstructionChange: (String) -> Unit,
    onErrorLogChange: (String) -> Unit,
    onAuditProject: () -> Unit,
    onGenerateCodePatch: () -> Unit,
    onFixWorkspaceError: () -> Unit,
    onApplyCodePatch: () -> Unit,
    onInsertDeployTemplate: (DeployTemplateType) -> Unit,
    onGitHubTargetRepoChange: (String) -> Unit,
    onGitHubBranchChange: (String) -> Unit,
    onGitHubCommitMessageChange: (String) -> Unit,
    onToggleGitHubPushPrivate: () -> Unit,
    onPushToGitHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showNewFileForm by remember { mutableStateOf(false) }
    var fileFilterQuery by remember { mutableStateOf("") }

    val openZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportZipUri(uri)
        }
    }

    val exportZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            onExportZipUri(uri)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. ZIP Importer & Workspace Switcher (Zero-permission SAF)
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
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = "ZIP Workspace",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "استخراج ZIP و تکمیل برنامه‌های نیمه‌کاره",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusPill(
                    label = "ZERO-PERM SAF",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Text(
                text = "فایل زیپ پروژه‌های نیمه‌کاره گوگل AI Studio، اندروید یا وب را انتخاب کن تا واقعی استخراج شود، ${selectedPersona.persianName} تمام فایل‌ها را ببیند و کدها را تکمیل کند، سپس به گیت‌هاب / فایربیس / ZIP جدید بفرستد.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        openZipLauncher.launch(
                            arrayOf(
                                "application/zip",
                                "application/x-zip-compressed",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("import_project_zip_button")
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("انتخاب و استخراج ZIP")
                }

                if (activeWorkspace != null) {
                    OutlinedButton(
                        onClick = {
                            val defaultName = "${activeWorkspace.displayName}_completed.zip"
                            exportZipLauncher.launch(defaultName)
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("export_project_zip_button")
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Export ZIP", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("خروجی ZIP")
                    }
                }
            }

            if (workspaceStatusMessage != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = workspaceStatusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                }
            }

            if (savedWorkspaces.isNotEmpty()) {
                Text(
                    text = "پروژه‌های استخراج‌شده روی گوشی:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savedWorkspaces.forEach { ws ->
                        val selected = activeWorkspace?.projectId == ws.projectId
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 10.dp, end = 4.dp)
                            ) {
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectWorkspace(ws) },
                                    label = { Text("${ws.displayName} (${ws.fileCount})") },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                )
                                IconButton(onClick = { onDeleteWorkspace(ws.projectId) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete workspace",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeWorkspace == null) {
            return@Column
        }

        // 2. Extracted File Explorer & Real Code Editor
        StudioCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فایل‌های استخراج‌شده (${activeWorkspace.fileCount} فایل)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(
                    onClick = { showNewFileForm = !showNewFileForm },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("فایل جدید")
                }
            }

            if (showNewFileForm) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newFilePathInput,
                        onValueChange = onNewFilePathChange,
                        label = { Text("مسیر فایل (مثلاً src/components/NewPanel.tsx)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            onCreateNewFile()
                            showNewFileForm = false
                        },
                        enabled = newFilePathInput.isNotBlank(),
                        modifier = Modifier.heightIn(min = 52.dp)
                    ) {
                        Text("ساخت")
                    }
                }
            }

            OutlinedTextField(
                value = fileFilterQuery,
                onValueChange = { fileFilterQuery = it },
                label = { Text("جستجو در بین فایل‌های پروژه (مثلاً App.tsx یا MainActivity)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val filteredFiles = remember(activeWorkspace.files, fileFilterQuery) {
                if (fileFilterQuery.isBlank()) activeWorkspace.files
                else activeWorkspace.files.filter { it.relativePath.contains(fileFilterQuery, ignoreCase = true) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredFiles.take(30).forEach { entry ->
                    FilterChip(
                        selected = selectedFilePath == entry.relativePath,
                        onClick = { if (entry.isEditableText) onOpenFile(entry.relativePath) },
                        enabled = entry.isEditableText,
                        label = {
                            Text(
                                text = entry.relativePath.substringAfterLast('/').ifBlank { entry.relativePath },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            if (selectedFilePath.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedFilePath,
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        IconButton(
                            onClick = {
                                AndroidIntentActions.copyToClipboard(context, selectedFilePath, openFileContent)
                            }
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy file code")
                        }
                        Button(
                            onClick = onSaveOpenFile,
                            enabled = isOpenFileModified,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("save_workspace_file_button")
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isOpenFileModified) "ذخیره فایل*" else "ذخیره‌شده")
                        }
                    }
                }

                OutlinedTextField(
                    value = openFileContent,
                    onValueChange = onOpenFileContentChange,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    minLines = 7,
                    maxLines = 16,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workspace_code_editor")
                )
            }
        }

        // 3. AI Code Inspection & Auto-Completion Engine
        StudioCard(borderColor = MaterialTheme.colorScheme.secondary) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Code,
                    contentDescription = "AI Code Completion",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "کدنویسی و تکمیل خودکار پروژه توسط ${selectedPersona.persianName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedButton(
                onClick = onAuditProject,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("audit_workspace_button")
            ) {
                Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("تحلیل کل پروژه نیمه‌کاره و لیست کارهای باقی‌مانده")
            }

            when (projectAuditState) {
                AiUiState.Idle -> Unit
                AiUiState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("در حال بررسی ساختار فایل‌ها و کدهای پروژه...")
                    }
                }
                is AiUiState.Streaming, is AiUiState.Success -> {
                    val text = if (projectAuditState is AiUiState.Streaming) projectAuditState.text else (projectAuditState as AiUiState.Success).text
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        )
                    }
                }
                is AiUiState.Error -> {
                    Text(text = projectAuditState.message, color = MaterialTheme.colorScheme.error)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            OutlinedTextField(
                value = codeAiInstruction,
                onValueChange = onCodeAiInstructionChange,
                label = { Text("بگو این فایل یا بخش پروژه چطور تکمیل/اصلاح شود؟") },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("code_ai_instruction_input")
            )

            Button(
                onClick = onGenerateCodePatch,
                enabled = !codePatchLoading && codeAiInstruction.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("generate_ai_code_patch_button")
            ) {
                if (codePatchLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(if (codePatchLoading) "در حال نوشتن کد کامل..." else "نوشتن و تکمیل کد با هوش مصنوعی")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            OutlinedTextField(
                value = errorLogInput,
                onValueChange = onErrorLogChange,
                label = { Text("لاگ ارور، خطای بیلد یا توضیح باگ پروژه را پیست کن...") },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("workspace_error_log_input")
            )

            OutlinedButton(
                onClick = onFixWorkspaceError,
                enabled = !codePatchLoading && errorLogInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("fix_workspace_error_button")
            ) {
                Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("شکار علت باگ و تولید پچ رفع ارور")
            }

            if (codePatchSuggestion != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "پیشنهاد آماده اعمال روی: ${codePatchSuggestion.targetFilePath}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = codePatchSuggestion.summaryPersian,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!codePatchSuggestion.extraFilePath.isNullOrBlank()) {
                            Text(
                                text = "فایل همراه: ${codePatchSuggestion.extraFilePath}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = codePatchSuggestion.updatedFileContent.take(650) +
                                if (codePatchSuggestion.updatedFileContent.length > 650) "\n..." else "",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onApplyCodePatch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("apply_ai_code_patch_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("اعمال مستقیم کد روی فایل پروژه")
                        }
                    }
                }
            }
        }

        // 4. Deploy Workflows (GitHub Actions / Firebase / AI Studio Pages) + Real GitHub Push
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
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = "Deploy & GitHub Push",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "دپلو خودکار و Push مستقیم به GitHub / Firebase",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusPill(
                    label = "REAL PUSH",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "ابتدا در صورت نیاز فایل‌های آماده دپلو را به پروژه اضافه کن، سپس با GitHub Token پروژه را مستقیم در مخزن جدید یا موجود گیت‌هاب پوش و کامیت کن تا ورک‌فلو بیلد/دپلو اجرا شود.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeployTemplateType.entries.forEach { tpl ->
                    OutlinedButton(
                        onClick = { onInsertDeployTemplate(tpl) },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("deploy_template_${tpl.name}")
                    ) {
                        Text(tpl.persianLabel)
                    }
                }
            }

            OutlinedTextField(
                value = gitHubTargetRepo,
                onValueChange = onGitHubTargetRepoChange,
                label = { Text("نام مخزن هدف در GitHub (@$gitHubUsername/repo)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_push_repo_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = gitHubBranch,
                    onValueChange = onGitHubBranchChange,
                    label = { Text("شاخه (Branch)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text("خصوصی", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(4.dp))
                    Switch(checked = gitHubPushPrivate, onCheckedChange = { onToggleGitHubPushPrivate() })
                }
            }

            OutlinedTextField(
                value = gitHubCommitMessage,
                onValueChange = onGitHubCommitMessageChange,
                label = { Text("پیام Commit") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (gitHubToken.isBlank()) {
                Text(
                    text = "توجه: برای ساخت مخزن یا Push واقعی، در تب «گیت‌هاب و ترموکس» Personal Access Token با دسترسی repo را وارد کنید.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onPushToGitHub,
                enabled = gitHubPushState !is GitHubPushState.Running && gitHubTargetRepo.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("push_workspace_github_button")
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("ساخت مخزن و Push واقعی تمام فایل‌ها به GitHub")
            }

            when (gitHubPushState) {
                GitHubPushState.Idle -> Unit
                is GitHubPushState.Running -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(gitHubPushState.progressText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is GitHubPushState.Success -> {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = gitHubPushState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { AndroidIntentActions.openUrl(context, gitHubPushState.repoUrl) }) {
                                Icon(Icons.Filled.Launch, contentDescription = "Open Repo on GitHub")
                            }
                        }
                    }
                }
                is GitHubPushState.Error -> {
                    Text(
                        text = gitHubPushState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
