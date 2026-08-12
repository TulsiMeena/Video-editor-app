package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.data.model.MediaItemRef
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object MediaUtils {
    private const val TAG = "MediaUtils"

    data class MediaMetadata(
        val durationMs: Long,
        val width: Int,
        val height: Int,
        val mimeType: String,
        val isValid: Boolean
    )

    fun validateAndExtractMetadata(context: Context, uri: Uri): MediaMetadata {
        val resolver = context.contentResolver
        var durationMs = 0L
        var width = 0
        var height = 0
        var mimeType = resolver.getType(uri) ?: ""

        // Check if stream is readable
        var inputStream: InputStream? = null
        try {
            inputStream = resolver.openInputStream(uri)
            if (inputStream == null) {
                return MediaMetadata(0L, 0, 0, "", false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URI input stream: ${e.message}")
            return MediaMetadata(0L, 0, 0, "", false)
        } finally {
            try {
                inputStream?.close()
            } catch (_: Exception) {}
        }

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val extractedMime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

            if (!extractedMime.isNullOrEmpty()) {
                mimeType = extractedMime
            }

            durationMs = durationStr?.toLongOrNull() ?: 0L
            width = widthStr?.toIntOrNull() ?: 0
            height = heightStr?.toIntOrNull() ?: 0

        } catch (e: Exception) {
            Log.w(TAG, "MediaMetadataRetriever notice: ${e.message}")
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        // If width/height still 0 and it's an image, try decoding bounds
        if (width <= 0 || height <= 0) {
            try {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                    width = options.outWidth
                    height = options.outHeight
                    if (mimeType.isEmpty() && options.outMimeType != null) {
                        mimeType = options.outMimeType
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding image bounds: ${e.message}")
            }
        }

        return MediaMetadata(
            durationMs = durationMs,
            width = width,
            height = height,
            mimeType = mimeType,
            isValid = true
        )
    }

    fun generateThumbnail(context: Context, uri: Uri, isVideo: Boolean): String? {
        val thumbDir = File(context.cacheDir, "thumbnails").apply {
            if (!exists()) mkdirs()
        }
        val outputFile = File(thumbDir, "thumb_${UUID.randomUUID()}.jpg")

        var bitmap: Bitmap? = null
        if (isVideo) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                // Get frame at 1 second or 0
                bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(0)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating video frame thumbnail: ${e.message}")
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {}
            }
        } else {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 2 // downsample for memory efficiency
                    }
                    bitmap = BitmapFactory.decodeStream(stream, null, options)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating image thumbnail: ${e.message}")
            }
        }

        return if (bitmap != null) {
            try {
                FileOutputStream(outputFile).use { out ->
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                bitmap?.recycle()
                outputFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Error saving thumbnail: ${e.message}")
                null
            }
        } else null
    }

    fun getCacheSizeMb(context: Context): Double {
        val thumbDir = File(context.cacheDir, "thumbnails")
        if (!thumbDir.exists()) return 0.0
        var totalBytes = 0L
        thumbDir.listFiles()?.forEach { file ->
            totalBytes += file.length()
        }
        return totalBytes / (1024.0 * 1024.0)
    }

    fun clearCache(context: Context): Boolean {
        val thumbDir = File(context.cacheDir, "thumbnails")
        if (!thumbDir.exists()) return true
        var success = true
        thumbDir.listFiles()?.forEach { file ->
            if (!file.delete()) success = false
        }
        return success
    }

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "00:00"
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hours = minutes / 60
        return if (hours > 0) {
            val remainingMins = minutes % 60
            String.format("%02d:%02d:%02d", hours, remainingMins, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
