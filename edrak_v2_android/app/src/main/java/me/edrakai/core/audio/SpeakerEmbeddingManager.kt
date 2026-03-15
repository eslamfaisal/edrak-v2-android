package me.edrakai.core.audio

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Speaker Embedding Manager — Phase 3A stub.
 *
 * This is a pure-Kotlin placeholder that:
 *   - Compiles cleanly without native SHERPA-ONNX dependency
 *   - Returns `null` for embeddings (disables diarization gracefully)
 *   - Is a drop-in replace target when the native AAR is bundled in Phase 3C
 *
 * ## To activate real on-device diarization (Phase 3C):
 *   1. Download `sherpa-onnx-android-<version>.aar` from:
 *      https://github.com/k2-fsa/sherpa-onnx/releases
 *   2. Place it in `app/libs/`
 *   3. Add to `app/build.gradle.kts`:
 *      ```kotlin
 *      implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
 *      ```
 *   4. Uncomment the SHERPA-ONNX imports in this file and replace the stub body
 *      with the real SpeakerEmbeddingExtractor wiring (see commented code below).
 *
 * Model to use: `nemo_en_speakernet.onnx` — place in `app/src/main/assets/sherpa-onnx/`
 */
@Singleton
class SpeakerEmbeddingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val SIMILARITY_THRESHOLD = 0.85f
        // TODO Phase 3C: uncomment when AAR is bundled
        // private const val MODEL_ASSET = "sherpa-onnx/nemo_en_speakernet.onnx"
    }

    // TODO Phase 3C: initialize SHERPA-ONNX extractor
    // private val extractor: SpeakerEmbeddingExtractor by lazy {
    //     val modelPath = copyAssetToCache(MODEL_ASSET)
    //     val config = SpeakerEmbeddingExtractorConfig(model = modelPath, numThreads = 2, debug = false)
    //     SpeakerEmbeddingExtractor(config = config)
    // }

    /**
     * Extracts a speaker embedding from normalized float PCM audio.
     *
     * **Phase 3A stub:** always returns null → service gracefully returns "SPEAKER_1" label.
     * **Phase 3C:** replace with real SHERPA-ONNX `SpeakerEmbeddingExtractor` call.
     */
    fun extractEmbedding(samples: FloatArray, sampleRate: Int = 16000): FloatArray? {
        if (samples.size < sampleRate / 2) return null
        // TODO Phase 3C: use SHERPA-ONNX extractor
        // val stream = extractor.createStream()
        // stream.acceptWaveform(samples, sampleRate)
        // if (!extractor.isReady(stream)) return null
        // extractor.computeEmbeddingInPlace(stream)
        // return stream.embedding
        Timber.v("SpeakerEmbeddingManager: stub — SHERPA-ONNX not yet bundled (Phase 3C)")
        return null
    }

    /**
     * Cosine similarity between two embedding vectors.
     * Pure-Kotlin, no native code needed.
     */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        val len = minOf(a.size, b.size)
        var dot = 0f; var normA = 0f; var normB = 0f
        for (i in 0 until len) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom < 1e-6f) 0f else dot / denom
    }

    /**
     * Returns true if cosine similarity exceeds threshold.
     */
    fun isUser(
        liveEmbedding: FloatArray,
        userEmbedding: FloatArray,
        threshold: Float = SIMILARITY_THRESHOLD
    ): Boolean = cosineSimilarity(liveEmbedding, userEmbedding) >= threshold

    // TODO Phase 3C: uncomment for asset-to-cache copy
    // private fun copyAssetToCache(assetName: String): String { ... }
}
