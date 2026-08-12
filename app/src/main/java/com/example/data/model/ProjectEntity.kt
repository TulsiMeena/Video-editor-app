package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val projectType: String, // "VIDEO", "IMAGE", "MIXED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null,
    val mediaItemsJson: String = "[]", // Serialized list of TimelineClip
    val audioClipsJson: String = "[]", // Serialized list of AudioClip
    val textLayersJson: String = "[]", // Serialized list of TextLayer
    val captionsJson: String = "[]" // Serialized list of CaptionSegment
)
