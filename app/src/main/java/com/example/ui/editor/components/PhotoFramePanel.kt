package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.PhotoFrameSettings
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun PhotoFramePanel(
    frameSettings: PhotoFrameSettings,
    onUpdateFrame: (Float, Long, Float, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var frameWidth by remember { mutableFloatStateOf(frameSettings.frameWidthDp) }
    var cornerRadius by remember { mutableFloatStateOf(frameSettings.cornerRadiusDp) }
    var frameStyle by remember { mutableStateOf(frameSettings.style) }

    val styles = listOf("NONE", "MODERN", "POLAROID", "FILM", "NEON", "WOOD")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurfaceElevated)
            .padding(12.dp)
    ) {
        Text("Frame Presets", style = MaterialTheme.typography.labelMedium, color = LuminaCyan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { style ->
                val isSelected = frameStyle == style
                Surface(
                    onClick = {
                        frameStyle = style
                        val w = if (style == "NONE") 0f else 12f
                        val r = if (style == "POLAROID") 4f else 16f
                        frameWidth = w
                        cornerRadius = r
                        onUpdateFrame(w, 0xFFFFFFFF, r, style)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) LuminaCyan else LuminaSurface,
                    modifier = Modifier.testTag("frame_style_$style")
                ) {
                    Text(
                        text = style,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Frame Width Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Width: ${frameWidth.toInt()}dp", style = MaterialTheme.typography.labelSmall, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = frameWidth,
                onValueChange = {
                    frameWidth = it
                    onUpdateFrame(frameWidth, 0xFFFFFFFF, cornerRadius, frameStyle)
                },
                valueRange = 0f..40f,
                colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
