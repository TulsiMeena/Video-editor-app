package com.example.ui.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TextLayer
import kotlin.math.roundToInt

@Composable
fun PreviewOverlayCanvas(
    textLayers: List<TextLayer>,
    currentTimeMs: Long,
    selectedTextLayerId: String?,
    safeAreaRatio: String?, // "9:16", "16:9", "1:1", "4:5", null
    onSelectTextLayer: (String) -> Unit,
    onUpdateTextTransform: (String, Float, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxHeightPx = constraints.maxHeight.toFloat()

        // 1. Optional Safe Area Guides Overlay
        if (!safeAreaRatio.isNullOrBlank()) {
            val dashPath = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val marginX = size.width * 0.1f
                val marginY = size.height * 0.1f

                drawRect(
                    color = Color.Cyan.copy(alpha = 0.5f),
                    topLeft = Offset(marginX, marginY),
                    size = Size(size.width - 2 * marginX, size.height - 2 * marginY),
                    style = Stroke(width = 2f, pathEffect = dashPath)
                )
            }
        }

        // 2. Render Text Layers
        textLayers.forEach { layer ->
            if (layer.isVisibleAt(currentTimeMs)) {
                val isSelected = layer.id == selectedTextLayerId

                // Animation factor calculation
                val progressMs = currentTimeMs - layer.startTimelineMs
                val animFactor = if (layer.animationType != "NONE" && progressMs < layer.animationDurationMs) {
                    (progressMs.toFloat() / layer.animationDurationMs.toFloat()).coerceIn(0f, 1f)
                } else 1f

                val animScale = when (layer.animationType) {
                    "POP", "ZOOM_IN" -> layer.scale * animFactor
                    "ZOOM_OUT" -> layer.scale * (1.5f - animFactor * 0.5f)
                    else -> layer.scale
                }

                val animAlpha = when (layer.animationType) {
                    "FADE" -> layer.opacity * animFactor
                    else -> layer.opacity
                }

                val fontStyle = when (layer.fontFamily) {
                    "SERIF" -> FontFamily.Serif
                    "MONOSPACE" -> FontFamily.Monospace
                    "SANS_SERIF" -> FontFamily.SansSerif
                    "CURSIVE" -> FontFamily.Cursive
                    else -> FontFamily.Default
                }

                val textAlign = when (layer.alignment) {
                    "LEFT" -> TextAlign.Left
                    "RIGHT" -> TextAlign.Right
                    else -> TextAlign.Center
                }

                val textStyle = TextStyle(
                    color = Color(layer.textColor),
                    fontSize = layer.fontSizeSp.sp,
                    fontWeight = if (layer.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (layer.isItalic) FontStyle.Italic else FontStyle.Normal,
                    fontFamily = fontStyle,
                    textAlign = textAlign,
                    shadow = if (layer.hasShadow) Shadow(
                        color = Color(layer.shadowColor).copy(alpha = layer.shadowOpacity),
                        blurRadius = layer.shadowBlurRadiusDp
                    ) else null
                )

                // Drag, Pinch Scale & Rotate gestures
                val xPosPx = (layer.positionX * maxWidthPx).coerceIn(0f, maxWidthPx)
                val yPosPx = (layer.positionY * maxHeightPx).coerceIn(0f, maxHeightPx)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xPosPx.roundToInt(), yPosPx.roundToInt()) }
                        .scale(animScale)
                        .rotate(layer.rotation)
                        .alpha(animAlpha)
                        .pointerInput(layer.id) {
                            detectTransformGestures { _, pan, zoom, rotation ->
                                onSelectTextLayer(layer.id)
                                val newX = (layer.positionX + pan.x / maxWidthPx).coerceIn(0.05f, 0.95f)
                                val newY = (layer.positionY + pan.y / maxHeightPx).coerceIn(0.05f, 0.95f)
                                val newScale = (layer.scale * zoom).coerceIn(0.2f, 5.0f)
                                val newRot = (layer.rotation + rotation) % 360f
                                onUpdateTextTransform(layer.id, newX, newY, newScale, newRot)
                            }
                        }
                        .then(
                            if (layer.hasBackground) {
                                Modifier
                                    .background(
                                        color = Color(layer.backgroundColor).copy(alpha = layer.backgroundOpacity),
                                        shape = if (layer.isBackgroundRounded) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            } else Modifier
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(1.5.dp, Color.Cyan, RoundedCornerShape(6.dp))
                            } else Modifier
                        )
                        .testTag("preview_text_layer_${layer.id}")
                ) {
                    Text(
                        text = layer.text,
                        style = textStyle
                    )
                }
            }
        }
    }
}
