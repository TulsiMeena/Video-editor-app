package com.example.data.model

import java.util.UUID

data class CaptionWord(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

data class CaptionSegment(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val startTimelineMs: Long,
    val endTimelineMs: Long,
    val words: List<CaptionWord> = emptyList(),
    val stylePreset: String = "CLASSIC", // "CLASSIC", "BOLD", "MINIMAL", "CINEMA", "SOCIAL", "KARAOKE"
    val fontFamily: String = "DEFAULT",
    val fontSizeSp: Int = 22,
    val textColor: Long = 0xFFFFFFFF, // ARGB Long
    val highlightColor: Long = 0xFFFFD700, // Gold ARGB for Karaoke word highlight
    val strokeColor: Long = 0xFF000000,
    val hasStroke: Boolean = true,
    val hasBackground: Boolean = true,
    val backgroundColor: Long = 0xAA000000,
    val positionY: Float = 0.85f // Normalized vertical position (0.0 top, 1.0 bottom)
) {
    val durationMs: Long
        get() = (endTimelineMs - startTimelineMs).coerceAtLeast(100L)

    fun isVisibleAt(timelineTimeMs: Long): Boolean {
        return timelineTimeMs in startTimelineMs..endTimelineMs
    }

    /**
     * Gets the word currently active at timelineTimeMs for Karaoke rendering.
     * Returns index of word if found, or -1.
     */
    fun getActiveWordIndexAt(timelineTimeMs: Long): Int {
        if (words.isEmpty()) return -1
        return words.indexOfFirst { timelineTimeMs in it.startMs..it.endMs }
    }
}
