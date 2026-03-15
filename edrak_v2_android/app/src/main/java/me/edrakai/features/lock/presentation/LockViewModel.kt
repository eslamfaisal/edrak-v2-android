package me.edrakai.features.lock.presentation

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.edrakai.core.audio.WavWriter
import me.edrakai.core.security.TokenManager
import me.edrakai.features.onboarding.domain.usecase.VerifyVoiceUseCase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

private const val SAMPLE_RATE   = 16_000
private const val MAX_ATTEMPTS  = 3

@HiltViewModel
class LockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val verifyVoiceUseCase: VerifyVoiceUseCase,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LockState(
            displayName = tokenManager.getUserDisplayName() ?: "there",
            passphrase  = tokenManager.getPassphrase() ?: "",
        )
    )
    val state: StateFlow<LockState> = _state.asStateFlow()

    private val _effects = Channel<LockEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    // Recording
    private var audioRecord: AudioRecord? = null
    private var capturedPcm: ByteArray? = null

    fun onEvent(event: LockEvent) {
        when (event) {
            LockEvent.StartRecording -> startRecording()
            LockEvent.StopAndVerify  -> stopAndVerify()
            LockEvent.Retry          -> resetForRetry()
            LockEvent.ForgotVoice    -> forgotVoice()
        }
    }

    // ─── Recording ────────────────────────────────────────────────────────────

    private fun startRecording() {
        if (_state.value.isRecording) return
        viewModelScope.launch(Dispatchers.IO) {
            val minBuffer = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            )
            val rec = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuffer,
            )
            audioRecord = rec
            rec.startRecording()
            _state.update { it.copy(isRecording = true, status = LockStatus.RECORDING, statusMessage = "🎤  Speaking…  Release when done") }
            Timber.d("LockScreen: recording started")
        }
    }

    private fun stopAndVerify() {
        if (!_state.value.isRecording) return
        viewModelScope.launch(Dispatchers.IO) {
            // Drain remaining PCM from AudioRecord
            val chunks = mutableListOf<ByteArray>()
            val rec = audioRecord ?: return@launch
            val buf = ByteArray(SAMPLE_RATE * 2)   // 1 s worth
            var read: Int
            while (rec.read(buf, 0, buf.size).also { read = it } > 0) {
                chunks += buf.copyOf(read)
                // Stop when we've read 1 extra buffer after user released
                break
            }
            rec.stop()
            rec.release()
            audioRecord = null

            val pcm = chunks.fold(ByteArray(0)) { acc, b -> acc + b }
            capturedPcm = pcm

            _state.update { it.copy(isRecording = false, status = LockStatus.ANALYZING, statusMessage = "⚡  Analyzing your voice…") }

            verify(pcm)
        }
    }

    private suspend fun verify(pcm: ByteArray) {
        val wavFile = File(context.cacheDir, "lock_verify_${System.currentTimeMillis()}.wav")
        WavWriter.write(wavFile, pcm)

        val result = withContext(Dispatchers.IO) { verifyVoiceUseCase(wavFile) }
        result.onSuccess { vr ->
            val newAttempts = _state.value.attemptCount + if (!vr.match) 1 else 0
            _state.update { it.copy(liveConfidence = vr.confidence) }

            when {
                vr.match -> {
                    _state.update { it.copy(status = LockStatus.MATCHED, statusMessage = "✅  Voice recognized! Unlocking…") }
                    Timber.i("LockScreen: voice verified — confidence=${vr.confidence}")
                    kotlinx.coroutines.delay(1_200)
                    _effects.send(LockEffect.NavigateToHome)
                }
                newAttempts >= MAX_ATTEMPTS -> {
                    _state.update { it.copy(status = LockStatus.LOCKED_OUT, attemptCount = newAttempts, statusMessage = "🔒  Too many attempts. Please sign in again.") }
                    Timber.w("LockScreen: locked out after $MAX_ATTEMPTS attempts")
                    kotlinx.coroutines.delay(2_000)
                    tokenManager.clearTokens()
                    _effects.send(LockEffect.NavigateToLogin)
                }
                else -> {
                    _state.update {
                        it.copy(
                            status       = LockStatus.FAILED,
                            attemptCount = newAttempts,
                            statusMessage = "❌  Not recognized. ${MAX_ATTEMPTS - newAttempts} attempt${if (MAX_ATTEMPTS - newAttempts == 1) "" else "s"} left.",
                        )
                    }
                }
            }
        }.onFailure { err ->
            Timber.e(err, "LockScreen: verify network error")
            _state.update { it.copy(status = LockStatus.FAILED, statusMessage = "Network error. Please try again.") }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun resetForRetry() {
        _state.update { it.copy(status = LockStatus.IDLE, liveConfidence = 0f, statusMessage = "Say your passphrase to unlock") }
    }

    private fun forgotVoice() {
        viewModelScope.launch {
            tokenManager.clearTokens()
            _effects.send(LockEffect.NavigateToLogin)
        }
    }

    override fun onCleared() {
        audioRecord?.apply { try { stop() } catch (_: Exception) {}; release() }
        super.onCleared()
    }
}
