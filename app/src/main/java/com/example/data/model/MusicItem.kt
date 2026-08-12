package com.example.data.model

data class MusicItem(
    val id: String,
    val title: String,
    val artist: String = "Lumina Audio",
    val category: String, // Cinematic, Emotional, Happy, Sad, Romantic, Travel, Vlog, Action, Motivational, Chill, Funny, Shorts/Reels
    val durationMs: Long,
    val assetOrUriPath: String
)
