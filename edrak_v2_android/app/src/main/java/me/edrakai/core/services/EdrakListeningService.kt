package me.edrakai.core.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.edrakai.core.audio.AudioVadHelper
import me.edrakai.core.audio.SpeakerEmbeddingManager
import me.edrakai.core.notifications.ListeningNotificationHelper
import me.edrakai.core.notifications.ListeningNotificationHelper.Companion.ACTION_STOP
import me.edrakai.core.notifications.ListeningNotificationHelper.Companion.NOTIFICATION_ID
import timber.log.Timber
import javax.inject.Inject

/**
 * EdrakListeningService — Phase 3A
 *
 * Foreground service that captures microphone audio continuously.
 * Pipeline (per 10ms frame = 160 samples @ 16kHz):
 *   1. AudioRecord → ShortArray frame
 *   2. AudioVadHelper.isSpeech() for voice activity detection
 *   3. Speech frames accumulated into 1-second speech buffer
 *   4. On chunk boundary → SpeakerEmbeddingManager for speaker diarization
 *   5. Emits LiveAudioChunk via [audioChunkFlow] for ViewModel consumption
 *
 * BroadcastReceiver handles ACTION_STOP from the notification "Stop" button.
 */
@AndroidEntryPoint
class EdrakListeningService : Service() {

    @Inject lateinit var notificationHelper: ListeningNotificationHelper
    @Inject lateinit var speakerEmbeddingManager: SpeakerEmbeddingManager

    // ─── Audio configuration ─────────────────────────────────────────────────────
    private val SAMPLE_RATE       = 16_000           // 16 kHz
    private val FRAME_SAMPLES     = 160              // 10ms frame @ 16kHz
    private val CHUNK_SAMPLES     = SAMPLE_RATE      // 1-second accumulation
    private val CHANNEL_CONFIG    = android.media.AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT      = android.media.AudioFormat.ENCODING_PCM_16BIT

    // ─── State ───────────────────────────────────────────────────────────────────
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Speaker label for the enrolled user — populated from DataStore in Phase 3B */
    private var enrolledUserEmbedding: FloatArray? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioChunkFlow = MutableStateFlow<LiveAudioChunk?>(null)
    val audioChunkFlow: StateFlow<LiveAudioChunk?> = _audioChunkFlow.asStateFlow()

    // ─── Companion / shared state (accessed by ViewModel via singleton instance) ─
    companion object {
        private val _state = MutableStateFlow(ListeningServiceState())
        val state: StateFlow<ListeningServiceState> = _state.asStateFlow()

        const val ACTION_STATE_CHANGED = "me.edrakai.action.LISTENING_STATE_CHANGED"
        const val EXTRA_IS_LISTENING   = "is_listening"

        fun start(context: Context) {
            val intent = Intent(context, EdrakListeningService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, EdrakListeningService::class.java))
        }
    }

    // ─── Notification stop receiver ──────────────────────────────────────────────
    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_STOP) {
                Timber.d("EdrakListeningService: STOP broadcast received")
                stop(this@EdrakListeningService)
            }
        }
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        ContextCompat.registerReceiver(
            this,
            stopReceiver,
            IntentFilter(ACTION_STOP),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        Timber.d("EdrakListeningService: created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIFICATION_ID,
            notificationHelper.buildListeningNotification(isListening = true)
        )
        startRecording()
        return START_STICKY
    }

    override fun onDestroy() {
        stopRecording()
        unregisterReceiver(stopReceiver)
        serviceScope.cancel()
        updateState(isListening = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
        Timber.d("EdrakListeningService: destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ─── Recording Pipeline ──────────────────────────────────────────────────────

    private fun startRecording() {
        if (_isListening.value) return
        val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val bufferSize = maxOf(minBuffer, FRAME_SAMPLES * 4)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            ).also { it.startRecording() }
        } catch (e: SecurityException) {
            Timber.e(e, "EdrakListeningService: RECORD_AUDIO permission not granted")
            stopSelf(); return
        }

        // Request transient audio focus (duck music, don't stop navigation)
        requestAudioFocus()

        _isListening.value = true
        updateState(isListening = true)

        recordingJob = serviceScope.launch {
            val frame       = ShortArray(FRAME_SAMPLES)
            val speechBuf   = ShortArray(CHUNK_SAMPLES)
            var speechCount = 0
            var chunkIndex  = 0

            while (isActive) {
                val read = audioRecord?.read(frame, 0, FRAME_SAMPLES) ?: break
                if (read <= 0) continue

                if (AudioVadHelper.isSpeech(frame)) {
                    // Accumulate speech frames
                    val copyLen = minOf(read, CHUNK_SAMPLES - speechCount)
                    System.arraycopy(frame, 0, speechBuf, speechCount, copyLen)
                    speechCount += copyLen

                    // When we have 1 second of speech, emit a chunk
                    if (speechCount >= CHUNK_SAMPLES) {
                        val pcmFloat = AudioVadHelper.bufferToFloat(speechBuf, speechCount)
                        val speakerLabel = resolveSpeaker(pcmFloat)
                        _audioChunkFlow.value = LiveAudioChunk(
                            index       = chunkIndex++,
                            pcmFloats   = pcmFloat,
                            speakerTag  = speakerLabel,
                            durationMs  = 1000L
                        )
                        speechCount = 0
                    }
                } else if (speechCount > 0) {
                    // Silence detected: flush whatever we accumulated
                    val pcmFloat = AudioVadHelper.bufferToFloat(speechBuf, speechCount)
                    val speakerLabel = resolveSpeaker(pcmFloat)
                    _audioChunkFlow.value = LiveAudioChunk(
                        index      = chunkIndex++,
                        pcmFloats  = pcmFloat,
                        speakerTag = speakerLabel,
                        durationMs = (speechCount * 1000L) / SAMPLE_RATE
                    )
                    speechCount = 0
                }
            }

            Timber.d("EdrakListeningService: recording loop ended")
        }

        Timber.i("EdrakListeningService: recording started")
    }

    private fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        abandonAudioFocus()
        _isListening.value = false
        Timber.i("EdrakListeningService: recording stopped")
    }

    // ─── Speaker Diarization ─────────────────────────────────────────────────────

    /**
     * Attempts to match the PCM chunk to the enrolled user embedding.
     * Falls back to "SPEAKER_1" labeling if no embedding is available yet (Phase 3B wires this).
     */
    private fun resolveSpeaker(pcmFloat: FloatArray): String {
        val enrolled = enrolledUserEmbedding ?: return "SPEAKER_1"
        val live = speakerEmbeddingManager.extractEmbedding(pcmFloat)
            ?: return "SPEAKER_UNKNOWN"
        return if (speakerEmbeddingManager.isUser(live, enrolled)) "YOU" else "SPEAKER_OTHER"
    }

    /** Called from Phase 3B once the enrollment embedding is loaded from DataStore. */
    fun setUserEmbedding(embedding: FloatArray) {
        enrolledUserEmbedding = embedding
        Timber.d("EdrakListeningService: user embedding set (${embedding.size} dims)")
    }

    // ─── Audio Focus ─────────────────────────────────────────────────────────────

    private var audioFocusRequest: AudioFocusRequest? = null

    private fun requestAudioFocus() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setOnAudioFocusChangeListener { /* no-op: we continue recording regardless */ }
            .build()
            .also { am.requestAudioFocus(it) }
    }

    private fun abandonAudioFocus() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }

    // ─── State broadcasting ──────────────────────────────────────────────────────

    private fun updateState(isListening: Boolean) {
        _state.value = ListeningServiceState(isListening = isListening)
        sendBroadcast(
            Intent(ACTION_STATE_CHANGED)
                .setPackage(packageName)
                .putExtra(EXTRA_IS_LISTENING, isListening)
        )
    }
}

// ─── Data classes ────────────────────────────────────────────────────────────────

data class LiveAudioChunk(
    val index: Int,
    val pcmFloats: FloatArray,
    val speakerTag: String,    // "YOU", "SPEAKER_OTHER", "SPEAKER_UNKNOWN", "SPEAKER_1"
    val durationMs: Long
) {
    override fun equals(other: Any?) = other is LiveAudioChunk && index == other.index
    override fun hashCode() = index
}

data class ListeningServiceState(
    val isListening: Boolean = false
)
