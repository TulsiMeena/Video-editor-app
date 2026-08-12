package com.example.data.model

import java.util.UUID

data class ClipEffect(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Effect",
    val type: String = "BLUR", // "BLUR", "GLITCH", "ZOOM_IN", "ZOOM_OUT", "SHAKE", "PULSE", "SLOW_ZOOM", "CAMERA_PUSH", "CAMERA_PULL", "LIGHT_LEAK", "RETRO_NOISE", "VIGNETTE", "GRAIN"
    val category: String = "Blur", // "Light", "Blur", "Glitch", "Motion", "Retro", "Cinematic", "Party", "Vlog", "Dream", "Distortion"
    val isEnabled: Boolean = true,
    val intensity: Float = 50f, // 0 to 100
    val speed: Float = 1.0f,    // 0.5 to 2.0
    val startOffsetMs: Long = 0L,
    val endOffsetMs: Long = 5000L
)
