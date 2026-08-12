package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaObsidian
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.ui.theme.LuminaViolet
import com.example.utils.MediaUtils

@Composable
fun AudioClipToolbar(
    selectedAudioClip: AudioClip?,
    onSplitAtPlayhead: () -> Unit,
    onDeleteAudioClip: () -> Unit,
    onDuplicateAudioClip: () -> Unit,
    onMoveAudioClipLeft: () -> Unit,
    onMoveAudioClipRight: () -> Unit,
    onSetAudioVolume: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onSetFadeIn: (Long) -> Unit,
    onSetFadeOut: (Long) -> Unit,
    onSetAudioSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedAudioClip == null) {
        Surface(
            modifier = modifier.fillMaxWidth().height(60.dp),
            color = LuminaObsidian
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tap an Audio Clip on timeline to edit Volume, Fade & Speed",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaObsidian)
            .border(1.dp, LuminaSurfaceBorder, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .padding(10.dp)
    ) {
        // Top Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = LuminaViolet
                ) {
                    Text(
                        text = selectedAudioClip.audioType,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = selectedAudioClip.title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1
                )
            }

            Text(
                text = "Vol: ${(selectedAudioClip.effectiveVolume * 100).toInt()}% • ${MediaUtils.formatDuration(selectedAudioClip.effectiveDurationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = LuminaCyan
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Tool Action Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Split
            ToolButton(
                icon = Icons.Default.ContentCut,
                label = "Split",
                onClick = onSplitAtPlayhead,
                testTag = "audio_tool_split"
            )

            // Move Left
            ToolButton(
                icon = Icons.Default.ArrowBack,
                label = "Shift -1s",
                onClick = onMoveAudioClipLeft,
                testTag = "audio_tool_left"
            )

            // Move Right
            ToolButton(
                icon = Icons.Default.ArrowForward,
                label = "Shift +1s",
                onClick = onMoveAudioClipRight,
                testTag = "audio_tool_right"
            )

            // Duplicate
            ToolButton(
                icon = Icons.Default.ContentCopy,
                label = "Duplicate",
                onClick = onDuplicateAudioClip,
                testTag = "audio_tool_duplicate"
            )

            // Delete
            ToolButton(
                icon = Icons.Default.Delete,
                label = "Delete",
                onClick = onDeleteAudioClip,
                tint = MaterialTheme.colorScheme.error,
                testTag = "audio_tool_delete"
            )

            // Mute / Unmute
            ToolButton(
                icon = if (selectedAudioClip.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                label = if (selectedAudioClip.isMuted) "Unmute" else "Mute",
                onClick = onToggleMute,
                testTag = "audio_tool_mute"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Volume Presets & Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Volume:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            listOf(0.0f, 0.25f, 0.50f, 0.75f, 1.0f, 1.50f, 2.0f).forEach { vol ->
                val isSel = selectedAudioClip.volume == vol
                Surface(
                    onClick = { onSetAudioVolume(vol) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) LuminaCyan else LuminaSurfaceElevated
                ) {
                    Text(
                        text = "${(vol * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isSel) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Fade In & Fade Out Durations
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Fade In: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                listOf(0L, 1000L, 2000L, 3000L, 5000L).forEach { fade ->
                    val isSel = selectedAudioClip.fadeInMs == fade
                    Surface(
                        onClick = { onSetFadeIn(fade) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) LuminaViolet else LuminaSurfaceElevated,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "${fade / 1000}s",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Fade Out: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                listOf(0L, 1000L, 2000L, 3000L, 5000L).forEach { fade ->
                    val isSel = selectedAudioClip.fadeOutMs == fade
                    Surface(
                        onClick = { onSetFadeOut(fade) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) LuminaViolet else LuminaSurfaceElevated,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "${fade / 1000}s",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
