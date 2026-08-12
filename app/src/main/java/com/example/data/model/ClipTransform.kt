package com.example.data.model

data class ClipTransform(
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f
) {
    val isDefault: Boolean
        get() = positionX == 0f && positionY == 0f && scale == 1.0f && rotation == 0f && opacity == 1.0f
}
