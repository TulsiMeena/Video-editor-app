package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaObsidian
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.ui.theme.LuminaViolet
import com.example.utils.MediaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipToolbar(
    selectedClip: TimelineClip?,
    onSplitAtPlayhead: () -> Unit,
    onDeleteClip: () -> Unit,
    onDuplicateClip: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onReverseClip: () -> Unit,
    onFreezeFrame: () -> Unit,
    onRotateClip: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onSetFitFill: (String) -> Unit,
    onSetCropPreset: (String) -> Unit,
    onSetVolume: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onTrimClip: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedClip == null) return

    var activeSheet by remember { mutableStateOf<String?>(null) } // "TRIM", "SPEED", "VOLUME", "CROP", "INFO"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurface)
            .border(1.dp, LuminaSurfaceBorder)
            .padding(vertical = 4.dp)
    ) {
        // Selected Clip Info Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Editing: ${selectedClip.name}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = LuminaCyan
            )

            Text(
                text = "${selectedClip.width}x${selectedClip.height} • ${MediaUtils.formatDuration(selectedClip.effectiveDurationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }

        // Horizontal Scrollable Editing Tools Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButton(
                icon = Icons.Default.ContentCut,
                label = "Split",
                onClick = onSplitAtPlayhead,
                testTag = "tool_split_button"
            )

            ToolButton(
                icon = Icons.Default.Crop,
                label = "Trim",
                onClick = { activeSheet = "TRIM" },
                testTag = "tool_trim_button"
            )

            ToolButton(
                icon = Icons.Default.Speed,
                label = "Speed (${selectedClip.speed}x)",
                onClick = { activeSheet = "SPEED" },
                testTag = "tool_speed_button"
            )

            ToolButton(
                icon = Icons.Default.VolumeUp,
                label = "Volume (${(selectedClip.effectiveVolume * 100).toInt()}%)",
                onClick = { activeSheet = "VOLUME" },
                testTag = "tool_volume_button"
            )

            ToolButton(
                icon = Icons.Default.Delete,
                label = "Delete",
                onClick = onDeleteClip,
                isDangerous = true,
                testTag = "tool_delete_button"
            )

            ToolButton(
                icon = Icons.Default.ContentCopy,
                label = "Duplicate",
                onClick = onDuplicateClip,
                testTag = "tool_duplicate_button"
            )

            ToolButton(
                icon = Icons.Default.AcUnit,
                label = "Freeze Frame",
                onClick = onFreezeFrame,
                testTag = "tool_freeze_button"
            )

            ToolButton(
                icon = Icons.Default.Transform,
                label = "Reverse",
                onClick = onReverseClip,
                testTag = "tool_reverse_button"
            )

            ToolButton(
                icon = Icons.Default.RotateRight,
                label = "Rotate",
                onClick = onRotateClip,
                testTag = "tool_rotate_button"
            )

            ToolButton(
                icon = Icons.Default.Flip,
                label = "Flip H",
                onClick = onFlipHorizontal,
                testTag = "tool_fliph_button"
            )

            ToolButton(
                icon = Icons.Default.AspectRatio,
                label = "Fit / Fill",
                onClick = {
                    val nextMode = if (selectedClip.contentScale == "FILL") "FIT" else "FILL"
                    onSetFitFill(nextMode)
                },
                testTag = "tool_fitfill_button"
            )

            ToolButton(
                icon = Icons.Default.Crop,
                label = "Crop",
                onClick = { activeSheet = "CROP" },
                testTag = "tool_crop_button"
            )

            ToolButton(
                icon = Icons.Default.ArrowBack,
                label = "Move Left",
                onClick = onMoveLeft,
                testTag = "tool_moveleft_button"
            )

            ToolButton(
                icon = Icons.Default.ArrowForward,
                label = "Move Right",
                onClick = onMoveRight,
                testTag = "tool_moveright_button"
            )

            ToolButton(
                icon = Icons.Default.Info,
                label = "Clip Info",
                onClick = { activeSheet = "INFO" },
                testTag = "tool_info_button"
            )
        }
    }

    // Modal Bottom Sheets for Tools
    when (activeSheet) {
        "TRIM" -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = LuminaSurfaceElevated
            ) {
                var range by remember {
                    mutableStateOf(selectedClip.trimStartMs.toFloat()..selectedClip.trimEndMs.toFloat())
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("Trim Clip Handles", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start: ${MediaUtils.formatDuration(range.start.toLong())}  |  End: ${MediaUtils.formatDuration(range.endInclusive.toLong())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LuminaCyan
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    RangeSlider(
                        value = range,
                        onValueChange = { range = it },
                        valueRange = 0f..selectedClip.sourceDurationMs.toFloat(),
                        onValueChangeFinished = {
                            onTrimClip(range.start.toLong(), range.endInclusive.toLong())
                        },
                        modifier = Modifier.testTag("trim_range_slider")
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { activeSheet = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Done", color = LuminaCyan)
                    }
                }
            }
        }

        "SPEED" -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = LuminaSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("Clip Playback Speed", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    val presets = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 4.0f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { speed ->
                            Surface(
                                onClick = {
                                    onSetSpeed(speed)
                                    activeSheet = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedClip.speed == speed) LuminaViolet else LuminaSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder)
                            ) {
                                Text(
                                    text = "${speed}x",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        "VOLUME" -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = LuminaSurfaceElevated
            ) {
                var vol by remember { mutableStateOf(selectedClip.volume) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Audio Volume (${(vol * 100).toInt()}%)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        TextButton(onClick = onToggleMute) {
                            Text(if (selectedClip.isMuted) "Unmute" else "Mute", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = vol,
                        onValueChange = {
                            vol = it
                            onSetVolume(it)
                        },
                        valueRange = 0f..2f,
                        modifier = Modifier.testTag("volume_slider")
                    )
                }
            }
        }

        "CROP" -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = LuminaSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("Crop Preset", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    val presets = listOf("ORIGINAL", "16:9", "9:16", "1:1", "4:5", "FREE")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            Surface(
                                onClick = {
                                    onSetCropPreset(preset)
                                    activeSheet = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedClip.cropPreset == preset) LuminaCyan else LuminaSurface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedClip.cropPreset == preset) LuminaObsidian else Color.White,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        "INFO" -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = LuminaSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("Clip Properties", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("Clip Name", selectedClip.name)
                    InfoRow("Media Type", selectedClip.mediaType)
                    InfoRow("Resolution", "${selectedClip.width} x ${selectedClip.height}")
                    InfoRow("Source Duration", MediaUtils.formatDuration(selectedClip.sourceDurationMs))
                    InfoRow("Effective Duration", MediaUtils.formatDuration(selectedClip.effectiveDurationMs))
                    InfoRow("Playback Speed", "${selectedClip.speed}x")
                    InfoRow("Mime Type", selectedClip.mimeType)
                    InfoRow("URI", selectedClip.uri)

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { activeSheet = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Close", color = LuminaCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun ToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isDangerous: Boolean = false,
    tint: Color? = null,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = LuminaSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
        modifier = Modifier.testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint ?: if (isDangerous) MaterialTheme.colorScheme.error else LuminaCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isDangerous) MaterialTheme.colorScheme.error else Color.White
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}
