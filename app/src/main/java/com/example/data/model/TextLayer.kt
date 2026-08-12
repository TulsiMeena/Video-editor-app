package com.example.data.model

import java.util.UUID

data class TextLayer(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "Text",
    val startTimelineMs: Long = 0L,
    val endTimelineMs: Long = 5000L,
    val positionX: Float = 0.5f, // 0.0 to 1.0 normalized x position on preview
    val positionY: Float = 0.5f, // 0.0 to 1.0 normalized y position on preview
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f,
    val opacity: Float = 1.0f,
    val fontFamily: String = "DEFAULT", // "DEFAULT", "SERIF", "MONOSPACE", "SANS_SERIF", "CURSIVE", "BOLD_HEADER", "HINDI_DEFAULT"
    val fontSizeSp: Int = 24,
    val textColor: Long = 0xFFFFFFFF, // ARGB Long
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val alignment: String = "CENTER", // "LEFT", "CENTER", "RIGHT"
    val letterSpacingSp: Float = 0.0f,
    val lineSpacingMultiplier: Float = 1.0f,
    val hasStroke: Boolean = false,
    val strokeColor: Long = 0xFF000000,
    val strokeWidthDp: Float = 2.0f,
    val hasShadow: Boolean = false,
    val shadowColor: Long = 0xFF000000,
    val shadowOpacity: Float = 0.5f,
    val shadowBlurRadiusDp: Float = 4.0f,
    val hasBackground: Boolean = false,
    val backgroundColor: Long = 0x88000000,
    val backgroundOpacity: Float = 0.5f,
    val isBackgroundRounded: Boolean = true,
    val animationType: String = "NONE", // "NONE", "FADE", "POP", "SLIDE_UP", "SLIDE_DOWN", "ZOOM_IN", "ZOOM_OUT"
    val animationDurationMs: Long = 500L
) {
    val durationMs: Long
        get() = (endTimelineMs - startTimelineMs).coerceAtLeast(100L)

    fun isVisibleAt(timelineTimeMs: Long): Boolean {
        return timelineTimeMs in startTimelineMs..endTimelineMs
    }
}
