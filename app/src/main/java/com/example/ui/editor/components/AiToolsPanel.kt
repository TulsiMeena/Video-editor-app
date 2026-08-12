package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimelineClip
import com.example.data.repository.AiPreferencesRepository
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.utils.AiServiceHelper
import com.example.utils.SmartHighlightSuggestion
import kotlinx.coroutines.launch

data class AiToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val requiresApi: Boolean = false
)

@Composable
fun AiToolsPanel(
    selectedClip: TimelineClip?,
    clips: List<TimelineClip>,
    totalDurationMs: Long,
    onOpenAiAutoEdit: () -> Unit = {},
    onOpenAutoCaptions: () -> Unit,
    onOpenBgRemoval: () -> Unit,
    onOpenObjectRemoval: () -> Unit = {},
    onOpenAiEnhance: () -> Unit,
    onOpenNoiseReduction: () -> Unit,
    onOpenSilenceRemoval: () -> Unit,
    onOpenSmartCrop: () -> Unit,
    onOpenHighlightDetection: () -> Unit = {},
    onOpenSceneDetection: () -> Unit,
    onOpenAiReframe: () -> Unit,
    onOpenBeatSync: () -> Unit = {},
    onOpenAutoColor: () -> Unit = {},
    onOpenThumbnailGenerator: () -> Unit = {},
    onOpenTitleAssist: () -> Unit = {},
    onOpenAiSettings: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val aiRepo = remember { AiPreferencesRepository(context) }
    val scope = rememberCoroutineScope()

    val isApiConfigured = remember { aiRepo.isApiConfigured() }
    var highlightSuggestions by remember { mutableStateOf<List<SmartHighlightSuggestion>>(emptyList()) }

    LaunchedEffect(clips, totalDurationMs) {
        scope.launch {
            highlightSuggestions = AiServiceHelper.detectSmartHighlights(clips, totalDurationMs)
        }
    }

    val tools = listOf(
        AiToolItem("auto_edit", "AI Auto Edit", "Smart Timeline Cuts", Icons.Default.AutoAwesome),
        AiToolItem("captions", "Auto Captions", "Hindi, English, Hinglish", Icons.Default.ClosedCaption),
        AiToolItem("bg_removal", "Remove Background", "Matting & Replacement", Icons.Default.Layers),
        AiToolItem("object_removal", "Object Removal", "Inpaint & Track", Icons.Default.LayersClear),
        AiToolItem("enhance", "AI Enhance", "Upscale, Denoise & Detail", Icons.Default.HighQuality),
        AiToolItem("noise", "Noise Reduction", "Low, Med, High Audio Clean", Icons.Default.GraphicEq),
        AiToolItem("silence", "Remove Silence", "Auto Detect Speech Pauses", Icons.Default.VolumeOff),
        AiToolItem("smart_crop", "Smart Crop", "16:9, 9:16, 1:1 Aspect", Icons.Default.Crop),
        AiToolItem("highlights", "Highlight Detect", "Find Key Action Clips", Icons.Default.Star),
        AiToolItem("scene", "Detect Scenes", "Timeline Cut Detection", Icons.Default.MovieFilter),
        AiToolItem("reframe", "AI Reframe", "Auto Subject Tracking", Icons.Default.CenterFocusStrong),
        AiToolItem("beat_sync", "Beat Sync", "Rhythm & Music Cuts", Icons.Default.MusicNote),
        AiToolItem("auto_color", "AI Auto Color", "Exposure & White Balance", Icons.Default.AutoFixHigh),
        AiToolItem("thumbnail", "AI Thumbnail", "Keyframe & Text Export", Icons.Default.Image),
        AiToolItem("title_assist", "Title & Tags", "Gemini Metadata Assist", Icons.Default.EditNote)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LuminaSurface)
            .padding(12.dp)
            .testTag("ai_tools_panel")
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LuminaCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Tools Hub",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status Badge & Settings Button
                Surface(
                    onClick = onOpenAiSettings,
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                    modifier = Modifier.testTag("ai_settings_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isApiConfigured) Color.Green else Color.Yellow, RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = if (isApiConfigured) "AI Online" else "Local AI Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                        Icon(Icons.Default.Settings, contentDescription = "AI Settings", tint = LuminaCyan, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Smart Highlights Banner (If any suggestion exists)
        if (highlightSuggestions.isNotEmpty()) {
            val sug = highlightSuggestions.first()
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = LuminaCyan.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LuminaCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("smart_highlight_banner")
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = LuminaCyan, modifier = Modifier.size(20.dp))
                        Column {
                            Text(sug.title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text(sug.description, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        }
                    }
                    Button(
                        onClick = {
                            if (sug.actionType == "TRIM_PAUSE") onOpenSilenceRemoval()
                            else onOpenSceneDetection()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                        modifier = Modifier.testTag("apply_highlight_suggestion_button")
                    ) {
                        Text("Apply", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Grid/Carousel of 8 AI Tools
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tools.forEach { tool ->
                Surface(
                    onClick = {
                        when (tool.id) {
                            "auto_edit" -> onOpenAiAutoEdit()
                            "captions" -> onOpenAutoCaptions()
                            "bg_removal" -> onOpenBgRemoval()
                            "object_removal" -> onOpenObjectRemoval()
                            "enhance" -> onOpenAiEnhance()
                            "noise" -> onOpenNoiseReduction()
                            "silence" -> onOpenSilenceRemoval()
                            "smart_crop" -> onOpenSmartCrop()
                            "highlights" -> onOpenHighlightDetection()
                            "scene" -> onOpenSceneDetection()
                            "reframe" -> onOpenAiReframe()
                            "beat_sync" -> onOpenBeatSync()
                            "auto_color" -> onOpenAutoColor()
                            "thumbnail" -> onOpenThumbnailGenerator()
                            "title_assist" -> onOpenTitleAssist()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                    modifier = Modifier
                        .width(130.dp)
                        .testTag("ai_tool_${tool.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(tool.icon, contentDescription = null, tint = LuminaCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(tool.title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text(tool.subtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}
