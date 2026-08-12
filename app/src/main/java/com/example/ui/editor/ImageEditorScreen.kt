package com.example.ui.editor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.editor.ExportEngine
import com.example.ui.editor.components.CollagePanel
import com.example.ui.editor.components.ColorAdjustmentsPanel
import com.example.ui.editor.components.DrawPanel
import com.example.ui.editor.components.EditorTabButton
import com.example.ui.editor.components.FiltersPanel
import com.example.ui.editor.components.ImageCanvasView
import com.example.ui.editor.components.ImageLayersPanel
import com.example.ui.editor.components.PhotoFramePanel
import com.example.ui.editor.components.ResizeCanvasPanel
import com.example.ui.editor.components.ShapesPanel
import com.example.ui.editor.components.StickersPanel
import com.example.ui.editor.components.TextEditorPanel
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaObsidian
import com.example.ui.theme.LuminaSurface
import com.example.ui.theme.LuminaSurfaceBorder
import com.example.ui.theme.LuminaSurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    projectId: String,
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    val project by viewModel.project.collectAsStateWithLifecycle()
    val clips by viewModel.clips.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val isMediaUnavailable by viewModel.isMediaUnavailable.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val isShowingBefore by viewModel.isShowingBefore.collectAsStateWithLifecycle()

    // Prompt 7 Image State Flows
    val stickers by viewModel.stickers.collectAsStateWithLifecycle()
    val drawingPaths by viewModel.drawingPaths.collectAsStateWithLifecycle()
    val shapes by viewModel.shapes.collectAsStateWithLifecycle()
    val frameSettings by viewModel.frameSettings.collectAsStateWithLifecycle()
    val straightenSettings by viewModel.straightenSettings.collectAsStateWithLifecycle()
    val blurSettings by viewModel.blurSettings.collectAsStateWithLifecycle()
    val collageSettings by viewModel.collageSettings.collectAsStateWithLifecycle()
    val selectedStickerId by viewModel.selectedStickerId.collectAsStateWithLifecycle()
    val selectedShapeId by viewModel.selectedShapeId.collectAsStateWithLifecycle()
    val imageFitMode by viewModel.imageFitMode.collectAsStateWithLifecycle()
    val cropBounds by viewModel.cropBounds.collectAsStateWithLifecycle()
    val textLayers by viewModel.textLayers.collectAsStateWithLifecycle()
    val selectedTextLayerId by viewModel.selectedTextLayerId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    var isEditingTitle by remember { mutableStateOf(false) }
    var titleInputValue by remember { mutableStateOf("") }
    var showGridOverlay by remember { mutableStateOf(false) }
    var showExportModal by remember { mutableStateOf(false) }
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()

    val primaryClip = clips.firstOrNull()

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
                            text = project?.name ?: "Photo Project",
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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Undo Button
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) Color.White else Color.DarkGray
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
                            tint = if (canRedo) Color.White else Color.DarkGray
                        )
                    }

                    // Before / After Press & Hold
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isShowingBefore) LuminaCyan else LuminaSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuminaSurfaceBorder),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .testTag("before_after_button")
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        viewModel.setShowingBefore(true)
                                        tryAwaitRelease()
                                        viewModel.setShowingBefore(false)
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = "BEFORE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isShowingBefore) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    // Grid Overlay Toggle
                    IconButton(
                        onClick = { showGridOverlay = !showGridOverlay },
                        modifier = Modifier.testTag("grid_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Grid Overlay",
                            tint = if (showGridOverlay) LuminaCyan else Color.Gray
                        )
                    }

                    // Save / Export Button
                    Surface(
                        onClick = { showExportModal = true },
                        shape = RoundedCornerShape(20.dp),
                        color = LuminaCyan,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_image_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
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
                            text = "Selected image file is missing or unreadable.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // 1. CENTER INTERACTIVE IMAGE PREVIEW CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                ImageCanvasView(
                    clip = primaryClip,
                    isShowingBefore = isShowingBefore,
                    showGridOverlay = showGridOverlay,
                    activeTab = activeTab,
                    cropBounds = cropBounds,
                    straightenAngle = straightenSettings.angleDegrees,
                    frameSettings = frameSettings,
                    blurRadius = blurSettings.blurRadius,
                    stickers = stickers,
                    drawingPaths = drawingPaths,
                    shapes = shapes,
                    textLayers = textLayers,
                    fitMode = imageFitMode,
                    onUpdateSticker = { viewModel.updateSticker(it) },
                    onUpdateShape = { viewModel.updateShape(it) },
                    onUpdateTextLayer = { viewModel.updateTextLayer(it) },
                    onAddDrawingPoint = { pos, col, size, type -> },
                    onCropBoundsChanged = { l, t, r, b -> viewModel.setCropBounds(l, t, r, b) }
                )
            }

            // 2. MODULAR EDITOR TOOL DRAWER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(LuminaSurfaceElevated)
            ) {
                when (activeTab) {
                    "CROP" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            Text("Crop Ratio Presets", style = MaterialTheme.typography.labelSmall, color = LuminaCyan)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("ORIGINAL", "1:1", "4:5", "9:16", "16:9", "3:4", "FREE").forEach { ratio ->
                                    val isSelected = (primaryClip?.cropPreset ?: "ORIGINAL") == ratio
                                    Surface(
                                        onClick = {
                                            primaryClip?.let { viewModel.setCropPreset(it.id, ratio) }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) LuminaCyan else LuminaSurface,
                                        modifier = Modifier.testTag("crop_preset_$ratio")
                                    ) {
                                        Text(
                                            text = ratio,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.Black else Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            TextButton(onClick = { viewModel.resetCrop() }) {
                                Text("Reset Crop", color = LuminaCyan, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    "ROTATE" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = { primaryClip?.let { viewModel.rotateClip(it.id) } },
                                    shape = RoundedCornerShape(10.dp),
                                    color = LuminaSurfaceBorder
                                ) {
                                    Text("Rotate 90°", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(8.dp))
                                }

                                Surface(
                                    onClick = { primaryClip?.let { viewModel.flipHorizontal(it.id) } },
                                    shape = RoundedCornerShape(10.dp),
                                    color = LuminaSurfaceBorder
                                ) {
                                    Text("Flip H", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(8.dp))
                                }

                                Surface(
                                    onClick = { primaryClip?.let { viewModel.flipVertical(it.id) } },
                                    shape = RoundedCornerShape(10.dp),
                                    color = LuminaSurfaceBorder
                                ) {
                                    Text("Flip V", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(8.dp))
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Straighten (${straightenSettings.angleDegrees.toInt()}°)", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = straightenSettings.angleDegrees,
                                    onValueChange = { viewModel.setStraightenAngle(it) },
                                    valueRange = -45f..45f,
                                    colors = SliderDefaults.colors(thumbColor = LuminaCyan, activeTrackColor = LuminaCyan),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    "ADJUST" -> {
                        primaryClip?.let { clip ->
                            ColorAdjustmentsPanel(
                                adjustments = clip.colorAdjustments,
                                onAdjustmentsChange = { newAdj ->
                                    viewModel.updateColorAdjustmentsForSelectedClip(newAdj)
                                },
                                onResetAll = { viewModel.resetColorAdjustmentsForSelectedClip() }
                            )
                        }
                    }

                    "FILTERS" -> {
                        primaryClip?.let { clip ->
                            FiltersPanel(
                                selectedFilterName = clip.filterName,
                                filterIntensity = clip.filterIntensity,
                                onSelectFilter = { preset ->
                                    viewModel.setFilterForSelectedClip(preset)
                                },
                                onIntensityChange = { intensity ->
                                    viewModel.setFilterIntensityForSelectedClip(intensity)
                                }
                            )
                        }
                    }

                    "TEXT" -> {
                        val activeTextLayer = textLayers.find { it.id == selectedTextLayerId } ?: textLayers.firstOrNull()
                        if (activeTextLayer != null) {
                            TextEditorPanel(
                                textLayer = activeTextLayer,
                                onUpdateLayer = { updated -> viewModel.updateTextLayer(updated) },
                                onDismiss = { viewModel.selectTextLayer(null) }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Surface(
                                    onClick = { viewModel.addTextLayer("Your Text Here") },
                                    shape = RoundedCornerShape(12.dp),
                                    color = LuminaCyan
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add Text Layer", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                                    }
                                }
                            }
                        }
                    }

                    "STICKERS" -> {
                        StickersPanel(
                            stickers = stickers,
                            selectedStickerId = selectedStickerId,
                            onAddSticker = { cat, sym -> viewModel.addSticker(cat, sym) },
                            onUpdateSticker = { viewModel.updateSticker(it) },
                            onDeleteSticker = { viewModel.deleteSticker(it) },
                            onDuplicateSticker = { viewModel.duplicateSticker(it) }
                        )
                    }

                    "DRAW" -> {
                        DrawPanel(onClearDrawings = { viewModel.clearDrawings() })
                    }

                    "SHAPES" -> {
                        ShapesPanel(
                            shapes = shapes,
                            selectedShapeId = selectedShapeId,
                            onAddShape = { viewModel.addShape(it) },
                            onUpdateShape = { viewModel.updateShape(it) },
                            onDeleteShape = { viewModel.deleteShape(it) }
                        )
                    }

                    "BG_AI" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Surface(
                                    onClick = { viewModel.removeBackgroundForActiveClip() },
                                    shape = RoundedCornerShape(12.dp),
                                    color = LuminaCyan
                                ) {
                                    Text("AI Remove BG", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                                }

                                Surface(
                                    onClick = { viewModel.applyAutoEnhance() },
                                    shape = RoundedCornerShape(12.dp),
                                    color = com.example.ui.theme.LuminaViolet
                                ) {
                                    Text("Auto-Enhance", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                                }
                            }
                        }
                    }

                    "LAYERS" -> {
                        ImageLayersPanel(
                            layers = emptyList(),
                            onToggleVisibility = { },
                            onDeleteLayer = { }
                        )
                    }

                    "CANVAS" -> {
                        ResizeCanvasPanel(
                            currentRatio = aspectRatio,
                            fitMode = imageFitMode,
                            onSetRatio = { viewModel.setAspectRatio(it) },
                            onSetFitMode = { viewModel.setFitMode(it) }
                        )
                    }

                    "FRAME" -> {
                        PhotoFramePanel(
                            frameSettings = frameSettings,
                            onUpdateFrame = { w, col, r, st -> viewModel.setPhotoFrame(w, col, r, st) }
                        )
                    }

                    "COLLAGE" -> {
                        CollagePanel(
                            collageSettings = collageSettings,
                            onSetCollagePreset = { viewModel.setCollagePreset(it) }
                        )
                    }

                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select an editing tool below", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    }
                }
            }

            // 3. BOTTOM SCROLLABLE TOOL TABS BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LuminaSurface)
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))

                EditorTabButton(
                    label = "Crop",
                    icon = Icons.Default.Crop,
                    isSelected = activeTab == "CROP",
                    onClick = { viewModel.setActiveTab("CROP") },
                    tag = "tab_crop"
                )

                EditorTabButton(
                    label = "Rotate & Flip",
                    icon = Icons.Default.RotateRight,
                    isSelected = activeTab == "ROTATE",
                    onClick = { viewModel.setActiveTab("ROTATE") },
                    tag = "tab_rotate"
                )

                EditorTabButton(
                    label = "Adjust",
                    icon = Icons.Default.Tune,
                    isSelected = activeTab == "ADJUST",
                    onClick = { viewModel.setActiveTab("ADJUST") },
                    tag = "tab_adjust"
                )

                EditorTabButton(
                    label = "Filters",
                    icon = Icons.Default.Filter,
                    isSelected = activeTab == "FILTERS",
                    onClick = { viewModel.setActiveTab("FILTERS") },
                    tag = "tab_filters"
                )

                EditorTabButton(
                    label = "Text",
                    icon = Icons.Default.TextFields,
                    isSelected = activeTab == "TEXT",
                    onClick = { viewModel.setActiveTab("TEXT") },
                    tag = "tab_text"
                )

                EditorTabButton(
                    label = "Stickers",
                    icon = Icons.Default.EmojiEmotions,
                    isSelected = activeTab == "STICKERS",
                    onClick = { viewModel.setActiveTab("STICKERS") },
                    tag = "tab_stickers"
                )

                EditorTabButton(
                    label = "Draw",
                    icon = Icons.Default.Brush,
                    isSelected = activeTab == "DRAW",
                    onClick = { viewModel.setActiveTab("DRAW") },
                    tag = "tab_draw"
                )

                EditorTabButton(
                    label = "Shapes",
                    icon = Icons.Default.Category,
                    isSelected = activeTab == "SHAPES",
                    onClick = { viewModel.setActiveTab("SHAPES") },
                    tag = "tab_shapes"
                )

                EditorTabButton(
                    label = "AI & BG",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = activeTab == "BG_AI",
                    onClick = { viewModel.setActiveTab("BG_AI") },
                    tag = "tab_bg_ai"
                )

                EditorTabButton(
                    label = "Layers",
                    icon = Icons.Default.Layers,
                    isSelected = activeTab == "LAYERS",
                    onClick = { viewModel.setActiveTab("LAYERS") },
                    tag = "tab_layers"
                )

                EditorTabButton(
                    label = "Canvas & Scale",
                    icon = Icons.Default.AspectRatio,
                    isSelected = activeTab == "CANVAS",
                    onClick = { viewModel.setActiveTab("CANVAS") },
                    tag = "tab_canvas"
                )

                EditorTabButton(
                    label = "Frame",
                    icon = Icons.Default.BorderOuter,
                    isSelected = activeTab == "FRAME",
                    onClick = { viewModel.setActiveTab("FRAME") },
                    tag = "tab_frame"
                )

                EditorTabButton(
                    label = "Collage",
                    icon = Icons.Default.GridView,
                    isSelected = activeTab == "COLLAGE",
                    onClick = { viewModel.setActiveTab("COLLAGE") },
                    tag = "tab_collage"
                )

                Spacer(modifier = Modifier.width(8.dp))
            }
        }
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
                        focusedBorderColor = LuminaCyan,
                        unfocusedBorderColor = LuminaSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_image_title_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateProjectTitle(titleInputValue)
                        isEditingTitle = false
                    }
                ) {
                    Text("Save", color = LuminaCyan)
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
            projectTitle = project?.name ?: "Photo Project",
            projectType = "IMAGE",
            clips = clips,
            audioClips = emptyList(),
            textLayers = textLayers,
            captions = emptyList(),
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
}
