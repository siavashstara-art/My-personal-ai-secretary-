package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Upgrade
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.model.AiUiState
import com.example.model.ApprovalStatus
import com.example.model.CodingAppInfo
import com.example.model.DevHubMode
import com.example.model.GitHubLoadState
import com.example.model.TermuxCommand
import com.example.model.UpgradeProposal
import com.example.ui.components.AndroidIntentActions
import com.example.ui.components.StatusPill
import com.example.ui.components.StudioCard

@Composable
fun DevHubScreen(
    devHubMode: DevHubMode,
    username: String,
    token: String,
    gitHubState: GitHubLoadState,
    codingApps: List<CodingAppInfo>,
    termuxCommands: List<TermuxCommand>,
    upgradeRequestInput: String,
    upgradeProposals: List<UpgradeProposal>,
    upgradeProposalLoading: Boolean,
    upgradeError: String?,
    capabilityRunInput: String,
    capabilityRunState: AiUiState,
    onSelectDevHubMode: (DevHubMode) -> Unit,
    onUsernameChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onLoadGitHub: () -> Unit,
    onRefreshCodingApps: () -> Unit,
    onAskAiForDevScript: (String) -> Unit,
    onUpgradeRequestChange: (String) -> Unit,
    onRequestUpgradeProposal: () -> Unit,
    onApproveProposal: (Long) -> Unit,
    onRejectProposal: (Long) -> Unit,
    onCapabilityRunInputChange: (String) -> Unit,
    onRunApprovedCapability: (UpgradeProposal) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        onRefreshCodingApps()
        if (gitHubState is GitHubLoadState.Idle) {
            onLoadGitHub()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-mode switcher: GitHub & Termux vs Self-Upgrade Architect
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = devHubMode == DevHubMode.GitHubAndTermux,
                onClick = { onSelectDevHubMode(DevHubMode.GitHubAndTermux) },
                label = { Text("گیت‌هاب و ترموکس") },
                leadingIcon = {
                    Icon(Icons.Filled.Terminal, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("devhub_mode_github")
            )
            FilterChip(
                selected = devHubMode == DevHubMode.SelfUpgradeArchitect,
                onClick = { onSelectDevHubMode(DevHubMode.SelfUpgradeArchitect) },
                label = { Text("ارتقا و قابلیت‌ها") },
                leadingIcon = {
                    Icon(Icons.Filled.Upgrade, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("devhub_mode_upgrade")
            )
        }

        if (devHubMode == DevHubMode.SelfUpgradeArchitect) {
            SelfUpgradeArchitectSection(
                upgradeRequestInput = upgradeRequestInput,
                upgradeProposals = upgradeProposals,
                isLoading = upgradeProposalLoading,
                error = upgradeError,
                capabilityRunInput = capabilityRunInput,
                capabilityRunState = capabilityRunState,
                onUpgradeRequestChange = onUpgradeRequestChange,
                onRequestUpgradeProposal = onRequestUpgradeProposal,
                onApproveProposal = onApproveProposal,
                onRejectProposal = onRejectProposal,
                onCapabilityRunInputChange = onCapabilityRunInputChange,
                onRunApprovedCapability = onRunApprovedCapability
            )
            return@Column
        }

        // 1. Live GitHub Activity Card
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
                        imageVector = Icons.Filled.Code,
                        contentDescription = "GitHub",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "فعالیت گیت‌هاب (Live GitHub)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusPill(
                    label = "REST API",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("نام کاربری GitHub") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_username_input")
            )

            OutlinedTextField(
                value = token,
                onValueChange = onTokenChange,
                label = { Text("Personal Access Token (اختیاری - برای مخازن خصوصی)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_token_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLoadGitHub,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("fetch_github_button")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("همگام‌سازی گیت‌هاب")
                }
                OutlinedButton(
                    onClick = {
                        onAskAiForDevScript(
                            "یک دستور Termux و اسکریپت کامل Git برای همگام‌سازی مخازن کاربر $username و مدیریت شاخه‌ها و کامیت‌ها آماده کن."
                        )
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("ai_git_helper_button")
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "AI Git Helper", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("کمک AI")
                }
            }

            when (gitHubState) {
                GitHubLoadState.Idle -> Unit
                GitHubLoadState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("در حال دریافت فعالیت‌های گیت‌هاب...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is GitHubLoadState.Error -> {
                    Text(
                        text = gitHubState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is GitHubLoadState.Success -> {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = gitHubState.user.name ?: gitHubState.user.login,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "@${gitHubState.user.login} • ${gitHubState.user.publicRepos} مخزن • ${gitHubState.user.followers} دنبال‌کننده",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (gitHubState.user.htmlUrl.isNotBlank()) {
                            IconButton(onClick = { AndroidIntentActions.openUrl(context, gitHubState.user.htmlUrl) }) {
                                Icon(Icons.Filled.Launch, contentDescription = "Open GitHub profile")
                            }
                        }
                    }

                    if (gitHubState.events.isNotEmpty()) {
                        Text(
                            text = "آخرین رویدادها (Recent Activity)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        gitHubState.events.take(5).forEach { event ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = event.type.removeSuffix("Event"),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = event.repo.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = event.createdAt.take(10),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (gitHubState.repos.isNotEmpty()) {
                        Text(
                            text = "مخازن اخیر (Repositories)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        gitHubState.repos.take(5).forEach { repo ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = repo.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${repo.language ?: "Code"} • ★ ${repo.stars} • ${repo.description ?: "بدون توضیح"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                    IconButton(onClick = { AndroidIntentActions.openUrl(context, repo.htmlUrl) }) {
                                        Icon(Icons.Filled.Launch, contentDescription = "Open repository")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Termux & Coding Apps Detector
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
                        imageVector = Icons.Filled.Terminal,
                        contentDescription = "Termux & Coding Apps",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "ترموکس و برنامه‌های کدنویسی من",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onRefreshCodingApps,
                    modifier = Modifier.testTag("refresh_coding_apps_button")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh installed coding apps")
                }
            }

            Text(
                text = "شناسایی و اجرای مستقیم Termux، GitHub و ویرایشگرهای کد نصب‌شده روی گوشی.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            codingApps.forEach { app ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${app.category} • ${if (app.isInstalled) "نصب‌شده" else "نصب‌نشده"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (app.isInstalled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                AndroidIntentActions.launchPackageOrUrl(
                                    context,
                                    app.packageName,
                                    app.fallbackUrl
                                )
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(if (app.isInstalled) "اجرا" else "صفحه اپ")
                        }
                    }
                }
            }
        }

        // 3. Termux & Git Command Deck
        StudioCard {
            Text(
                text = "دستورات آماده Termux & Git",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            termuxCommands.forEach { cmd ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = cmd.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = cmd.description,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = cmd.command,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { AndroidIntentActions.copyToClipboard(context, cmd.title, cmd.command) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy command", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("کپی دستور")
                            }
                            OutlinedButton(
                                onClick = { AndroidIntentActions.shareText(context, cmd.title, cmd.command) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share command", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("ارسال به Termux")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelfUpgradeArchitectSection(
    upgradeRequestInput: String,
    upgradeProposals: List<UpgradeProposal>,
    isLoading: Boolean,
    error: String?,
    capabilityRunInput: String,
    capabilityRunState: AiUiState,
    onUpgradeRequestChange: (String) -> Unit,
    onRequestUpgradeProposal: () -> Unit,
    onApproveProposal: (Long) -> Unit,
    onRejectProposal: (Long) -> Unit,
    onCapabilityRunInputChange: (String) -> Unit,
    onRunApprovedCapability: (UpgradeProposal) -> Unit
) {
    val context = LocalContext.current

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
                Icon(Icons.Filled.Security, contentDescription = "Approval Gated", tint = MaterialTheme.colorScheme.tertiary)
                Text(
                    text = "ارتقای خودکار و توسعه قابلیت‌ها",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            StatusPill(
                label = "OWNER APPROVAL",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        Text(
            text = "این بخش با درخواست شما پیشنهاد کد (Kotlin/Termux) و مهارت جدید منشی را طراحی می‌کند، اما هیچ تغییری بدون «تایید نهایی شما» فعال نمی‌شود (کاملاً منطبق با قوانین امنیتی Play).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = upgradeRequestInput,
            onValueChange = onUpgradeRequestChange,
            label = { Text("چه قابلیت یا اختیاری اضافه شود؟ (مثلاً: خلاصه‌ساز روزانه کامیت‌های گیت‌هاب)") },
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upgrade_request_input")
        )

        Button(
            onClick = onRequestUpgradeProposal,
            enabled = !isLoading && upgradeRequestInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("generate_upgrade_proposal_button")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(if (isLoading) "در حال طراحی پیشنهاد ارتقا..." else "طراحی کد و پیشنهاد ارتقا برای تایید من")
        }

        if (error != null) {
            Text(text = error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
    }

    if (upgradeProposals.any { it.status == ApprovalStatus.Approved }) {
        StudioCard {
            Text(
                text = "ورودی برای اجرای قابلیت‌های تاییدشده",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = capabilityRunInput,
                onValueChange = onCapabilityRunInputChange,
                label = { Text("متن یا دستور ورودی برای اجرا (اختیاری)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capability_run_input")
            )
        }
    }

    upgradeProposals.forEach { proposal ->
        val borderColor = when (proposal.status) {
            ApprovalStatus.PendingApproval -> MaterialTheme.colorScheme.primary
            ApprovalStatus.Approved -> MaterialTheme.colorScheme.tertiary
            ApprovalStatus.Rejected -> MaterialTheme.colorScheme.outline
        }
        StudioCard(borderColor = borderColor) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = proposal.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = when (proposal.status) {
                        ApprovalStatus.PendingApproval -> "در انتظار تایید"
                        ApprovalStatus.Approved -> "تایید و فعال"
                        ApprovalStatus.Rejected -> "رد شده"
                    },
                    containerColor = when (proposal.status) {
                        ApprovalStatus.PendingApproval -> MaterialTheme.colorScheme.primaryContainer
                        ApprovalStatus.Approved -> MaterialTheme.colorScheme.tertiaryContainer
                        ApprovalStatus.Rejected -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = when (proposal.status) {
                        ApprovalStatus.PendingApproval -> MaterialTheme.colorScheme.onPrimaryContainer
                        ApprovalStatus.Approved -> MaterialTheme.colorScheme.onTertiaryContainer
                        ApprovalStatus.Rejected -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Text(
                text = "محدوده اختیار: ${proposal.authorityScope}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "قانون مهارت: ${proposal.systemPromptAddition}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "ارزیابی امنیتی: ${proposal.riskSummary}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "کد پیشنهادی (Kotlin / Termux):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = proposal.codePatch,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { AndroidIntentActions.copyToClipboard(context, proposal.title, proposal.codePatch) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy patch", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("کپی کد")
                        }
                        OutlinedButton(
                            onClick = { AndroidIntentActions.shareText(context, proposal.title, proposal.codePatch) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share patch", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("ارسال به Termux")
                        }
                    }
                }
            }

            when (proposal.status) {
                ApprovalStatus.PendingApproval -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onApproveProposal(proposal.id) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("approve_upgrade_${proposal.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("تایید و فعال‌سازی")
                        }
                        OutlinedButton(
                            onClick = { onRejectProposal(proposal.id) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("reject_upgrade_${proposal.id}")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("رد کردن")
                        }
                    }
                }
                ApprovalStatus.Approved -> {
                    Button(
                        onClick = { onRunApprovedCapability(proposal) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("run_capability_${proposal.id}")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("اجرای این قابلیت با منشی فعال")
                    }
                }
                ApprovalStatus.Rejected -> Unit
            }
        }
    }

    when (capabilityRunState) {
        AiUiState.Idle -> Unit
        AiUiState.Loading -> {
            StudioCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("در حال اجرای قابلیت تاییدشده...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        is AiUiState.Streaming, is AiUiState.Success -> {
            val text = if (capabilityRunState is AiUiState.Streaming) capabilityRunState.text else (capabilityRunState as AiUiState.Success).text
            StudioCard(borderColor = MaterialTheme.colorScheme.tertiary) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("نتیجه اجرای قابلیت", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                    IconButton(onClick = { AndroidIntentActions.copyToClipboard(context, "Capability Output", text) }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy result")
                    }
                }
                Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        is AiUiState.Error -> {
            StudioCard(borderColor = MaterialTheme.colorScheme.error) {
                Text(text = capabilityRunState.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
