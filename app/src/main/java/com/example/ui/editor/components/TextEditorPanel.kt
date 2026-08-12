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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TextLayer
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaObsidian
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.ui.theme.LuminaViolet

@Composable
fun TextEditorPanel(
    textLayer: TextLayer,
    onUpdateLayer: (TextLayer) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var contentText by remember(textLayer) { mutableStateOf(textLayer.text) }
    var selectedTab by remember { mutableStateOf("TEXT") } // "TEXT", "STYLE", "ANIMATION"

    val colorPresets = listOf(
        0xFFFFFFFF, 0xFFFFE500, 0xFF00E5FF, 0xFFFF2A6D,
        0xFF00FF66, 0xFFFF9900, 0xFFD800FF, 0xFF000000
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaObsidian)
            .border(1.dp, LuminaSurfaceBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Text Layer",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                color = LuminaViolet
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Field with Hindi / Devanagari support
        OutlinedTextField(
            value = contentText,
            onValueChange = {
                contentText = it
                onUpdateLayer(textLayer.copy(text = it))
            },
            label = { Text("Text Content (Supports Hindi/Unicode)", color = Color.Gray) },
            singleLine = false,
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LuminaCyan,
                unfocusedBorderColor = LuminaSurfaceBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().testTag("text_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector: Text / Style / Animation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("TEXT" to "Font & Size", "STYLE" to "Style & Background", "ANIMATION" to "Animation").forEach { (tab, label) ->
                val isSel = selectedTab == tab
                FilterChip(
                    selected = isSel,
                    onClick = { selectedTab = tab },
                    label = { Text(label, color = if (isSel) Color.White else Color.Gray) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LuminaViolet,
                        containerColor = LuminaSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            "TEXT" -> {
                // Font Family Selector
                Text("Font Family", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DEFAULT", "SERIF", "MONOSPACE", "SANS_SERIF", "CURSIVE", "BOLD_HEADER").forEach { font ->
                        val isSel = textLayer.fontFamily == font
                        Surface(
                            onClick = { onUpdateLayer(textLayer.copy(fontFamily = font)) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) LuminaCyan else LuminaSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder)
                        ) {
                            Text(
                                text = font,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (isSel) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Font Size & Opacity Sliders
                Text("Font Size: ${textLayer.fontSizeSp} sp", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Slider(
                    value = textLayer.fontSizeSp.toFloat(),
                    onValueChange = { onUpdateLayer(textLayer.copy(fontSizeSp = it.toInt())) },
                    valueRange = 12f..72f,
                    colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan)
                )

                Text("Text Opacity: ${(textLayer.opacity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Slider(
                    value = textLayer.opacity,
                    onValueChange = { onUpdateLayer(textLayer.copy(opacity = it)) },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = LuminaViolet, activeTrackColor = LuminaViolet)
                )

                // Bold / Italic / Alignment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onUpdateLayer(textLayer.copy(isBold = !textLayer.isBold)) },
                            modifier = Modifier.background(if (textLayer.isBold) LuminaViolet else LuminaSurfaceElevated, CircleShape)
                        ) {
                            Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = Color.White)
                        }

                        IconButton(
                            onClick = { onUpdateLayer(textLayer.copy(isItalic = !textLayer.isItalic)) },
                            modifier = Modifier.background(if (textLayer.isItalic) LuminaViolet else LuminaSurfaceElevated, CircleShape)
                        ) {
                            Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = Color.White)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LEFT" to Icons.Default.FormatAlignLeft, "CENTER" to Icons.Default.FormatAlignCenter, "RIGHT" to Icons.Default.FormatAlignRight).forEach { (align, icon) ->
                            val isSel = textLayer.alignment == align
                            IconButton(
                                onClick = { onUpdateLayer(textLayer.copy(alignment = align)) },
                                modifier = Modifier.background(if (isSel) LuminaCyan else LuminaSurfaceElevated, CircleShape)
                            ) {
                                Icon(icon, contentDescription = align, tint = if (isSel) Color.Black else Color.White)
                            }
                        }
                    }
                }
            }

            "STYLE" -> {
                // Text Color Palette
                Text("Text Color", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorPresets.forEach { colorLong ->
                        val isSel = textLayer.textColor == colorLong
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(
                                    width = if (isSel) 3.dp else 1.dp,
                                    color = if (isSel) LuminaCyan else Color.DarkGray,
                                    shape = CircleShape
                                )
                                .clickable { onUpdateLayer(textLayer.copy(textColor = colorLong)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stroke Toggle & Width
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Text Stroke / Outline", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    Switch(
                        checked = textLayer.hasStroke,
                        onCheckedChange = { onUpdateLayer(textLayer.copy(hasStroke = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = LuminaCyan)
                    )
                }

                if (textLayer.hasStroke) {
                    Slider(
                        value = textLayer.strokeWidthDp,
                        onValueChange = { onUpdateLayer(textLayer.copy(strokeWidthDp = it)) },
                        valueRange = 1f..10f,
                        colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Background Box Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Background Box", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    Switch(
                        checked = textLayer.hasBackground,
                        onCheckedChange = { onUpdateLayer(textLayer.copy(hasBackground = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = LuminaViolet)
                    )
                }
            }

            "ANIMATION" -> {
                Text("Entrance Animation", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("NONE", "FADE", "POP", "SLIDE_UP", "SLIDE_DOWN", "ZOOM_IN", "ZOOM_OUT").forEach { anim ->
                        val isSel = textLayer.animationType == anim
                        Surface(
                            onClick = { onUpdateLayer(textLayer.copy(animationType = anim)) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) LuminaViolet else LuminaSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder)
                        ) {
                            Text(
                                text = anim,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
