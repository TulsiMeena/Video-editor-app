package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AudioClip
import com.example.data.model.TextLayer
import com.example.data.model.TimelineClip
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaObsidian
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.ui.theme.LuminaViolet
import com.example.utils.AudioUtils
import com.example.utils.MediaUtils
import java.io.File

@Composable
fun TimelineView(
    clips: List<TimelineClip>,
    audioClips: List<AudioClip> = emptyList(),
    textLayers: List<TextLayer> = emptyList(),
    selectedClipId: String?,
    selectedAudioClipId: String? = null,
    selectedTextLayerId: String? = null,
    currentTimeMs: Long,
    totalDurationMs: Long,
    zoomScale: Float,
    onSelectClip: (String) -> Unit,
    onSelectAudioClip: (String) -> Unit = {},
    onSelectTextLayer: (String) -> Unit = {},
    onSeekTime: (Long) -> Unit,
    onTrimClip: (String, Long, Long) -> Unit,
    onZoomChange: (Float) -> Unit,
    onAddMediaClick: () -> Unit,
    onAddMusicClick: () -> Unit = {},
    onOpenVoiceOverClick: () -> Unit = {},
    onExtractAudioClick: () -> Unit = {},
    onAddTextClick: () -> Unit = {},
    onEditTransitionClick: (String) -> Unit = {}, // Selected clip ID for transition
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val pixelsPerSec = 30 * zoomScale
    val totalWidthDp = ((totalDurationMs / 1000f) * pixelsPerSec).coerceAtLeast(300f).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuminaSurfaceElevated)
            .padding(vertical = 4.dp)
    ) {
        // Timeline Header: Action Badges & Zoom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Quick Action Buttons: + Music, Voice-over, Extract Audio, + Text
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                // + Music
                Surface(
                    onClick = onAddMusicClick,
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaViolet,
                    modifier = Modifier.testTag("add_music_track_button")
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Music", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }

                // 🎤 Voice-over
                Surface(
                    onClick = onOpenVoiceOverClick,
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaCyan,
                    modifier = Modifier.testTag("open_voiceover_button")
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Voice-over", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                    }
                }

                // Extract Audio
                Surface(
                    onClick = onExtractAudioClick,
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                    modifier = Modifier.testTag("extract_audio_button")
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = LuminaCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Extract Audio", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White)
                    }
                }

                // + Text
                Surface(
                    onClick = onAddTextClick,
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminaCyan),
                    modifier = Modifier.testTag("add_text_track_button")
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Subtitles, contentDescription = null, tint = LuminaCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Text", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = LuminaCyan)
                    }
                }
            }

            // Zoom Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onZoomChange((zoomScale - 0.25f).coerceAtLeast(0.5f)) },
                    modifier = Modifier.size(24.dp).testTag("zoom_out_button")
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }

                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                IconButton(
                    onClick = { onZoomChange((zoomScale + 0.25f).coerceAtMost(4.0f)) },
                    modifier = Modifier.size(24.dp).testTag("zoom_in_button")
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Horizontal Scrollable Multi-Track Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .horizontalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .width(totalWidthDp + 120.dp)
                    .padding(horizontal = 16.dp)
            ) {
                // 1. TIME RULER HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .background(LuminaObsidian.copy(alpha = 0.6f))
                ) {
                    val intervalSec = if (zoomScale < 1f) 5 else 2
                    val numMarkers = (totalDurationMs / 1000 / intervalSec).toInt() + 2

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(numMarkers) { idx ->
                            val sec = idx * intervalSec
                            Text(
                                text = String.format("%02d:%02d", sec / 60, sec % 60),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 2. VIDEO TRACK
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(LuminaObsidian)
                        .border(1.dp, LuminaSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    clips.forEachIndexed { index, clip ->
                        val isSelected = clip.id == selectedClipId
                        val clipDurationSec = clip.effectiveDurationMs / 1000f
                        val clipWidth = (clipDurationSec * pixelsPerSec).coerceAtLeast(60f).dp

                        Box(
                            modifier = Modifier
                                .width(clipWidth)
                                .fillMaxHeight()
                                .padding(horizontal = 1.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) LuminaViolet.copy(alpha = 0.3f) else LuminaSurface)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) LuminaCyan else LuminaSurfaceBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectClip(clip.id) }
                                .testTag("timeline_clip_${clip.id}")
                        ) {
                            if (clip.thumbnailPath != null && File(clip.thumbnailPath).exists()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(File(clip.thumbnailPath)).crossfade(true).build(),
                                    contentDescription = clip.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    alpha = 0.5f
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxSize().padding(3.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = clip.name,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    if (clip.speed != 1.0f) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = LuminaViolet) {
                                            Text(text = "${clip.speed}x", style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp), color = Color.White, modifier = Modifier.padding(horizontal = 2.dp))
                                        }
                                    }
                                }

                                // Keyframe Markers Overlay
                                if (clip.keyframes.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(10.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        clip.keyframes.forEach { _ ->
                                            Text("◇", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = LuminaCyan)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = MediaUtils.formatDuration(clip.effectiveDurationMs), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = LuminaCyan)
                                    if (clip.isMuted) {
                                        Icon(Icons.Default.VolumeOff, contentDescription = "Muted", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }

                        // Transition Node Button (✦) between adjacent clips
                        if (index < clips.size - 1) {
                            Surface(
                                onClick = { onEditTransitionClick(clip.id) },
                                shape = CircleShape,
                                color = if (clip.transitionToNext != null && clip.transitionToNext.type != "NONE") LuminaCyan else LuminaSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LuminaCyan),
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(20.dp)
                                    .testTag("transition_marker_${clip.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "✦",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = if (clip.transitionToNext != null && clip.transitionToNext.type != "NONE") Color.Black else LuminaCyan
                                    )
                                }
                            }
                        }
                    }

                    // Add Media Button at end
                    Surface(
                        onClick = onAddMediaClick,
                        shape = RoundedCornerShape(8.dp),
                        color = LuminaSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuminaCyan),
                        modifier = Modifier.padding(start = 6.dp).size(width = 60.dp, height = 56.dp).testTag("add_media_timeline_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Add Media", tint = LuminaCyan, modifier = Modifier.size(16.dp))
                            Text("+ Media", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = LuminaCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. AUDIO TRACK
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LuminaObsidian)
                        .border(1.dp, LuminaSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    if (audioClips.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audio Track (Tap + Music or Voice-over)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color.DarkGray)
                        }
                    } else {
                        audioClips.forEach { audio ->
                            val isSel = audio.id == selectedAudioClipId
                            val offsetDp = ((audio.startTimelineMs / 1000f) * pixelsPerSec).dp
                            val widthDp = ((audio.effectiveDurationMs / 1000f) * pixelsPerSec).coerceAtLeast(40f).dp

                            val waveformPeaks = remember(audio.id) { AudioUtils.generateWaveformPeaks(audio.id, 24) }

                            Box(
                                modifier = Modifier
                                    .offset(x = offsetDp)
                                    .width(widthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) LuminaCyan.copy(alpha = 0.3f) else LuminaViolet.copy(alpha = 0.6f))
                                    .border(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) LuminaCyan else LuminaViolet,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onSelectAudioClip(audio.id) }
                                    .testTag("timeline_audio_${audio.id}")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(2.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${audio.audioType}: ${audio.title}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        Text(text = MediaUtils.formatDuration(audio.effectiveDurationMs), style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp), color = LuminaCyan)
                                    }

                                    // Waveform Visualizer Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        waveformPeaks.forEach { peak ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(peak)
                                                    .clip(RoundedCornerShape(1.dp))
                                                    .background(if (isSel) LuminaCyan else Color.White.copy(alpha = 0.8f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 4. TEXT TRACK
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LuminaObsidian)
                        .border(1.dp, LuminaSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    if (textLayers.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Text Track (Tap + Text to add titles)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color.DarkGray)
                        }
                    } else {
                        textLayers.forEach { layer ->
                            val isSel = layer.id == selectedTextLayerId
                            val offsetDp = ((layer.startTimelineMs / 1000f) * pixelsPerSec).dp
                            val widthDp = ((layer.durationMs / 1000f) * pixelsPerSec).coerceAtLeast(40f).dp

                            Box(
                                modifier = Modifier
                                    .offset(x = offsetDp)
                                    .width(widthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) LuminaViolet else LuminaSurfaceElevated)
                                    .border(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) LuminaCyan else LuminaSurfaceBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onSelectTextLayer(layer.id) }
                                    .testTag("timeline_text_${layer.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Subtitles, contentDescription = null, tint = LuminaCyan, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = layer.text,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Draggable Red/Cyan Playhead
            val playheadProgressRatio = if (totalDurationMs > 0) currentTimeMs.toFloat() / totalDurationMs.toFloat() else 0f
            val playheadOffsetDp = (playheadProgressRatio * (totalWidthDp.value)).coerceIn(0f, totalWidthDp.value).dp

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .offset(x = playheadOffsetDp + 16.dp)
                    .background(LuminaCyan)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            val deltaMs = ((delta / pixelsPerSec) * 1000).toLong()
                            onSeekTime((currentTimeMs + deltaMs).coerceIn(0L, totalDurationMs))
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopCenter)
                        .clip(CircleShape)
                        .background(LuminaCyan)
                )
            }
        }
    }
}
