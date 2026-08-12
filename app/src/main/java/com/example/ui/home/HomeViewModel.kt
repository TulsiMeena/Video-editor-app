package com.example.ui.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ExportRecordEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TemplateEntity
import com.example.data.repository.ExportRecordRepository
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val projectRepository: ProjectRepository
    private val templateRepository: TemplateRepository
    private val exportRecordRepository: ExportRecordRepository

    init {
        val db = AppDatabase.getDatabase(application)
        projectRepository = ProjectRepository(application, db.projectDao())
        templateRepository = TemplateRepository(application, db.templateDao())
        exportRecordRepository = ExportRecordRepository(db.exportRecordDao())

        // Seed default templates on launch if empty
        viewModelScope.launch {
            templateRepository.seedDefaultTemplatesIfEmpty()
        }
    }

    val projects: StateFlow<List<ProjectEntity>> = projectRepository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val templates: StateFlow<List<TemplateEntity>> = templateRepository.allTemplates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val exportHistory: StateFlow<List<ExportRecordEntity>> = exportRecordRepository.allExportRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isProcessingMedia = MutableStateFlow(false)
    val isProcessingMedia: StateFlow<Boolean> = _isProcessingMedia.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _newProjectCreated = MutableStateFlow<ProjectEntity?>(null)
    val newProjectCreated: StateFlow<ProjectEntity?> = _newProjectCreated.asStateFlow()

    fun createProjectFromMedia(uris: List<Uri>, isPrimaryVideo: Boolean) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _isProcessingMedia.value = true
            _errorMessage.value = null

            val result = projectRepository.createProjectFromUris(uris, isPrimaryVideo)
            _isProcessingMedia.value = false

            result.onSuccess { project ->
                _newProjectCreated.value = project
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "This media format isn't supported."
            }
        }
    }

    fun createProjectFromTemplate(template: TemplateEntity, mediaUri: Uri) {
        viewModelScope.launch {
            _isProcessingMedia.value = true
            _errorMessage.value = null

            val result = projectRepository.createProjectFromUris(listOf(mediaUri), template.mediaType == "VIDEO")
            _isProcessingMedia.value = false

            result.onSuccess { baseProject ->
                // Custom project from template
                val updatedProject = baseProject.copy(
                    name = "${template.name} Project",
                    mediaItemsJson = template.templateJson.ifBlank { baseProject.mediaItemsJson }
                )
                projectRepository.updateProjectMediaAndName(updatedProject)
                _newProjectCreated.value = updatedProject
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to apply template."
            }
        }
    }

    fun consumeNewProjectEvent() {
        _newProjectCreated.value = null
    }

    fun renameProject(id: String, newName: String) {
        viewModelScope.launch {
            projectRepository.renameProject(id, newName)
        }
    }

    fun duplicateProject(id: String) {
        viewModelScope.launch {
            projectRepository.duplicateProject(id)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            projectRepository.deleteProject(id)
        }
    }

    fun deleteExportRecord(record: ExportRecordEntity) {
        viewModelScope.launch {
            exportRecordRepository.deleteExportRecord(record)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
