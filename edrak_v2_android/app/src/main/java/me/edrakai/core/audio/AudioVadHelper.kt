package me.edrakai.core.audio

import timber.log.Timber
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Voice Activity Detection using RMS energy on raw 16-bit PCM frames.
 *
 * Uses the dBFS (decibels relative to full scale) metric:
 *   dBFS = 20 * log10( RMS / 32768.0 )
 *
 * Typical values:
 *   - Silence / ambient noise: -60 to -40 dBFS
 *   - Normal speech: -30 to -10 dBFS
 *
 * Default threshold: -40 dBFS — tunable per environment.
 */
object AudioVadHelper {

    private const val DEFAULT_SPEECH_THRESHOLD_DBFS = -40.0

    /**
     * Returns true if the given PCM frame contains speech.
     *
     * @param frame    16-bit signed PCM samples (one audio frame, typically 10ms = 160 samples @ 16kHz)
     * @param threshold  dBFS threshold above which we consider audio as speech (default -40 dBFS)
     */
    fun isSpeech(
        frame: ShortArray,
        threshold: Double = DEFAULT_SPEECH_THRESHOLD_DBFS
    ): Boolean {
        if (frame.isEmpty()) return false
        val rms = computeRms(frame)
        if (rms < 1.0) return false   // guard against pure silence (avoid log10(0))
        val dbfs = 20.0 * log10(rms / 32768.0)
        return dbfs > threshold
    }

    /**
     * Computes Root Mean Square of 16-bit PCM samples.
     */
    fun computeRms(frame: ShortArray): Double {
        var sum = 0.0
        for (sample in frame) sum += sample.toLong() * sample.toLong()
        return sqrt(sum / frame.size)
    }

    /**
     * Converts a ShortArray to FloatArray normalized to [-1.0, 1.0].
     * Required by SHERPA-ONNX embedding extractor.
     */
    fun toFloatNormalized(frame: ShortArray): FloatArray =
        FloatArray(frame.size) { i -> frame[i] / 32768.0f }

    /**
     * Converts a larger ShortArray buffer (e.g. 1-second worth) to normalized FloatArray.
     */
    fun bufferToFloat(buffer: ShortArray, validSamples: Int): FloatArray =
        FloatArray(validSamples) { i -> buffer[i] / 32768.0f }
}
