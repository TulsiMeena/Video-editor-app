package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CollageSettings
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun CollagePanel(
    collageSettings: CollageSettings,
    onSetCollagePreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf("NONE", "2_PHOTOS", "3_PHOTOS", "4_PHOTOS", "6_PHOTOS", "9_PHOTOS")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurfaceElevated)
            .padding(12.dp)
    ) {
        Text("Photo Collage Presets", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = collageSettings.presetName == preset
                Surface(
                    onClick = { onSetCollagePreset(preset) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) LuminaCyan else LuminaSurface,
                    modifier = Modifier.testTag("collage_preset_$preset")
                ) {
                    Text(
                        text = preset.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
