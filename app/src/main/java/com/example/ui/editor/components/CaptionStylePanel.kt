package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaptionSegment
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated

@Composable
fun CaptionStylePanel(
    selectedCaption: CaptionSegment?,
    allCaptions: List<CaptionSegment>,
    onUpdateCaption: (CaptionSegment) -> Unit,
    onDeleteCaption: (String) -> Unit,
    onSplitCaption: (String) -> Unit,
    onMergeNextCaption: (String) -> Unit,
    onExportSrt: () -> Unit,
    onClose: () -> Unit
) {
    if (selectedCaption == null) return

    var editedText by remember(selectedCaption) { mutableStateOf(selectedCaption.text) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LuminaSurface)
            .padding(12.dp)
            .testTag("caption_style_panel")
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ClosedCaption, contentDescription = null, tint = LuminaCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Caption Editor & Styling",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Row {
                TextButton(onClick = onExportSrt, modifier = Modifier.testTag("export_srt_button")) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = LuminaCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(".SRT File", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Caption Text Input
        OutlinedTextField(
            value = editedText,
            onValueChange = {
                editedText = it
                onUpdateCaption(selectedCaption.copy(text = it))
            },
            label = { Text("Caption Text") },
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("caption_text_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Preset Styles Selector
        Text(
            text = "Preset Style:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("CLASSIC", "BOLD", "MINIMAL", "CINEMA", "SOCIAL", "KARAOKE").forEach { preset ->
                val isSel = selectedCaption.stylePreset == preset
                Surface(
                    onClick = {
                        val styled = when (preset) {
                            "KARAOKE" -> selectedCaption.copy(stylePreset = preset, fontSizeSp = 26, textColor = 0xFFFFFFFF, highlightColor = 0xFFFFD700)
                            "CINEMA" -> selectedCaption.copy(stylePreset = preset, fontSizeSp = 20, hasBackground = true, backgroundColor = 0xCC000000)
                            "BOLD" -> selectedCaption.copy(stylePreset = preset, fontSizeSp = 28, hasStroke = true, strokeColor = 0xFF000000)
                            "SOCIAL" -> selectedCaption.copy(stylePreset = preset, fontSizeSp = 24, textColor = 0xFFFFEB3B)
                            else -> selectedCaption.copy(stylePreset = preset, fontSizeSp = 22)
                        }
                        onUpdateCaption(styled)
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) LuminaCyan else LuminaSurfaceElevated,
                    border = if (isSel) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                    modifier = Modifier.testTag("preset_$preset")
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSel) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Controls: Size, Position Y, Delete, Split, Merge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onDeleteCaption(selectedCaption.id) },
                modifier = Modifier.testTag("delete_caption_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }

            IconButton(
                onClick = { onSplitCaption(selectedCaption.id) },
                modifier = Modifier.testTag("split_caption_button")
            ) {
                Icon(Icons.Default.ContentCut, contentDescription = "Split", tint = LuminaCyan)
            }

            IconButton(
                onClick = { onMergeNextCaption(selectedCaption.id) },
                modifier = Modifier.testTag("merge_caption_button")
            ) {
                Icon(Icons.Default.MergeType, contentDescription = "Merge Next", tint = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Font Size adjustment
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val newSize = (selectedCaption.fontSizeSp - 2).coerceAtLeast(12)
                    onUpdateCaption(selectedCaption.copy(fontSizeSp = newSize))
                }) {
                    Text("-", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                Text(
                    text = "${selectedCaption.fontSizeSp} sp",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
                IconButton(onClick = {
                    val newSize = (selectedCaption.fontSizeSp + 2).coerceAtMost(48)
                    onUpdateCaption(selectedCaption.copy(fontSizeSp = newSize))
                }) {
                    Text("+", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
        }
    }
}
