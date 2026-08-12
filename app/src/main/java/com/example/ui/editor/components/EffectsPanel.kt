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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

import com.example.data.model.ClipEffect
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun EffectsPanel(
    effects: List<ClipEffect>,
    onAddEffect: (String, String) -> Unit,
    onUpdateEffect: (ClipEffect) -> Unit,
    onRemoveEffect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf("Blur", "Glitch", "Motion", "Light", "Retro", "Cinematic", "Party", "Vlog", "Dream", "Distortion")
    }

    val presetTemplates = remember {
        listOf(
            ClipEffect(name = "Gaussian Blur", type = "BLUR", category = "Blur"),
            ClipEffect(name = "Digital Glitch", type = "GLITCH", category = "Glitch"),
            ClipEffect(name = "Zoom In", type = "ZOOM_IN", category = "Motion"),
            ClipEffect(name = "Zoom Out", type = "ZOOM_OUT", category = "Motion"),
            ClipEffect(name = "Camera Shake", type = "SHAKE", category = "Motion"),
            ClipEffect(name = "Rhythmic Pulse", type = "PULSE", category = "Motion"),
            ClipEffect(name = "Slow Cinematic Zoom", type = "SLOW_ZOOM", category = "Motion"),
            ClipEffect(name = "Camera Push", type = "CAMERA_PUSH", category = "Motion"),
            ClipEffect(name = "Camera Pull", type = "CAMERA_PULL", category = "Motion"),
            ClipEffect(name = "Sun Lens Leak", type = "LIGHT_LEAK", category = "Light"),
            ClipEffect(name = "Retro VHS Grain", type = "RETRO_NOISE", category = "Retro")
        )
    }

    var selectedCategory by remember { mutableStateOf("Blur") }
    var selectedActiveEffectId by remember { mutableStateOf(effects.firstOrNull()?.id) }

    val activeEffect = effects.firstOrNull { it.id == selectedActiveEffectId } ?: effects.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurface)
            .padding(12.dp)
    ) {
        // Category Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    onClick = { selectedCategory = cat },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) LuminaCyan else LuminaSurfaceElevated,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                    modifier = Modifier.testTag("effect_cat_$cat")
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Effects in Selected Category to Add
        val categoryTemplates = presetTemplates.filter { it.category == selectedCategory }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryTemplates.forEach { template ->
                OutlinedButton(
                    onClick = { onAddEffect(template.type, template.category) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_effect_${template.type.lowercase()}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = LuminaCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(template.name, style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Applied Effects Stack List
        if (effects.isNotEmpty()) {
            Text("Applied Effects (${effects.size}):", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                effects.forEach { fx ->
                    val isSelected = fx.id == activeEffect?.id
                    Surface(
                        onClick = { selectedActiveEffectId = fx.id },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) LuminaCyan else LuminaSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) LuminaCyan else LuminaSurfaceBorder),
                        modifier = Modifier.testTag("applied_effect_${fx.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fx.type,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.Black else Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onUpdateEffect(fx.copy(isEnabled = !fx.isEnabled)) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (fx.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = { onRemoveEffect(fx.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Red else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Controls for Active Effect
            activeEffect?.let { fx ->
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LuminaSurfaceElevated, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${fx.type} Settings", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Switch(
                            checked = fx.isEnabled,
                            onCheckedChange = { onUpdateEffect(fx.copy(isEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LuminaCyan),
                            modifier = Modifier.testTag("effect_toggle_${fx.id}")
                        )
                    }

                    // Intensity Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Intensity", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.width(70.dp))
                        Slider(
                            value = fx.intensity,
                            onValueChange = { onUpdateEffect(fx.copy(intensity = it)) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                            modifier = Modifier.weight(1f).testTag("effect_intensity_slider")
                        )
                        Text("${fx.intensity.toInt()}", style = MaterialTheme.typography.labelSmall, color = LuminaCyan, modifier = Modifier.width(36.dp))
                    }

                    // Speed Slider if Glitch / Motion
                    if (fx.type == "GLITCH" || fx.type.startsWith("ZOOM") || fx.type == "SHAKE" || fx.type == "PULSE") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Speed", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.width(70.dp))
                            Slider(
                                value = fx.speed,
                                onValueChange = { onUpdateEffect(fx.copy(speed = it)) },
                                valueRange = 0.5f..2.0f,
                                colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                                modifier = Modifier.weight(1f).testTag("effect_speed_slider")
                            )
                            Text("%.1fx".format(fx.speed), style = MaterialTheme.typography.labelSmall, color = LuminaCyan, modifier = Modifier.width(36.dp))
                        }
                    }
                }
            }
        }
    }
}
