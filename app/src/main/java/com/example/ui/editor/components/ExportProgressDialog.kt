package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import com.example.editor.ExportEngine
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaObsidian
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.ui.theme.LuminaViolet

@Composable
fun ExportProgressDialog(
    exportState: ExportEngine.ExportState,
    onStartExport: (ExportEngine.ExportConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedResolution by remember { mutableStateOf("1080p FHD") }
    var selectedFps by remember { mutableStateOf(30) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LuminaSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                when (exportState) {
                    is ExportEngine.ExportState.Idle -> {
                        Text(
                            text = "Export Video Settings",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Resolution", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))

                        val resOptions = listOf(
                            "720p HD" to Pair(1280, 720),
                            "1080p FHD" to Pair(1920, 1080),
                            "2K QHD" to Pair(2560, 1440),
                            "4K Ultra" to Pair(3840, 2160)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            resOptions.forEach { (resName, _) ->
                                Surface(
                                    onClick = { selectedResolution = resName },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedResolution == resName) LuminaViolet else LuminaSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = resName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Frame Rate", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30, 60).forEach { fps ->
                                Surface(
                                    onClick = { selectedFps = fps },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedFps == fps) LuminaCyan else LuminaSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${fps} FPS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selectedFps == fps) LuminaObsidian else Color.White,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val (w, h) = resOptions.find { it.first == selectedResolution }?.second ?: Pair(1920, 1080)
                                    onStartExport(
                                        ExportEngine.ExportConfig(
                                            resolutionName = selectedResolution,
                                            fps = selectedFps,
                                            targetWidth = w,
                                            targetHeight = h
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("confirm_export_button")
                            ) {
                                Text("Start Export", color = LuminaObsidian, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is ExportEngine.ExportState.Progress -> {
                        Text(
                            text = "Exporting Video...",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = exportState.stageName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LuminaCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { exportState.percentage / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = LuminaCyan,
                            trackColor = LuminaSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${exportState.percentage}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.LightGray,
                            modifier = Modifier.align(Alignment.End)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cancel Export", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    is ExportEngine.ExportState.Success -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = LuminaCyan,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Export Completed!",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Saved to Gallery Movies / LuminaStudio folder.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Done", color = LuminaObsidian, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is ExportEngine.ExportState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Export Failed",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = exportState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
