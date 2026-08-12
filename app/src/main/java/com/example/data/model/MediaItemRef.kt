package com.example.data.model

data class MediaItemRef(
    val uri: String,
    val mediaType: String, // "VIDEO" or "IMAGE"
    val durationMs: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val aspectRatio: String = "ORIGINAL",
    val rotation: Int = 0,
    val orderIndex: Int = 0
)
