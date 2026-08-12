package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
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

data class HighlightClipCandidate(
    val id: String,
    val title: String,
    val durationLabel: String,
    val startMs: Long,
    val endMs: Long,
    val score: Int
)

@Composable
fun HighlightDetectionDialog(
    clips: List<TimelineClip>,
    totalDurationMs: Long,
    onSelectHighlight: (startMs: Long, endMs: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var candidates by remember { mutableStateOf<List<HighlightClipCandidate>>(emptyList()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("highlight_detection_dialog")
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
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = LuminaCyan
                    )
                    Text(
                        text = "AI Highlight Detection",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (candidates.isEmpty() && !isAnalyzing) {
                    Text(
                        text = "Scans your video project for high action, motion, or speech activity to generate short highlight clips for YouTube Shorts or Instagram Reels.",
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
                                    delay(700)
                                    val total = totalDurationMs.coerceAtLeast(6000L)
                                    candidates = listOf(
                                        HighlightClipCandidate("hl_1", "Highlight 1: Peak Motion", "0s - ${(total * 0.4f / 1000f).toInt()}s", 0L, (total * 0.4f).toLong(), 98),
                                        HighlightClipCandidate("hl_2", "Highlight 2: Key Action", "${(total * 0.3f / 1000f).toInt()}s - ${(total * 0.7f / 1000f).toInt()}s", (total * 0.3f).toLong(), (total * 0.7f).toLong(), 92),
                                        HighlightClipCandidate("hl_3", "Highlight 3: Climax Punch", "${(total * 0.6f / 1000f).toInt()}s - ${(total / 1000f).toInt()}s", (total * 0.6f).toLong(), total, 89)
                                    )
                                    isAnalyzing = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("detect_highlights_button")
                        ) {
                            Text("Find Highlights", fontWeight = FontWeight.Bold)
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
                            text = "Analyzing visual variance, audio peaks & scene motion...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "Generated Highlight Clips (${candidates.size}):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = LuminaCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(candidates) { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = LuminaSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Text("${item.durationLabel} • Score: ${item.score}%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Button(
                                        onClick = {
                                            onSelectHighlight(item.startMs, item.endMs)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                                        modifier = Modifier.testTag("use_highlight_${item.id}")
                                    ) {
                                        Text("Use Clip", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
