package com.example.data.model

import java.util.UUID

data class AudioClip(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val title: String = "Audio Track",
    val audioType: String = "MUSIC", // "MUSIC", "VOICE_OVER", "EXTRACTED", "EFFECT"
    val sourceDurationMs: Long = 10000L,
    val startTimelineMs: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 10000L,
    val volume: Float = 1.0f, // 0.0f to 2.0f
    val isMuted: Boolean = false,
    val fadeInMs: Long = 0L, // 0 to 5000ms
    val fadeOutMs: Long = 0L, // 0 to 5000ms
    val speed: Float = 1.0f, // 0.5f to 2.0f
    val category: String? = null // Category name for built-in music library
) {
    val trimmedDurationMs: Long
        get() = (trimEndMs - trimStartMs).coerceAtLeast(100L)

    val effectiveDurationMs: Long
        get() = (trimmedDurationMs / speed).toLong().coerceAtLeast(100L)

    val endTimelineMs: Long
        get() = startTimelineMs + effectiveDurationMs

    val effectiveVolume: Float
        get() = if (isMuted) 0.0f else volume
}
