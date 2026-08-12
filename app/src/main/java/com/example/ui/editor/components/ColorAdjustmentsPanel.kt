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
import com.example.data.model.ColorAdjustments
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun ColorAdjustmentsPanel(
    adjustments: ColorAdjustments,
    onAdjustmentsChange: (ColorAdjustments) -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controls = remember {
        listOf(
            "Brightness", "Contrast", "Saturation", "Exposure",
            "Temperature", "Tint", "Highlights", "Shadows",
            "Sharpen", "Fade", "Vignette", "Grain"
        )
    }

    var selectedControl by remember { mutableStateOf("Brightness") }

    val currentValue = when (selectedControl) {
        "Brightness" -> adjustments.brightness
        "Contrast" -> adjustments.contrast
        "Saturation" -> adjustments.saturation
        "Exposure" -> adjustments.exposure
        "Temperature" -> adjustments.temperature
        "Tint" -> adjustments.tint
        "Highlights" -> adjustments.highlights
        "Shadows" -> adjustments.shadows
        "Sharpen" -> adjustments.sharpen
        "Fade" -> adjustments.fade
        "Vignette" -> adjustments.vignette
        "Grain" -> adjustments.grain
        else -> 0f
    }

    fun updateControlValue(valNew: Float) {
        val updated = when (selectedControl) {
            "Brightness" -> adjustments.copy(brightness = valNew)
            "Contrast" -> adjustments.copy(contrast = valNew)
            "Saturation" -> adjustments.copy(saturation = valNew)
            "Exposure" -> adjustments.copy(exposure = valNew)
            "Temperature" -> adjustments.copy(temperature = valNew)
            "Tint" -> adjustments.copy(tint = valNew)
            "Highlights" -> adjustments.copy(highlights = valNew)
            "Shadows" -> adjustments.copy(shadows = valNew)
            "Sharpen" -> adjustments.copy(sharpen = valNew)
            "Fade" -> adjustments.copy(fade = valNew)
            "Vignette" -> adjustments.copy(vignette = valNew)
            "Grain" -> adjustments.copy(grain = valNew)
            else -> adjustments
        }
        onAdjustmentsChange(updated)
    }

    fun resetSelectedControl() {
        updateControlValue(0f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurface)
            .padding(12.dp)
    ) {
        // Control Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            controls.forEach { controlName ->
                val isSelected = selectedControl == controlName
                Surface(
                    onClick = { selectedControl = controlName },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) LuminaCyan else LuminaSurfaceElevated,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                    modifier = Modifier.testTag("color_control_$controlName")
                ) {
                    Text(
                        text = controlName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slider for Active Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedControl,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.width(85.dp)
            )

            Slider(
                value = currentValue,
                onValueChange = { updateControlValue(it) },
                valueRange = -100f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = LuminaCyan,
                    activeTrackColor = LuminaCyan,
                    inactiveTrackColor = LuminaSurfaceBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("color_slider_${selectedControl.lowercase()}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (currentValue > 0) "+${currentValue.toInt()}" else "${currentValue.toInt()}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = LuminaCyan,
                modifier = Modifier.width(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { resetSelectedControl() },
                modifier = Modifier.testTag("reset_control_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray, modifier = Modifier.height(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset $selectedControl", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onResetAll,
                modifier = Modifier.testTag("reset_all_controls_button")
            ) {
                Text("Reset All", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
            }
        }
    }
}
