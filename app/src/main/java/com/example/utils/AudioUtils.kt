package com.example.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object AudioUtils {

    /**
     * Retrieves duration of an audio URI in milliseconds.
     */
    fun getAudioDurationMs(context: Context, uri: Uri): Long {
        var retriever: MediaMetadataRetriever? = null
        return try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 10000L
        } catch (e: Exception) {
            10000L
        } finally {
            try {
                retriever?.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * Generates a deterministic list of waveform peak amplitudes (values between 0.1f and 1.0f)
     * for drawing clean waveforms on audio clips in the timeline.
     */
    fun generateWaveformPeaks(clipId: String, sampleCount: Int = 40): List<Float> {
        val seed = clipId.hashCode()
        val random = java.util.Random(seed.toLong())
        return List(sampleCount) { idx ->
            val base = 0.2f + random.nextFloat() * 0.7f
            // add some rhythmic variation
            val sine = Math.sin(idx * 0.4).toFloat() * 0.15f
            (base + sine).coerceIn(0.15f, 1.0f)
        }
    }

    /**
     * Generates a valid offline PCM WAV audio file for demo/royalty-free music tracks
     * so previewing and playing built-in music functions 100% offline with actual audio playback.
     */
    fun getOrCreateDemoAudioFile(context: Context, filename: String, baseFrequency: Double, durationSec: Int): File {
        val cacheDir = context.cacheDir
        val audioFile = File(cacheDir, filename)
        if (audioFile.exists() && audioFile.length() > 0) {
            return audioFile
        }

        try {
            val sampleRate = 22050
            val numSamples = sampleRate * durationSec
            val sample = DoubleArray(numSamples)
            val generatedSnd = ByteArray(2 * numSamples)

            // Generate chord / melody structure
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val chordFreq = when (((t / 1.5).toInt()) % 4) {
                    0 -> baseFrequency // Root
                    1 -> baseFrequency * 1.25 // Major 3rd
                    2 -> baseFrequency * 1.5 // 5th
                    else -> baseFrequency * 1.125 // Minor 2nd / variation
                }
                // Envelope (fade in / fade out every 1.5s)
                val env = Math.sin(Math.PI * (t % 1.5) / 1.5)
                val wave = Math.sin(2.0 * Math.PI * chordFreq * t) * env
                val valShort = (wave * 20000).toInt().coerceIn(-32000, 32000)

                generatedSnd[2 * i] = (valShort and 0x00ff).toByte()
                generatedSnd[2 * i + 1] = ((valShort and 0xff00) shr 8).toByte()
            }

            val out = FileOutputStream(audioFile)
            writeWavHeader(out, sampleRate, numSamples, 1, 16)
            out.write(generatedSnd)
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return audioFile
    }

    private fun writeWavHeader(out: FileOutputStream, sampleRate: Int, totalSamples: Int, channels: Int, bitDepth: Int) {
        val totalDataLen = totalSamples * channels * (bitDepth / 8)
        val totalAudioLen = totalDataLen + 36
        val byteRate = sampleRate * channels * (bitDepth / 8)

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        header[4] = (totalAudioLen and 0xff).toByte()
        header[5] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[6] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[7] = ((totalAudioLen shr 24) and 0xff).toByte()

        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        header[16] = 16 // length of format data
        header[17] = 0
        header[18] = 0
        header[19] = 0

        header[20] = 1 // PCM
        header[21] = 0

        header[22] = channels.toByte()
        header[23] = 0

        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()

        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()

        header[32] = (channels * (bitDepth / 8)).toByte() // block align
        header[33] = 0

        header[34] = bitDepth.toByte()
        header[35] = 0

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        header[40] = (totalDataLen and 0xff).toByte()
        header[41] = ((totalDataLen shr 8) and 0xff).toByte()
        header[42] = ((totalDataLen shr 16) and 0xff).toByte()
        header[43] = ((totalDataLen shr 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }
}
