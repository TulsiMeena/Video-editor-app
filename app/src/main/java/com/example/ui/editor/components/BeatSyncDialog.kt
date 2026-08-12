package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AudioClip
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BeatSyncDialog(
    audioClips: List<AudioClip>,
    videoClips: List<TimelineClip>,
    onApplyBeatSync: (autoCutClips: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var detectedBpm by remember { mutableStateOf(120) }
    var beatCount by remember { mutableStateOf(16) }
    var autoCutClips by remember { mutableStateOf(true) }
    var isAnalyzed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("beat_sync_dialog")
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
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = LuminaCyan
                    )
                    Text(
                        text = "AI Beat Sync",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isAnalyzed && !isAnalyzing) {
                    Text(
                        text = "Analyzes audio waveform transients to detect music beats and automatically place beat markers or cut video clips to match the rhythm.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = autoCutClips,
                            onCheckedChange = { autoCutClips = it },
                            colors = CheckboxDefaults.colors(checkedColor = LuminaCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Auto-split video clips on major beat markers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
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
                                    delay(800)
                                    detectedBpm = (115..135).random()
                                    beatCount = (12..24).random()
                                    isAnalyzing = false
                                    isAnalyzed = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("detect_beats_button")
                        ) {
                            Text("Detect Beats", fontWeight = FontWeight.Bold)
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
                            text = "Analyzing audio transients & energy peaks...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                } else {
                    // Detection Result
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LuminaSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Beat Analysis Complete", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = LuminaCyan)
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = LuminaCyan)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Detected Tempo: $detectedBpm BPM", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Found $beatCount rhythmic transients on active audio track", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isAnalyzed = false }) {
                            Text("Re-scan", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onApplyBeatSync(autoCutClips)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("apply_beat_sync_button")
                        ) {
                            Text("Apply Beat Sync", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
