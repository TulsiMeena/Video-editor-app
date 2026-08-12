package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ClipTransform
import com.example.data.model.TransformKeyframe
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun TransformKeyframePanel(
    playheadOffsetMs: Long,
    keyframes: List<TransformKeyframe>,
    baseTransform: ClipTransform,
    onAddKeyframe: (TransformKeyframe) -> Unit,
    onDeleteKeyframe: (String) -> Unit,
    onUpdateTransform: (ClipTransform) -> Unit,
    onResetTransform: (String) -> Unit, // "POSITION", "SCALE", "ROTATION", "OPACITY", "ALL"
    modifier: Modifier = Modifier
) {
    val easings = remember { listOf("LINEAR", "EASE_IN", "EASE_OUT", "EASE_IN_OUT") }

    // Check if keyframe exists near current playhead position
    val existingKeyframe = keyframes.firstOrNull { Math.abs(it.timeOffsetMs - playheadOffsetMs) < 200L }

    val activeTransform = if (existingKeyframe != null) {
        ClipTransform(
            positionX = existingKeyframe.positionX,
            positionY = existingKeyframe.positionY,
            scale = existingKeyframe.scale,
            rotation = existingKeyframe.rotation,
            opacity = existingKeyframe.opacity
        )
    } else {
        baseTransform
    }

    var selectedEasing by remember { mutableStateOf(existingKeyframe?.easing ?: "LINEAR") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurface)
            .padding(12.dp)
    ) {
        // Keyframe Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Playhead: ${playheadOffsetMs / 1000f}s", style = MaterialTheme.typography.labelMedium, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                if (existingKeyframe != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LuminaCyan
                    ) {
                        Text("◇ Keyframe Active", style = MaterialTheme.typography.labelSmall, color = Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            if (existingKeyframe == null) {
                OutlinedButton(
                    onClick = {
                        val newKf = TransformKeyframe(
                            timeOffsetMs = playheadOffsetMs,
                            positionX = baseTransform.positionX,
                            positionY = baseTransform.positionY,
                            scale = baseTransform.scale,
                            rotation = baseTransform.rotation,
                            opacity = baseTransform.opacity,
                            easing = selectedEasing
                        )
                        onAddKeyframe(newKf)
                    },
                    modifier = Modifier.testTag("add_keyframe_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = LuminaCyan, modifier = Modifier.height(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("◇ Add Keyframe", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                }
            } else {
                OutlinedButton(
                    onClick = { onDeleteKeyframe(existingKeyframe.id) },
                    modifier = Modifier.testTag("delete_keyframe_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.height(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("◆ Delete Keyframe", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Position X & Y Sliders
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Position X", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.width(75.dp))
            Slider(
                value = activeTransform.positionX,
                onValueChange = { onUpdateTransform(activeTransform.copy(positionX = it)) },
                valueRange = -500f..500f,
                colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                modifier = Modifier.weight(1f).testTag("transform_pos_x_slider")
            )
            Text("${activeTransform.positionX.toInt()}", style = MaterialTheme.typography.labelSmall, color = LuminaCyan, modifier = Modifier.width(40.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Position Y", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.width(75.dp))
            Slider(
                value = activeTransform.positionY,
                onValueChange = { onUpdateTransform(activeTransform.copy(positionY = it)) },
                valueRange = -500f..500f,
                colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                modifier = Modifier.weight(1f).testTag("transform_pos_y_slider")
            )
            Text("${activeTransform.positionY.toInt()}", style = MaterialTheme.typography.labelSmall, color = LuminaCyan, modifier = Modifier.width(40.dp))
        }

        // Scale & Rotation Sliders
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Scale", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.width(75.dp))
            Slider(
                value = activeTransform.scale,
                onValueChange = { onUpdateTransform(activeTransform.copy(scale = it)) },
                valueRange = 0.2f..3.0f,
                colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                modifier = Modifier.weight(1f).testTag("transform_scale_slider")
            )
            Text("%.2fx".format(activeTransform.scale), style = MaterialTheme.typography.labelSmall, color = LuminaCyan, modifier = Modifier.width(40.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Rotation", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.width(75.dp))
            Slider(
                value = activeTransform.rotation,
                onValueChange = { onUpdateTransform(activeTransform.copy(rotation = it)) },
                valueRange = -180f..180f,
                colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                modifier = Modifier.weight(1f).testTag("transform_rotation_slider")
            )
            Text("${activeTransform.rotation.toInt()}°", style = MaterialTheme.typography.labelSmall, color = LuminaCyan, modifier = Modifier.width(40.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Opacity", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.width(75.dp))
            Slider(
                value = activeTransform.opacity,
                onValueChange = { onUpdateTransform(activeTransform.copy(opacity = it)) },
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                modifier = Modifier.weight(1f).testTag("transform_opacity_slider")
            )
            Text("${(activeTransform.opacity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = LuminaCyan, modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Easing selector
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Easing:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                easings.forEach { ease ->
                    val isSelected = selectedEasing == ease
                    Surface(
                        onClick = {
                            selectedEasing = ease
                            if (existingKeyframe != null) {
                                onAddKeyframe(existingKeyframe.copy(easing = ease))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) LuminaCyan else LuminaSurfaceElevated,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                        modifier = Modifier.testTag("keyframe_easing_$ease")
                    ) {
                        Text(
                            text = ease.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reset Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("POSITION", "SCALE", "ROTATION", "OPACITY", "ALL").forEach { resetType ->
                OutlinedButton(
                    onClick = { onResetTransform(resetType) },
                    modifier = Modifier.testTag("reset_transform_${resetType.lowercase()}")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray, modifier = Modifier.height(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset ${resetType.lowercase().capitalize()}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}
