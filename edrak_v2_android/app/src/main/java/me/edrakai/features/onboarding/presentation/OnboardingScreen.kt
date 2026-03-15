package me.edrakai.features.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import me.edrakai.ui.theme.EdrakColors

@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Collect one-time effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OnboardingEffect.NavigateToHome -> onNavigateToHome()
                is OnboardingEffect.ShowError -> { /* handled inline via state */ }
            }
        }
    }

    AnimatedContent(
        targetState = state.step,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "onboarding_step",
    ) { step ->
        when (step) {
            OnboardingStep.WELCOME   -> WelcomeStep(onStart = { viewModel.onEvent(OnboardingEvent.StartSetup) })
            OnboardingStep.RECORDING -> RecordingStep(state = state, onEvent = viewModel::onEvent)
            OnboardingStep.SUCCESS   -> SuccessStep(onContinue = { viewModel.onEvent(OnboardingEvent.Continue) })
        }
    }
}

// ─── Step 1: Welcome ─────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EdrakColors.DeepMidnightBlue)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Mic icon with glow ring
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(2.dp, EdrakColors.NeonCyan.copy(alpha = 0.3f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(EdrakColors.NeonCyan.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = EdrakColors.NeonCyan,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = "Voice Setup",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Edrak needs to recognize your voice so it can separate your speech from others in a conversation.\n\nYou'll record 3 short phrases — it takes less than a minute.",
            fontSize = 15.sp,
            color = EdrakColors.Slate400,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EdrakColors.NeonCyan),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Start Setup", color = EdrakColors.DeepMidnightBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ─── Step 2: Recording ───────────────────────────────────────────────────────

@Composable
private fun RecordingStep(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    val phrase = ENROLLMENT_PHRASES.getOrNull(state.currentPhraseIndex) ?: return
    val progress = (state.currentPhraseIndex + 1f) / ENROLLMENT_PHRASES.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EdrakColors.DeepMidnightBlue)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(40.dp))
            Text("Phrase ${state.currentPhraseIndex + 1} of ${ENROLLMENT_PHRASES.size}",
                color = EdrakColors.Slate400, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ENROLLMENT_PHRASES.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (i == state.currentPhraseIndex) 10.dp else 8.dp)
                            .background(
                                if (i <= state.currentPhraseIndex) EdrakColors.NeonCyan
                                else EdrakColors.Slate400.copy(alpha = 0.3f),
                                CircleShape,
                            )
                    )
                }
            }
        }

        // Phrase card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A2540), RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\"$phrase\"",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
            )
        }

        // Recording mic button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                state.isUploading -> {
                    CircularProgressIndicator(color = EdrakColors.NeonCyan, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Uploading...", color = EdrakColors.Slate400, fontSize = 14.sp)
                }
                state.uploadError != null -> {
                    Text("Upload failed", color = Color(0xFFFF6B6B), fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onEvent(OnboardingEvent.RetryUpload) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                    ) { Text("Retry", color = Color.White) }
                }
                else -> {
                    PulsatingMicButton(
                        isRecording = state.isRecording,
                        onClick = {
                            if (state.isRecording) onEvent(OnboardingEvent.StopRecording)
                            else onEvent(OnboardingEvent.StartRecording)
                        },
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (state.isRecording) "Tap to stop recording" else "Tap mic to record",
                        color = EdrakColors.Slate400,
                        fontSize = 14.sp,
                    )
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun PulsatingMicButton(isRecording: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse_scale",
    )

    Box(contentAlignment = Alignment.Center) {
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .background(EdrakColors.NeonCyan.copy(alpha = 0.15f), CircleShape)
            )
        }
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(80.dp)
                .background(
                    if (isRecording) Color(0xFFFF4444) else EdrakColors.NeonCyan,
                    CircleShape,
                ),
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) "Stop" else "Record",
                tint = if (isRecording) Color.White else EdrakColors.DeepMidnightBlue,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

// ─── Step 3: Success ─────────────────────────────────────────────────────────

@Composable
private fun SuccessStep(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EdrakColors.DeepMidnightBlue)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EdrakColors.NeonCyan,
            modifier = Modifier.size(96.dp),
        )
        Spacer(Modifier.height(32.dp))
        Text("Voice Setup Complete! 🎉", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "Edrak can now recognize your voice and separate your speech from others automatically.",
            fontSize = 15.sp,
            color = EdrakColors.Slate400,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EdrakColors.NeonCyan),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Start Using Edrak", color = EdrakColors.DeepMidnightBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
