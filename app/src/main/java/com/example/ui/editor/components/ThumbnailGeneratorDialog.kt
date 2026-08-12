package com.example.ui.editor.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import java.io.File
import java.io.FileOutputStream

@Composable
fun ThumbnailGeneratorDialog(
    clips: List<TimelineClip>,
    totalDurationMs: Long,
    onDismiss: () -> Unit,
    onThumbnailExported: (filePath: String) -> Unit
) {
    val context = LocalContext.current
    var titleOverlayText by remember { mutableStateOf("MUST WATCH!") }
    var selectedAspect by remember { mutableStateOf("16:9") }
    var selectedFormat by remember { mutableStateOf("JPG") }
    var selectedFrameIdx by remember { mutableStateOf(0) }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    val frameOffsets = listOf(0.1f, 0.25f, 0.5f, 0.75f, 0.9f)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("thumbnail_generator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = LuminaCyan
                    )
                    Text(
                        text = "AI Thumbnail Generator",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select keyframe candidate:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    frameOffsets.forEachIndexed { idx, frac ->
                        val isSel = selectedFrameIdx == idx
                        Surface(
                            onClick = { selectedFrameIdx = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) LuminaCyan.copy(alpha = 0.2f) else LuminaSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) LuminaCyan else LuminaSurfaceBorder
                            ),
                            modifier = Modifier
                                .size(70.dp, 50.dp)
                                .testTag("frame_cand_$idx")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Frame ${idx + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Overlay Input
                OutlinedTextField(
                    value = titleOverlayText,
                    onValueChange = { titleOverlayText = it },
                    label = { Text("Title Overlay Text") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminaCyan,
                        unfocusedBorderColor = LuminaSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("thumbnail_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Aspect Ratio:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("16:9", "9:16", "1:1").forEach { ratio ->
                                val isSel = selectedAspect == ratio
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedAspect = ratio },
                                    label = { Text(ratio, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    Column {
                        Text("Format:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("JPG", "PNG", "WEBP").forEach { fmt ->
                                val isSel = selectedFormat == fmt
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedFormat = fmt },
                                    label = { Text(fmt, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                exportStatusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = msg, style = MaterialTheme.typography.labelMedium, color = LuminaCyan)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val exported = saveThumbnailFile(context, titleOverlayText, selectedAspect, selectedFormat)
                            exportStatusMessage = "Thumbnail saved: ${exported.name}"
                            onThumbnailExported(exported.absolutePath)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                        modifier = Modifier.testTag("export_thumbnail_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Export", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun saveThumbnailFile(context: android.content.Context, text: String, aspect: String, format: String): File {
    val width = 1280
    val height = when (aspect) {
        "9:16" -> 2275
        "1:1" -> 1280
        else -> 720
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background fill
    val bgPaint = Paint().apply { color = android.graphics.Color.DKGRAY }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Draw text
    if (text.isNotBlank()) {
        val textPaint = Paint().apply {
            color = android.graphics.Color.YELLOW
            textSize = 72f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, width / 2f, height / 2f, textPaint)
    }

    val fileExt = format.lowercase()
    val outFile = File(context.getExternalFilesDir(null) ?: context.cacheDir, "thumbnail_${System.currentTimeMillis()}.$fileExt")
    val compressFormat = when (format) {
        "PNG" -> Bitmap.CompressFormat.PNG
        "WEBP" -> Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.JPEG
    }

    FileOutputStream(outFile).use { out ->
        bitmap.compress(compressFormat, 90, out)
    }

    return outFile
}
