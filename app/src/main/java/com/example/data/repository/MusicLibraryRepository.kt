package com.example.data.repository

import android.content.Context
import com.example.data.model.MusicItem
import com.example.utils.AudioUtils

class MusicLibraryRepository(private val context: Context) {

    val categories = listOf(
        "ALL",
        "Cinematic",
        "Emotional",
        "Happy",
        "Sad",
        "Romantic",
        "Travel",
        "Vlog",
        "Action",
        "Motivational",
        "Chill",
        "Funny",
        "Shorts/Reels"
    )

    private val rawMusicList = listOf(
        MusicItem("m1", "Aura Cinematic Rise", "Lumina Studio Assets", "Cinematic", 15000L, "cinematic_rise.wav"),
        MusicItem("m2", "Sunset Horizon", "Lumina Studio Assets", "Emotional", 15000L, "sunset_horizon.wav"),
        MusicItem("m3", "Sunlight Beats", "Lumina Studio Assets", "Happy", 12000L, "sunlight_beats.wav"),
        MusicItem("m4", "Melancholy Piano", "Lumina Studio Assets", "Sad", 18000L, "melancholy_piano.wav"),
        MusicItem("m5", "Acoustic Warmth", "Lumina Studio Assets", "Romantic", 15000L, "acoustic_warmth.wav"),
        MusicItem("m6", "Wanderlust Groove", "Lumina Studio Assets", "Travel", 15000L, "wanderlust_groove.wav"),
        MusicItem("m7", "Daily Vlog Vibe", "Lumina Studio Assets", "Vlog", 12000L, "daily_vlog_vibe.wav"),
        MusicItem("m8", "High Octane Rush", "Lumina Studio Assets", "Action", 15000L, "high_octane_rush.wav"),
        MusicItem("m9", "Rise & Conquer", "Lumina Studio Assets", "Motivational", 15000L, "rise_conquer.wav"),
        MusicItem("m10", "Lofi Breeze", "Lumina Studio Assets", "Chill", 18000L, "lofi_breeze.wav"),
        MusicItem("m11", "Whimsical Bounce", "Lumina Studio Assets", "Funny", 10000L, "whimsical_bounce.wav"),
        MusicItem("m12", "Viral Reel Punch", "Lumina Studio Assets", "Shorts/Reels", 8000L, "viral_reel_punch.wav")
    )

    /**
     * Prepares local WAV files for demo tracks if needed and returns full list.
     */
    fun getMusicItems(query: String = "", category: String = "ALL"): List<MusicItem> {
        val filtered = rawMusicList.filter { item ->
            val matchesCategory = (category == "ALL" || item.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.artist.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        // Ensure demo sound files exist on disk
        return filtered.map { item ->
            val freq = when (item.category) {
                "Cinematic" -> 260.0
                "Emotional" -> 220.0
                "Happy" -> 330.0
                "Sad" -> 196.0
                "Romantic" -> 246.0
                "Travel" -> 293.0
                "Vlog" -> 349.0
                "Action" -> 174.0
                "Motivational" -> 392.0
                "Chill" -> 261.0
                "Funny" -> 440.0
                else -> 523.0
            }
            val durationSec = (item.durationMs / 1000).toInt()
            val file = AudioUtils.getOrCreateDemoAudioFile(context, item.assetOrUriPath, freq, durationSec)
            item.copy(assetOrUriPath = file.absolutePath)
        }
    }
}
