package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun ResizeCanvasPanel(
    currentRatio: String,
    fitMode: String,
    onSetRatio: (String) -> Unit,
    onSetFitMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf("1:1", "4:5", "9:16", "16:9", "3:4", "ORIGINAL")
    val fitModes = listOf("FIT_SCREEN", "FIT_WIDTH", "FIT_HEIGHT", "FILL")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurfaceElevated)
            .padding(12.dp)
    ) {
        Text("Canvas Ratio", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { ratio ->
                val isSelected = currentRatio == ratio
                Surface(
                    onClick = { onSetRatio(ratio) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) LuminaCyan else LuminaSurface,
                    modifier = Modifier.testTag("canvas_ratio_$ratio")
                ) {
                    Text(
                        text = ratio,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(vertical = 2.dp))

        Text("Image Scaling & Fit", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fitModes.forEach { mode ->
                val isSelected = fitMode == mode
                Surface(
                    onClick = { onSetFitMode(mode) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) LuminaCyan else LuminaSurface,
                    modifier = Modifier.testTag("canvas_fit_$mode")
                ) {
                    Text(
                        text = mode.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
