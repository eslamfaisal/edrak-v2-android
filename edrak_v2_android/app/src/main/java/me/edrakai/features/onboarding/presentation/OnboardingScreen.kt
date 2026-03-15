package me.edrakai.features.onboarding.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutCubic
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import me.edrakai.ui.theme.EdrakColors

// ─── Root ─────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OnboardingEffect.NavigateToHome -> onNavigateToHome()
                is OnboardingEffect.ShowError      -> { }
            }
        }
    }

    AnimatedContent(
        targetState = state.step,
        transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(350)) },
        label = "onboarding_step",
    ) { step ->
        when (step) {
            OnboardingStep.WELCOME          -> WelcomeStep(onStart = { viewModel.onEvent(OnboardingEvent.StartSetup) })
            OnboardingStep.PASSPHRASE_SETUP -> PassphraseSetupStep(state = state, onEvent = viewModel::onEvent)
            OnboardingStep.RECORDING        -> RecordingStep(state = state, viewModel = viewModel, onEvent = viewModel::onEvent)
            OnboardingStep.VERIFICATION     -> VerificationStep(state = state, onEvent = viewModel::onEvent)
            OnboardingStep.SUCCESS          -> SuccessStep(onContinue = { viewModel.onEvent(OnboardingEvent.Continue) })
        }
    }
}

// ─── Step 1: Welcome ─────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(EdrakColors.DeepMidnightBlue).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(120.dp).border(2.dp, EdrakColors.NeonCyan.copy(alpha = 0.3f), CircleShape))
            Box(modifier = Modifier.size(90.dp).background(EdrakColors.NeonCyan.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Mic, null, tint = EdrakColors.NeonCyan, modifier = Modifier.size(48.dp))
            }
        }
        Spacer(Modifier.height(40.dp))
        Text("Voice Setup", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "Edrak recognizes your voice as your identity.\n\n1️⃣  Set a voice passphrase (your voice PIN)\n2️⃣  Record 3 short phrases\n3️⃣  Real-time voice verification",
            fontSize = 15.sp, color = EdrakColors.Slate400, textAlign = TextAlign.Center, lineHeight = 23.sp,
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EdrakColors.NeonCyan),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Get Started", color = EdrakColors.DeepMidnightBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ─── Step 2: Passphrase Setup ─────────────────────────────────────────────────

@Composable
private fun PassphraseSetupStep(state: OnboardingState, onEvent: (OnboardingEvent) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxSize().background(EdrakColors.DeepMidnightBlue).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(72.dp).background(EdrakColors.NeonCyan.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Lock, null, tint = EdrakColors.NeonCyan, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text("Set Your Voice Passphrase", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            "This phrase will be your voice PIN. You'll say it every time you open Edrak to verify it's really you.",
            fontSize = 14.sp, color = EdrakColors.Slate400, textAlign = TextAlign.Center, lineHeight = 21.sp,
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = state.passphraseInput,
            onValueChange = { onEvent(OnboardingEvent.PassphraseChanged(it)) },
            label = { Text("Your passphrase") },
            placeholder = { Text("e.g. Open Edrak now", color = EdrakColors.Slate400) },
            isError = state.passphraseError != null,
            supportingText = {
                if (state.passphraseError != null) {
                    Text(state.passphraseError, color = Color(0xFFFF6B6B))
                } else {
                    Text("Choose anything memorable — 3+ characters", color = EdrakColors.Slate400, fontSize = 12.sp)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboard?.hide()
                onEvent(OnboardingEvent.ConfirmPassphrase)
            }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = EdrakColors.NeonCyan,
                unfocusedBorderColor = EdrakColors.Slate400.copy(alpha = 0.4f),
                focusedLabelColor    = EdrakColors.NeonCyan,
                cursorColor          = EdrakColors.NeonCyan,
                focusedTextColor     = Color.White,
                unfocusedTextColor   = Color.White,
            ),
        )

        Spacer(Modifier.height(24.dp))

        // Example hints
        Box(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1A2540), RoundedCornerShape(12.dp)).padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("💡  Good passphrase examples", fontSize = 13.sp, color = EdrakColors.NeonCyan, fontWeight = FontWeight.SemiBold)
                listOf("Open Edrak now", "Hello Edrak, it's me", "My voice is my key").forEach { ex ->
                    Text("• \"$ex\"", fontSize = 13.sp, color = EdrakColors.Slate400)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                keyboard?.hide()
                onEvent(OnboardingEvent.ConfirmPassphrase)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled = state.passphraseInput.length >= 3,
            colors = ButtonDefaults.buttonColors(
                containerColor = EdrakColors.NeonCyan,
                disabledContainerColor = EdrakColors.Slate400.copy(alpha = 0.3f),
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Save Passphrase & Continue", color = EdrakColors.DeepMidnightBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ─── Step 3: Recording (passphrase + 2 fixed phrases) ─────────────────────────

@Composable
private fun RecordingStep(
    state: OnboardingState,
    viewModel: OnboardingViewModel,
    onEvent: (OnboardingEvent) -> Unit,
) {
    val phrase = viewModel.currentEnrollmentPhrase()
    val totalPhrases = 3            // passphrase + 2 fixed
    val context = LocalContext.current

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onEvent(OnboardingEvent.StartRecording)
        else Toast.makeText(context, "Microphone permission is required", Toast.LENGTH_LONG).show()
    }

    fun requestMicOrStart() {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (ok) onEvent(OnboardingEvent.StartRecording) else permLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(EdrakColors.DeepMidnightBlue).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(40.dp))
            // Phase label
            if (state.currentPhraseIndex == 0) {
                Text("🔑  Your Passphrase", color = EdrakColors.NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Text("Phrase ${state.currentPhraseIndex + 1} of $totalPhrases", color = EdrakColors.Slate400, fontSize = 14.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalPhrases) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == state.currentPhraseIndex) 10.dp else 8.dp)
                            .background(
                                when {
                                    i < state.currentPhraseIndex  -> EdrakColors.NeonCyan
                                    i == state.currentPhraseIndex -> EdrakColors.NeonCyan.copy(alpha = 0.7f)
                                    else                           -> EdrakColors.Slate400.copy(alpha = 0.3f)
                                },
                                CircleShape,
                            )
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1A2540), RoundedCornerShape(16.dp)).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.currentPhraseIndex == 0) {
                    Text("YOUR PASSPHRASE", fontSize = 10.sp, color = EdrakColors.NeonCyan, letterSpacing = 2.sp)
                }
                Text(text = "\"$phrase\"", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.White, textAlign = TextAlign.Center, lineHeight = 30.sp)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                state.isUploading -> {
                    CircularProgressIndicator(color = EdrakColors.NeonCyan, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Uploading…", color = EdrakColors.Slate400, fontSize = 14.sp)
                }
                state.uploadError != null -> {
                    Text("Upload failed", color = Color(0xFFFF6B6B), fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onEvent(OnboardingEvent.RetryUpload) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))) {
                        Text("Retry", color = Color.White)
                    }
                }
                else -> {
                    PulsatingMicButton(isRecording = state.isRecording, onClick = {
                        if (state.isRecording) onEvent(OnboardingEvent.StopRecording) else requestMicOrStart()
                    })
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (state.isRecording) "Tap to stop recording" else "Tap mic to record",
                        color = EdrakColors.Slate400, fontSize = 14.sp,
                    )
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

// ─── Step 4: Real-time Verification ───────────────────────────────────────────

@Composable
private fun VerificationStep(state: OnboardingState, onEvent: (OnboardingEvent) -> Unit) {
    val context = LocalContext.current
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onEvent(OnboardingEvent.StartLiveVerification)
        else Toast.makeText(context, "Microphone permission required", Toast.LENGTH_LONG).show()
    }
    fun requestMicOrStart() {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (ok) onEvent(OnboardingEvent.StartLiveVerification) else permLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val isActive = state.verificationStatus == VerificationStatus.LISTENING || state.verificationStatus == VerificationStatus.ANALYZING
    val animConf by animateFloatAsState(
        targetValue = state.liveConfidence,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "confidence",
    )
    val confColor = when {
        animConf >= 0.80f -> Color(0xFF00E676)
        animConf >= 0.55f -> Color(0xFFFFD600)
        else -> Color(0xFFFF5252)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(EdrakColors.DeepMidnightBlue).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically),
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Voice Verification", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Speak naturally — a word, a sentence, anything. We're confirming it's really you.",
            fontSize = 14.sp, color = EdrakColors.Slate400, textAlign = TextAlign.Center, lineHeight = 21.sp)

        // Live confidence ring
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            if (isActive) {
                val pulse = rememberInfiniteTransition(label = "ring")
                val ringScale by pulse.animateFloat(1f, 1.18f, infiniteRepeatable(tween(900, easing = EaseInOut), RepeatMode.Reverse), label = "rs")
                Box(modifier = Modifier.size(200.dp).scale(ringScale).border(2.dp, confColor.copy(alpha = 0.25f), CircleShape))
            }
            CircularProgressIndicator(
                progress = { animConf.coerceIn(0f, 1f) },
                modifier = Modifier.size(180.dp), color = confColor,
                trackColor = Color.White.copy(alpha = 0.08f), strokeWidth = 8.dp, strokeCap = StrokeCap.Round,
            )
            Box(
                modifier = Modifier.size(110.dp).background(
                    when (state.verificationStatus) {
                        VerificationStatus.MATCHED   -> Color(0xFF00E676).copy(alpha = 0.15f)
                        VerificationStatus.ANALYZING -> EdrakColors.NeonCyan.copy(alpha = 0.08f)
                        else -> EdrakColors.NeonCyan.copy(alpha = 0.10f)
                    }, CircleShape,
                ),
                contentAlignment = Alignment.Center,
            ) {
                val wave = rememberInfiniteTransition(label = "wave")
                when (state.verificationStatus) {
                    VerificationStatus.MATCHED   -> Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00E676), modifier = Modifier.size(52.dp))
                    VerificationStatus.ANALYZING -> CircularProgressIndicator(color = EdrakColors.NeonCyan, modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                    VerificationStatus.IDLE      -> Icon(Icons.Default.Mic, null, tint = EdrakColors.Slate400, modifier = Modifier.size(44.dp))
                    else -> Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf(0, 150, 300).forEach { d ->
                            val h by wave.animateFloat(8f, 32f, infiniteRepeatable(tween(500, delayMillis = d, easing = EaseOutCubic), RepeatMode.Reverse), label = "b$d")
                            Box(modifier = Modifier.size(width = 6.dp, height = h.dp).background(EdrakColors.NeonCyan, RoundedCornerShape(3.dp)))
                        }
                    }
                }
            }
            if (animConf > 0f) Text("${(animConf * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = confColor, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
        }

        Text(state.verificationMessage, fontSize = 16.sp, fontWeight = FontWeight.Medium,
            color = when (state.verificationStatus) {
                VerificationStatus.MATCHED  -> Color(0xFF00E676)
                VerificationStatus.NO_MATCH -> Color(0xFFFFD600)
                else -> Color.White
            }, textAlign = TextAlign.Center)

        if (state.consecutiveMatches > 0 || isActive) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { i ->
                    Box(modifier = Modifier.size(width = 60.dp, height = 8.dp).background(
                        if (i < state.consecutiveMatches) Color(0xFF00E676) else Color.White.copy(alpha = 0.12f),
                        RoundedCornerShape(4.dp),
                    ))
                }
            }
            Text("${state.consecutiveMatches}/2 confirmations", fontSize = 12.sp, color = EdrakColors.Slate400)
        }

        when (state.verificationStatus) {
            VerificationStatus.IDLE, VerificationStatus.NO_MATCH -> Button(
                onClick = { requestMicOrStart() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EdrakColors.NeonCyan),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Mic, null, modifier = Modifier.size(20.dp), tint = EdrakColors.DeepMidnightBlue)
                Text("  Start Speaking", color = EdrakColors.DeepMidnightBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            VerificationStatus.LISTENING, VerificationStatus.ANALYZING -> Button(
                onClick = { onEvent(OnboardingEvent.StopLiveVerification) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A3550)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.MicOff, null, modifier = Modifier.size(20.dp), tint = Color(0xFFFF5252))
                Text("  Stop", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            VerificationStatus.MATCHED -> {}
        }

        if (state.verificationStatus != VerificationStatus.MATCHED) {
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A2540), RoundedCornerShape(12.dp)).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tips for best results", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EdrakColors.NeonCyan)
                    listOf("🔇  Quiet environment", "📱  Hold phone 20–30 cm away", "🗣️  Normal speaking volume").forEach {
                        Text(it, fontSize = 12.sp, color = EdrakColors.Slate400, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// ─── Step 5: Success ──────────────────────────────────────────────────────────

@Composable
private fun SuccessStep(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(EdrakColors.DeepMidnightBlue).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00E676), modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(32.dp))
        Text("Voice Setup Complete! 🎉", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text("Edrak will now use your voice passphrase to unlock the app securely each time you open it.", fontSize = 15.sp, color = EdrakColors.Slate400, textAlign = TextAlign.Center, lineHeight = 22.sp)
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

// ─── Shared: Pulsating mic button ─────────────────────────────────────────────

@Composable
private fun PulsatingMicButton(isRecording: Boolean, onClick: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(1f, if (isRecording) 1.15f else 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "ps")
    Box(contentAlignment = Alignment.Center) {
        if (isRecording) Box(modifier = Modifier.size(100.dp).scale(scale).background(Color(0xFFFF4444).copy(alpha = 0.15f), CircleShape))
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(80.dp).background(if (isRecording) Color(0xFFFF4444) else EdrakColors.NeonCyan, CircleShape),
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                tint = if (isRecording) Color.White else EdrakColors.DeepMidnightBlue,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
