package com.example.ui.editor.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaObsidian
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.ui.theme.LuminaViolet
import com.example.utils.MediaUtils
import com.example.utils.VoiceRecorderHelper
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun VoiceOverModal(
    onKeepVoiceOver: (File, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val voiceRecorder = remember { VoiceRecorderHelper(context) }

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (!granted) {
            showPermissionDeniedDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDeniedDialog = false
                onDismiss()
            },
            title = { Text("Microphone Permission Required", color = Color.White) },
            text = { Text("Microphone permission is required for voice-over recording.", color = Color.LightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        showPermissionDeniedDialog = false
                        onDismiss()
                    }
                ) {
                    Text("Open Settings", color = LuminaCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDeniedDialog = false
                    onDismiss()
                }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = LuminaSurfaceElevated
        )
        return
    }

    if (!permissionGranted) return

    // Recording Flow States: "COUNTDOWN", "RECORDING", "PREVIEW"
    var recordState by remember { mutableStateOf("COUNTDOWN") }
    var countdownValue by remember { mutableIntStateOf(3) }
    var recordedTimeMs by remember { mutableLongStateOf(0L) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                previewPlayer?.stop()
                previewPlayer?.release()
            } catch (_: Exception) {}
            voiceRecorder.cancelRecording()
        }
    }

    // Countdown Loop
    LaunchedEffect(recordState) {
        if (recordState == "COUNTDOWN") {
            countdownValue = 3
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            // Start recording after countdown
            voiceRecorder.startRecording(
                onSuccess = { file ->
                    recordedFile = file
                    recordState = "RECORDING"
                },
                onError = {
                    recordState = "PREVIEW"
                }
            )
        } else if (recordState == "RECORDING") {
            recordedTimeMs = 0L
            while (recordState == "RECORDING") {
                delay(100)
                recordedTimeMs += 100
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LuminaObsidian,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = LuminaCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice-Over Recording",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (recordState) {
                    "COUNTDOWN" -> {
                        Text("Get Ready!", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "$countdownValue",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 64.sp
                            ),
                            color = LuminaCyan
                        )
                    }

                    "RECORDING" -> {
                        Text(
                            text = MediaUtils.formatDuration(recordedTimeMs),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Dynamic Waveform Bars
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(40.dp)
                        ) {
                            repeat(12) { idx ->
                                val barHeight = (12 + (idx * 5 + (recordedTimeMs / 100).toInt() * 7) % 28).dp
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(LuminaCyan)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Stop Recording Button
                        IconButton(
                            onClick = {
                                val file = voiceRecorder.stopRecording()
                                recordedFile = file
                                recordState = "PREVIEW"
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                                .testTag("stop_recording_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    "PREVIEW" -> {
                        Text(
                            text = "Recorded: ${MediaUtils.formatDuration(recordedTimeMs)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Retake
                            Surface(
                                onClick = {
                                    recordedFile?.delete()
                                    recordState = "COUNTDOWN"
                                },
                                shape = CircleShape,
                                color = LuminaSurfaceElevated,
                                modifier = Modifier.testTag("retake_voice_button")
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retake", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }

                            // Preview Play/Pause
                            IconButton(
                                onClick = {
                                    val file = recordedFile
                                    if (file != null) {
                                        if (isPreviewPlaying) {
                                            try { previewPlayer?.pause() } catch (_: Exception) {}
                                            isPreviewPlaying = false
                                        } else {
                                            try {
                                                val player = MediaPlayer()
                                                player.setDataSource(file.absolutePath)
                                                player.prepare()
                                                player.start()
                                                player.setOnCompletionListener { isPreviewPlaying = false }
                                                previewPlayer = player
                                                isPreviewPlaying = true
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(LuminaViolet)
                                    .testTag("preview_voice_button")
                            ) {
                                Icon(
                                    imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White
                                )
                            }

                            // Keep
                            Surface(
                                onClick = {
                                    val file = recordedFile
                                    if (file != null && file.exists()) {
                                        onKeepVoiceOver(file, recordedTimeMs)
                                    }
                                    onDismiss()
                                },
                                shape = CircleShape,
                                color = LuminaCyan,
                                modifier = Modifier.testTag("keep_voice_button")
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = "Keep", tint = Color.Black)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Keep", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
