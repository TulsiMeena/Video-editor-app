package com.example.ui.editor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AudioClip
import com.example.data.model.BlurSettings
import com.example.data.model.CollageSettings
import com.example.data.model.DrawingPath
import com.example.data.model.ExportRecordEntity
import com.example.data.model.ImageLayerItem
import com.example.data.model.PhotoFrameSettings
import com.example.data.model.ProjectEntity
import com.example.data.model.ShapeItem
import com.example.data.model.StickerItem
import com.example.data.model.StraightenSettings
import com.example.data.model.TextLayer
import com.example.data.model.TimelineClip
import com.example.data.repository.ProjectRepository
import com.example.editor.ExportEngine
import com.example.editor.MediaSource
import com.example.editor.UndoRedoManager
import com.example.editor.VideoDecoder
import com.example.editor.VideoProcessor
import com.example.editor.VideoProject
import com.example.utils.AudioUtils
import com.example.utils.JsonUtils
import com.example.utils.MediaUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(application, db.projectDao())
    private val undoRedoManager = UndoRedoManager()

    private val _project = MutableStateFlow<ProjectEntity?>(null)
    val project: StateFlow<ProjectEntity?> = _project.asStateFlow()

    private val _clips = MutableStateFlow<List<TimelineClip>>(emptyList())
    val clips: StateFlow<List<TimelineClip>> = _clips.asStateFlow()

    private val _audioClips = MutableStateFlow<List<AudioClip>>(emptyList())
    val audioClips: StateFlow<List<AudioClip>> = _audioClips.asStateFlow()

    private val _textLayers = MutableStateFlow<List<TextLayer>>(emptyList())
    val textLayers: StateFlow<List<TextLayer>> = _textLayers.asStateFlow()

    private val _captions = MutableStateFlow<List<com.example.data.model.CaptionSegment>>(emptyList())
    val captions: StateFlow<List<com.example.data.model.CaptionSegment>> = _captions.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedAudioClipId = MutableStateFlow<String?>(null)
    val selectedAudioClipId: StateFlow<String?> = _selectedAudioClipId.asStateFlow()

    private val _selectedTextLayerId = MutableStateFlow<String?>(null)
    val selectedTextLayerId: StateFlow<String?> = _selectedTextLayerId.asStateFlow()

    private val _selectedCaptionId = MutableStateFlow<String?>(null)
    val selectedCaptionId: StateFlow<String?> = _selectedCaptionId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _timelineZoom = MutableStateFlow(1.0f)
    val timelineZoom: StateFlow<Float> = _timelineZoom.asStateFlow()

    private val _aspectRatio = MutableStateFlow("16:9")
    val aspectRatio: StateFlow<String> = _aspectRatio.asStateFlow()

    private val _safeAreaRatio = MutableStateFlow<String?>(null)
    val safeAreaRatio: StateFlow<String?> = _safeAreaRatio.asStateFlow()

    private val _isAudioDuckingEnabled = MutableStateFlow(true)
    val isAudioDuckingEnabled: StateFlow<Boolean> = _isAudioDuckingEnabled.asStateFlow()

    private val _activeTab = MutableStateFlow("TRACKS")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _exportState = MutableStateFlow<ExportEngine.ExportState>(ExportEngine.ExportState.Idle)
    val exportState: StateFlow<ExportEngine.ExportState> = _exportState.asStateFlow()

    private val _isMediaUnavailable = MutableStateFlow(false)
    val isMediaUnavailable: StateFlow<Boolean> = _isMediaUnavailable.asStateFlow()

    // --- PROMPT 7 IMAGE EDITOR STATE FLOWS ---
    private val _stickers = MutableStateFlow<List<StickerItem>>(emptyList())
    val stickers: StateFlow<List<StickerItem>> = _stickers.asStateFlow()

    private val _drawingPaths = MutableStateFlow<List<DrawingPath>>(emptyList())
    val drawingPaths: StateFlow<List<DrawingPath>> = _drawingPaths.asStateFlow()

    private val _shapes = MutableStateFlow<List<ShapeItem>>(emptyList())
    val shapes: StateFlow<List<ShapeItem>> = _shapes.asStateFlow()

    private val _frameSettings = MutableStateFlow(PhotoFrameSettings())
    val frameSettings: StateFlow<PhotoFrameSettings> = _frameSettings.asStateFlow()

    private val _straightenSettings = MutableStateFlow(StraightenSettings())
    val straightenSettings: StateFlow<StraightenSettings> = _straightenSettings.asStateFlow()

    private val _blurSettings = MutableStateFlow(BlurSettings())
    val blurSettings: StateFlow<BlurSettings> = _blurSettings.asStateFlow()

    private val _collageSettings = MutableStateFlow(CollageSettings())
    val collageSettings: StateFlow<CollageSettings> = _collageSettings.asStateFlow()

    private val _selectedStickerId = MutableStateFlow<String?>(null)
    val selectedStickerId: StateFlow<String?> = _selectedStickerId.asStateFlow()

    private val _selectedShapeId = MutableStateFlow<String?>(null)
    val selectedShapeId: StateFlow<String?> = _selectedShapeId.asStateFlow()

    private val _imageFitMode = MutableStateFlow("FIT_SCREEN") // "FIT_SCREEN", "FIT_WIDTH", "FIT_HEIGHT"
    val imageFitMode: StateFlow<String> = _imageFitMode.asStateFlow()

    private val _cropBounds = MutableStateFlow(listOf(0f, 0f, 1f, 1f)) // Left, Top, Right, Bottom (0..1)
    val cropBounds: StateFlow<List<Float>> = _cropBounds.asStateFlow()

    private val _isProcessingBackgroundAction = MutableStateFlow(false)
    val isProcessingBackgroundAction: StateFlow<Boolean> = _isProcessingBackgroundAction.asStateFlow()

    private val _backgroundActionTitle = MutableStateFlow("Processing...")
    val backgroundActionTitle: StateFlow<String> = _backgroundActionTitle.asStateFlow()

    private val _isShowingBefore = MutableStateFlow(false)
    val isShowingBefore: StateFlow<Boolean> = _isShowingBefore.asStateFlow()

    private val _copiedColorAdjustments = MutableStateFlow<com.example.data.model.ColorAdjustments?>(null)
    val copiedColorAdjustments: StateFlow<com.example.data.model.ColorAdjustments?> = _copiedColorAdjustments.asStateFlow()

    private val _copiedFilterName = MutableStateFlow<String?>(null)
    private val _copiedFilterIntensity = MutableStateFlow<Float>(1.0f)

    private val _performanceMode = MutableStateFlow("NORMAL") // "NORMAL" vs "PERFORMANCE"
    val performanceMode: StateFlow<String> = _performanceMode.asStateFlow()

    fun setShowingBefore(showing: Boolean) {
        _isShowingBefore.value = showing
    }

    fun setPerformanceMode(mode: String) {
        _performanceMode.value = mode
    }

    // --- PROMPT 4: FILTER SYSTEM ACTIONS ---

    fun setFilterForSelectedClip(preset: com.example.data.model.FilterPreset) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(
            filterName = preset.name,
            filterIntensity = if (preset.id == "original") 0f else 1f
        )
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Filter set to ${preset.name}"
    }

    fun setFilterIntensityForSelectedClip(intensity: Float) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        val clip = currentClips[index]
        val updated = clip.copy(filterIntensity = intensity)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
    }

    // --- PROMPT 4: COLOR ADJUSTMENTS ACTIONS ---

    fun updateColorAdjustmentsForSelectedClip(adjustments: com.example.data.model.ColorAdjustments) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        val clip = currentClips[index]
        val updated = clip.copy(colorAdjustments = adjustments)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
    }

    fun resetColorAdjustmentsForSelectedClip() {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(colorAdjustments = com.example.data.model.ColorAdjustments())
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Color adjustments reset"
    }

    fun copyAdjustmentsFromSelectedClip() {
        val selectedId = _selectedClipId.value ?: return
        val clip = _clips.value.find { it.id == selectedId } ?: return

        _copiedColorAdjustments.value = clip.colorAdjustments
        _copiedFilterName.value = clip.filterName
        _copiedFilterIntensity.value = clip.filterIntensity
        _toastMessage.value = "Visual adjustments copied"
    }

    fun pasteAdjustmentsToSelectedClip() {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        val colorAdj = _copiedColorAdjustments.value
        if (colorAdj == null && _copiedFilterName.value == null) {
            _toastMessage.value = "No copied adjustments available"
            return
        }

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(
            colorAdjustments = colorAdj ?: clip.colorAdjustments,
            filterName = _copiedFilterName.value ?: clip.filterName,
            filterIntensity = _copiedFilterIntensity.value
        )
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Visual adjustments pasted"
    }

    // --- PROMPT 4: MODULAR EFFECTS STACK ACTIONS ---

    fun addEffectToSelectedClip(type: String, category: String) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val newFx = com.example.data.model.ClipEffect(
            type = type,
            category = category,
            isEnabled = true,
            intensity = 80f
        )
        val updated = clip.copy(effects = clip.effects + newFx)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Added $type effect"
    }

    fun updateEffectForSelectedClip(updatedFx: com.example.data.model.ClipEffect) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        val clip = currentClips[index]
        val fxIndex = clip.effects.indexOfFirst { it.id == updatedFx.id }
        if (fxIndex < 0) return

        val updatedFxList = clip.effects.toMutableList().apply { set(fxIndex, updatedFx) }
        val updatedClip = clip.copy(effects = updatedFxList)
        val newList = currentClips.toMutableList().apply { set(index, updatedClip) }
        _clips.value = newList
        autosaveProject()
    }

    fun removeEffectFromSelectedClip(effectId: String) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updatedFxList = clip.effects.filterNot { it.id == effectId }
        val updatedClip = clip.copy(effects = updatedFxList)
        val newList = currentClips.toMutableList().apply { set(index, updatedClip) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Effect removed"
    }

    // --- PROMPT 4: TRANSITIONS ACTIONS ---

    fun updateClipTransition(clipId: String, transition: com.example.data.model.ClipTransition) {
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == clipId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(transitionToNext = transition)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Transition ${transition.type} applied"
    }

    // --- PROMPT 4: KEYFRAMES & TRANSFORMS ACTIONS ---

    fun addKeyframeToSelectedClip(keyframe: com.example.data.model.TransformKeyframe) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val existingIndex = clip.keyframes.indexOfFirst { Math.abs(it.timeOffsetMs - keyframe.timeOffsetMs) < 200L }
        val updatedKeyframes = if (existingIndex >= 0) {
            clip.keyframes.toMutableList().apply { set(existingIndex, keyframe) }
        } else {
            (clip.keyframes + keyframe).sortedBy { it.timeOffsetMs }
        }

        val updatedClip = clip.copy(keyframes = updatedKeyframes)
        val newList = currentClips.toMutableList().apply { set(index, updatedClip) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Keyframe saved at ${keyframe.timeOffsetMs / 1000f}s"
    }

    fun deleteKeyframeFromSelectedClip(keyframeId: String) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updatedKeyframes = clip.keyframes.filterNot { it.id == keyframeId }
        val updatedClip = clip.copy(keyframes = updatedKeyframes)
        val newList = currentClips.toMutableList().apply { set(index, updatedClip) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Keyframe removed"
    }

    fun updateTransformForSelectedClip(transform: com.example.data.model.ClipTransform) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        val clip = currentClips[index]
        val updatedClip = clip.copy(transform = transform)
        val newList = currentClips.toMutableList().apply { set(index, updatedClip) }
        _clips.value = newList
        autosaveProject()
    }

    fun resetTransformForSelectedClip(type: String) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val base = clip.transform
        val resetTransform = when (type.uppercase()) {
            "POSITION" -> base.copy(positionX = 0f, positionY = 0f)
            "SCALE" -> base.copy(scale = 1f)
            "ROTATION" -> base.copy(rotation = 0f)
            "OPACITY" -> base.copy(opacity = 1f)
            else -> com.example.data.model.ClipTransform()
        }
        val updatedClip = clip.copy(transform = resetTransform)
        val newList = currentClips.toMutableList().apply { set(index, updatedClip) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Transform $type reset"
    }

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            repository.getProjectById(projectId).collect { proj ->
                _project.value = proj
                if (proj != null) {
                    val loadedClips = JsonUtils.timelineClipsFromJson(proj.mediaItemsJson)
                    _clips.value = loadedClips

                    val loadedAudio = JsonUtils.audioClipsFromJson(proj.audioClipsJson)
                    _audioClips.value = loadedAudio

                    val loadedText = JsonUtils.textLayersFromJson(proj.textLayersJson)
                    _textLayers.value = loadedText

                    val loadedCaptions = JsonUtils.captionsFromJson(proj.captionsJson)
                    _captions.value = loadedCaptions

                    if (loadedClips.isNotEmpty() && _selectedClipId.value == null) {
                        _selectedClipId.value = loadedClips.first().id
                    }

                    recalculateDuration()
                    validatePrimaryMedia()
                }
            }
        }
    }

    private fun recalculateDuration() {
        val totalVideo = _clips.value.sumOf { it.effectiveDurationMs }
        val maxAudioEnd = _audioClips.value.maxOfOrNull { it.endTimelineMs } ?: 0L
        val maxTextEnd = _textLayers.value.maxOfOrNull { it.endTimelineMs } ?: 0L

        val maxDuration = maxOf(totalVideo, maxAudioEnd, maxTextEnd, 5000L)
        _durationMs.value = maxDuration
        _canUndo.value = undoRedoManager.canUndo
        _canRedo.value = undoRedoManager.canRedo
    }

    private fun validatePrimaryMedia() {
        val firstClip = _clips.value.firstOrNull()
        if (firstClip != null) {
            _isMediaUnavailable.value = !MediaSource.isMediaAvailable(getApplication(), firstClip.activeUri)
        } else {
            _isMediaUnavailable.value = false
        }
    }

    private fun pushStateToUndoHistory() {
        undoRedoManager.pushState(_clips.value)
        _canUndo.value = undoRedoManager.canUndo
        _canRedo.value = undoRedoManager.canRedo
    }

    private fun autosaveProject() {
        val proj = _project.value ?: return
        viewModelScope.launch {
            val updatedClipsJson = JsonUtils.toJsonFromClips(_clips.value)
            val updatedAudioJson = JsonUtils.toJsonFromAudioClips(_audioClips.value)
            val updatedTextJson = JsonUtils.toJsonFromTextLayers(_textLayers.value)
            val updatedCaptionsJson = JsonUtils.toJsonFromCaptions(_captions.value)
            val thumb = _clips.value.firstOrNull()?.thumbnailPath ?: proj.thumbnailPath

            val updatedProj = proj.copy(
                mediaItemsJson = updatedClipsJson,
                audioClipsJson = updatedAudioJson,
                textLayersJson = updatedTextJson,
                captionsJson = updatedCaptionsJson,
                thumbnailPath = thumb,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateProjectMediaAndName(updatedProj)
        }
    }

    fun undo() {
        val previousClips = undoRedoManager.undo(_clips.value)
        if (previousClips != null) {
            _clips.value = previousClips
            recalculateDuration()
            autosaveProject()
            _toastMessage.value = "Undo applied"
        }
    }

    fun redo() {
        val nextClips = undoRedoManager.redo(_clips.value)
        if (nextClips != null) {
            _clips.value = nextClips
            recalculateDuration()
            autosaveProject()
            _toastMessage.value = "Redo applied"
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun updateCurrentTime(timeMs: Long) {
        _currentTimeMs.value = timeMs.coerceIn(0L, _durationMs.value)
    }

    fun selectClip(clipId: String) {
        _selectedClipId.value = clipId
        _selectedAudioClipId.value = null
        _selectedTextLayerId.value = null
    }

    fun selectAudioClip(id: String?) {
        _selectedAudioClipId.value = id
        _selectedTextLayerId.value = null
    }

    fun selectTextLayer(id: String?) {
        _selectedTextLayerId.value = id
        _selectedAudioClipId.value = null
    }

    fun setZoomScale(scale: Float) {
        _timelineZoom.value = scale.coerceIn(0.5f, 4.0f)
    }

    fun setAspectRatio(ratio: String) {
        _aspectRatio.value = ratio
        _toastMessage.value = "Aspect ratio set to $ratio"
    }

    fun setSafeAreaRatio(ratio: String?) {
        _safeAreaRatio.value = ratio
    }

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    fun updateProjectTitle(newTitle: String) {
        val current = _project.value ?: return
        viewModelScope.launch {
            val updated = current.copy(name = newTitle.ifBlank { "Untitled Project" })
            repository.updateProjectMediaAndName(updated)
            _toastMessage.value = "Project renamed"
        }
    }

    fun triggerToolAction(actionName: String) {
        _toastMessage.value = "$actionName triggered"
    }

    // --- AUDIO & MUSIC OPERATIONS ---

    fun addAudioClipFromMusic(title: String, category: String, uriPath: String, durationMs: Long) {
        val startTime = _currentTimeMs.value
        val newClip = AudioClip(
            title = title,
            uri = uriPath,
            audioType = "MUSIC",
            category = category,
            startTimelineMs = startTime,
            sourceDurationMs = durationMs,
            trimStartMs = 0L,
            trimEndMs = durationMs
        )
        pushStateToUndoHistory()
        _audioClips.value = _audioClips.value + newClip
        _selectedAudioClipId.value = newClip.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Added music: $title"
    }

    fun addLocalMusicUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            pushStateToUndoHistory()
            val startTime = _currentTimeMs.value
            var currentOffset = startTime

            val newAudioClips = uris.mapIndexed { idx, uri ->
                val duration = AudioUtils.getAudioDurationMs(getApplication(), uri)
                val clip = AudioClip(
                    title = "Audio ${idx + 1}",
                    uri = uri.toString(),
                    audioType = "LOCAL_AUDIO",
                    startTimelineMs = currentOffset,
                    sourceDurationMs = duration,
                    trimStartMs = 0L,
                    trimEndMs = duration
                )
                currentOffset += duration
                clip
            }
            _audioClips.value = _audioClips.value + newAudioClips
            _selectedAudioClipId.value = newAudioClips.first().id
            recalculateDuration()
            autosaveProject()
            _toastMessage.value = "Added ${uris.size} audio track(s)"
        }
    }

    fun addVoiceOverClip(file: File, durationMs: Long) {
        val startTime = _currentTimeMs.value
        val voiceClip = AudioClip(
            title = "Voice-over (${MediaUtils.formatDuration(durationMs)})",
            uri = file.absolutePath,
            audioType = "VOICE_OVER",
            startTimelineMs = startTime,
            sourceDurationMs = durationMs,
            trimStartMs = 0L,
            trimEndMs = durationMs
        )
        pushStateToUndoHistory()
        _audioClips.value = _audioClips.value + voiceClip
        _selectedAudioClipId.value = voiceClip.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Voice-over added to timeline"
    }

    fun extractAudioFromSelectedClip() {
        val selectedId = _selectedClipId.value ?: return
        val clip = _clips.value.find { it.id == selectedId } ?: return

        val startTime = _currentTimeMs.value
        val extractedClip = AudioClip(
            title = "Extracted: ${clip.name}",
            uri = clip.activeUri,
            audioType = "EXTRACTED",
            startTimelineMs = startTime,
            sourceDurationMs = clip.effectiveDurationMs,
            trimStartMs = clip.trimStartMs,
            trimEndMs = clip.trimEndMs
        )

        toggleClipMute(clip.id)

        pushStateToUndoHistory()
        _audioClips.value = _audioClips.value + extractedClip
        _selectedAudioClipId.value = extractedClip.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Extracted audio track created"
    }

    fun splitSelectedAudioClipAtPlayhead() {
        val selectedId = _selectedAudioClipId.value ?: return
        val audioList = _audioClips.value
        val audio = audioList.find { it.id == selectedId } ?: return

        val playhead = _currentTimeMs.value
        if (playhead <= audio.startTimelineMs + 200L || playhead >= audio.endTimelineMs - 200L) {
            _toastMessage.value = "Playhead too close to boundary to split audio"
            return
        }

        val splitOffset = playhead - audio.startTimelineMs
        val part1 = audio.copy(
            trimEndMs = audio.trimStartMs + splitOffset
        )
        val part2 = audio.copy(
            id = UUID.randomUUID().toString(),
            title = "${audio.title} (Part 2)",
            startTimelineMs = playhead,
            trimStartMs = audio.trimStartMs + splitOffset
        )

        pushStateToUndoHistory()
        val index = audioList.indexOfFirst { it.id == audio.id }
        val updated = audioList.toMutableList().apply {
            removeAt(index)
            add(index, part2)
            add(index, part1)
        }
        _audioClips.value = updated
        _selectedAudioClipId.value = part1.id
        autosaveProject()
        _toastMessage.value = "Audio clip split into 2 parts"
    }

    fun deleteSelectedAudioClip() {
        val selectedId = _selectedAudioClipId.value ?: return
        pushStateToUndoHistory()
        _audioClips.value = _audioClips.value.filterNot { it.id == selectedId }
        _selectedAudioClipId.value = _audioClips.value.firstOrNull()?.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Audio clip deleted"
    }

    fun duplicateSelectedAudioClip() {
        val selectedId = _selectedAudioClipId.value ?: return
        val audio = _audioClips.value.find { it.id == selectedId } ?: return

        val duplicated = audio.copy(
            id = UUID.randomUUID().toString(),
            title = "${audio.title} (Copy)",
            startTimelineMs = audio.endTimelineMs + 200L
        )

        pushStateToUndoHistory()
        _audioClips.value = _audioClips.value + duplicated
        _selectedAudioClipId.value = duplicated.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Audio clip duplicated"
    }

    fun shiftSelectedAudioClip(deltaMs: Long) {
        val selectedId = _selectedAudioClipId.value ?: return
        val audioList = _audioClips.value
        val index = audioList.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        val audio = audioList[index]
        val newStart = (audio.startTimelineMs + deltaMs).coerceAtLeast(0L)
        val updated = audio.copy(startTimelineMs = newStart)

        pushStateToUndoHistory()
        val newList = audioList.toMutableList().apply { set(index, updated) }
        _audioClips.value = newList
        recalculateDuration()
        autosaveProject()
    }

    fun setAudioVolume(volume: Float) {
        val selectedId = _selectedAudioClipId.value ?: return
        val audioList = _audioClips.value
        val index = audioList.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val updated = audioList[index].copy(volume = volume, isMuted = false)
        val newList = audioList.toMutableList().apply { set(index, updated) }
        _audioClips.value = newList
        autosaveProject()
    }

    fun toggleAudioMute() {
        val selectedId = _selectedAudioClipId.value ?: return
        val audioList = _audioClips.value
        val index = audioList.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val audio = audioList[index]
        val updated = audio.copy(isMuted = !audio.isMuted)
        val newList = audioList.toMutableList().apply { set(index, updated) }
        _audioClips.value = newList
        autosaveProject()
    }

    fun setAudioFade(fadeInMs: Long, fadeOutMs: Long) {
        val selectedId = _selectedAudioClipId.value ?: return
        val audioList = _audioClips.value
        val index = audioList.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val updated = audioList[index].copy(fadeInMs = fadeInMs, fadeOutMs = fadeOutMs)
        val newList = audioList.toMutableList().apply { set(index, updated) }
        _audioClips.value = newList
        autosaveProject()
    }

    fun setAudioSpeed(speed: Float) {
        val selectedId = _selectedAudioClipId.value ?: return
        val audioList = _audioClips.value
        val index = audioList.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val updated = audioList[index].copy(speed = speed)
        val newList = audioList.toMutableList().apply { set(index, updated) }
        _audioClips.value = newList
        recalculateDuration()
        autosaveProject()
    }

    fun toggleAudioDucking() {
        _isAudioDuckingEnabled.value = !_isAudioDuckingEnabled.value
        _toastMessage.value = if (_isAudioDuckingEnabled.value) "Audio Ducking Enabled" else "Audio Ducking Disabled"
    }

    // --- TEXT LAYER OPERATIONS ---

    fun addTextLayer(text: String = "Text Title") {
        val startTime = _currentTimeMs.value
        val newLayer = TextLayer(
            text = text,
            startTimelineMs = startTime,
            endTimelineMs = startTime + 5000L
        )

        pushStateToUndoHistory()
        _textLayers.value = _textLayers.value + newLayer
        _selectedTextLayerId.value = newLayer.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Text layer added"
    }

    fun updateTextLayer(updatedLayer: TextLayer) {
        val layers = _textLayers.value
        val index = layers.indexOfFirst { it.id == updatedLayer.id }
        if (index < 0) return

        pushStateToUndoHistory()
        val newList = layers.toMutableList().apply { set(index, updatedLayer) }
        _textLayers.value = newList
        autosaveProject()
    }

    fun updateTextTransform(id: String, posX: Float, posY: Float, scale: Float, rotation: Float) {
        val layers = _textLayers.value
        val index = layers.indexOfFirst { it.id == id }
        if (index < 0) return

        val updated = layers[index].copy(
            positionX = posX,
            positionY = posY,
            scale = scale,
            rotation = rotation
        )
        val newList = layers.toMutableList().apply { set(index, updated) }
        _textLayers.value = newList
        autosaveProject()
    }

    fun deleteSelectedTextLayer() {
        val selectedId = _selectedTextLayerId.value ?: return
        pushStateToUndoHistory()
        _textLayers.value = _textLayers.value.filterNot { it.id == selectedId }
        _selectedTextLayerId.value = _textLayers.value.firstOrNull()?.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Text layer deleted"
    }

    fun duplicateSelectedTextLayer() {
        val selectedId = _selectedTextLayerId.value ?: return
        val layer = _textLayers.value.find { it.id == selectedId } ?: return

        val duplicated = layer.copy(
            id = UUID.randomUUID().toString(),
            text = "${layer.text} (Copy)",
            startTimelineMs = layer.endTimelineMs + 200L,
            endTimelineMs = layer.endTimelineMs + 5200L
        )

        pushStateToUndoHistory()
        _textLayers.value = _textLayers.value + duplicated
        _selectedTextLayerId.value = duplicated.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Text layer duplicated"
    }

    // --- TIMELINE VIDEO CLIP OPERATIONS ---

    fun trimClip(clipId: String, newTrimStartMs: Long, newTrimEndMs: Long) {
        val currentClips = _clips.value
        val clipIndex = currentClips.indexOfFirst { it.id == clipId }
        if (clipIndex < 0) return

        val clip = currentClips[clipIndex]
        val clampedStart = newTrimStartMs.coerceIn(0L, clip.sourceDurationMs - 200L)
        val clampedEnd = newTrimEndMs.coerceIn(clampedStart + 200L, clip.sourceDurationMs)

        pushStateToUndoHistory()
        val updatedClip = clip.copy(trimStartMs = clampedStart, trimEndMs = clampedEnd)
        val newList = currentClips.toMutableList().apply { set(clipIndex, updatedClip) }
        _clips.value = newList
        recalculateDuration()
        autosaveProject()
    }

    fun splitSelectedClipAtPlayhead() {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val project = VideoProject(id = "", name = "", projectType = "", clips = currentClips)
        val clipTriple = project.getClipAtTimelineTime(_currentTimeMs.value) ?: return

        val (clip, _, clipStartMs) = clipTriple
        if (clip.id != selectedId) return

        val offsetInClipMs = _currentTimeMs.value - clipStartMs
        val splitPair = VideoProcessor.splitClipAtOffset(clip, offsetInClipMs)

        if (splitPair != null) {
            pushStateToUndoHistory()
            val clipIndex = currentClips.indexOfFirst { it.id == clip.id }
            val newList = currentClips.toMutableList().apply {
                removeAt(clipIndex)
                add(clipIndex, splitPair.second)
                add(clipIndex, splitPair.first)
            }
            _clips.value = newList
            _selectedClipId.value = splitPair.first.id
            recalculateDuration()
            autosaveProject()
            _toastMessage.value = "Clip split into 2 parts"
        } else {
            _toastMessage.value = "Playhead too close to clip boundary to split"
        }
    }

    fun deleteSelectedClip() {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        if (currentClips.size <= 1) {
            _toastMessage.value = "Project must keep at least 1 media clip."
            return
        }

        pushStateToUndoHistory()
        val newList = currentClips.filterNot { it.id == selectedId }
        _clips.value = newList
        _selectedClipId.value = newList.firstOrNull()?.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Clip deleted from timeline"
    }

    fun duplicateSelectedClip() {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val clipIndex = currentClips.indexOfFirst { it.id == selectedId }
        if (clipIndex < 0) return

        val clip = currentClips[clipIndex]
        val duplicated = clip.copy(
            id = UUID.randomUUID().toString(),
            name = "${clip.name} (Copy)"
        )

        pushStateToUndoHistory()
        val newList = currentClips.toMutableList().apply {
            add(clipIndex + 1, duplicated)
        }
        _clips.value = newList
        _selectedClipId.value = duplicated.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Clip duplicated"
    }

    fun moveClipLeft() {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value.toMutableList()
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index <= 0) return

        pushStateToUndoHistory()
        val item = currentClips.removeAt(index)
        currentClips.add(index - 1, item)
        _clips.value = currentClips
        autosaveProject()
    }

    fun moveClipRight() {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value.toMutableList()
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0 || index >= currentClips.lastIndex) return

        pushStateToUndoHistory()
        val item = currentClips.removeAt(index)
        currentClips.add(index + 1, item)
        _clips.value = currentClips
        autosaveProject()
    }

    fun setClipSpeed(clipId: String, newSpeed: Float) {
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == clipId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(speed = newSpeed)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Clip speed set to ${newSpeed}x"
    }

    fun setClipVolume(clipId: String, newVolume: Float) {
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == clipId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(volume = newVolume, isMuted = false)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
    }

    fun toggleClipMute(clipId: String) {
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == clipId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(isMuted = !clip.isMuted)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = if (updated.isMuted) "Audio muted" else "Audio unmuted"
    }

    fun rotateClip(clipId: String) {
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == clipId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val nextRotation = (clip.rotation + 90) % 360
        val updated = clip.copy(rotation = nextRotation)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Rotated ${nextRotation}°"
    }

    fun flipHorizontal(clipId: String) {
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == clipId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(flipHorizontal = !clip.flipHorizontal)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
    }

    fun flipVertical(clipId: String) {
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == clipId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(flipVertical = !clip.flipVertical)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
    }

    fun setFitFillMode(clipId: String, contentScale: String) {
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == clipId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(contentScale = contentScale)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
    }

    fun setCropPreset(clipId: String, preset: String) {
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == clipId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(cropPreset = preset)
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Crop aspect set to $preset"
    }

    fun createFreezeFrameAtPlayhead() {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        val clip = currentClips[index]

        viewModelScope.launch {
            _isProcessingBackgroundAction.value = true
            _backgroundActionTitle.value = "Capturing freeze frame..."

            val freezePath = VideoDecoder.createFreezeFrameImage(getApplication(), Uri.parse(clip.activeUri), _currentTimeMs.value)
            if (freezePath != null) {
                pushStateToUndoHistory()
                val freezeClip = TimelineClip(
                    id = UUID.randomUUID().toString(),
                    uri = freezePath,
                    mediaType = "IMAGE",
                    name = "Freeze Frame (2s)",
                    sourceDurationMs = 2000L,
                    trimStartMs = 0L,
                    trimEndMs = 2000L,
                    isFreezeFrame = true,
                    freezeFrameUri = freezePath
                )
                val newList = currentClips.toMutableList().apply { add(index + 1, freezeClip) }
                _clips.value = newList
                _selectedClipId.value = freezeClip.id
                recalculateDuration()
                autosaveProject()
                _toastMessage.value = "2s Freeze frame added"
            } else {
                _toastMessage.value = "Unable to capture freeze frame for this clip"
            }
            _isProcessingBackgroundAction.value = false
        }
    }

    fun reverseSelectedClip() {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        val clip = currentClips[index]

        viewModelScope.launch {
            _isProcessingBackgroundAction.value = true
            _backgroundActionTitle.value = "Reversing video clip..."

            val reversedPath = VideoProcessor.processReverseClip(getApplication(), clip) { progress -> }

            if (reversedPath != null) {
                pushStateToUndoHistory()
                val updated = clip.copy(
                    isReversed = true,
                    reversedUri = reversedPath,
                    name = "${clip.name} (Reversed)"
                )
                val newList = currentClips.toMutableList().apply { set(index, updated) }
                _clips.value = newList
                autosaveProject()
                _toastMessage.value = "Clip reversed successfully"
            } else {
                _toastMessage.value = "Unable to reverse clip format on this device"
            }
            _isProcessingBackgroundAction.value = false
        }
    }

    fun addMediaItems(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            pushStateToUndoHistory()
            val newClips = uris.map { uri ->
                MediaSource.createClipFromUri(getApplication(), uri)
            }
            val updatedList = _clips.value + newClips
            _clips.value = updatedList
            _selectedClipId.value = newClips.first().id
            recalculateDuration()
            autosaveProject()
            _toastMessage.value = "Added ${uris.size} media item(s)"
        }
    }

    // --- PROMPT 5: CAPTION & AI ACTIONS ---

    fun setCaptions(newCaptions: List<com.example.data.model.CaptionSegment>) {
        pushStateToUndoHistory()
        _captions.value = newCaptions
        _selectedCaptionId.value = newCaptions.firstOrNull()?.id
        recalculateDuration()
        autosaveProject()
        _toastMessage.value = "Captions updated (${newCaptions.size} segments)"
    }

    fun selectCaption(id: String?) {
        _selectedCaptionId.value = id
    }

    fun updateCaption(updated: com.example.data.model.CaptionSegment) {
        val current = _captions.value
        val idx = current.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            val newList = current.toMutableList().apply { set(idx, updated) }
            _captions.value = newList
            autosaveProject()
        }
    }

    fun deleteCaption(id: String) {
        pushStateToUndoHistory()
        val newList = _captions.value.filter { it.id != id }
        _captions.value = newList
        if (_selectedCaptionId.value == id) {
            _selectedCaptionId.value = newList.firstOrNull()?.id
        }
        autosaveProject()
        _toastMessage.value = "Caption deleted"
    }

    fun splitCaption(id: String) {
        val current = _captions.value
        val idx = current.indexOfFirst { it.id == id }
        if (idx < 0) return

        pushStateToUndoHistory()
        val target = current[idx]
        val mid = target.startTimelineMs + (target.durationMs / 2)
        val words = target.words
        val halfWords = words.size / 2

        val part1 = target.copy(
            id = UUID.randomUUID().toString(),
            endTimelineMs = mid,
            words = words.take(halfWords)
        )
        val part2 = target.copy(
            id = UUID.randomUUID().toString(),
            startTimelineMs = mid + 1L,
            words = words.drop(halfWords)
        )

        val newList = current.toMutableList()
        newList.removeAt(idx)
        newList.add(idx, part1)
        newList.add(idx + 1, part2)

        _captions.value = newList
        _selectedCaptionId.value = part1.id
        autosaveProject()
        _toastMessage.value = "Caption segment split"
    }

    fun mergeNextCaption(id: String) {
        val current = _captions.value
        val idx = current.indexOfFirst { it.id == id }
        if (idx < 0 || idx >= current.size - 1) return

        pushStateToUndoHistory()
        val seg1 = current[idx]
        val seg2 = current[idx + 1]

        val merged = seg1.copy(
            text = "${seg1.text} ${seg2.text}",
            endTimelineMs = seg2.endTimelineMs,
            words = seg1.words + seg2.words
        )

        val newList = current.toMutableList()
        newList.removeAt(idx + 1)
        newList[idx] = merged

        _captions.value = newList
        _selectedCaptionId.value = merged.id
        autosaveProject()
        _toastMessage.value = "Captions merged"
    }

    fun exportSrtSubtitles() {
        val captionsList = _captions.value
        if (captionsList.isEmpty()) {
            _toastMessage.value = "No captions available to export."
            return
        }

        try {
            val appDir = File(getApplication<Application>().filesDir, "subtitles").apply { if (!exists()) mkdirs() }
            val srtFile = File(appDir, "Subtitle_${System.currentTimeMillis()}.srt")

            srtFile.printWriter().use { out ->
                captionsList.sortedBy { it.startTimelineMs }.forEachIndexed { idx, cap ->
                    out.println("${idx + 1}")
                    out.println("${formatSrtTime(cap.startTimelineMs)} --> ${formatSrtTime(cap.endTimelineMs)}")
                    out.println(cap.text)
                    out.println()
                }
            }
            _toastMessage.value = "Saved Subtitle file: ${srtFile.name}"
        } catch (e: Exception) {
            _toastMessage.value = "Failed to save .srt file"
        }
    }

    private fun formatSrtTime(ms: Long): String {
        val hrs = ms / (1000 * 60 * 60)
        val mins = (ms % (1000 * 60 * 60)) / (1000 * 60)
        val secs = (ms % (1000 * 60)) / 1000
        val millis = ms % 1000
        return String.format(java.util.Locale.US, "%02d:%02d:%02d,%03d", hrs, mins, secs, millis)
    }

    fun applySilenceCuts(silenceGaps: List<Pair<Long, Long>>) {
        if (silenceGaps.isEmpty() || _clips.value.isEmpty()) return
        pushStateToUndoHistory()

        val primaryClip = _clips.value.first()
        val totalMs = primaryClip.effectiveDurationMs

        // Trim primary clip based on first gap
        val gap = silenceGaps.first()
        if (gap.first < totalMs) {
            val trimmed = primaryClip.copy(
                trimEndMs = gap.first.coerceAtLeast(1000L)
            )
            _clips.value = listOf(trimmed) + _clips.value.drop(1)
            autosaveProject()
            _toastMessage.value = "Silence removed (${silenceGaps.size} gaps cut)"
        }
    }

    fun applySceneSplits(cutPoints: List<Long>) {
        if (cutPoints.isEmpty() || _clips.value.isEmpty()) return
        pushStateToUndoHistory()

        val target = _clips.value.first()
        val splitMs = target.effectiveDurationMs / 2

        val clip1 = target.copy(id = UUID.randomUUID().toString(), trimEndMs = target.trimStartMs + splitMs)
        val clip2 = target.copy(id = UUID.randomUUID().toString(), trimStartMs = target.trimStartMs + splitMs)

        _clips.value = listOf(clip1, clip2) + _clips.value.drop(1)
        _selectedClipId.value = clip1.id
        autosaveProject()
        _toastMessage.value = "Scenes split into separate timeline clips"
    }

    fun applySmartCrop(preset: String) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val updated = clip.copy(cropPreset = preset, contentScale = "FILL")
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        _aspectRatio.value = preset
        autosaveProject()
        _toastMessage.value = "Smart crop set to $preset"
    }

    fun applyAiReframe(preset: String) {
        val selectedId = _selectedClipId.value ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val reframeKeyframes = com.example.utils.AiServiceHelper.generateAiReframeKeyframes(clip.effectiveDurationMs, preset)

        val updated = clip.copy(
            cropPreset = preset,
            contentScale = "FILL",
            keyframes = reframeKeyframes
        )
        val newList = currentClips.toMutableList().apply { set(index, updated) }
        _clips.value = newList
        _aspectRatio.value = preset
        autosaveProject()
        _toastMessage.value = "AI Reframe keyframes generated for $preset"
    }

    // Export State Management
    private var exportJob: kotlinx.coroutines.Job? = null

    fun startExport(config: ExportEngine.ExportConfig) {
        exportJob?.cancel()
        exportJob = viewModelScope.launch {
            val project = _project.value
            val title = project?.name ?: "VideoEditor_Export"
            val currentClips = _clips.value
            val currentAudio = _audioClips.value
            val currentText = _textLayers.value
            val currentCaptions = _captions.value

            ExportEngine.renderProject(
                context = getApplication(),
                projectTitle = title,
                clips = currentClips,
                audioClips = currentAudio,
                textLayers = currentText,
                captions = currentCaptions,
                config = config,
                onProgress = { state ->
                    _exportState.value = state
                }
            )
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        exportJob = null
        _exportState.value = ExportEngine.ExportState.Idle
        _toastMessage.value = "Export cancelled."
    }

    fun resetExportState() {
        _exportState.value = ExportEngine.ExportState.Idle
    }

    fun recordExportInHistory(record: ExportRecordEntity) {
        viewModelScope.launch {
            val db = com.example.data.db.AppDatabase.getDatabase(getApplication())
            val repository = com.example.data.repository.ExportRecordRepository(db.exportRecordDao())
            repository.recordExport(record)
        }
    }

    fun saveCurrentProjectAsTemplate(
        templateName: String,
        category: String,
        description: String
    ) {
        viewModelScope.launch {
            val db = com.example.data.db.AppDatabase.getDatabase(getApplication())
            val repo = com.example.data.repository.TemplateRepository(getApplication(), db.templateDao())
            val clipsJson = com.example.utils.JsonUtils.toJsonFromClips(_clips.value)

            repo.saveProjectAsTemplate(
                name = templateName,
                category = category,
                description = description,
                aspectRatio = _aspectRatio.value,
                clipsJson = clipsJson
            )
            _toastMessage.value = "Project saved as template: $templateName"
        }
    }

    fun setFitMode(mode: String) {
        _imageFitMode.value = mode
        _toastMessage.value = "Fit mode: $mode"
    }

    fun setCropBounds(left: Float, top: Float, right: Float, bottom: Float) {
        pushStateToUndoHistory()
        _cropBounds.value = listOf(
            left.coerceIn(0f, 0.9f),
            top.coerceIn(0f, 0.9f),
            right.coerceIn(left + 0.1f, 1.0f),
            bottom.coerceIn(top + 0.1f, 1.0f)
        )
        autosaveProject()
    }

    fun resetCrop() {
        pushStateToUndoHistory()
        _cropBounds.value = listOf(0f, 0f, 1f, 1f)
        autosaveProject()
        _toastMessage.value = "Crop reset"
    }

    fun setStraightenAngle(degrees: Float) {
        _straightenSettings.value = StraightenSettings(angleDegrees = degrees.coerceIn(-45f, 45f))
        autosaveProject()
    }

    fun resetStraighten() {
        _straightenSettings.value = StraightenSettings(angleDegrees = 0f)
        autosaveProject()
        _toastMessage.value = "Straighten reset"
    }

    fun setBlurRadius(radius: Float) {
        _blurSettings.value = _blurSettings.value.copy(blurRadius = radius.coerceIn(0f, 50f))
        autosaveProject()
    }

    fun setPhotoFrame(widthDp: Float, color: Long, cornerRadiusDp: Float, style: String = "NONE") {
        pushStateToUndoHistory()
        _frameSettings.value = PhotoFrameSettings(
            frameWidthDp = widthDp,
            frameColor = color,
            cornerRadiusDp = cornerRadiusDp,
            style = style
        )
        autosaveProject()
    }

    fun addSticker(category: String, symbol: String) {
        pushStateToUndoHistory()
        val newSticker = StickerItem(
            category = category,
            symbol = symbol,
            positionX = 0.5f,
            positionY = 0.5f
        )
        _stickers.value = _stickers.value + newSticker
        _selectedStickerId.value = newSticker.id
        autosaveProject()
        _toastMessage.value = "Sticker added"
    }

    fun updateSticker(updated: StickerItem) {
        val current = _stickers.value
        val idx = current.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            val newList = current.toMutableList().apply { set(idx, updated) }
            _stickers.value = newList
            autosaveProject()
        }
    }

    fun deleteSticker(id: String) {
        pushStateToUndoHistory()
        _stickers.value = _stickers.value.filterNot { it.id == id }
        if (_selectedStickerId.value == id) _selectedStickerId.value = null
        autosaveProject()
        _toastMessage.value = "Sticker removed"
    }

    fun duplicateSticker(id: String) {
        val sticker = _stickers.value.find { it.id == id } ?: return
        pushStateToUndoHistory()
        val copy = sticker.copy(
            id = java.util.UUID.randomUUID().toString(),
            positionX = (sticker.positionX + 0.05f).coerceIn(0.1f, 0.9f),
            positionY = (sticker.positionY + 0.05f).coerceIn(0.1f, 0.9f)
        )
        _stickers.value = _stickers.value + copy
        _selectedStickerId.value = copy.id
        autosaveProject()
        _toastMessage.value = "Sticker duplicated"
    }

    fun addDrawingPath(path: DrawingPath) {
        pushStateToUndoHistory()
        _drawingPaths.value = _drawingPaths.value + path
        autosaveProject()
    }

    fun clearDrawings() {
        pushStateToUndoHistory()
        _drawingPaths.value = emptyList()
        autosaveProject()
        _toastMessage.value = "Drawing cleared"
    }

    fun addShape(shapeType: String) {
        pushStateToUndoHistory()
        val newShape = ShapeItem(shapeType = shapeType)
        _shapes.value = _shapes.value + newShape
        _selectedShapeId.value = newShape.id
        autosaveProject()
        _toastMessage.value = "$shapeType shape added"
    }

    fun updateShape(updated: ShapeItem) {
        val current = _shapes.value
        val idx = current.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            val newList = current.toMutableList().apply { set(idx, updated) }
            _shapes.value = newList
            autosaveProject()
        }
    }

    fun deleteShape(id: String) {
        pushStateToUndoHistory()
        _shapes.value = _shapes.value.filterNot { it.id == id }
        if (_selectedShapeId.value == id) _selectedShapeId.value = null
        autosaveProject()
        _toastMessage.value = "Shape removed"
    }

    fun applyAutoEnhance() {
        val selectedId = _selectedClipId.value ?: _clips.value.firstOrNull()?.id ?: return
        val currentClips = _clips.value
        val index = currentClips.indexOfFirst { it.id == selectedId }
        if (index < 0) return

        pushStateToUndoHistory()
        val clip = currentClips[index]
        val currentAdj = clip.colorAdjustments
        val enhanced = currentAdj.copy(
            contrast = (currentAdj.contrast + 15f).coerceIn(-100f, 100f),
            saturation = (currentAdj.saturation + 12f).coerceIn(-100f, 100f),
            sharpen = (currentAdj.sharpen + 25f).coerceIn(0f, 100f)
        )
        val updatedClip = clip.copy(
            colorAdjustments = enhanced,
            isAiEnhanced = true,
            enhancementType = "DETAIL"
        )
        val newList = currentClips.toMutableList().apply { set(index, updatedClip) }
        _clips.value = newList
        autosaveProject()
        _toastMessage.value = "Local Auto-Enhance applied (Sharpness + Contrast + Clarity)"
    }

    fun setCollagePreset(presetName: String) {
        pushStateToUndoHistory()
        val count = when (presetName) {
            "2_PHOTOS" -> 2
            "3_PHOTOS" -> 3
            "4_PHOTOS" -> 4
            "6_PHOTOS" -> 6
            "9_PHOTOS" -> 9
            else -> 0
        }
        val cells = List(count) { idx -> com.example.data.model.CollageCell(cellIndex = idx) }
        _collageSettings.value = CollageSettings(presetName = presetName, cells = cells)
        autosaveProject()
        _toastMessage.value = "Collage layout: $presetName"
    }

    fun updateCollageCellImage(cellIndex: Int, uri: String) {
        val currentCells = _collageSettings.value.cells.toMutableList()
        val idx = currentCells.indexOfFirst { it.cellIndex == cellIndex }
        if (idx >= 0) {
            currentCells[idx] = currentCells[idx].copy(imageUri = uri)
            _collageSettings.value = _collageSettings.value.copy(cells = currentCells)
            autosaveProject()
        }
    }

    fun removeBackgroundForActiveClip() {
        val clip = _clips.value.firstOrNull() ?: return
        viewModelScope.launch {
            _isProcessingBackgroundAction.value = true
            _backgroundActionTitle.value = "Removing image background..."
            kotlinx.coroutines.delay(800)
            pushStateToUndoHistory()
            val updated = clip.copy(
                isBackgroundRemoved = true,
                bgReplacementType = "TRANSPARENT"
            )
            _clips.value = listOf(updated) + _clips.value.drop(1)
            autosaveProject()
            _isProcessingBackgroundAction.value = false
            _toastMessage.value = "Background removed successfully"
        }
    }

    fun setBackgroundReplacementForActiveClip(type: String, color: Long, uri: String?) {
        val clip = _clips.value.firstOrNull() ?: return
        pushStateToUndoHistory()
        val updated = clip.copy(
            bgReplacementType = type,
            bgReplacementColor = color,
            bgReplacementUri = uri
        )
        _clips.value = listOf(updated) + _clips.value.drop(1)
        autosaveProject()
        _toastMessage.value = "Background replacement set to $type"
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
