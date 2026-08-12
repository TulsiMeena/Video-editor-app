package com.example.ui.editor.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AudioClip
import com.example.data.model.CaptionSegment
import com.example.data.model.ExportRecordEntity
import com.example.data.model.TextLayer
import com.example.data.model.TimelineClip
import com.example.editor.ExportEngine
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaObsidian
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.ui.theme.LuminaTextMuted
import com.example.ui.theme.LuminaViolet
import com.example.utils.MediaUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SocialPreset(
    val title: String,
    val subtitle: String,
    val aspectRatio: String,
    val resolution: String,
    val fps: Int,
    val quality: String
)

val SOCIAL_PRESETS = listOf(
    SocialPreset("Instagram Reel", "Vertical 9:16 High", "9:16", "1080p", 30, "High"),
    SocialPreset("YouTube Short", "60 FPS Smooth", "9:16", "1080p", 60, "High"),
    SocialPreset("YouTube Video", "16:9 Widescreen", "16:9", "1080p", 30, "Maximum"),
    SocialPreset("Instagram Post", "Square 1:1 Feed", "1:1", "1080p", 30, "Standard"),
    SocialPreset("Instagram Story", "Full Screen 9:16", "9:16", "1080p", 30, "High")
)

@Composable
fun ExportDialog(
    projectTitle: String,
    projectType: String, // "VIDEO" or "IMAGE"
    clips: List<TimelineClip>,
    audioClips: List<AudioClip> = emptyList(),
    textLayers: List<TextLayer> = emptyList(),
    captions: List<CaptionSegment> = emptyList(),
    currentAspectRatio: String,
    exportState: ExportEngine.ExportState,
    onStartExport: (ExportEngine.ExportConfig) -> Unit,
    onCancelExport: () -> Unit,
    onDismiss: () -> Unit,
    onRecordHistory: (ExportRecordEntity) -> Unit
) {
    val context = LocalContext.current
    val totalDurationMs = clips.sumOf { it.effectiveDurationMs }
    val deviceCaps = remember { ExportEngine.checkDeviceCapabilities() }
    val smartRecommend = remember { ExportEngine.recommendConfig(clips, currentAspectRatio) }

    // State Variables
    var activeTab by remember { mutableStateOf("RECOMMENDED") } // "RECOMMENDED", "SOCIAL_PRESETS", "CUSTOM"
    var selectedResolution by remember { mutableStateOf(smartRecommend.resolutionName) }
    var selectedFps by remember { mutableIntStateOf(smartRecommend.fps) }
    var selectedQuality by remember { mutableStateOf(smartRecommend.qualityName) }
    var selectedAspectRatio by remember { mutableStateOf(currentAspectRatio) }
    var selectedAudioQuality by remember { mutableStateOf(smartRecommend.audioQualityName) }
    var customBitrateMbps by remember { mutableFloatStateOf(10f) }
    var isSilentExport by remember { mutableStateOf(false) }

    // Image Export Specific State
    var imageFormat by remember { mutableStateOf("JPG") } // "JPG", "PNG", "WEBP"
    var imageQuality by remember { mutableFloatStateOf(90f) }

    val defaultDateTitle = remember {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
        "VideoEditor_${sdf.format(Date())}"
    }
    var exportFileName by remember { mutableStateOf(projectTitle.ifBlank { defaultDateTitle }) }

    val currentDimensions = remember(selectedResolution, selectedAspectRatio) {
        ExportEngine.getDimensionsForPreset(selectedResolution, selectedAspectRatio)
    }

    val currentConfig = remember(
        selectedResolution, selectedFps, selectedQuality, selectedAspectRatio,
        selectedAudioQuality, customBitrateMbps, exportFileName, projectType, imageFormat, imageQuality
    ) {
        val calculatedBitrate = if (selectedQuality == "Custom") {
            (customBitrateMbps * 1_000_000).toInt()
        } else {
            when (selectedQuality) {
                "Low" -> 4_000_000
                "Standard" -> 7_000_000
                "High" -> 12_000_000
                "Maximum" -> 25_000_000
                else -> 10_000_000
            }
        }

        ExportEngine.ExportConfig(
            resolutionName = selectedResolution,
            fps = selectedFps,
            targetWidth = currentDimensions.first,
            targetHeight = currentDimensions.second,
            bitrateBps = calculatedBitrate,
            qualityName = selectedQuality,
            aspectRatio = selectedAspectRatio,
            audioQualityName = if (isSilentExport) "Silent" else selectedAudioQuality,
            customFileName = exportFileName,
            mediaType = projectType,
            imageFormat = imageFormat,
            imageQuality = imageQuality.toInt()
        )
    }

    val estimatedSize = remember(currentConfig, totalDurationMs) {
        ExportEngine.calculateEstimatedFileSize(totalDurationMs, currentConfig)
    }

    // Handle completed state
    LaunchedEffect(exportState) {
        if (exportState is ExportEngine.ExportState.Success) {
            onRecordHistory(exportState.record)
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (exportState !is ExportEngine.ExportState.Progress) {
                onDismiss()
            }
        },
        containerColor = LuminaSurfaceElevated,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = LuminaViolet,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (projectType == "IMAGE") "Export Image" else "Export Video",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                if (exportState !is ExportEngine.ExportState.Progress) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_export_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
            ) {
                when (exportState) {
                    is ExportEngine.ExportState.Progress -> {
                        ExportProgressView(
                            progress = exportState,
                            onCancel = onCancelExport
                        )
                    }
                    is ExportEngine.ExportState.Success -> {
                        ExportSuccessView(
                            success = exportState,
                            onDone = onDismiss
                        )
                    }
                    else -> {
                        // Configuration Form
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // 1. Thumbnail Preview & Project Details Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(LuminaSurface)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LuminaObsidian),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val thumbPath = clips.firstOrNull()?.thumbnailPath
                                    if (!thumbPath.isNullOrEmpty() && File(thumbPath).exists()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(File(thumbPath))
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.VideoLibrary,
                                            contentDescription = null,
                                            tint = LuminaTextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = projectTitle,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Duration: ${MediaUtils.formatDuration(totalDurationMs)} • ${currentDimensions.first}x${currentDimensions.second}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Est. Size: ~${Formatter.formatShortFileSize(context, estimatedSize)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = LuminaCyan
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. Export Preset Category Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LuminaSurface)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("RECOMMENDED", "PRESETS", "CUSTOM").forEach { tabKey ->
                                    val isSelected = activeTab == tabKey
                                    Surface(
                                        onClick = {
                                            activeTab = tabKey
                                            if (tabKey == "RECOMMENDED") {
                                                selectedResolution = smartRecommend.resolutionName
                                                selectedFps = smartRecommend.fps
                                                selectedQuality = smartRecommend.qualityName
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .testTag("export_tab_$tabKey"),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) LuminaViolet else Color.Transparent
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = when (tabKey) {
                                                    "RECOMMENDED" -> "Smart Rec."
                                                    "PRESETS" -> "Social Presets"
                                                    else -> "Custom"
                                                },
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.sp
                                                ),
                                                color = if (isSelected) Color.White else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (activeTab == "RECOMMENDED") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = LuminaEmerald.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminaEmerald.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = null,
                                            tint = LuminaEmerald,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Recommended for your project:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "${smartRecommend.resolutionName} • ${smartRecommend.fps} FPS • ${smartRecommend.qualityName} Quality",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Analyzed source media and device hardware capability.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (activeTab == "PRESETS") {
                                Text(
                                    text = "Select Social Media Platform",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(SOCIAL_PRESETS) { preset ->
                                        val isSelected = selectedResolution == preset.resolution &&
                                                selectedFps == preset.fps &&
                                                selectedAspectRatio == preset.aspectRatio

                                        Card(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .clickable {
                                                    selectedResolution = preset.resolution
                                                    selectedFps = preset.fps
                                                    selectedQuality = preset.quality
                                                    selectedAspectRatio = preset.aspectRatio
                                                }
                                                .testTag("preset_card_${preset.title.replace(" ", "_")}"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) LuminaViolet.copy(alpha = 0.3f) else LuminaSurface
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) LuminaViolet else LuminaSurfaceBorder
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = preset.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = Color.White,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = preset.subtitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Gray,
                                                    fontSize = 10.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "${preset.resolution} • ${preset.fps}fps",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = LuminaCyan,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // 3. Aspect Ratio Presets Bar
                            Text(
                                text = "Aspect Ratio",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("9:16", "16:9", "1:1", "4:5", "3:4").forEach { ratio ->
                                    val isSelected = selectedAspectRatio == ratio
                                    Surface(
                                        onClick = { selectedAspectRatio = ratio },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) LuminaCyan.copy(alpha = 0.25f) else LuminaSurface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) LuminaCyan else LuminaSurfaceBorder
                                        )
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = ratio,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) LuminaCyan else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 4. Resolution Selection
                            Text(
                                text = "Resolution",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("480p", "720p", "1080p", "2K", "4K").forEach { res ->
                                    val isSupported = res != "4K" || deviceCaps.is4kSupported
                                    val isSelected = selectedResolution == res

                                    Surface(
                                        onClick = {
                                            if (isSupported) selectedResolution = res
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            !isSupported -> Color.DarkGray.copy(alpha = 0.3f)
                                            isSelected -> LuminaViolet
                                            else -> LuminaSurface
                                        },
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) LuminaViolet else LuminaSurfaceBorder
                                        )
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = res,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (!isSupported) Color.Gray else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                            if (!deviceCaps.is4kSupported) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "4K export isn't supported on this device.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 5. FPS Selection
                            Text(
                                text = "Frame Rate (FPS)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(24, 25, 30, 50, 60).forEach { fpsVal ->
                                    val isSelected = selectedFps == fpsVal
                                    Surface(
                                        onClick = { selectedFps = fpsVal },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) LuminaViolet else LuminaSurface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) LuminaViolet else LuminaSurfaceBorder
                                        )
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = "$fpsVal",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 6. Quality & Custom Bitrate Slider
                            Text(
                                text = "Quality / Bitrate",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Low", "Standard", "High", "Maximum", "Custom").forEach { q ->
                                    val isSelected = selectedQuality == q
                                    Surface(
                                        onClick = { selectedQuality = q },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) LuminaViolet else LuminaSurface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) LuminaViolet else LuminaSurfaceBorder
                                        )
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = q,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.sp
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            if (selectedQuality == "Custom") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bitrate: ${customBitrateMbps.toInt()} Mbps", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                                }
                                Slider(
                                    value = customBitrateMbps,
                                    onValueChange = { customBitrateMbps = it },
                                    valueRange = 2f..50f,
                                    colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 7. Audio Quality & Silent Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Silent Export (Mute All)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Switch(
                                    checked = isSilentExport,
                                    onCheckedChange = { isSilentExport = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = LuminaCyan,
                                        checkedTrackColor = LuminaCyan.copy(alpha = 0.4f)
                                    )
                                )
                            }

                            if (!isSilentExport) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Low", "Standard", "High").forEach { audioQ ->
                                        val isSelected = selectedAudioQuality == audioQ
                                        Surface(
                                            onClick = { selectedAudioQuality = audioQ },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelected) LuminaCyan.copy(alpha = 0.25f) else LuminaSurface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) LuminaCyan else LuminaSurfaceBorder
                                            )
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text(
                                                    text = "Audio $audioQ",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = if (isSelected) LuminaCyan else Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 8. Output File Name Field
                            Text(
                                text = "File Name",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = exportFileName,
                                onValueChange = { exportFileName = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuminaViolet,
                                    unfocusedBorderColor = LuminaSurfaceBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_filename_input")
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (exportState !is ExportEngine.ExportState.Progress && exportState !is ExportEngine.ExportState.Success) {
                Button(
                    onClick = { onStartExport(currentConfig) },
                    colors = ButtonDefaults.buttonColors(containerColor = LuminaViolet),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Export (${currentConfig.resolutionName})", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun ExportProgressView(
    progress: ExportEngine.ExportState.Progress,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = { progress.percentage / 100f },
            modifier = Modifier.size(80.dp),
            color = LuminaViolet,
            strokeWidth = 6.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = progress.stageName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${progress.percentage}% Completed",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = LuminaCyan
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { progress.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = LuminaViolet,
            trackColor = LuminaSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.testTag("cancel_export_button")
        ) {
            Text("Cancel Export", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExportSuccessView(
    success: ExportEngine.ExportState.Success,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val record = success.record

    val contentUri: Uri = remember(record) {
        try {
            if (!record.fileUri.isNullOrEmpty() && record.fileUri.startsWith("content://")) {
                Uri.parse(record.fileUri)
            } else {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    File(record.filePath)
                )
            }
        } catch (e: Exception) {
            Uri.fromFile(File(record.filePath))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(LuminaEmerald.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = LuminaEmerald,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "✓ Saved to Gallery",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = LuminaEmerald
            )
        )

        Text(
            text = if (record.mediaType == "IMAGE") "Saved to Pictures/ZoyaVideoEditor" else "Saved to Movies/ZoyaVideoEditor",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = LuminaSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = record.fileName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Resolution: ${record.resolution}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
                if (record.durationMs > 0) {
                    Text(
                        text = "Duration: ${MediaUtils.formatDuration(record.durationMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
                Text(
                    text = "Size: ${Formatter.formatShortFileSize(context, record.fileSizeBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuminaCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Share
            Button(
                onClick = {
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = if (record.mediaType == "IMAGE") "image/*" else "video/*"
                            putExtra(Intent.EXTRA_STREAM, contentUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Exported ${if (record.mediaType == "IMAGE") "Image" else "Video"}"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuminaViolet),
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_share_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share", fontSize = 12.sp)
            }

            // Open Video
            Button(
                onClick = {
                    try {
                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(contentUri, if (record.mediaType == "IMAGE") "image/*" else "video/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(viewIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No app available to open media", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuminaSurfaceElevated),
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_open_button")
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (record.mediaType == "IMAGE") "Open Image" else "Open Video", fontSize = 11.sp, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_edit_again_button"),
                border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder)
            ) {
                Text("Edit Again", color = Color.White)
            }

            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = LuminaEmerald),
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_done_button")
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}
