package com.example.editor

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ColorAdjustments
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import java.io.File
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoRenderer(
    clip: TimelineClip?,
    isPlaying: Boolean,
    clipOffsetMs: Long,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier,
    isShowingBefore: Boolean = false,
    performanceMode: String = "HIGH_QUALITY",
    nextClip: TimelineClip? = null,
    captions: List<com.example.data.model.CaptionSegment> = emptyList(),
    currentTimelineMs: Long = 0L
) {
    if (clip == null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(LuminaSurface)
                .border(1.dp, LuminaSurfaceBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "No clip selected",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val context = LocalContext.current
    val contentScale = if (clip.contentScale == "FILL") ContentScale.Crop else ContentScale.Fit

    // Compute keyframe transform or default transform
    val currentTransform = if (isShowingBefore) {
        com.example.data.model.ClipTransform()
    } else {
        clip.getInterpolatedTransform(clipOffsetMs)
    }

    // Compute active motion effects (Zoom In, Shake, Pulse, etc.)
    var motionScaleAdd = 0f
    var motionOffsetX = 0f
    var motionOffsetY = 0f

    if (!isShowingBefore) {
        val activeMotionEffects = clip.effects.filter { it.isEnabled && (it.type.startsWith("ZOOM") || it.type in listOf("SHAKE", "PULSE", "SLOW_ZOOM", "CAMERA_PUSH", "CAMERA_PULL")) }
        for (effect in activeMotionEffects) {
            val progress = ((clipOffsetMs - effect.startOffsetMs).toFloat() / (effect.endOffsetMs - effect.startOffsetMs).coerceAtLeast(100L)).coerceIn(0f, 1f)
            val factor = effect.intensity / 50f // 1.0 at default 50
            when (effect.type) {
                "ZOOM_IN" -> motionScaleAdd += progress * 0.3f * factor
                "ZOOM_OUT" -> motionScaleAdd += (1f - progress) * 0.3f * factor
                "SLOW_ZOOM" -> motionScaleAdd += progress * 0.15f * factor
                "SHAKE" -> {
                    val t = (clipOffsetMs / 50f) * effect.speed
                    motionOffsetX += (sin(t) * 15f * factor)
                    motionOffsetY += (sin(t * 1.3f) * 15f * factor)
                }
                "PULSE" -> {
                    val t = (clipOffsetMs / 100f) * effect.speed
                    motionScaleAdd += (sin(t) * 0.08f * factor)
                }
                "CAMERA_PUSH" -> {
                    motionScaleAdd += progress * 0.2f * factor
                    motionOffsetY -= progress * 20f * factor
                }
                "CAMERA_PULL" -> {
                    motionScaleAdd += (1f - progress) * 0.2f * factor
                    motionOffsetY += progress * 20f * factor
                }
            }
        }
    }

    // Compute combined Color Matrix for Color Adjustments + Filter Preset
    val colorMatrix: ColorMatrix? = remember(
        clip.filterName, clip.filterIntensity, clip.colorAdjustments, isShowingBefore, performanceMode
    ) {
        if (isShowingBefore) {
            ColorMatrix()
        } else {
            val adj = if (performanceMode == "PERFORMANCE") {
                // Simplified color adjustments for low-end device performance mode
                ColorAdjustments(brightness = clip.colorAdjustments.brightness, contrast = clip.colorAdjustments.contrast, saturation = clip.colorAdjustments.saturation)
            } else {
                clip.colorAdjustments
            }
            ColorFilterUtils.createCombinedColorMatrix(
                adjustments = adj,
                filterName = clip.filterName,
                filterIntensity = clip.filterIntensity
            )
        }
    }

    val finalScaleX = (currentTransform.scale + motionScaleAdd) * (if (clip.flipHorizontal) -1f else 1f)
    val finalScaleY = (currentTransform.scale + motionScaleAdd) * (if (clip.flipVertical) -1f else 1f)
    val finalRotation = clip.rotation.toFloat() + currentTransform.rotation
    val finalOpacity = if (isShowingBefore) 1.0f else currentTransform.opacity.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(1.dp, LuminaSurfaceBorder, RoundedCornerShape(12.dp))
            .graphicsLayer {
                translationX = currentTransform.positionX + motionOffsetX
                translationY = currentTransform.positionY + motionOffsetY
                rotationZ = finalRotation
                scaleX = finalScaleX
                scaleY = finalScaleY
                alpha = finalOpacity
            }
            .clickable { onTogglePlay() },
        contentAlignment = Alignment.Center
    ) {
        if (clip.mediaType == "IMAGE" || clip.isFreezeFrame) {
            val imageSource = clip.freezeFrameUri ?: clip.uri
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(if (imageSource.startsWith("/")) File(imageSource) else Uri.parse(imageSource))
                    .crossfade(true)
                    .build(),
                contentDescription = "Clip Preview",
                contentScale = contentScale,
                colorFilter = colorMatrix?.let { ColorFilter.colorMatrix(it) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Video Preview Surface
            var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse(clip.activeUri))
                        setOnPreparedListener { mp ->
                            mp.isLooping = false
                            val vol = clip.effectiveVolume
                            mp.setVolume(vol, vol)
                            seekTo((clip.trimStartMs + clipOffsetMs).toInt())
                            if (isPlaying) start()
                        }
                        setOnCompletionListener {
                            seekTo(clip.trimStartMs.toInt())
                        }
                    }
                },
                update = { view ->
                    videoViewRef = view
                    val vol = clip.effectiveVolume
                    view.post {
                        try {
                            if (isPlaying) {
                                if (!view.isPlaying) view.start()
                            } else {
                                if (view.isPlaying) view.pause()
                                val targetSeek = (clip.trimStartMs + clipOffsetMs).toInt()
                                view.seekTo(targetSeek)
                            }
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Vignette Overlay Shader
        val vignetteIntensity = if (isShowingBefore) 0f else clip.vignetteSettings.intensity
        if (vignetteIntensity > 0f) {
            val sizeFactor = (clip.vignetteSettings.size / 100f).coerceIn(0.1f, 1.0f)
            val alphaFactor = (vignetteIntensity / 100f) * 0.85f

            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = (size.minDimension / 2f) * (1.2f - (1f - sizeFactor) * 0.5f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = alphaFactor)),
                        center = center,
                        radius = radius
                    ),
                    radius = size.maxDimension,
                    center = center
                )
            }
        }

        // Film Grain Overlay Shader
        val grainAmount = if (isShowingBefore) 0f else clip.grainSettings.amount
        if (grainAmount > 0f && performanceMode != "PERFORMANCE") {
            val grainAlpha = (grainAmount / 100f) * 0.15f

            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotSize = (clip.grainSettings.size / 100f) * 6.dp.toPx() + 2.dp.toPx()
                val step = dotSize * 3f
                var x = 0f
                var y = 0f
                while (y < size.height) {
                    x = (y.toInt() % 7) * 2f
                    while (x < size.width) {
                        drawRect(
                            color = if ((x.toInt() + y.toInt()) % 2 == 0) Color.White.copy(alpha = grainAlpha) else Color.Black.copy(alpha = grainAlpha),
                            topLeft = Offset(x, y),
                            size = Size(dotSize, dotSize)
                        )
                        x += step
                    }
                    y += step
                }
            }
        }

        // Glitch Effect Overlay
        if (!isShowingBefore) {
            val glitchEffects = clip.effects.filter { it.isEnabled && it.type == "GLITCH" }
            for (glitch in glitchEffects) {
                if (clipOffsetMs in glitch.startOffsetMs..glitch.endOffsetMs) {
                    val glitchAlpha = (glitch.intensity / 100f) * 0.35f
                    val shiftOffset = (sin(clipOffsetMs / 30f * glitch.speed) * (glitch.intensity / 5f)).dp

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { translationX = shiftOffset.toPx() }
                    ) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Cyan.copy(alpha = glitchAlpha),
                                    Color.Transparent,
                                    Color.Magenta.copy(alpha = glitchAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                    }
                }
            }
        }

        // Active Captions & Karaoke Overlay
        if (!isShowingBefore && captions.isNotEmpty()) {
            val activeCaption = captions.find { it.isVisibleAt(currentTimelineMs) }
            if (activeCaption != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val activeWordIndex = activeCaption.getActiveWordIndexAt(currentTimelineMs)
                    val isKaraoke = activeCaption.stylePreset == "KARAOKE" || activeCaption.words.isNotEmpty()

                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeCaption.hasBackground) Color(activeCaption.backgroundColor) else Color.Transparent,
                        border = if (activeCaption.hasStroke) androidx.compose.foundation.BorderStroke(1.dp, Color(activeCaption.strokeColor)) else null,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        if (isKaraoke && activeCaption.words.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                activeCaption.words.forEachIndexed { idx, word ->
                                    val isHighlighted = idx == activeWordIndex
                                    androidx.compose.material3.Text(
                                        text = "${word.word} ",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = androidx.compose.ui.unit.TextUnit(activeCaption.fontSizeSp.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp),
                                            fontWeight = if (isHighlighted) androidx.compose.ui.text.font.FontWeight.ExtraBold else androidx.compose.ui.text.font.FontWeight.Normal
                                        ),
                                        color = if (isHighlighted) Color(activeCaption.highlightColor) else Color(activeCaption.textColor)
                                    )
                                }
                            }
                        } else {
                            androidx.compose.material3.Text(
                                text = activeCaption.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = androidx.compose.ui.unit.TextUnit(activeCaption.fontSizeSp.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                ),
                                color = Color(activeCaption.textColor),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
