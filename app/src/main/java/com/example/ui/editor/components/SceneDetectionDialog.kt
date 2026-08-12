package com.example.ui.editor.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.utils.AiServiceHelper
import kotlinx.coroutines.launch

@Composable
fun SceneDetectionDialog(
    clips: List<TimelineClip>,
    onSplitAllScenes: (List<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isAnalyzing by remember { mutableStateOf(true) }
    var detectedCutPoints by remember { mutableStateOf<List<Long>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            val res = AiServiceHelper.detectSceneCutPoints(clips)
            detectedCutPoints = res
            isAnalyzing = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("scene_detection_dialog")
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
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = null,
                        tint = LuminaCyan
                    )
                    Text(
                        text = "Scene Detection",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isAnalyzing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = LuminaCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Analyzing video frames for visual scene changes...", color = Color.LightGray)
                    }
                } else {
                    Text(
                        text = "Detected ${detectedCutPoints.size} scene boundaries on the timeline:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(detectedCutPoints.mapIndexed { idx, point -> Pair(idx + 1, point) }) { (idx, timeMs) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LuminaSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Scene $idx Cut Point",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "@ ${timeMs / 1000f}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LuminaCyan
                                    )
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
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSplitAllScenes(detectedCutPoints)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("split_all_scenes_button")
                        ) {
                            Text("Split All Scenes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
