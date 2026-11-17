package com.example.ui.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AdvisoryDomain
import com.example.model.AiUiState
import com.example.model.AssistantPersona

@Composable
fun AdvisoryHubSection(
    selectedPersona: AssistantPersona,
    advisoryDomain: AdvisoryDomain,
    advisoryProfileInput: String,
    advisoryGoalInput: String,
    advisoryAiState: AiUiState,
    isSpeaking: Boolean,
    onSelectDomain: (AdvisoryDomain) -> Unit,
    onProfileChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onApplyPreset: (String, String) -> Unit,
    onGeneratePlan: () -> Unit,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit
) {
    val context = LocalContext.current

    StudioCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مشاوره تخصصی و برنامه‌ریزی با ${selectedPersona.persianName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${selectedPersona.archetypeTag} • رژیم، بدنسازی، ترک اعتیاد، تحصیلی، تجاری و شبیه‌ساز ترید",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            StatusPill(
                label = "۶ تخصص",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // 6-Domain Horizontal Chip Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdvisoryDomain.entries.forEach { domain ->
                FilterChip(
                    selected = advisoryDomain == domain,
                    onClick = { onSelectDomain(domain) },
                    label = { Text(domain.persianLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = domainIcon(domain),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("advisory_domain_${domain.name.lowercase()}")
                )
            }
        }

        // Policy / Safety Guidance Banner depending on domain
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = when (advisoryDomain) {
                    AdvisoryDomain.TradingStrategySim ->
                        "توجه منطبق با قوانین Creator گوگل‌پلی: اتصال مستقیم به صرافی، کیف‌پول رمزارز و اجرای خودکار خرید/فروش مالی واقعی در این اپ فعال نیست. این بخش استراتژی ترید، نقاط ورود/خروج، حد ضرر و شبیه‌سازی آموزشی (Paper Trading) را با تایید شما آماده می‌کند."
                    AdvisoryDomain.AddictionRecovery ->
                        "حمایت رفتاری و انگیزشی ترک اعتیاد: تمرکز بر کنترل وسوسه، برنامه‌ریزی روزانه و پیشگیری از لغزش است و جایگزین مراقبت پزشک متخصص در موارد حاد نیست."
                    AdvisoryDomain.NutritionDiet, AdvisoryDomain.BodybuildingWorkout ->
                        "برنامه تغذیه و تمرین متناسب با مشخصات هر فرد طراحی می‌شود؛ در صورت داشتن بیماری زمینه‌ای با پزشک مشورت شود."
                    AdvisoryDomain.BusinessInvestment ->
                        "تحلیل ساختار کسب‌وکار، مدیریت ریسک سرمایه و اولویت‌بندی اجرایی با رویکرد آموزشی و مشورتی."
                    AdvisoryDomain.AcademicCounseling ->
                        "برنامه‌ریزی درسی، کنکور، زبان یا پروژه دانشگاهی متناسب با سطح و زمان هر فرد."
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(10.dp)
            )
        }

        // Quick Preset Helper
        val (presetProfile, presetGoal) = domainPreset(advisoryDomain)
        OutlinedButton(
            onClick = { onApplyPreset(presetProfile, presetGoal) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("advisory_apply_preset_button")
        ) {
            Text("پر کردن نمونه آماده برای «${advisoryDomain.persianLabel}»")
        }

        OutlinedTextField(
            value = advisoryProfileInput,
            onValueChange = onProfileChange,
            label = { Text("مشخصات فرد (سن، جنسیت، شرایط جسمی/تحصیلی/سرمایه)") },
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("advisory_profile_input")
        )

        OutlinedTextField(
            value = advisoryGoalInput,
            onValueChange = onGoalChange,
            label = { Text("هدف و جزئیات دقیق درخواست") },
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("advisory_goal_input")
        )

        Button(
            onClick = onGeneratePlan,
            enabled = advisoryProfileInput.isNotBlank() || advisoryGoalInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("advisory_generate_button")
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("تهیه برنامه و مشاوره با صدای ${selectedPersona.persianName}")
        }
    }

    when (advisoryAiState) {
        AiUiState.Idle -> Unit
        AiUiState.Loading -> {
            StudioCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text(
                        text = "${selectedPersona.persianName} در حال تنظیم ${advisoryDomain.persianLabel}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        is AiUiState.Streaming, is AiUiState.Success -> {
            val text = if (advisoryAiState is AiUiState.Streaming) advisoryAiState.text
            else (advisoryAiState as AiUiState.Success).text
            StudioCard(borderColor = MaterialTheme.colorScheme.secondary) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${advisoryDomain.persianLabel} (${selectedPersona.persianName})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        IconButton(
                            onClick = { if (isSpeaking) onStopSpeaking() else onSpeakText(text) },
                            modifier = Modifier.testTag("advisory_speak_button")
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                contentDescription = "Speak advisory plan"
                            )
                        }
                        IconButton(onClick = { AndroidIntentActions.copyToClipboard(context, advisoryDomain.persianLabel, text) }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy advisory plan")
                        }
                        IconButton(onClick = { AndroidIntentActions.shareText(context, advisoryDomain.persianLabel, text) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share advisory plan")
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
                    text = "خطا: ${advisoryAiState.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun domainIcon(domain: AdvisoryDomain): ImageVector {
    return when (domain) {
        AdvisoryDomain.NutritionDiet -> Icons.Filled.Restaurant
        AdvisoryDomain.BodybuildingWorkout -> Icons.Filled.FitnessCenter
        AdvisoryDomain.AddictionRecovery -> Icons.Filled.Favorite
        AdvisoryDomain.AcademicCounseling -> Icons.Filled.School
        AdvisoryDomain.BusinessInvestment -> Icons.Filled.BusinessCenter
        AdvisoryDomain.TradingStrategySim -> Icons.Filled.CandlestickChart
    }
}

private fun domainPreset(domain: AdvisoryDomain): Pair<String, String> {
    return when (domain) {
        AdvisoryDomain.NutritionDiet ->
            "مرد ۳۲ ساله، قد ۱۷۸، وزن ۸۶ کیلو، تحرک متوسط" to "کاهش ۶ کیلو چربی در ۸ هفته با غذاهای ساده و اقتصادی"
        AdvisoryDomain.BodybuildingWorkout ->
            "آقای ۳۰ ساله، سابقه ۳ ماه باشگاه، ۴ روز تمرین در هفته" to "افزایش حجم عضلانی و فرم‌دهی بالاتنه و پا با برنامه Push/Pull/Legs"
        AdvisoryDomain.AddictionRecovery ->
            "بزرگسال، قصد قطع کامل عادت ناسالم و کنترل وسوسه عصرگاهی" to "برنامه ۷ روزه مهار وسوسه، جایگزینی عادت و پیگیری روزانه با انگیزه بالا"
        AdvisoryDomain.AcademicCounseling ->
            "دانشجوی نرم‌افزار / داوطلب آزمون، روزی ۵ ساعت زمان آزاد" to "تقسیم‌بندی مطالعه، مرور فعال و برنامه هفتگی بدون خستگی"
        AdvisoryDomain.BusinessInvestment ->
            "توسعه‌دهنده اپلیکیشن با سرمایه اولیه متوسط و درآمد جانبی" to "استراتژی بازاریابی اپلیکیشن، تنوع‌بخشی سرمایه و مدیریت ریسک"
        AdvisoryDomain.TradingStrategySim ->
            "تریدر آموزشی (Paper Trading)، مدیریت ریسک حداکثر ۲ درصد در هر معامله" to "طراحی چک‌لیست ورود، حد ضرر، حد سود و نسبت ریسک به ریوارد ۱ به ۲ روی جفت‌ارز یا شاخص"
    }
}
