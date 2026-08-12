package com.example.utils

import com.example.data.model.AudioClip
import com.example.data.model.MediaItemRef
import com.example.data.model.TextLayer
import com.example.data.model.TimelineClip
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonUtils {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val mediaItemRefType = Types.newParameterizedType(List::class.java, MediaItemRef::class.java)
    private val mediaItemRefAdapter = moshi.adapter<List<MediaItemRef>>(mediaItemRefType)

    private val timelineClipType = Types.newParameterizedType(List::class.java, TimelineClip::class.java)
    private val timelineClipAdapter = moshi.adapter<List<TimelineClip>>(timelineClipType)

    private val audioClipType = Types.newParameterizedType(List::class.java, AudioClip::class.java)
    private val audioClipAdapter = moshi.adapter<List<AudioClip>>(audioClipType)

    private val textLayerType = Types.newParameterizedType(List::class.java, TextLayer::class.java)
    private val textLayerAdapter = moshi.adapter<List<TextLayer>>(textLayerType)

    private val captionSegmentType = Types.newParameterizedType(List::class.java, com.example.data.model.CaptionSegment::class.java)
    private val captionSegmentAdapter = moshi.adapter<List<com.example.data.model.CaptionSegment>>(captionSegmentType)

    fun toJson(items: List<MediaItemRef>): String {
        return try {
            mediaItemRefAdapter.toJson(items)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun mediaItemsFromJson(json: String): List<MediaItemRef> {
        return try {
            mediaItemRefAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJsonFromClips(clips: List<TimelineClip>): String {
        return try {
            timelineClipAdapter.toJson(clips)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun timelineClipsFromJson(json: String): List<TimelineClip> {
        if (json.isBlank() || json == "[]") return emptyList()

        // Try parsing as TimelineClip list first
        try {
            val clips = timelineClipAdapter.fromJson(json)
            if (!clips.isNullOrEmpty()) return clips
        } catch (_: Exception) {}

        // Fallback: Try parsing as MediaItemRef list and converting
        return try {
            val items = mediaItemRefAdapter.fromJson(json) ?: emptyList()
            items.map { item ->
                val duration = if (item.durationMs > 0) item.durationMs else 5000L
                TimelineClip(
                    uri = item.uri,
                    mediaType = item.mediaType,
                    sourceDurationMs = duration,
                    trimStartMs = 0L,
                    trimEndMs = duration,
                    rotation = item.rotation,
                    width = if (item.width > 0) item.width else 1920,
                    height = if (item.height > 0) item.height else 1080
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJsonFromAudioClips(clips: List<AudioClip>): String {
        return try {
            audioClipAdapter.toJson(clips)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun audioClipsFromJson(json: String): List<AudioClip> {
        if (json.isBlank() || json == "[]") return emptyList()
        return try {
            audioClipAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJsonFromTextLayers(layers: List<TextLayer>): String {
        return try {
            textLayerAdapter.toJson(layers)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun textLayersFromJson(json: String): List<TextLayer> {
        if (json.isBlank() || json == "[]") return emptyList()
        return try {
            textLayerAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJsonFromCaptions(captions: List<com.example.data.model.CaptionSegment>): String {
        return try {
            captionSegmentAdapter.toJson(captions)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun captionsFromJson(json: String): List<com.example.data.model.CaptionSegment> {
        if (json.isBlank() || json == "[]") return emptyList()
        return try {
            captionSegmentAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
