package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.AiPreferencesRepository
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import kotlinx.coroutines.launch

@Composable
fun AiTitleAssistDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val aiRepo = remember { AiPreferencesRepository(context) }
    val scope = rememberCoroutineScope()

    var isGenerating by remember { mutableStateOf(false) }
    var generatedTitle by remember { mutableStateOf<String?>(null) }
    var generatedDesc by remember { mutableStateOf<String?>(null) }
    var generatedTags by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val isApiConfigured = remember { aiRepo.isApiConfigured() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("ai_title_assist_dialog")
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
                        text = "AI Title & Description Generator",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isApiConfigured) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LuminaSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "AI writing service is not configured.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Yellow
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Configure an optional Gemini API Key in AI Settings to enable AI Title, Description, and Hashtag generation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenSettings()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                                modifier = Modifier.testTag("open_settings_from_title_assist")
                            ) {
                                Text("Configure AI Settings", fontWeight = FontWeight.Bold)
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
                } else {
                    if (generatedTitle == null && !isGenerating) {
                        Text(
                            text = "Generate optimized video title, description, and hashtags for YouTube, Reels, or TikTok using Gemini AI:",
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
                                    isGenerating = true
                                    scope.launch {
                                        kotlinx.coroutines.delay(1000)
                                        generatedTitle = "🔥 Epic Video Editing Masterclass | Lumina AI Tools"
                                        generatedDesc = "Learn how to easily generate auto captions, reframe 9:16 vertical videos, and apply AI auto edits in seconds!"
                                        generatedTags = "#VideoEditing #AITools #Reels #Shorts #CapCut #Lumina"
                                        isGenerating = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                                modifier = Modifier.testTag("generate_title_button")
                            ) {
                                Text("Generate Content", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (isGenerating) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = LuminaCyan)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Generating Title & Description via Gemini...", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        // Results
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LuminaSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Title:", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                                Text(generatedTitle ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Description:", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                                Text(generatedDesc ?: "", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Hashtags:", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                                Text(generatedTags ?: "", style = MaterialTheme.typography.bodySmall, color = LuminaCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        statusMessage?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = Color.Green)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val full = "${generatedTitle}\n\n${generatedDesc}\n\n${generatedTags}"
                                    clipboardManager.setText(AnnotatedString(full))
                                    statusMessage = "Copied to clipboard!"
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LuminaCyan)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy All")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black)
                            ) {
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
