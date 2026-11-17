package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.AssistantPersona

private const val TAG = "VideoChatStage"

@Composable
fun VideoChatStage(
    persona: AssistantPersona,
    lastPersonaReply: String,
    partialTranscript: String,
    isListening: Boolean,
    isSpeaking: Boolean,
    rmsLevel: Float,
    isAutoVoiceEnabled: Boolean,
    onToggleMic: () -> Unit,
    onToggleAutoVoice: () -> Unit,
    onStopSpeaking: () -> Unit,
    onEndVideoCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSelfCameraOn by remember { mutableStateOf(false) }
    var cameraNotice by remember { mutableStateOf<String?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isSelfCameraOn = true
            cameraNotice = null
        } else {
            isSelfCameraOn = false
            cameraNotice = "دسترسی دوربین داده نشد؛ تماس با نمای کاراکتر ادامه دارد."
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "voice_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.05f else 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 450 else 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSpeaking || isListening) 2.dp else 1.dp,
            color = when {
                isSpeaking -> MaterialTheme.colorScheme.primary
                isListening -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.outline
            }
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                Image(
                    painter = painterResource(id = persona.portraitRes),
                    contentDescription = persona.persianName,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale),
                    contentScale = ContentScale.Crop
                )

                // Top gradient + status badges
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${persona.persianName} • ${persona.age} ساله",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = when {
                                isSpeaking -> "در حال صحبت (${persona.voiceLabel})"
                                isListening -> "در حال گوش دادن به شما..."
                                else -> "تماس تصویری آماده"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSpeaking) MaterialTheme.colorScheme.primary
                            else if (isListening) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Self-camera PIP
                    if (isSelfCameraOn) {
                        SelfCameraPip(
                            modifier = Modifier
                                .size(width = 84.dp, height = 112.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }

                // Bottom gradient + live caption & transcript
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                                )
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isSpeaking) {
                            SpeakingWaveformRow(intensity = 0.85f)
                        } else if (isListening) {
                            SpeakingWaveformRow(intensity = rmsLevel.coerceAtLeast(0.25f))
                        }

                        if (partialTranscript.isNotBlank()) {
                            Text(
                                text = "شما: $partialTranscript",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (lastPersonaReply.isNotBlank()) {
                            Text(
                                text = lastPersonaReply,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (cameraNotice != null) {
                Text(
                    text = cameraNotice.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Video Call Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("video_mic_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = "Voice to Chat Mic"
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (isSelfCameraOn) {
                            isSelfCameraOn = false
                        } else {
                            val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                            if (!hasCamera) {
                                cameraNotice = "دوربین سخت‌افزاری روی این دستگاه شناسایی نشد."
                            } else {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    isSelfCameraOn = true
                                    cameraNotice = null
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("video_camera_toggle_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isSelfCameraOn) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (isSelfCameraOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                        contentDescription = "Toggle Self Camera"
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (isSpeaking) onStopSpeaking() else onToggleAutoVoice()
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("video_voice_toggle_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isAutoVoiceEnabled) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (isAutoVoiceEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                        contentDescription = "Toggle Chat to Voice TTS"
                    )
                }

                FilledIconButton(
                    onClick = onEndVideoCall,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("video_end_call_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "Close Video Stage"
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeakingWaveformRow(intensity: Float) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = listOf(10, 18, 24, 14, 22, 12, 16)
        heights.forEach { base ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((base * intensity).coerceAtLeast(6f).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = "پخش زنده صدا",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SelfCameraPip(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        var provider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                provider = cameraProvider
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val selector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
            } catch (e: Exception) {
                Log.e(TAG, "CameraX PIP bind failed", e)
            }
        }, executor)

        onDispose {
            provider?.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
