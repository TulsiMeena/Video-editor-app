package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun BackgroundRemovalPanel(
    selectedClip: TimelineClip,
    onUpdateClip: (TimelineClip) -> Unit,
    onClose: () -> Unit
) {
    var isEnabled by remember(selectedClip) { mutableStateOf(selectedClip.isBackgroundRemoved) }
    var bgType by remember(selectedClip) { mutableStateOf(selectedClip.bgReplacementType) }
    var softness by remember(selectedClip) { mutableStateOf(selectedClip.edgeSoftness) }
    var feather by remember(selectedClip) { mutableStateOf(selectedClip.feather) }
    var strength by remember(selectedClip) { mutableStateOf(selectedClip.strength) }

    val presetColors = listOf(
        0xFF00FF00, // Green Screen
        0xFF0000FF, // Blue Screen
        0xFF000000, // Black
        0xFFFFFFFF, // White
        0xFFFF5722, // Orange
        0xFF9C27B0  // Purple
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LuminaSurface)
            .padding(12.dp)
            .testTag("background_removal_panel")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = LuminaCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Background Removal & Replacement",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LuminaSurfaceElevated, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Remove Background",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Switch(
                checked = isEnabled,
                onCheckedChange = {
                    isEnabled = it
                    onUpdateClip(selectedClip.copy(isBackgroundRemoved = it))
                },
                colors = SwitchDefaults.colors(checkedThumbColor = LuminaCyan, checkedTrackColor = LuminaCyan.copy(alpha = 0.3f)),
                modifier = Modifier.testTag("bg_removal_switch")
            )
        }

        if (isEnabled) {
            Spacer(modifier = Modifier.height(12.dp))

            // Replacement Type
            Text("Replacement Canvas:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("TRANSPARENT", "COLOR", "IMAGE").forEach { type ->
                    val isSel = bgType == type
                    Surface(
                        onClick = {
                            bgType = type
                            onUpdateClip(selectedClip.copy(bgReplacementType = type))
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) LuminaCyan else LuminaSurfaceElevated,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bg_type_$type")
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSel) Color.Black else Color.White,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            if (bgType == "COLOR") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { col ->
                        val colorObj = Color(col)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorObj)
                                .border(
                                    2.dp,
                                    if (selectedClip.bgReplacementColor == col) LuminaCyan else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    onUpdateClip(selectedClip.copy(bgReplacementColor = col))
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Edge Softness
            Text("Edge Softness (${softness.toInt()}%)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Slider(
                value = softness,
                onValueChange = {
                    softness = it
                    onUpdateClip(selectedClip.copy(edgeSoftness = it))
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                modifier = Modifier.testTag("edge_softness_slider")
            )

            // Feathering
            Text("Feather (${feather.toInt()}%)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Slider(
                value = feather,
                onValueChange = {
                    feather = it
                    onUpdateClip(selectedClip.copy(feather = it))
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                modifier = Modifier.testTag("feather_slider")
            )
        }
    }
}
