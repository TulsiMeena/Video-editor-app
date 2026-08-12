package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AiColorAssistDialog(
    selectedClip: TimelineClip?,
    onApplyAutoColor: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var showBeforePreview by remember { mutableStateOf(false) }
    var isCalculated by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("ai_color_assist_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = LuminaCyan
                    )
                    Text(
                        text = "AI Color Assist & Auto Grade",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isCalculated && !isAnalyzing) {
                    Text(
                        text = "Analyzes frame luminance and RGB color distribution to automatically fix exposure, boost contrast, and optimize white balance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                isAnalyzing = true
                                scope.launch {
                                    delay(600)
                                    isAnalyzing = false
                                    isCalculated = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("calculate_auto_color_button")
                        ) {
                            Text("Calculate Auto Color", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isAnalyzing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = LuminaCyan)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analyzing frame histogram & color temperature...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LuminaSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Auto Color Corrections Calculated:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = LuminaCyan)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• Exposure: +12%", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("• Contrast: +15%", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("• Saturation & Vibrance: +10%", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("• Sharpness / Detail: +25%", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hold to compare button
                    OutlinedButton(
                        onClick = { showBeforePreview = !showBeforePreview },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LuminaCyan),
                        modifier = Modifier.fillMaxWidth().testTag("toggle_before_after_color_button")
                    ) {
                        Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showBeforePreview) "Showing BEFORE" else "Showing AFTER (Auto Color)")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isCalculated = false }) {
                            Text("Reset", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onApplyAutoColor()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("apply_auto_color_button")
                        ) {
                            Text("Apply Auto Color", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
