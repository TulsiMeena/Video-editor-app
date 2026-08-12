package com.example.data.model

import java.util.UUID

data class TimelineClip(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val mediaType: String = "VIDEO", // "VIDEO" or "IMAGE"
    val name: String = "Clip",
    val sourceDurationMs: Long = 5000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 5000L,
    val speed: Float = 1.0f, // 0.25x to 4.0x
    val rotation: Int = 0, // 0, 90, 180, 270
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val cropPreset: String = "ORIGINAL", // "ORIGINAL", "16:9", "9:16", "1:1", "4:5", "FREE"
    val contentScale: String = "FIT", // "FIT", "FILL"
    val volume: Float = 1.0f, // 0.0f to 2.0f
    val isMuted: Boolean = false,
    val isReversed: Boolean = false,
    val reversedUri: String? = null,
    val isFreezeFrame: Boolean = false,
    val freezeFrameUri: String? = null,
    val width: Int = 1920,
    val height: Int = 1080,
    val fps: Float = 30.0f,
    val mimeType: String = "video/mp4",
    val thumbnailPath: String? = null,
    
    // PROMPT 4 FIELDS
    val filterName: String = "Original",
    val filterIntensity: Float = 1.0f, // 0.0f to 1.0f
    val colorAdjustments: ColorAdjustments = ColorAdjustments(),
    val vignetteSettings: VignetteSettings = VignetteSettings(),
    val grainSettings: FilmGrainSettings = FilmGrainSettings(),
    val effects: List<ClipEffect> = emptyList(),
    val transitionToNext: ClipTransition? = null,
    val keyframes: List<TransformKeyframe> = emptyList(),
    val transform: ClipTransform = ClipTransform(),

    // PROMPT 5 AI FIELDS
    val isBackgroundRemoved: Boolean = false,
    val bgReplacementType: String = "TRANSPARENT", // "TRANSPARENT", "COLOR", "IMAGE", "VIDEO"
    val bgReplacementColor: Long = 0xFF00FF00,
    val bgReplacementUri: String? = null,
    val edgeSoftness: Float = 20f,
    val feather: Float = 20f,
    val strength: Float = 80f,
    val isAiEnhanced: Boolean = false,
    val enhancementType: String = "NONE", // "NONE", "SHARPEN", "DENOISE", "UPSCALE", "DETAIL"
    val noiseReductionLevel: String = "NONE" // "NONE", "LOW", "MEDIUM", "HIGH"
) {
    val trimmedDurationMs: Long
        get() = (trimEndMs - trimStartMs).coerceAtLeast(100L)

    val effectiveDurationMs: Long
        get() = (trimmedDurationMs / speed).toLong().coerceAtLeast(100L)

    val effectiveVolume: Float
        get() = if (isMuted) 0.0f else volume

    val activeUri: String
        get() = freezeFrameUri ?: reversedUri ?: uri

    fun getInterpolatedTransform(offsetMs: Long): ClipTransform {
        if (keyframes.isEmpty()) return transform

        val sortedKeyframes = keyframes.sortedBy { it.timeOffsetMs }
        val first = sortedKeyframes.first()
        val last = sortedKeyframes.last()

        if (offsetMs <= first.timeOffsetMs) {
            return ClipTransform(first.positionX, first.positionY, first.scale, first.rotation, first.opacity)
        }
        if (offsetMs >= last.timeOffsetMs) {
            return ClipTransform(last.positionX, last.positionY, last.scale, last.rotation, last.opacity)
        }

        for (i in 0 until sortedKeyframes.size - 1) {
            val k1 = sortedKeyframes[i]
            val k2 = sortedKeyframes[i + 1]
            if (offsetMs in k1.timeOffsetMs..k2.timeOffsetMs) {
                val span = (k2.timeOffsetMs - k1.timeOffsetMs).coerceAtLeast(1L)
                val rawFraction = (offsetMs - k1.timeOffsetMs).toFloat() / span.toFloat()

                val easedFraction = when (k1.easing) {
                    "EASE_IN" -> rawFraction * rawFraction
                    "EASE_OUT" -> rawFraction * (2f - rawFraction)
                    "EASE_IN_OUT" -> if (rawFraction < 0.5f) 2f * rawFraction * rawFraction else -1f + (4f - 2f * rawFraction) * rawFraction
                    else -> rawFraction // LINEAR
                }

                val posX = k1.positionX + (k2.positionX - k1.positionX) * easedFraction
                val posY = k1.positionY + (k2.positionY - k1.positionY) * easedFraction
                val sc = k1.scale + (k2.scale - k1.scale) * easedFraction
                val rot = k1.rotation + (k2.rotation - k1.rotation) * easedFraction
                val op = k1.opacity + (k2.opacity - k1.opacity) * easedFraction

                return ClipTransform(
                    positionX = posX,
                    positionY = posY,
                    scale = sc,
                    rotation = rot,
                    opacity = op.coerceIn(0f, 1f)
                )
            }
        }

        return transform
    }
}

