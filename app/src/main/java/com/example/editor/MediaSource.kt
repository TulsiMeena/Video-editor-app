package com.example.editor

import android.content.Context
import android.net.Uri
import com.example.data.model.TimelineClip
import com.example.utils.MediaUtils
import java.io.File
import java.util.UUID

object MediaSource {

    fun createClipFromUri(context: Context, uri: Uri): TimelineClip {
        val meta = MediaUtils.validateAndExtractMetadata(context, uri)
        val isVideo = meta.mimeType.contains("video", ignoreCase = true) || meta.durationMs > 0
        val duration = if (isVideo && meta.durationMs > 0) meta.durationMs else 5000L
        val thumbnail = MediaUtils.generateThumbnail(context, uri, isVideo)

        val name = if (isVideo) "Video Clip" else "Image Clip"

        return TimelineClip(
            id = UUID.randomUUID().toString(),
            uri = uri.toString(),
            mediaType = if (isVideo) "VIDEO" else "IMAGE",
            name = name,
            sourceDurationMs = duration,
            trimStartMs = 0L,
            trimEndMs = duration,
            width = if (meta.width > 0) meta.width else 1920,
            height = if (meta.height > 0) meta.height else 1080,
            mimeType = if (meta.mimeType.isNotEmpty()) meta.mimeType else if (isVideo) "video/mp4" else "image/jpeg",
            thumbnailPath = thumbnail
        )
    }

    fun isMediaAvailable(context: Context, uriString: String): Boolean {
        return try {
            val uri = Uri.parse(uriString)
            if (uriString.startsWith("file://") || uriString.startsWith("/")) {
                val path = uri.path ?: uriString
                File(path).exists()
            } else {
                val meta = MediaUtils.validateAndExtractMetadata(context, uri)
                meta.isValid
            }
        } catch (e: Exception) {
            false
        }
    }
}
