package me.edrakai.features.listening.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.edrakai.features.listening.domain.model.LiveTranscriptEntry
import me.edrakai.ui.theme.EdrakColors

@Composable
fun ListeningScreen(
    onNavigateBack: () -> Unit,
    viewModel: ListeningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Auto-scroll to latest entry
    LaunchedEffect(uiState.transcriptLines.size) {
        if (uiState.transcriptLines.isNotEmpty()) {
            listState.animateScrollToItem(uiState.transcriptLines.lastIndex)
        }
    }

    // Error snackbar
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        containerColor = EdrakColors.DeepMidnightBlue,
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Header ─────────────────────────────────────────────────────────
            Text(
                text = "Live Listening",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(4.dp))

            // Session timer
            val durationText = remember(uiState.sessionDurationSeconds) {
                val s = uiState.sessionDurationSeconds
                "%02d:%02d".format(s / 60, s % 60)
            }
            Text(
                text = if (uiState.isListening) durationText else "Tap to start",
                color = if (uiState.isListening) EdrakColors.NeonCyan else EdrakColors.Slate400,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            // ── Pulsing Mic Button ──────────────────────────────────────────────
            PulsingMicButton(
                isListening = uiState.isListening,
                onToggle = {
                    if (uiState.isListening) viewModel.onStopListening()
                    else viewModel.onStartListening()
                }
            )

            Spacer(Modifier.height(32.dp))

            // ── Live Transcript ─────────────────────────────────────────────────
            if (uiState.transcriptLines.isEmpty()) {
                EmptyTranscriptPlaceholder(isListening = uiState.isListening)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.transcriptLines, key = { it.id }) { entry ->
                        TranscriptEntryCard(entry = entry)
                    }
                }
            }
        }
    }
}

// ─── Pulsing Mic Button ────────────────────────────────────────────────────────

@Composable
private fun PulsingMicButton(isListening: Boolean, onToggle: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = if (isListening) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer glow ring (visible only when listening)
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(pulseScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                EdrakColors.NeonCyan.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        // Mic button
        FloatingActionButton(
            onClick = onToggle,
            modifier = Modifier.size(72.dp),
            containerColor = if (isListening) EdrakColors.NeonCyan else EdrakColors.Slate700,
            contentColor  = if (isListening) EdrakColors.DeepMidnightBlue else Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = if (isListening) 12.dp else 4.dp
            )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = if (isListening) "Stop listening" else "Start listening",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ─── Transcript Entry ──────────────────────────────────────────────────────────

@Composable
private fun TranscriptEntryCard(entry: LiveTranscriptEntry) {
    val isUser = entry.speakerTag == "YOU" || entry.speakerTag == "SPEAKER_1"
    val speakerColor = when (entry.speakerTag) {
        "YOU", "SPEAKER_1"    -> EdrakColors.NeonCyan
        "SPEAKER_OTHER"       -> Color(0xFFB0AFF0)    // soft violet for "other"
        else                  -> EdrakColors.Slate400
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = if (isUser) EdrakColors.NeonCyan.copy(alpha = 0.1f)
                            else Color(0xFF1A1F35),
                    shape = RoundedCornerShape(
                        topStart    = if (isUser) 16.dp else 4.dp,
                        topEnd      = if (isUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd   = 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text  = entry.speakerTag,
                color = speakerColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = entry.text,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                overflow = TextOverflow.Clip
            )
        }
    }
}

// ─── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyTranscriptPlaceholder(isListening: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isListening) "Listening for speech…"
                   else "Start listening to see the transcript",
            color = EdrakColors.Slate400,
            fontSize = 15.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
