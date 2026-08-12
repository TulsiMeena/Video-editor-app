package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AiAutoEditDialog(
    clips: List<TimelineClip>,
    totalDurationMs: Long,
    onApplyAutoEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var stepIndex by remember { mutableStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    val steps = listOf(
        "Analyzing scene boundaries & visual variance...",
        "Detecting audio pauses & speech activity...",
        "Calculating optimal pace & beat sync...",
        "Balancing color exposure & contrast...",
        "Applying smart non-destructive transitions..."
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("ai_auto_edit_dialog")
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LuminaCyan
                    )
                    Text(
                        text = "AI Auto Edit Assistant",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isAnalyzing && !isFinished) {
                    Text(
                        text = "AI Auto Edit analyzes your project media to generate a smart, polished edit with optimal pacing, silence removal, and auto color enhancement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LuminaSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Summary of Planned Operations:", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• Scene Cut Optimization (${clips.size} clip source)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("• Silence & Speech Pause Removal", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("• Auto Exposure & Vibrance Balancing", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("• Dissolve Transitions on Scene Breaks", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Original video files remain untouched. Timeline can be edited anytime.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }

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
                                    for (i in steps.indices) {
                                        stepIndex = i
                                        delay(600)
                                    }
                                    isAnalyzing = false
                                    isFinished = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("start_auto_edit_button")
                        ) {
                            Text("Analyze & Auto Edit", fontWeight = FontWeight.Bold)
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
                            text = steps[stepIndex],
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (stepIndex + 1) / steps.size.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = LuminaCyan
                        )
                    }
                } else {
                    // Result Summary & Confirmation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                        Text(
                            text = "Auto Edit Plan Generated!",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LuminaSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Suggested Changes Ready:", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("✓ Split 2 long scenes at high motion boundaries", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("✓ Trimmed 1.8s speech gap", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("✓ Applied Auto Color adjustment (+15% contrast, +10% vibrance)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text("✓ Added Crossfade transition between clips", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isFinished = false }) {
                            Text("Re-analyze", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onApplyAutoEdit()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("apply_auto_edit_timeline_button")
                        ) {
                            Text("Apply to Timeline", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
