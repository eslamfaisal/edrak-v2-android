package me.edrakai.features.onboarding.presentation

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.edrakai.core.audio.WavWriter
import me.edrakai.core.security.TokenManager
import me.edrakai.features.onboarding.domain.usecase.EnrollVoiceUseCase
import me.edrakai.features.onboarding.domain.usecase.VerifyVoiceUseCase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Fixed enrollment phrases (phrases 1 and 2).
 * Phrase 0 is dynamic — it's the passphrase the user sets during onboarding.
 */
val FIXED_ENROLLMENT_PHRASES = listOf(
    "Edrak يتعرف على صوتي تلقائياً",
    "أنا مستخدم Edrak وأستخدمه كل يوم",
)

private const val SAMPLE_RATE        = 16_000
private const val CHUNK_DURATION_MS  = 2_500L
private const val CONSECUTIVE_NEEDED = 2

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val enrollVoiceUseCase: EnrollVoiceUseCase,
    private val verifyVoiceUseCase: VerifyVoiceUseCase,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** All phrases: [passphrase, phrase1, phrase2] — built after passphrase confirmed. */
    private var allEnrollmentPhrases: List<String> = emptyList()

    // Enrollment recording (MediaRecorder for M4A)
    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null

    // Live verification
    private var liveVerifyJob: Job? = null

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.StartSetup               -> _state.update { it.copy(step = OnboardingStep.PASSPHRASE_SETUP) }
            is OnboardingEvent.PassphraseChanged     -> _state.update { it.copy(passphraseInput = event.text, passphraseError = null) }
            OnboardingEvent.ConfirmPassphrase        -> confirmPassphrase()
            OnboardingEvent.StartRecording           -> startEnrollmentRecording()
            OnboardingEvent.StopRecording            -> stopEnrollmentAndUpload()
            OnboardingEvent.RetryUpload              -> retryUpload()
            OnboardingEvent.StartLiveVerification    -> startLiveVerification()
            OnboardingEvent.StopLiveVerification     -> stopLiveVerification()
            OnboardingEvent.Continue                 -> navigateToHome()
        }
    }

    // ─── Passphrase Setup ──────────────────────────────────────────────────────

    private fun confirmPassphrase() {
        val phrase = _state.value.passphraseInput.trim()
        if (phrase.length < 3) {
            _state.update { it.copy(passphraseError = "Passphrase must be at least 3 characters") }
            return
        }
        // Save encrypted passphrase and build full enrollment list
        tokenManager.savePassphrase(phrase)
        allEnrollmentPhrases = listOf(phrase) + FIXED_ENROLLMENT_PHRASES
        _state.update { it.copy(step = OnboardingStep.RECORDING, passphraseError = null) }
        Timber.d("Passphrase confirmed — enrollment phrases: ${allEnrollmentPhrases.size}")
    }

    // ─── Enrollment Recording ──────────────────────────────────────────────────

    private fun startEnrollmentRecording() {
        if (_state.value.isRecording) return
        val file = File(context.cacheDir, "voice_phrase_${System.currentTimeMillis()}.m4a")
        currentAudioFile = file

        @Suppress("DEPRECATION")
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context) else MediaRecorder()

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(SAMPLE_RATE)
            setAudioEncodingBitRate(64_000)
            setOutputFile(file.absolutePath)
            try {
                prepare()
                start()
                mediaRecorder = this
                _state.update { it.copy(isRecording = true, uploadError = null) }
                Timber.d("Enrollment recording started: ${file.name}")
            } catch (e: Exception) {
                Timber.e(e, "MediaRecorder failed")
                release()
                viewModelScope.launch { _effects.send(OnboardingEffect.ShowError("Microphone unavailable: ${e.message}")) }
            }
        }
    }

    private fun stopEnrollmentAndUpload() {
        val file = currentAudioFile ?: return
        mediaRecorder?.let { try { it.stop() } catch (_: Exception) {}; it.release() }
        mediaRecorder = null
        _state.update { it.copy(isRecording = false) }
        uploadEnrollmentPhrase(file)
    }

    private fun uploadEnrollmentPhrase(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true) }
            val result = withContext(Dispatchers.IO) { enrollVoiceUseCase(file) }
            _state.update { it.copy(isUploading = false) }

            result.onSuccess {
                val nextIndex = _state.value.currentPhraseIndex + 1
                val recorded  = _state.value.recordedCount + 1
                if (nextIndex >= allEnrollmentPhrases.size) {
                    _state.update {
                        it.copy(
                            step = OnboardingStep.VERIFICATION,
                            currentPhraseIndex = nextIndex,
                            recordedCount = recorded,
                            verificationStatus = VerificationStatus.IDLE,
                            verificationMessage = "Tap the mic and speak naturally",
                        )
                    }
                } else {
                    _state.update { it.copy(currentPhraseIndex = nextIndex, recordedCount = recorded) }
                }
            }.onFailure { err ->
                _state.update { it.copy(uploadError = err.message ?: "Upload failed") }
                Timber.e(err, "Enrollment upload failed")
            }
        }
    }

    private fun retryUpload() {
        val file = currentAudioFile
        if (file != null && file.exists()) uploadEnrollmentPhrase(file)
        else _state.update { it.copy(uploadError = null) }
    }

    /** Returns the phrase for the current enrollment index, or empty if not ready. */
    fun currentEnrollmentPhrase(): String =
        allEnrollmentPhrases.getOrNull(_state.value.currentPhraseIndex) ?: ""

    // ─── Live Verification ─────────────────────────────────────────────────────

    private fun startLiveVerification() {
        if (liveVerifyJob?.isActive == true) return
        _state.update {
            it.copy(
                verificationStatus  = VerificationStatus.LISTENING,
                liveConfidence      = 0f,
                consecutiveMatches  = 0,
                verificationMessage = "🎤  Speak naturally, we're listening…",
            )
        }

        liveVerifyJob = viewModelScope.launch(Dispatchers.IO) {
            val minBuffer  = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val chunkBytes = (SAMPLE_RATE * CHUNK_DURATION_MS / 1000 * 2).toInt()
            val bufferSize = maxOf(minBuffer, chunkBytes)

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize,
            )
            try {
                audioRecord.startRecording()
                while (isActive && _state.value.verificationStatus != VerificationStatus.MATCHED) {
                    val pcm = ByteArray(chunkBytes)
                    var bytesRead = 0
                    while (bytesRead < chunkBytes && isActive) {
                        val r = audioRecord.read(pcm, bytesRead, chunkBytes - bytesRead)
                        if (r > 0) bytesRead += r else break
                    }
                    if (!isActive) break

                    _state.update { it.copy(verificationStatus = VerificationStatus.ANALYZING, verificationMessage = "⚡  Analyzing…") }
                    val wav = File(context.cacheDir, "verify_${System.currentTimeMillis()}.wav")
                    WavWriter.write(wav, pcm.copyOf(bytesRead))

                    verifyVoiceUseCase(wav).onSuccess { vr ->
                        val newMatches = if (vr.match) _state.value.consecutiveMatches + 1 else 0
                        val msg = when {
                            vr.match && newMatches >= CONSECUTIVE_NEEDED -> "✅  Voice recognized!"
                            vr.match                                     -> "⚡  Almost there! Keep speaking…"
                            vr.confidence > 0.65f                        -> "🔄  Getting closer, keep talking…"
                            else                                         -> "🎤  Make sure you're in a quiet place…"
                        }
                        _state.update {
                            it.copy(
                                liveConfidence     = vr.confidence,
                                consecutiveMatches = newMatches,
                                verificationMessage = msg,
                                verificationStatus = when {
                                    newMatches >= CONSECUTIVE_NEEDED -> VerificationStatus.MATCHED
                                    vr.match -> VerificationStatus.LISTENING
                                    else     -> VerificationStatus.NO_MATCH
                                },
                            )
                        }
                        if (newMatches >= CONSECUTIVE_NEEDED) {
                            tokenManager.setVoiceSetupComplete(true)
                            delay(1_800)
                            _state.update { it.copy(step = OnboardingStep.SUCCESS) }
                        } else {
                            delay(500)
                            if (_state.value.verificationStatus != VerificationStatus.MATCHED) {
                                _state.update { it.copy(verificationStatus = VerificationStatus.LISTENING, verificationMessage = "🎤  Keep speaking…") }
                            }
                        }
                    }.onFailure { err ->
                        Timber.w(err, "Verify chunk failed — retrying")
                        delay(500)
                        _state.update { it.copy(verificationStatus = VerificationStatus.LISTENING, verificationMessage = "🎤  Speak naturally, we're listening…") }
                    }
                }
            } finally {
                try { audioRecord.stop(); audioRecord.release() } catch (_: Exception) {}
            }
        }
    }

    private fun stopLiveVerification() {
        liveVerifyJob?.cancel()
        liveVerifyJob = null
        if (_state.value.verificationStatus != VerificationStatus.MATCHED) {
            _state.update { it.copy(verificationStatus = VerificationStatus.IDLE, verificationMessage = "Tap the mic and speak naturally") }
        }
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private fun navigateToHome() {
        viewModelScope.launch { _effects.send(OnboardingEffect.NavigateToHome) }
    }

    override fun onCleared() {
        liveVerifyJob?.cancel()
        mediaRecorder?.apply { try { stop() } catch (_: Exception) {}; release() }
        super.onCleared()
    }
}
