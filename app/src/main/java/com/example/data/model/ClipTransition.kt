package com.example.data.model

data class ClipTransition(
    val type: String = "NONE", // "NONE", "FADE", "DISSOLVE", "BLACK", "WHITE", "SLIDE_LEFT", "SLIDE_RIGHT", "SLIDE_UP", "SLIDE_DOWN", "ZOOM", "PUSH", "WIPE"
    val durationMs: Long = 500L, // 100L, 250L, 500L, 750L, 1000L, 2000L
    val audioCrossfade: Boolean = true
)
