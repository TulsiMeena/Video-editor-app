package com.example.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object VideoDecoder {
    private const val TAG = "VideoDecoder"

    /**
     * Extracts frame bitmap at specific time (in ms) from video URI.
     */
    suspend fun extractFrameAtTime(context: Context, uri: Uri, timeMs: Long): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        return@withContext try {
            retriever.setDataSource(context, uri)
            val timeUs = timeMs * 1000L
            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(timeUs)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting frame at $timeMs ms: ${e.message}")
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * Creates a freeze frame image file on disk and returns absolute path.
     */
    suspend fun createFreezeFrameImage(context: Context, uri: Uri, timeMs: Long): String? = withContext(Dispatchers.IO) {
        val bitmap = extractFrameAtTime(context, uri, timeMs) ?: run {
            // Fallback for image
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) { null }
        } ?: return@withContext null

        val freezeDir = File(context.filesDir, "freeze_frames").apply {
            if (!exists()) mkdirs()
        }
        val freezeFile = File(freezeDir, "freeze_${UUID.randomUUID()}.jpg")

        return@withContext try {
            FileOutputStream(freezeFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bitmap.recycle()
            freezeFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving freeze frame: ${e.message}")
            null
        }
    }
}
