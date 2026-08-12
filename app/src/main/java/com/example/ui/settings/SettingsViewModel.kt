package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.repository.ProjectRepository
import com.example.utils.MediaUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository

    init {
        val dao = AppDatabase.getDatabase(application).projectDao()
        repository = ProjectRepository(application, dao)
        refreshCacheSize()
    }

    private val _themeMode = MutableStateFlow("DARK") // "DARK", "LIGHT", "SYSTEM"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _cacheSizeMb = MutableStateFlow(0.0)
    val cacheSizeMb: StateFlow<Double> = _cacheSizeMb.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    fun refreshCacheSize() {
        _cacheSizeMb.value = MediaUtils.getCacheSizeMb(getApplication())
    }

    fun clearThumbnailCache() {
        viewModelScope.launch {
            val cleared = MediaUtils.clearCache(getApplication())
            refreshCacheSize()
            _message.value = if (cleared) "Thumbnail cache cleared successfully" else "Failed to clear cache"
        }
    }

    fun resetLocalProjects() {
        viewModelScope.launch {
            repository.deleteAllProjects()
            refreshCacheSize()
            _message.value = "All local projects have been reset"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
