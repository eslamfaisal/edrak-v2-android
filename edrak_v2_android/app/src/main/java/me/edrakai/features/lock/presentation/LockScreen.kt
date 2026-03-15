package me.edrakai.features.lock.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import me.edrakai.ui.theme.EdrakColors

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    onLogout: () -> Unit,
    viewModel: LockViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LockEffect.NavigateToHome  -> onUnlocked()
                is LockEffect.NavigateToLogin -> onLogout()
                is LockEffect.ShowError       -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.onEvent(LockEvent.StartRecording)
        else Toast.makeText(context, "Microphone permission required to unlock", Toast.LENGTH_LONG).show()
    }

    fun requestMicAndRecord() {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (ok) viewModel.onEvent(LockEvent.StartRecording) else permLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // ── Background gradient ─────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF04081A), Color(0xFF0A1628), Color(0xFF060D1C))
                )
            )
    ) {
        // Decorative blurred glow orbs
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .blur(80.dp)
                .background(EdrakColors.NeonCyan.copy(alpha = 0.07f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .blur(80.dp)
                .background(Color(0xFF6C63FF).copy(alpha = 0.07f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {

            Spacer(Modifier.height(16.dp))

            // ── Lock icon ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(1.dp, EdrakColors.NeonCyan.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (state.status) {
                        LockStatus.MATCHED -> Icons.Default.LockOpen
                        else               -> Icons.Default.Lock
                    },
                    contentDescription = null,
                    tint = when (state.status) {
                        LockStatus.MATCHED  -> Color(0xFF00E676)
                        LockStatus.FAILED,
                        LockStatus.LOCKED_OUT -> Color(0xFFFF5252)
                        else -> EdrakColors.NeonCyan
                    },
                    modifier = Modifier.size(32.dp),
                )
            }

            // ── Greeting ───────────────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Welcome back", fontSize = 15.sp, color = EdrakColors.Slate400)
                Text(
                    state.displayName,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            // ── Passphrase prompt card ─────────────────────────────────────────────
            if (state.passphrase.isNotEmpty() && state.status !in listOf(LockStatus.MATCHED, LockStatus.LOCKED_OUT)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, EdrakColors.NeonCyan.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Say your passphrase", fontSize = 12.sp, color = EdrakColors.Slate400, letterSpacing = 1.sp)
                        Text(
                            "\"${state.passphrase}\"",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Main mic widget ────────────────────────────────────────────────────
            VoiceLockMic(
                state = state,
                onPressStart  = { requestMicAndRecord() },
                onPressEnd    = { viewModel.onEvent(LockEvent.StopAndVerify) },
            )

            // ── Status message ─────────────────────────────────────────────────────
            AnimatedContent(
                targetState = state.statusMessage,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                label = "status_msg",
            ) { msg ->
                Text(
                    text = msg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (state.status) {
                        LockStatus.MATCHED    -> Color(0xFF00E676)
                        LockStatus.FAILED     -> Color(0xFFFFD600)
                        LockStatus.LOCKED_OUT -> Color(0xFFFF5252)
                        else                  -> Color.White
                    },
                    textAlign = TextAlign.Center,
                )
            }

            // ── Attempt dots ───────────────────────────────────────────────────────
            if (state.attemptCount > 0 && state.status != LockStatus.MATCHED) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (i < state.attemptCount) Color(0xFFFF5252)
                                    else Color.White.copy(alpha = 0.15f),
                                    CircleShape,
                                )
                        )
                    }
                }
                Text("${3 - state.attemptCount} attempts remaining", fontSize = 12.sp, color = EdrakColors.Slate400)
            }

            // ── Retry button ───────────────────────────────────────────────────────
            if (state.status == LockStatus.FAILED) {
                Button(
                    onClick = { viewModel.onEvent(LockEvent.Retry) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EdrakColors.NeonCyan.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Mic, null, tint = EdrakColors.NeonCyan, modifier = Modifier.size(18.dp))
                    Text("  Try Again", color = EdrakColors.NeonCyan, fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Forgot voice link ──────────────────────────────────────────────────
            if (state.status !in listOf(LockStatus.MATCHED, LockStatus.LOCKED_OUT)) {
                TextButton(onClick = { viewModel.onEvent(LockEvent.ForgotVoice) }) {
                    Text("Forgot voice? Sign in again", color = EdrakColors.Slate400, fontSize = 13.sp)
                }
            }
        }
    }
}

// ─── Voice Mic Widget ─────────────────────────────────────────────────────────

@Composable
private fun VoiceLockMic(
    state: LockState,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
) {
    val isRecording = state.isRecording

    // Animated confidence arc
    val animConf by animateFloatAsState(
        targetValue = state.liveConfidence,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "conf",
    )

    val arcColor = when {
        animConf >= 0.80f -> Color(0xFF00E676)
        animConf >= 0.55f -> Color(0xFFFFD600)
        else              -> EdrakColors.NeonCyan
    }

    // Outer pulse ring while recording
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue  = if (isRecording) 1.20f else 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulse_scale",
    )

    // Waveform bars
    val wave = rememberInfiniteTransition(label = "wave")

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        // Confidence progress ring
        if (state.liveConfidence > 0f || state.status == LockStatus.MATCHED) {
            CircularProgressIndicator(
                progress = { animConf.coerceIn(0f, 1f) },
                modifier = Modifier.size(210.dp),
                color = arcColor,
                trackColor = Color.White.copy(alpha = 0.06f),
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round,
            )
            if (animConf > 0f) {
                Text(
                    text = "${(animConf * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = arcColor,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                )
            }
        }

        // Pulse ring while recording
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .background(Color(0xFFFF1744).copy(alpha = 0.12f), CircleShape)
            )
        }

        // Main mic button — hold to record
        Box(
            modifier = Modifier
                .size(130.dp)
                .background(
                    when (state.status) {
                        LockStatus.MATCHED   -> Color(0xFF00E676).copy(alpha = 0.15f)
                        LockStatus.ANALYZING -> EdrakColors.NeonCyan.copy(alpha = 0.1f)
                        LockStatus.RECORDING -> Color(0xFFFF1744).copy(alpha = 0.15f)
                        else -> Color.White.copy(alpha = 0.05f)
                    },
                    CircleShape,
                )
                .border(
                    width = if (isRecording) 2.5.dp else 1.dp,
                    color = if (isRecording) Color(0xFFFF1744) else EdrakColors.NeonCyan.copy(alpha = 0.3f),
                    shape = CircleShape,
                )
                .pointerInput(state.status) {
                    detectTapGestures(
                        onPress = {
                            if (state.status == LockStatus.IDLE || state.status == LockStatus.FAILED) {
                                onPressStart()
                                tryAwaitRelease()
                                onPressEnd()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            when (state.status) {
                LockStatus.MATCHED -> Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00E676), modifier = Modifier.size(52.dp))
                LockStatus.ANALYZING -> CircularProgressIndicator(color = EdrakColors.NeonCyan, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                LockStatus.RECORDING -> {
                    // Animated waveform
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf(0, 120, 240, 360).forEach { delay ->
                            val barH by wave.animateFloat(
                                initialValue = 10f, targetValue = 38f,
                                animationSpec = infiniteRepeatable(tween(450, delayMillis = delay, easing = EaseOutCubic), RepeatMode.Reverse),
                                label = "bar_$delay",
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = 5.dp, height = barH.dp)
                                    .background(Color(0xFFFF1744), RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Mic, null, tint = EdrakColors.NeonCyan, modifier = Modifier.size(40.dp))
                        Text("Hold", fontSize = 11.sp, color = EdrakColors.Slate400)
                    }
                }
            }
        }
    }
}
