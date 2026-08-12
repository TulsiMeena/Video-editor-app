package com.example.editor

import com.example.data.model.TimelineClip

data class AudioTrackItem(
    val id: String,
    val name: String,
    val startTimeMs: Long,
    val durationMs: Long,
    val volume: Float = 1.0f
)

data class TextTrackItem(
    val id: String,
    val text: String,
    val startTimeMs: Long,
    val durationMs: Long
)

data class EffectTrackItem(
    val id: String,
    val effectName: String,
    val startTimeMs: Long,
    val durationMs: Long
)

data class Timeline(
    val videoClips: List<TimelineClip> = emptyList(),
    val audioTracks: List<AudioTrackItem> = emptyList(),
    val textTracks: List<TextTrackItem> = emptyList(),
    val effectTracks: List<EffectTrackItem> = emptyList(),
    val playheadMs: Long = 0L,
    val zoomScale: Float = 1.0f // 0.5x to 4.0x zoom
) {
    val totalDurationMs: Long
        get() = videoClips.sumOf { it.effectiveDurationMs }.coerceAtLeast(100L)
}
