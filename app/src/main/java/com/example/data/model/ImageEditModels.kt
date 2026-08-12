package com.example.data.model

import java.util.UUID

data class StickerItem(
    val id: String = UUID.randomUUID().toString(),
    val category: String = "Emoji", // "Emoji", "Love", "Funny", "Travel", "Food", "Birthday", "Festival", "Social", "Shapes", "Arrows"
    val symbol: String = "🔥",
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f,
    val opacity: Float = 1.0f,
    val isVisible: Boolean = true
)

data class DrawingPath(
    val id: String = UUID.randomUUID().toString(),
    val pointsJson: String = "[]",
    val colorHex: Long = 0xFFFF0000,
    val brushSize: Float = 10f,
    val brushType: String = "PEN", // "PEN", "MARKER", "ERASER"
    val opacity: Float = 1.0f,
    val isVisible: Boolean = true
)

data class ShapeItem(
    val id: String = UUID.randomUUID().toString(),
    val shapeType: String = "RECTANGLE", // "RECTANGLE", "CIRCLE", "LINE", "ARROW", "ROUNDED_RECTANGLE"
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val widthRatio: Float = 0.3f,
    val heightRatio: Float = 0.2f,
    val fillColor: Long = 0x80FF0000,
    val strokeColor: Long = 0xFFFFFFFF,
    val strokeWidth: Float = 4f,
    val opacity: Float = 1.0f,
    val rotation: Float = 0.0f,
    val isVisible: Boolean = true
)

data class PhotoFrameSettings(
    val frameWidthDp: Float = 0f,
    val frameColor: Long = 0xFFFFFFFF,
    val cornerRadiusDp: Float = 0f,
    val style: String = "NONE" // "NONE", "MODERN", "POLAROID", "FILM", "NEON", "WOOD"
)

data class StraightenSettings(
    val angleDegrees: Float = 0f // -45f to +45f
)

data class BlurSettings(
    val blurRadius: Float = 0f, // 0f to 50f
    val mode: String = "ENTIRE" // "ENTIRE", "SELECTIVE_RADIAL", "SELECTIVE_LINEAR"
)

data class CollageCell(
    val cellIndex: Int,
    val imageUri: String? = null,
    val scale: Float = 1.0f,
    val offsetX: Float = 0.0f,
    val offsetY: Float = 0.0f
)

data class CollageSettings(
    val presetName: String = "NONE", // "NONE", "2_PHOTOS", "3_PHOTOS", "4_PHOTOS", "6_PHOTOS", "9_PHOTOS"
    val cells: List<CollageCell> = emptyList()
)

data class ImageLayerItem(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "PHOTO", // "BACKGROUND", "PHOTO", "STICKER", "TEXT", "SHAPE", "DRAWING"
    val name: String = "Layer",
    val isVisible: Boolean = true,
    val opacity: Float = 1.0f,
    val blendMode: String = "NORMAL", // "NORMAL", "MULTIPLY", "SCREEN", "OVERLAY", "DARKEN", "LIGHTEN"
    val zIndex: Int = 0
)
