package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun DrawPanel(
    onClearDrawings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var brushType by remember { mutableStateOf("PEN") } // PEN, MARKER, ERASER
    var brushSize by remember { mutableFloatStateOf(10f) }
    var selectedColor by remember { mutableLongStateOf(0xFFFF0000) }

    val colors = listOf(
        0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00,
        0xFFFF00FF, 0xFF00FFFF, 0xFFFFFFFF, 0xFF000000, 0xFFFF9800
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(com.example.ui.theme.LuminaSurfaceElevated)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tool Selectors
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("PEN", "MARKER", "ERASER").forEach { tool ->
                    val isSelected = brushType == tool
                    Surface(
                        onClick = { brushType = tool },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) com.example.ui.theme.LuminaCyan else com.example.ui.theme.LuminaSurface,
                        modifier = Modifier.testTag("draw_tool_$tool")
                    ) {
                        Text(
                            text = tool,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onClearDrawings) {
                Icon(Icons.Default.Clear, contentDescription = "Clear Drawing", tint = Color.Red)
            }
        }

        // Color Palette
        if (brushType != "ERASER") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colors.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(hex), CircleShape)
                            .border(
                                width = if (selectedColor == hex) 2.dp else 0.dp,
                                color = if (selectedColor == hex) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = hex }
                    )
                }
            }
        }

        // Brush Size Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Brush Size: ${brushSize.toInt()}px", style = MaterialTheme.typography.labelSmall, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = brushSize,
                onValueChange = { brushSize = it },
                valueRange = 2f..50f,
                colors = SliderDefaults.colors(thumbColor = com.example.ui.theme.LuminaCyan, activeTrackColor = com.example.ui.theme.LuminaCyan),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
