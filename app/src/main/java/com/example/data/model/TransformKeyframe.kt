package com.example.data.model

import java.util.UUID

data class TransformKeyframe(
    val id: String = UUID.randomUUID().toString(),
    val timeOffsetMs: Long = 0L, // Offset from clip trim start
    val positionX: Float = 0f,   // Pixel offset or normalised offset
    val positionY: Float = 0f,
    val scale: Float = 1.0f,     // 0.1 to 5.0
    val rotation: Float = 0f,    // Degrees
    val opacity: Float = 1.0f,   // 0.0 to 1.0
    val easing: String = "LINEAR" // "LINEAR", "EASE_IN", "EASE_OUT", "EASE_IN_OUT"
)
