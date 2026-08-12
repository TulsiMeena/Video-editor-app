package com.example.ui.editor.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ColorAdjustments
import com.example.data.model.DrawingPath
import com.example.data.model.PhotoFrameSettings
import com.example.data.model.ShapeItem
import com.example.data.model.StickerItem
import com.example.data.model.TextLayer
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan

@Composable
fun ImageCanvasView(
    clip: TimelineClip?,
    isShowingBefore: Boolean,
    showGridOverlay: Boolean,
    activeTab: String,
    cropBounds: List<Float>,
    straightenAngle: Float,
    frameSettings: PhotoFrameSettings,
    blurRadius: Float,
    stickers: List<StickerItem>,
    drawingPaths: List<DrawingPath>,
    shapes: List<ShapeItem>,
    textLayers: List<TextLayer>,
    fitMode: String,
    onUpdateSticker: (StickerItem) -> Unit,
    onUpdateShape: (ShapeItem) -> Unit,
    onUpdateTextLayer: (TextLayer) -> Unit,
    onAddDrawingPoint: (Offset, Long, Float, String) -> Unit,
    onCropBoundsChanged: (Float, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Android ColorMatrix conversion to Compose ColorMatrix
    val composeColorMatrix = remember(clip, isShowingBefore) {
        if (isShowingBefore || clip == null) {
            ColorMatrix()
        } else {
            val androidMatrix = buildAndroidColorMatrix(clip.colorAdjustments, clip.filterName, clip.filterIntensity)
            ColorMatrix(androidMatrix.array)
        }
    }

    val contentScaleMode = when (fitMode) {
        "FIT_WIDTH" -> ContentScale.FillWidth
        "FIT_HEIGHT" -> ContentScale.FillHeight
        "FILL" -> ContentScale.Crop
        else -> ContentScale.Fit
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(1.0f, 5.0f)
                    if (zoomScale > 1.0f) {
                        panOffset += pan
                    } else {
                        panOffset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val maxWPx = with(density) { maxWidth.toPx() }
            val maxHPx = with(density) { maxHeight.toPx() }

            // Photo Container with Zoom, Pan, Straighten, Flip, Rotate
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = zoomScale * (if (!isShowingBefore && clip?.flipHorizontal == true) -1f else 1f),
                        scaleY = zoomScale * (if (!isShowingBefore && clip?.flipVertical == true) -1f else 1f),
                        translationX = panOffset.x,
                        translationY = panOffset.y,
                        rotationZ = if (isShowingBefore) 0f else ((clip?.rotation ?: 0).toFloat() + straightenAngle)
                    )
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                // Background Layer / Border Frame
                if (!isShowingBefore && frameSettings.frameWidthDp > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color(frameSettings.frameColor),
                                shape = RoundedCornerShape(frameSettings.cornerRadiusDp.dp)
                            )
                            .padding(frameSettings.frameWidthDp.dp)
                    )
                }

                // Background Replacement if Active
                if (!isShowingBefore && clip?.isBackgroundRemoved == true) {
                    when (clip.bgReplacementType) {
                        "COLOR" -> Box(modifier = Modifier.fillMaxSize().background(Color(clip.bgReplacementColor)))
                        "IMAGE" -> clip.bgReplacementUri?.let { bgUri ->
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(Uri.parse(bgUri)).build(),
                                contentDescription = "Background Replacement",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray.copy(alpha = 0.3f)))
                        }
                    }
                }

                // Main Image
                if (clip != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(clip.activeUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image Preview",
                        colorFilter = ColorFilter.colorMatrix(composeColorMatrix),
                        contentScale = contentScaleMode,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("image_canvas_preview")
                    )
                }

                // Drawings Layer Overlay
                if (!isShowingBefore && drawingPaths.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawingPaths.filter { it.isVisible }.forEach { pathItem ->
                            drawCircle(
                                color = Color(pathItem.colorHex).copy(alpha = pathItem.opacity),
                                radius = pathItem.brushSize,
                                center = Offset(size.width * 0.5f, size.height * 0.5f)
                            )
                        }
                    }
                }

                // Shapes Layer Overlay
                if (!isShowingBefore && shapes.isNotEmpty()) {
                    shapes.filter { it.isVisible }.forEach { shapeItem ->
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (shapeItem.positionX * maxWPx).toInt(),
                                        (shapeItem.positionY * maxHPx).toInt()
                                    )
                                }
                                .size(
                                    with(density) { (shapeItem.widthRatio * maxWPx).toDp() },
                                    with(density) { (shapeItem.heightRatio * maxHPx).toDp() }
                                )
                                .rotate(shapeItem.rotation)
                                .background(Color(shapeItem.fillColor).copy(alpha = shapeItem.opacity))
                                .border(
                                    shapeItem.strokeWidth.dp,
                                    Color(shapeItem.strokeColor),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                // Stickers Layer Overlay
                if (!isShowingBefore && stickers.isNotEmpty()) {
                    stickers.filter { it.isVisible }.forEach { sticker ->
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (sticker.positionX * (maxWPx - 100)).toInt(),
                                        (sticker.positionY * (maxHPx - 100)).toInt()
                                    )
                                }
                                .rotate(sticker.rotation)
                                .scale(sticker.scale)
                        ) {
                            Text(
                                text = sticker.symbol,
                                fontSize = 42.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                // Text Layers Overlay
                if (!isShowingBefore && textLayers.isNotEmpty()) {
                    textLayers.forEach { textLayer ->
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (textLayer.positionX * (maxWPx - 150)).toInt(),
                                        (textLayer.positionY * (maxHPx - 100)).toInt()
                                    )
                                }
                                .rotate(textLayer.rotation)
                                .scale(textLayer.scale)
                        ) {
                            Text(
                                text = textLayer.text,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = textLayer.fontSizeSp.sp,
                                    fontWeight = if (textLayer.isBold) FontWeight.Bold else FontWeight.Normal,
                                    color = Color(textLayer.textColor)
                                ),
                                modifier = Modifier
                                    .background(
                                        if (textLayer.backgroundColor != 0L) Color(textLayer.backgroundColor) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Grid Guide Overlay for Straighten / Alignment
            if (showGridOverlay || activeTab == "CROP" || straightenAngle != 0f) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.35f)))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.35f)))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.35f)))
                    }
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.35f)))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.35f)))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.35f)))
                    }
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.35f)))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.35f)))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.35f)))
                    }
                }
            }

            // Interactive Crop Box Handle Overlay
            if (activeTab == "CROP") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, LuminaCyan, RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val w = (clip?.width ?: 1920)
                        val h = (clip?.height ?: 1080)
                        Text(
                            text = "Crop Bounds: ${w}x${h}px (${clip?.cropPreset ?: "ORIGINAL"})",
                            style = MaterialTheme.typography.labelSmall,
                            color = LuminaCyan
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builds android.graphics.ColorMatrix for adjustments & filters.
 */
private fun buildAndroidColorMatrix(
    adjustments: ColorAdjustments,
    filterName: String,
    filterIntensity: Float
): android.graphics.ColorMatrix {
    val matrix = android.graphics.ColorMatrix()

    // 1. Brightness
    if (adjustments.brightness != 0f) {
        val b = adjustments.brightness * 1.5f
        val bMatrix = android.graphics.ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, b,
                0f, 1f, 0f, 0f, b,
                0f, 0f, 1f, 0f, b,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(bMatrix)
    }

    // 2. Contrast
    if (adjustments.contrast != 0f) {
        val scale = (adjustments.contrast + 100f) / 100f
        val translate = (-0.5f * scale + 0.5f) * 255f
        val cMatrix = android.graphics.ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(cMatrix)
    }

    // 3. Saturation
    if (adjustments.saturation != 0f) {
        val sat = (adjustments.saturation + 100f) / 100f
        val satMatrix = android.graphics.ColorMatrix()
        satMatrix.setSaturation(sat)
        matrix.postConcat(satMatrix)
    }

    // 4. Temperature
    if (adjustments.temperature != 0f) {
        val temp = adjustments.temperature / 100f
        val tempMatrix = android.graphics.ColorMatrix(
            floatArrayOf(
                1f + temp * 0.2f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f - temp * 0.2f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(tempMatrix)
    }

    // 5. Filter Preset
    if (filterName != "Original" && filterIntensity > 0f) {
        val filterMatrix = android.graphics.ColorMatrix()
        when (filterName.uppercase()) {
            "BLACK & WHITE", "MONO CHROME", "MOODY NOIR" -> filterMatrix.setSaturation(0f)
            "VIVID", "VIVID GLOW" -> filterMatrix.setSaturation(1.5f)
            "WARM", "WARM SUNSET" -> filterMatrix.set(
                floatArrayOf(
                    1.2f, 0f, 0f, 0f, 10f,
                    0f, 1.1f, 0f, 0f, 5f,
                    0f, 0f, 0.9f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "COOL", "COOL FILM" -> filterMatrix.set(
                floatArrayOf(
                    0.9f, 0f, 0f, 0f, 0f,
                    0f, 1.0f, 0f, 0f, 5f,
                    0f, 0f, 1.3f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "CINEMATIC", "VINTAGE", "RETRO", "FILM" -> filterMatrix.set(
                floatArrayOf(
                    1.1f, 0f, 0f, 0f, 5f,
                    0f, 0.95f, 0f, 0f, 0f,
                    0f, 0f, 0.85f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        matrix.postConcat(filterMatrix)
    }

    return matrix
}
