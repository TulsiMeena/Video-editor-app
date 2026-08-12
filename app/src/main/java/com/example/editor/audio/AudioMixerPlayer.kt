package com.example.editor.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.model.AudioClip

class AudioMixerPlayer(private val context: Context) {

    private val activePlayers = mutableMapOf<String, MediaPlayer>()
    private var duckingEnabled: Boolean = true
    private var duckingRatio: Float = 0.35f // Reduce music to 35% when voice-over plays

    fun updateAudioClips(clips: List<AudioClip>, currentTimeMs: Long, isPlaying: Boolean) {
        val activeIds = clips.map { it.id }.toSet()

        // Remove stopped or deleted clip players
        val keysToRemove = activePlayers.keys.filter { !activeIds.contains(it) }
        for (key in keysToRemove) {
            releasePlayer(key)
        }

        // Check if any voice-over clip is actively playing right now
        val isVoiceOverActive = clips.any { clip ->
            clip.audioType == "VOICE_OVER" && currentTimeMs in clip.startTimelineMs..clip.endTimelineMs
        }

        for (clip in clips) {
            val clipStart = clip.startTimelineMs
            val clipEnd = clip.endTimelineMs
            val isClipActive = currentTimeMs in clipStart..clipEnd

            if (isClipActive) {
                val existing = activePlayers[clip.id]
                val player = if (existing != null) {
                    existing
                } else {
                    val created = createPlayerForClip(clip) ?: continue
                    activePlayers[clip.id] = created
                    created
                }

                // Calculate clip internal offset
                val offsetInClipMs = ((currentTimeMs - clipStart) * clip.speed).toLong() + clip.trimStartMs
                val clampedOffsetMs = offsetInClipMs.coerceIn(clip.trimStartMs, clip.trimEndMs)

                // Calculate volume & apply Fade In / Fade Out
                var baseVol = clip.effectiveVolume
                val progressInClip = currentTimeMs - clipStart

                if (clip.fadeInMs > 0 && progressInClip < clip.fadeInMs) {
                    val factor = progressInClip.toFloat() / clip.fadeInMs.toFloat()
                    baseVol *= factor.coerceIn(0f, 1f)
                }

                val remainingInClip = clipEnd - currentTimeMs
                if (clip.fadeOutMs > 0 && remainingInClip < clip.fadeOutMs) {
                    val factor = remainingInClip.toFloat() / clip.fadeOutMs.toFloat()
                    baseVol *= factor.coerceIn(0f, 1f)
                }

                // Apply Audio Ducking for Music when Voice-Over is playing
                if (duckingEnabled && isVoiceOverActive && clip.audioType == "MUSIC") {
                    baseVol *= duckingRatio
                }

                val targetVol = baseVol.coerceIn(0f, 2f)
                player.setVolume(targetVol.coerceAtMost(1f), targetVol.coerceAtMost(1f))

                if (isPlaying) {
                    if (!player.isPlaying) {
                        try {
                            player.seekTo(clampedOffsetMs.toInt())
                            player.start()
                        } catch (_: Exception) {}
                    }
                } else {
                    if (player.isPlaying) {
                        try {
                            player.pause()
                        } catch (_: Exception) {}
                    }
                }
            } else {
                // If playhead moved out of clip range, pause/release
                val player = activePlayers[clip.id]
                if (player != null && player.isPlaying) {
                    try {
                        player.pause()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun createPlayerForClip(clip: AudioClip): MediaPlayer? {
        return try {
            val player = MediaPlayer()
            if (clip.uri.startsWith("/")) {
                player.setDataSource(clip.uri)
            } else {
                player.setDataSource(context, Uri.parse(clip.uri))
            }
            player.prepare()
            player
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun releaseAll() {
        for (key in activePlayers.keys.toList()) {
            releasePlayer(key)
        }
        activePlayers.clear()
    }

    private fun releasePlayer(id: String) {
        val player = activePlayers.remove(id)
        if (player != null) {
            try {
                if (player.isPlaying) player.stop()
                player.release()
            } catch (_: Exception) {}
        }
    }

    fun setDuckingEnabled(enabled: Boolean, ratio: Float = 0.35f) {
        this.duckingEnabled = enabled
        this.duckingRatio = ratio
    }
}
