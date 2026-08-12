package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CaptionSegment
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.utils.AiServiceHelper
import kotlinx.coroutines.launch

@Composable
fun AutoCaptionsDialog(
    clips: List<TimelineClip>,
    totalDurationMs: Long,
    onApplyCaptions: (List<CaptionSegment>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedLanguage by remember { mutableStateOf("Hindi") }
    var isProcessing by remember { mutableStateOf(false) }
    var progressPct by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf("") }
    var generatedPreviewCaptions by remember { mutableStateOf<List<CaptionSegment>?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("auto_captions_dialog")
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
                        imageVector = Icons.Default.ClosedCaption,
                        contentDescription = null,
                        tint = LuminaCyan
                    )
                    Text(
                        text = "AI Auto Captions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (generatedPreviewCaptions == null && !isProcessing) {
                    Text(
                        text = "Select spoken language in video to generate auto-synchronized subtitles:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    listOf("Hindi", "English", "Hinglish").forEach { lang ->
                        val isSel = selectedLanguage == lang
                        Surface(
                            onClick = { selectedLanguage = lang },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) LuminaCyan.copy(alpha = 0.2f) else LuminaSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) LuminaCyan else LuminaSurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("lang_option_$lang")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSel,
                                    onClick = { selectedLanguage = lang },
                                    colors = RadioButtonDefaults.colors(selectedColor = LuminaCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = lang,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSel) LuminaCyan else Color.White
                                )
                            }
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
                                isProcessing = true
                                scope.launch {
                                    val res = AiServiceHelper.generateAutoCaptions(
                                        context = context,
                                        clips = clips,
                                        totalDurationMs = totalDurationMs,
                                        language = selectedLanguage,
                                        onProgress = { pct, msg ->
                                            progressPct = pct
                                            statusMessage = msg
                                        }
                                    )
                                    isProcessing = false
                                    generatedPreviewCaptions = res
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("generate_captions_submit_button")
                        ) {
                            Text("Generate Captions", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isProcessing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = LuminaCyan)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressPct / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = LuminaCyan
                        )
                    }
                } else {
                    // Preview generated captions
                    Text(
                        text = "Generated Captions Preview (${generatedPreviewCaptions?.size ?: 0} Segments):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = LuminaCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(generatedPreviewCaptions.orEmpty()) { seg ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LuminaSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "\"${seg.text}\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${seg.startTimelineMs / 1000f}s - ${seg.endTimelineMs / 1000f}s (${seg.words.size} words)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
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
                        TextButton(onClick = { generatedPreviewCaptions = null }) {
                            Text("Regenerate", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                generatedPreviewCaptions?.let { onApplyCaptions(it) }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("apply_captions_button")
                        ) {
                            Text("Apply to Timeline", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
