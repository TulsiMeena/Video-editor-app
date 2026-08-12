package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.ProjectDao
import com.example.data.model.MediaItemRef
import com.example.data.model.ProjectEntity
import com.example.utils.JsonUtils
import com.example.utils.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ProjectRepository(
    private val context: Context,
    private val projectDao: ProjectDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    fun getProjectById(id: String): Flow<ProjectEntity?> = projectDao.getProjectById(id)

    suspend fun createProjectFromUris(
        uris: List<Uri>,
        isPrimaryVideo: Boolean
    ): Result<ProjectEntity> = withContext(Dispatchers.IO) {
        try {
            if (uris.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("No media selected"))
            }

            val mediaRefs = mutableListOf<MediaItemRef>()
            var primaryThumbPath: String? = null

            uris.forEachIndexed { index, uri ->
                val metadata = MediaUtils.validateAndExtractMetadata(context, uri)
                if (!metadata.isValid) {
                    return@withContext Result.failure(IllegalStateException("Selected media is not readable or valid."))
                }

                val isVideo = metadata.mimeType.contains("video", ignoreCase = true) || isPrimaryVideo
                val mediaType = if (isVideo) "VIDEO" else "IMAGE"

                val ref = MediaItemRef(
                    uri = uri.toString(),
                    mediaType = mediaType,
                    durationMs = metadata.durationMs,
                    width = metadata.width,
                    height = metadata.height,
                    orderIndex = index
                )
                mediaRefs.add(ref)

                if (primaryThumbPath == null) {
                    primaryThumbPath = MediaUtils.generateThumbnail(context, uri, isVideo)
                }
            }

            val primaryType = if (mediaRefs.all { it.mediaType == "VIDEO" }) {
                "VIDEO"
            } else if (mediaRefs.all { it.mediaType == "IMAGE" }) {
                "IMAGE"
            } else {
                "MIXED"
            }

            val defaultName = if (primaryType == "VIDEO") {
                "Video Project ${System.currentTimeMillis() % 1000}"
            } else if (primaryType == "IMAGE") {
                "Photo Project ${System.currentTimeMillis() % 1000}"
            } else {
                "Mix Project ${System.currentTimeMillis() % 1000}"
            }

            val project = ProjectEntity(
                id = UUID.randomUUID().toString(),
                name = defaultName,
                projectType = primaryType,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                thumbnailPath = primaryThumbPath,
                mediaItemsJson = JsonUtils.toJson(mediaRefs)
            )

            projectDao.insertProject(project)
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameProject(id: String, newName: String) = withContext(Dispatchers.IO) {
        val project = projectDao.getProjectByIdSync(id) ?: return@withContext
        val updated = project.copy(
            name = newName.trim(),
            updatedAt = System.currentTimeMillis()
        )
        projectDao.updateProject(updated)
    }

    suspend fun duplicateProject(id: String) = withContext(Dispatchers.IO) {
        val project = projectDao.getProjectByIdSync(id) ?: return@withContext
        val newId = UUID.randomUUID().toString()
        val duplicated = project.copy(
            id = newId,
            name = "${project.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        projectDao.insertProject(duplicated)
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        val project = projectDao.getProjectByIdSync(id)
        if (project != null) {
            // Delete cached thumbnail if it exists
            project.thumbnailPath?.let { path ->
                try {
                    File(path).delete()
                } catch (_: Exception) {}
            }
            projectDao.deleteProject(project)
        }
    }

    suspend fun updateProjectMediaAndName(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteAllProjects() = withContext(Dispatchers.IO) {
        projectDao.deleteAllProjects()
        MediaUtils.clearCache(context)
    }
}
