package me.edrakai.features.listening.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.edrakai.core.services.EdrakListeningService
import me.edrakai.features.listening.domain.model.LiveTranscriptEntry
import me.edrakai.features.listening.domain.usecase.GetActiveConversationUseCase
import me.edrakai.features.listening.domain.usecase.ObserveLiveTranscriptUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListeningViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeLiveTranscript: ObserveLiveTranscriptUseCase,
    private val getActiveConversation: GetActiveConversationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListeningUiState())
    val uiState: StateFlow<ListeningUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var transcriptJob: Job? = null

    // ─── Service state receiver ──────────────────────────────────────────────────

    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == EdrakListeningService.ACTION_STATE_CHANGED) {
                val listening = intent.getBooleanExtra(EdrakListeningService.EXTRA_IS_LISTENING, false)
                _uiState.update { it.copy(isListening = listening) }
                if (listening) startTimer() else stopTimer()
            }
        }
    }

    init {
        // Sync with current service state (service may already be running)
        _uiState.update { it.copy(isListening = EdrakListeningService.state.value.isListening) }
        if (EdrakListeningService.state.value.isListening) startTimer()

        // Register for service state broadcasts
        ContextCompat.registerReceiver(
            context,
            serviceStateReceiver,
            IntentFilter(EdrakListeningService.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Also observe via StateFlow (handles same-process updates)
        viewModelScope.launch {
            EdrakListeningService.state.collect { state ->
                _uiState.update { it.copy(isListening = state.isListening) }
            }
        }

        loadTranscript()
    }

    // ─── User actions ────────────────────────────────────────────────────────────

    fun onStartListening() {
        Timber.d("ListeningViewModel: starting service")
        _uiState.update { it.copy(errorMessage = null) }
        EdrakListeningService.start(context)
    }

    fun onStopListening() {
        Timber.d("ListeningViewModel: stopping service")
        EdrakListeningService.stop(context)
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ─── Internal ────────────────────────────────────────────────────────────────

    private fun loadTranscript() {
        transcriptJob?.cancel()
        transcriptJob = viewModelScope.launch {
            val conversationId = getActiveConversation() ?: return@launch
            observeLiveTranscript(conversationId).collect { entries ->
                _uiState.update { it.copy(transcriptLines = entries) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = 0L
            while (true) {
                _uiState.update { it.copy(sessionDurationSeconds = seconds) }
                delay(1000)
                seconds++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(sessionDurationSeconds = 0L) }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching { context.unregisterReceiver(serviceStateReceiver) }
    }
}

// ─── UI State ─────────────────────────────────────────────────────────────────

data class ListeningUiState(
    val isListening: Boolean           = false,
    val transcriptLines: List<LiveTranscriptEntry> = emptyList(),
    val sessionDurationSeconds: Long   = 0L,
    val errorMessage: String?          = null
)
