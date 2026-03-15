package me.edrakai.features.onboarding.presentation

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.edrakai.core.security.TokenManager
import me.edrakai.features.onboarding.domain.usecase.EnrollVoiceUseCase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/** Phrases the user reads aloud during voice enrollment. */
val ENROLLMENT_PHRASES = listOf(
    "مرحباً، هذا صوتي في Edrak",
    "Edrak يتعرف على صوتي تلقائياً",
    "أنا مستخدم Edrak وأستخدمه كل يوم",
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val enrollVoiceUseCase: EnrollVoiceUseCase,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.StartSetup     -> _state.update { it.copy(step = OnboardingStep.RECORDING) }
            OnboardingEvent.StartRecording -> startRecording()
            OnboardingEvent.StopRecording  -> stopRecordingAndUpload()
            OnboardingEvent.RetryUpload    -> retryUpload()
            OnboardingEvent.Continue       -> navigateToHome()
        }
    }

    // ─── Recording ────────────────────────────────────────────────────────────

    private fun startRecording() {
        if (_state.value.isRecording) return
        val file = File(context.cacheDir, "voice_phrase_${System.currentTimeMillis()}.m4a")
        currentAudioFile = file

        @Suppress("DEPRECATION")
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context)
        else
            MediaRecorder()

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(64000)
            setOutputFile(file.absolutePath)
            try {
                prepare()
                start()
                mediaRecorder = this
                _state.update { it.copy(isRecording = true, uploadError = null) }
                Timber.d("Recording started: ${file.name}")
            } catch (e: Exception) {
                Timber.e(e, "MediaRecorder failed to start")
                release()
                viewModelScope.launch {
                    _effects.send(OnboardingEffect.ShowError("Microphone unavailable: ${e.message}"))
                }
            }
        }
    }

    private fun stopRecordingAndUpload() {
        val file = currentAudioFile ?: return
        mediaRecorder?.let {
            try { it.stop() } catch (_: Exception) {}
            it.release()
            mediaRecorder = null
        }
        _state.update { it.copy(isRecording = false) }
        uploadPhrase(file)
    }

    private fun uploadPhrase(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true) }
            // Per Phase 1C spec: mock the API call while backend (Prompt 2C) is pending
            val result = mockEnroll(file)
            _state.update { it.copy(isUploading = false) }

            result.onSuccess {
                file.delete()
                val nextIndex = _state.value.currentPhraseIndex + 1
                val recorded = _state.value.recordedCount + 1
                if (nextIndex >= ENROLLMENT_PHRASES.size) {
                    // All phrases recorded → mark setup complete & go to success
                    tokenManager.setVoiceSetupComplete(true)
                    _state.update { it.copy(step = OnboardingStep.SUCCESS, recordedCount = recorded) }
                } else {
                    _state.update { it.copy(currentPhraseIndex = nextIndex, recordedCount = recorded) }
                }
            }.onFailure { err ->
                _state.update { it.copy(uploadError = err.message ?: "Upload failed") }
                Timber.e(err, "Voice enrollment upload failed")
            }
        }
    }

    private fun retryUpload() {
        val file = currentAudioFile
        if (file != null && file.exists()) {
            uploadPhrase(file)
        } else {
            _state.update { it.copy(uploadError = null) }
        }
    }

    /** Mock the backend call until Prompt 2C (backend) is implemented. */
    private suspend fun mockEnroll(file: File): Result<Unit> {
        delay(1500) // simulate network latency
        return Result.success(Unit)
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private fun navigateToHome() {
        viewModelScope.launch { _effects.send(OnboardingEffect.NavigateToHome) }
    }

    override fun onCleared() {
        mediaRecorder?.apply { try { stop() } catch (_: Exception) {}; release() }
        super.onCleared()
    }
}
