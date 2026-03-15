package me.edrakai.core.audio

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Writes raw PCM-16 samples into a valid WAV file.
 * Suitable for chunked real-time recording → backend verify requests.
 */
object WavWriter {

    /**
     * Writes [pcmData] (16-bit, mono, [sampleRate] Hz) into [outputFile] as a complete WAV.
     */
    fun write(
        outputFile: File,
        pcmData: ByteArray,
        sampleRate: Int = 16_000,
        channels: Int = 1,
        bitsPerSample: Int = 16,
    ) {
        val dataLen  = pcmData.size
        val totalLen = dataLen + 36          // WAV header is 44 bytes → dataLen + 36

        FileOutputStream(outputFile).use { fos ->
            // ── RIFF header ──────────────────────────────────────────────────
            fos.write("RIFF".toByteArray())
            fos.write(int32Le(totalLen))
            fos.write("WAVE".toByteArray())

            // ── fmt sub-chunk ────────────────────────────────────────────────
            fos.write("fmt ".toByteArray())
            fos.write(int32Le(16))                          // sub-chunk size for PCM
            fos.write(int16Le(1))                           // AudioFormat = PCM
            fos.write(int16Le(channels))
            fos.write(int32Le(sampleRate))
            fos.write(int32Le(sampleRate * channels * bitsPerSample / 8)) // byte rate
            fos.write(int16Le(channels * bitsPerSample / 8))              // block align
            fos.write(int16Le(bitsPerSample))

            // ── data sub-chunk ───────────────────────────────────────────────
            fos.write("data".toByteArray())
            fos.write(int32Le(dataLen))
            fos.write(pcmData)
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun int32Le(value: Int): ByteArray =
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()

    private fun int16Le(value: Int): ByteArray =
        ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value.toShort()).array()
}
