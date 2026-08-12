package com.example.editor

import com.example.data.model.TimelineClip

data class VideoProject(
    val id: String,
    val name: String,
    val projectType: String,
    val aspectRatio: String = "ORIGINAL",
    val clips: List<TimelineClip> = emptyList()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.effectiveDurationMs }.coerceAtLeast(100L)

    /**
     * Finds which clip is active at given project timeline time.
     * Returns Triple(clip, clipIndex, clipStartMs) or null if no clips.
     */
    fun getClipAtTimelineTime(currentTimeMs: Long): Triple<TimelineClip, Int, Long>? {
        if (clips.isEmpty()) return null
        var accumulatedMs = 0L
        val clampedTime = currentTimeMs.coerceIn(0L, totalDurationMs)

        for ((index, clip) in clips.withIndex()) {
            val clipDuration = clip.effectiveDurationMs
            val clipEndMs = accumulatedMs + clipDuration
            if (clampedTime in accumulatedMs..clipEndMs || index == clips.lastIndex) {
                return Triple(clip, index, accumulatedMs)
            }
            accumulatedMs = clipEndMs
        }
        return Triple(clips.last(), clips.lastIndex, (totalDurationMs - clips.last().effectiveDurationMs).coerceAtLeast(0L))
    }
}
