package com.example.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.editor.ExportEngine
import com.example.editor.VideoRenderer
import com.example.editor.audio.AudioMixerPlayer
import com.example.ui.editor.components.AudioClipToolbar
import com.example.ui.editor.components.ClipToolbar
import com.example.ui.editor.components.ExportProgressDialog
import com.example.ui.editor.components.MusicLibraryDialog
import com.example.ui.editor.components.PreviewOverlayCanvas
import com.example.ui.editor.components.TextEditorPanel
import com.example.ui.editor.components.TimelineView
import com.example.ui.editor.components.VoiceOverModal
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaObsidian
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated
import com.example.ui.theme.LuminaViolet
import com.example.utils.MediaUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    projectId: String,
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    val project by viewModel.project.collectAsStateWithLifecycle()
    val clips by viewModel.clips.collectAsStateWithLifecycle()
    val audioClips by viewModel.audioClips.collectAsStateWithLifecycle()
    val textLayers by viewModel.textLayers.collectAsStateWithLifecycle()
    val captions by viewModel.captions.collectAsStateWithLifecycle()

    val selectedClipId by viewModel.selectedClipId.collectAsStateWithLifecycle()
    val selectedAudioClipId by viewModel.selectedAudioClipId.collectAsStateWithLifecycle()
    val selectedTextLayerId by viewModel.selectedTextLayerId.collectAsStateWithLifecycle()
    val selectedCaptionId by viewModel.selectedCaptionId.collectAsStateWithLifecycle()

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentTimeMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val safeAreaRatio by viewModel.safeAreaRatio.collectAsStateWithLifecycle()
    val timelineZoom by viewModel.timelineZoom.collectAsStateWithLifecycle()
    val isAudioDuckingEnabled by viewModel.isAudioDuckingEnabled.collectAsStateWithLifecycle()

    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val isMediaUnavailable by viewModel.isMediaUnavailable.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val isProcessingBg by viewModel.isProcessingBackgroundAction.collectAsStateWithLifecycle()
    val bgTitle by viewModel.backgroundActionTitle.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Audio Mixer Player for synchronized preview audio
    val audioMixer = remember { AudioMixerPlayer(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioMixer.releaseAll()
        }
    }

    LaunchedEffect(audioClips, currentTimeMs, isPlaying, isAudioDuckingEnabled) {
        audioMixer.setDuckingEnabled(isAudioDuckingEnabled)
        audioMixer.updateAudioClips(audioClips, currentTimeMs, isPlaying)
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Playback loop simulation for playhead
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(50)
                val nextTime = currentTimeMs + 50
                if (nextTime >= durationMs) {
                    viewModel.updateCurrentTime(0L)
                    viewModel.setPlaying(false)
                } else {
                    viewModel.updateCurrentTime(nextTime)
                }
            }
        }
    }

    val isShowingBefore by viewModel.isShowingBefore.collectAsStateWithLifecycle()
    val copiedColorAdjustments by viewModel.copiedColorAdjustments.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

    // Title rename state
    var isEditingTitle by remember { mutableStateOf(false) }
    var titleInputValue by remember { mutableStateOf("") }

    // Modals & Panels State
    var showExportModal by remember { mutableStateOf(false) }
    var showMusicLibraryDialog by remember { mutableStateOf(false) }
    var showVoiceOverModal by remember { mutableStateOf(false) }
    var showTextEditorPanel by remember { mutableStateOf(false) }
    var transitionTargetClipId by remember { mutableStateOf<String?>(null) }

    // AI Tools Modals & Panels State
    var showAiSettingsDialog by remember { mutableStateOf(false) }
    var showPrivacyNoticeFor by remember { mutableStateOf<String?>(null) }
    var showAutoCaptionsDialog by remember { mutableStateOf(false) }
    var showBgRemovalPanel by remember { mutableStateOf(false) }
    var showSilenceRemovalDialog by remember { mutableStateOf(false) }
    var showSceneDetectionDialog by remember { mutableStateOf(false) }
    var showAiAutoEditDialog by remember { mutableStateOf(false) }
    var showObjectRemovalDialog by remember { mutableStateOf(false) }
    var showHighlightDetectionDialog by remember { mutableStateOf(false) }
    var showBeatSyncDialog by remember { mutableStateOf(false) }
    var showAutoColorDialog by remember { mutableStateOf(false) }
    var showThumbnailGeneratorDialog by remember { mutableStateOf(false) }
    var showTitleAssistDialog by remember { mutableStateOf(false) }

    // Local Media Pickers
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addMediaItems(uris)
        }
    }

    val localAudioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addLocalMusicUris(uris)
        }
    }

    val selectedClip = clips.find { it.id == selectedClipId } ?: clips.firstOrNull()
    val selectedAudioClip = audioClips.find { it.id == selectedAudioClipId }
    val selectedTextLayer = textLayers.find { it.id == selectedTextLayerId }
    val selectedCaption = captions.find { it.id == selectedCaptionId }

    // Calculate current offset within active clip
    val currentClipTriple = remember(clips, currentTimeMs) {
        var acc = 0L
        var foundClip = clips.firstOrNull()
        var clipOffset = 0L
        for (c in clips) {
            val effDur = c.effectiveDurationMs
            if (currentTimeMs in acc..(acc + effDur)) {
                foundClip = c
                clipOffset = currentTimeMs - acc
                break
            }
            acc += effDur
        }
        Triple(foundClip, acc, clipOffset)
    }

    val activeRawClip = currentClipTriple.first ?: selectedClip

    // Apply Before/After compare logic
    val activeClipForRendering = if (isShowingBefore && activeRawClip != null) {
        activeRawClip.copy(
            filterName = "Original",
            filterIntensity = 0f,
            colorAdjustments = com.example.data.model.ColorAdjustments(),
            effects = emptyList()
        )
    } else {
        activeRawClip
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            titleInputValue = project?.name ?: ""
                            isEditingTitle = true
                        }
                    ) {
                        Text(
                            text = project?.name ?: "Zypo Video Editor",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Audio Ducking Toggle
                    IconButton(
                        onClick = { viewModel.toggleAudioDucking() },
                        modifier = Modifier.testTag("ducking_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Ducking",
                            tint = if (isAudioDuckingEnabled) LuminaCyan else Color.DarkGray
                        )
                    }

                    // Undo Button
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) LuminaCyan else Color.DarkGray
                        )
                    }

                    // Redo Button
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) LuminaCyan else Color.DarkGray
                        )
                    }

                    // Save as Template Button
                    IconButton(
                        onClick = {
                            val title = project?.name ?: "Custom Template"
                            viewModel.saveCurrentProjectAsTemplate(
                                templateName = title,
                                category = "Custom",
                                description = "Custom timeline template created in studio."
                            )
                        },
                        modifier = Modifier.testTag("save_as_template_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Save as Template",
                            tint = LuminaCyan
                        )
                    }

                    // Export Button
                    Surface(
                        onClick = { showExportModal = true },
                        shape = RoundedCornerShape(20.dp),
                        color = LuminaViolet,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("export_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Export",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LuminaObsidian)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LuminaObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Media Warning if unreadable
            if (isMediaUnavailable) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Original media file is missing or unreadable.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // 1. CENTER VIDEO PREVIEW AREA + INTERACTIVE TEXT OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val aspectFloat = when (aspectRatio) {
                    "16:9" -> 16f / 9f
                    "9:16" -> 9f / 16f
                    "1:1" -> 1f
                    "4:5" -> 4f / 5f
                    else -> 16f / 9f
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.95f)
                        .aspectRatio(aspectFloat, matchHeightConstraintsFirst = true)
                ) {
                    // Video Rendering Surface
                    VideoRenderer(
                        clip = activeClipForRendering,
                        isPlaying = isPlaying,
                        clipOffsetMs = currentClipTriple.third,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        performanceMode = "HIGH_QUALITY",
                        captions = captions,
                        currentTimelineMs = currentTimeMs,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Interactive Text Layer Preview Canvas
                    PreviewOverlayCanvas(
                        textLayers = textLayers,
                        currentTimeMs = currentTimeMs,
                        selectedTextLayerId = selectedTextLayerId,
                        safeAreaRatio = safeAreaRatio,
                        onSelectTextLayer = { id ->
                            viewModel.selectTextLayer(id)
                            showTextEditorPanel = true
                        },
                        onUpdateTextTransform = { id, x, y, scale, rot ->
                            viewModel.updateTextTransform(id, x, y, scale, rot)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Before/After Compare Hold Button (Top Right)
                    Surface(
                        onClick = {},
                        shape = RoundedCornerShape(12.dp),
                        color = if (isShowingBefore) LuminaCyan else Color.Black.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuminaCyan),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .testTag("before_after_compare_button")
                    ) {
                        Text(
                            text = if (isShowingBefore) "Showing Original" else "Hold Before/After",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = if (isShowingBefore) Color.Black else Color.White,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable { viewModel.setShowingBefore(!isShowingBefore) }
                        )
                    }

                    // Play/Pause Overlay Icon
                    if (!isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .align(Alignment.Center)
                                .clickable { viewModel.togglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Timecode Overlay (Bottom Left)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${MediaUtils.formatDuration(currentTimeMs)} / ${MediaUtils.formatDuration(durationMs)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Aspect Ratio & Social Safe Area Guide Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LuminaSurface)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aspect:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                listOf("ORIGINAL", "16:9", "9:16", "1:1", "4:5").forEach { ratio ->
                    val isSelected = aspectRatio == ratio
                    Surface(
                        onClick = { viewModel.setAspectRatio(ratio) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) LuminaViolet else LuminaSurfaceElevated,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                        modifier = Modifier.testTag("ratio_$ratio")
                    ) {
                        Text(
                            text = ratio,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text("Safe Area:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                listOf(null to "Off", "9:16" to "Reels", "16:9" to "YouTube", "1:1" to "Square").forEach { (guide, label) ->
                    val isSelected = safeAreaRatio == guide
                    Surface(
                        onClick = { viewModel.setSafeAreaRatio(guide) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) LuminaCyan else LuminaSurfaceElevated,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. MULTI-TRACK TIMELINE COMPONENT
            TimelineView(
                clips = clips,
                audioClips = audioClips,
                textLayers = textLayers,
                selectedClipId = selectedClipId,
                selectedAudioClipId = selectedAudioClipId,
                selectedTextLayerId = selectedTextLayerId,
                currentTimeMs = currentTimeMs,
                totalDurationMs = durationMs,
                zoomScale = timelineZoom,
                onSelectClip = { viewModel.selectClip(it) },
                onSelectAudioClip = { viewModel.selectAudioClip(it) },
                onSelectTextLayer = { id ->
                    viewModel.selectTextLayer(id)
                    showTextEditorPanel = true
                },
                onSeekTime = { viewModel.updateCurrentTime(it) },
                onTrimClip = { id, start, end -> viewModel.trimClip(id, start, end) },
                onZoomChange = { viewModel.setZoomScale(it) },
                onAddMediaClick = { mediaPickerLauncher.launch("*/*") },
                onAddMusicClick = { showMusicLibraryDialog = true },
                onOpenVoiceOverClick = { showVoiceOverModal = true },
                onExtractAudioClick = { viewModel.extractAudioFromSelectedClip() },
                onAddTextClick = {
                    viewModel.addTextLayer("Text Title")
                    showTextEditorPanel = true
                },
                onEditTransitionClick = { clipId ->
                    transitionTargetClipId = clipId
                }
            )

            // Category Tool Tabs Bar (CLIP | AI TOOLS | CAPTIONS | FILTERS | ADJUST | EFFECTS | KEYFRAMES | AUDIO | TEXT)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LuminaSurface)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("CLIP", "AI TOOLS", "CAPTIONS", "FILTERS", "ADJUST", "EFFECTS", "KEYFRAMES", "AUDIO", "TEXT").forEach { tab ->
                    val isTabSelected = activeTab == tab
                    Surface(
                        onClick = { viewModel.setActiveTab(tab) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isTabSelected) LuminaCyan else LuminaSurfaceElevated,
                        border = if (isTabSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                        modifier = Modifier.testTag("tab_$tab")
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isTabSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // 3. EDITING TOOLBAR (DYNAMIC BASED ON ACTIVE TAB)
            when {
                showBgRemovalPanel && selectedClip != null -> {
                    com.example.ui.editor.components.BackgroundRemovalPanel(
                        selectedClip = selectedClip,
                        onUpdateClip = { updated ->
                            viewModel.trimClip(updated.id, updated.trimStartMs, updated.trimEndMs)
                        },
                        onClose = { showBgRemovalPanel = false }
                    )
                }

                activeTab == "AI TOOLS" -> {
                    com.example.ui.editor.components.AiToolsPanel(
                        selectedClip = selectedClip,
                        clips = clips,
                        totalDurationMs = durationMs,
                        onOpenAiAutoEdit = { showAiAutoEditDialog = true },
                        onOpenAutoCaptions = { showPrivacyNoticeFor = "Auto Captions" },
                        onOpenBgRemoval = { showBgRemovalPanel = true },
                        onOpenObjectRemoval = { showObjectRemovalDialog = true },
                        onOpenAiEnhance = { viewModel.applyAutoEnhance() },
                        onOpenNoiseReduction = { showPrivacyNoticeFor = "Audio Noise Reduction" },
                        onOpenSilenceRemoval = { showSilenceRemovalDialog = true },
                        onOpenSmartCrop = { viewModel.applySmartCrop("9:16") },
                        onOpenHighlightDetection = { showHighlightDetectionDialog = true },
                        onOpenSceneDetection = { showSceneDetectionDialog = true },
                        onOpenAiReframe = { viewModel.applyAiReframe("9:16") },
                        onOpenBeatSync = { showBeatSyncDialog = true },
                        onOpenAutoColor = { showAutoColorDialog = true },
                        onOpenThumbnailGenerator = { showThumbnailGeneratorDialog = true },
                        onOpenTitleAssist = { showTitleAssistDialog = true },
                        onOpenAiSettings = { showAiSettingsDialog = true },
                        onClose = { viewModel.setActiveTab("CLIP") }
                    )
                }

                (activeTab == "CAPTIONS" || selectedCaption != null) -> {
                    val activeCap = selectedCaption ?: captions.firstOrNull()
                    if (activeCap != null) {
                        com.example.ui.editor.components.CaptionStylePanel(
                            selectedCaption = activeCap,
                            allCaptions = captions,
                            onUpdateCaption = { viewModel.updateCaption(it) },
                            onDeleteCaption = { viewModel.deleteCaption(it) },
                            onSplitCaption = { viewModel.splitCaption(it) },
                            onMergeNextCaption = { viewModel.mergeNextCaption(it) },
                            onExportSrt = { viewModel.exportSrtSubtitles() },
                            onClose = { viewModel.setActiveTab("CLIP") }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LuminaSurface)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No captions generated yet.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showPrivacyNoticeFor = "Auto Captions" },
                                colors = ButtonDefaults.buttonColors(containerColor = LuminaCyan, contentColor = Color.Black),
                                modifier = Modifier.testTag("generate_captions_tab_button")
                            ) {
                                Text("Generate AI Auto Captions", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                activeTab == "FILTERS" && selectedClip != null -> {
                    com.example.ui.editor.components.FiltersPanel(
                        selectedFilterName = selectedClip.filterName,
                        filterIntensity = selectedClip.filterIntensity,
                        onSelectFilter = { preset -> viewModel.setFilterForSelectedClip(preset) },
                        onIntensityChange = { intensity -> viewModel.setFilterIntensityForSelectedClip(intensity) }
                    )
                }

                activeTab == "ADJUST" && selectedClip != null -> {
                    Column {
                        // Copy / Paste Adjustments Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LuminaSurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.copyAdjustmentsFromSelectedClip() },
                                modifier = Modifier.testTag("copy_adjustments_button")
                            ) {
                                Text("Copy Adjustments", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { viewModel.pasteAdjustmentsToSelectedClip() },
                                enabled = copiedColorAdjustments != null,
                                modifier = Modifier.testTag("paste_adjustments_button")
                            ) {
                                Text("Paste Adjustments", style = MaterialTheme.typography.labelSmall, color = if (copiedColorAdjustments != null) LuminaCyan else Color.Gray)
                            }
                        }

                        com.example.ui.editor.components.ColorAdjustmentsPanel(
                            adjustments = selectedClip.colorAdjustments,
                            onAdjustmentsChange = { adj -> viewModel.updateColorAdjustmentsForSelectedClip(adj) },
                            onResetAll = { viewModel.resetColorAdjustmentsForSelectedClip() }
                        )
                    }
                }

                activeTab == "EFFECTS" && selectedClip != null -> {
                    com.example.ui.editor.components.EffectsPanel(
                        effects = selectedClip.effects,
                        onAddEffect = { type, category -> viewModel.addEffectToSelectedClip(type, category) },
                        onUpdateEffect = { fx -> viewModel.updateEffectForSelectedClip(fx) },
                        onRemoveEffect = { fxId -> viewModel.removeEffectFromSelectedClip(fxId) }
                    )
                }

                activeTab == "KEYFRAMES" && selectedClip != null -> {
                    com.example.ui.editor.components.TransformKeyframePanel(
                        playheadOffsetMs = currentClipTriple.third,
                        keyframes = selectedClip.keyframes,
                        baseTransform = selectedClip.transform,
                        onAddKeyframe = { kf -> viewModel.addKeyframeToSelectedClip(kf) },
                        onDeleteKeyframe = { kfId -> viewModel.deleteKeyframeFromSelectedClip(kfId) },
                        onUpdateTransform = { trans -> viewModel.updateTransformForSelectedClip(trans) },
                        onResetTransform = { type -> viewModel.resetTransformForSelectedClip(type) }
                    )
                }

                showTextEditorPanel && selectedTextLayer != null -> {
                    TextEditorPanel(
                        textLayer = selectedTextLayer,
                        onUpdateLayer = { viewModel.updateTextLayer(it) },
                        onDismiss = { showTextEditorPanel = false }
                    )
                }

                selectedAudioClip != null || activeTab == "AUDIO" -> {
                    val audioToEdit = selectedAudioClip ?: audioClips.firstOrNull()
                    if (audioToEdit != null) {
                        AudioClipToolbar(
                            selectedAudioClip = audioToEdit,
                            onSplitAtPlayhead = { viewModel.splitSelectedAudioClipAtPlayhead() },
                            onDeleteAudioClip = { viewModel.deleteSelectedAudioClip() },
                            onDuplicateAudioClip = { viewModel.duplicateSelectedAudioClip() },
                            onMoveAudioClipLeft = { viewModel.shiftSelectedAudioClip(-1000L) },
                            onMoveAudioClipRight = { viewModel.shiftSelectedAudioClip(1000L) },
                            onSetAudioVolume = { vol -> viewModel.setAudioVolume(vol) },
                            onToggleMute = { viewModel.toggleAudioMute() },
                            onSetFadeIn = { fadeIn -> viewModel.setAudioFade(fadeIn, audioToEdit.fadeOutMs) },
                            onSetFadeOut = { fadeOut -> viewModel.setAudioFade(audioToEdit.fadeInMs, fadeOut) },
                            onSetAudioSpeed = { speed -> viewModel.setAudioSpeed(speed) }
                        )
                    } else {
                        ClipToolbar(
                            selectedClip = selectedClip,
                            onSplitAtPlayhead = { viewModel.splitSelectedClipAtPlayhead() },
                            onDeleteClip = { viewModel.deleteSelectedClip() },
                            onDuplicateClip = { viewModel.duplicateSelectedClip() },
                            onMoveLeft = { viewModel.moveClipLeft() },
                            onMoveRight = { viewModel.moveClipRight() },
                            onSetSpeed = { speed -> selectedClip?.let { viewModel.setClipSpeed(it.id, speed) } },
                            onReverseClip = { viewModel.reverseSelectedClip() },
                            onFreezeFrame = { viewModel.createFreezeFrameAtPlayhead() },
                            onRotateClip = { selectedClip?.let { viewModel.rotateClip(it.id) } },
                            onFlipHorizontal = { selectedClip?.let { viewModel.flipHorizontal(it.id) } },
                            onFlipVertical = { selectedClip?.let { viewModel.flipVertical(it.id) } },
                            onSetFitFill = { mode -> selectedClip?.let { viewModel.setFitFillMode(it.id, mode) } },
                            onSetCropPreset = { preset -> selectedClip?.let { viewModel.setCropPreset(it.id, preset) } },
                            onSetVolume = { vol -> selectedClip?.let { viewModel.setClipVolume(it.id, vol) } },
                            onToggleMute = { selectedClip?.let { viewModel.toggleClipMute(it.id) } },
                            onTrimClip = { start, end -> selectedClip?.let { viewModel.trimClip(it.id, start, end) } }
                        )
                    }
                }

                else -> {
                    ClipToolbar(
                        selectedClip = selectedClip,
                        onSplitAtPlayhead = { viewModel.splitSelectedClipAtPlayhead() },
                        onDeleteClip = { viewModel.deleteSelectedClip() },
                        onDuplicateClip = { viewModel.duplicateSelectedClip() },
                        onMoveLeft = { viewModel.moveClipLeft() },
                        onMoveRight = { viewModel.moveClipRight() },
                        onSetSpeed = { speed -> selectedClip?.let { viewModel.setClipSpeed(it.id, speed) } },
                        onReverseClip = { viewModel.reverseSelectedClip() },
                        onFreezeFrame = { viewModel.createFreezeFrameAtPlayhead() },
                        onRotateClip = { selectedClip?.let { viewModel.rotateClip(it.id) } },
                        onFlipHorizontal = { selectedClip?.let { viewModel.flipHorizontal(it.id) } },
                        onFlipVertical = { selectedClip?.let { viewModel.flipVertical(it.id) } },
                        onSetFitFill = { mode -> selectedClip?.let { viewModel.setFitFillMode(it.id, mode) } },
                        onSetCropPreset = { preset -> selectedClip?.let { viewModel.setCropPreset(it.id, preset) } },
                        onSetVolume = { vol -> selectedClip?.let { viewModel.setClipVolume(it.id, vol) } },
                        onToggleMute = { selectedClip?.let { viewModel.toggleClipMute(it.id) } },
                        onTrimClip = { start, end -> selectedClip?.let { viewModel.trimClip(it.id, start, end) } }
                    )
                }
            }
        }
    }

    // Music Library Dialog
    if (showMusicLibraryDialog) {
        MusicLibraryDialog(
            onSelectTrack = { track ->
                viewModel.addAudioClipFromMusic(track.title, track.category, track.assetOrUriPath, track.durationMs)
                showMusicLibraryDialog = false
            },
            onDismiss = { showMusicLibraryDialog = false }
        )
    }

    // Voice-Over Recording Modal
    if (showVoiceOverModal) {
        VoiceOverModal(
            onKeepVoiceOver = { file, durationMs ->
                viewModel.addVoiceOverClip(file, durationMs)
            },
            onDismiss = { showVoiceOverModal = false }
        )
    }

    // Transitions Dialog
    transitionTargetClipId?.let { clipId ->
        val targetClip = clips.find { it.id == clipId }
        com.example.ui.editor.components.TransitionsDialog(
            currentTransition = targetClip?.transitionToNext,
            maxDurationMs = targetClip?.effectiveDurationMs ?: 5000L,
            onSaveTransition = { transition ->
                viewModel.updateClipTransition(clipId, transition)
                transitionTargetClipId = null
            },
            onDismiss = { transitionTargetClipId = null }
        )
    }

    // Rename Dialog
    if (isEditingTitle) {
        AlertDialog(
            onDismissRequest = { isEditingTitle = false },
            title = { Text("Rename Project", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = titleInputValue,
                    onValueChange = { titleInputValue = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminaViolet,
                        unfocusedBorderColor = LuminaSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_title_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateProjectTitle(titleInputValue)
                        isEditingTitle = false
                    }
                ) {
                    Text("Save", color = LuminaViolet)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingTitle = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = LuminaSurfaceElevated
        )
    }

    // Export Dialog
    if (showExportModal || exportState !is ExportEngine.ExportState.Idle) {
        com.example.ui.editor.components.ExportDialog(
            projectTitle = project?.name ?: "Zypo Video Editor",
            projectType = project?.projectType ?: "VIDEO",
            clips = clips,
            audioClips = audioClips,
            textLayers = textLayers,
            captions = captions,
            currentAspectRatio = aspectRatio,
            exportState = exportState,
            onStartExport = { config -> viewModel.startExport(config) },
            onCancelExport = { viewModel.cancelExport() },
            onDismiss = {
                showExportModal = false
                viewModel.resetExportState()
            },
            onRecordHistory = { record ->
                viewModel.recordExportInHistory(record)
            }
        )
    }

    // Background Processing Dialog
    if (isProcessingBg) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = LuminaSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = LuminaCyan, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = bgTitle, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }
        }
    }

    // AI Settings Dialog
    if (showAiSettingsDialog) {
        com.example.ui.editor.components.AiSettingsDialog(
            onDismiss = { showAiSettingsDialog = false }
        )
    }

    // AI Privacy Warning Notice
    showPrivacyNoticeFor?.let { featureName ->
        com.example.ui.editor.components.PrivacyWarningDialog(
            featureTitle = featureName,
            onConfirmContinue = {
                showPrivacyNoticeFor = null
                if (featureName == "Auto Captions") {
                    showAutoCaptionsDialog = true
                }
            },
            onCancel = { showPrivacyNoticeFor = null }
        )
    }

    // Auto Captions Dialog
    if (showAutoCaptionsDialog) {
        com.example.ui.editor.components.AutoCaptionsDialog(
            clips = clips,
            totalDurationMs = durationMs,
            onApplyCaptions = { generated ->
                viewModel.setCaptions(generated)
                viewModel.setActiveTab("CAPTIONS")
                showAutoCaptionsDialog = false
            },
            onDismiss = { showAutoCaptionsDialog = false }
        )
    }

    // Silence Removal Dialog
    if (showSilenceRemovalDialog) {
        com.example.ui.editor.components.SilenceRemovalDialog(
            clips = clips,
            totalDurationMs = durationMs,
            onApplySilenceCuts = { gaps ->
                viewModel.applySilenceCuts(gaps)
                showSilenceRemovalDialog = false
            },
            onDismiss = { showSilenceRemovalDialog = false }
        )
    }

    // Scene Detection Dialog
    if (showSceneDetectionDialog) {
        com.example.ui.editor.components.SceneDetectionDialog(
            clips = clips,
            onSplitAllScenes = { cutPoints ->
                viewModel.applySceneSplits(cutPoints)
                showSceneDetectionDialog = false
            },
            onDismiss = { showSceneDetectionDialog = false }
        )
    }

    // AI Auto Edit Dialog
    if (showAiAutoEditDialog) {
        com.example.ui.editor.components.AiAutoEditDialog(
            clips = clips,
            totalDurationMs = durationMs,
            onApplyAutoEdit = {
                viewModel.applyAutoEnhance()
                viewModel.applySmartCrop("9:16")
            },
            onDismiss = { showAiAutoEditDialog = false }
        )
    }

    // AI Object Removal Dialog
    if (showObjectRemovalDialog) {
        com.example.ui.editor.components.AiObjectRemovalDialog(
            onDismiss = { showObjectRemovalDialog = false }
        )
    }

    // Highlight Detection Dialog
    if (showHighlightDetectionDialog) {
        com.example.ui.editor.components.HighlightDetectionDialog(
            clips = clips,
            totalDurationMs = durationMs,
            onSelectHighlight = { startMs, endMs ->
                selectedClip?.let { viewModel.trimClip(it.id, startMs, endMs) }
            },
            onDismiss = { showHighlightDetectionDialog = false }
        )
    }

    // Beat Sync Dialog
    if (showBeatSyncDialog) {
        com.example.ui.editor.components.BeatSyncDialog(
            audioClips = audioClips,
            videoClips = clips,
            onApplyBeatSync = { autoCut ->
                if (autoCut) {
                    viewModel.applySceneSplits(listOf(durationMs / 2))
                }
            },
            onDismiss = { showBeatSyncDialog = false }
        )
    }

    // Auto Color Dialog
    if (showAutoColorDialog) {
        com.example.ui.editor.components.AiColorAssistDialog(
            selectedClip = selectedClip,
            onApplyAutoColor = { viewModel.applyAutoEnhance() },
            onDismiss = { showAutoColorDialog = false }
        )
    }

    // Thumbnail Generator Dialog
    if (showThumbnailGeneratorDialog) {
        com.example.ui.editor.components.ThumbnailGeneratorDialog(
            clips = clips,
            totalDurationMs = durationMs,
            onThumbnailExported = { path -> },
            onDismiss = { showThumbnailGeneratorDialog = false }
        )
    }

    // AI Title Assist Dialog
    if (showTitleAssistDialog) {
        com.example.ui.editor.components.AiTitleAssistDialog(
            onOpenSettings = { showAiSettingsDialog = true },
            onDismiss = { showTitleAssistDialog = false }
        )
    }
}
