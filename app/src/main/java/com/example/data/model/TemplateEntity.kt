package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String, // Reels, Shorts, Vlog, Travel, Birthday, Wedding, Motivation, Cinematic, Photo Slideshow, Status, Festival, Product, Story
    val description: String,
    val thumbnailUrl: String? = null,
    val aspectRatio: String = "9:16",
    val durationMs: Long = 15000L,
    val isCustom: Boolean = false,
    val mediaType: String = "VIDEO", // "VIDEO" or "IMAGE"
    val templateJson: String = "{}", // Serialized clips, placeholders, text, audio references
    val createdAt: Long = System.currentTimeMillis()
)
