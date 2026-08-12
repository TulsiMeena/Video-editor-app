package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "export_history")
data class ExportRecordEntity(
    @PrimaryKey
    val id: String,
    val filePath: String,
    val fileUri: String? = null,
    val fileName: String,
    val projectTitle: String,
    val mediaType: String, // "VIDEO" or "IMAGE"
    val resolution: String, // e.g., "1080p (1080x1920)"
    val fps: Int = 30,
    val aspectRatio: String = "16:9",
    val fileSizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val thumbnailPath: String? = null,
    val exportedAt: Long = System.currentTimeMillis()
)
