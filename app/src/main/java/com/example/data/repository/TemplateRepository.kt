package com.example.data.repository

import android.content.Context
import com.example.data.db.TemplateDao
import com.example.data.model.AudioClip
import com.example.data.model.TextLayer
import com.example.data.model.TimelineClip
import com.example.data.model.TemplateEntity
import com.example.utils.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class TemplateRepository(
    private val context: Context,
    private val templateDao: TemplateDao
) {
    val allTemplates: Flow<List<TemplateEntity>> = templateDao.getAllTemplates()

    suspend fun seedDefaultTemplatesIfEmpty() = withContext(Dispatchers.IO) {
        val defaults = listOf(
            TemplateEntity(
                id = "tpl_reels_viral",
                name = "Viral Beat Sync Reel",
                category = "Reels",
                description = "Fast kinetic cuts synchronized to energetic beats. Perfect for Instagram Reels & TikTok.",
                aspectRatio = "9:16",
                durationMs = 15000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Clip 1 (Intro 3s)", sourceDurationMs = 3000L, trimStartMs = 0L, trimEndMs = 3000L, filterName = "VIVID_WARM"),
                        TimelineClip(id = "p2", uri = "", name = "Clip 2 (Drop 4s)", sourceDurationMs = 4000L, trimStartMs = 0L, trimEndMs = 4000L, filterName = "CYBERPUNK"),
                        TimelineClip(id = "p3", uri = "", name = "Clip 3 (Outro 8s)", sourceDurationMs = 8000L, trimStartMs = 0L, trimEndMs = 8000L, filterName = "CINEMATIC_TEAL")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_shorts_tech",
                name = "Tech Unboxing Short",
                category = "Shorts",
                description = "Clean vertical layout with top & bottom text banners and upbeat background music.",
                aspectRatio = "9:16",
                durationMs = 25000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Product Showcase", sourceDurationMs = 25000L, trimStartMs = 0L, trimEndMs = 25000L, filterName = "CLEAN_PORTRAIT")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_vlog_daily",
                name = "Daily Vlog Opener",
                category = "Vlog",
                description = "16:9 Widescreen aesthetic intro with title fade and warm color grade.",
                aspectRatio = "16:9",
                durationMs = 30000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Main Vlog Clip", sourceDurationMs = 30000L, trimStartMs = 0L, trimEndMs = 30000L, filterName = "GOLDEN_HOUR")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_travel_wanderlust",
                name = "Wanderlust Odyssey",
                category = "Travel",
                description = "Vintage film preset, slow zoom keyframes, and crossfade transition for travel memories.",
                aspectRatio = "9:16",
                durationMs = 20000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Landscape Scene 1", sourceDurationMs = 10000L, trimStartMs = 0L, trimEndMs = 10000L, filterName = "VINTAGE_FILM"),
                        TimelineClip(id = "p2", uri = "", name = "Landscape Scene 2", sourceDurationMs = 10000L, trimStartMs = 0L, trimEndMs = 10000L, filterName = "VINTAGE_FILM")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_birthday_celebration",
                name = "Happy Birthday Celebration",
                category = "Birthday",
                description = "Festive 1:1 post with sparkles effect, bold party typography, and cheerful soundtrack.",
                aspectRatio = "1:1",
                durationMs = 15000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Birthday Highlight", sourceDurationMs = 15000L, trimStartMs = 0L, trimEndMs = 15000L, filterName = "VIVID_WARM")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_wedding_romance",
                name = "Romantic Wedding Moments",
                category = "Wedding",
                description = "Soft cinematic glow, slow motion rate, and elegant title overlays.",
                aspectRatio = "16:9",
                durationMs = 45000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Ceremony Scene", sourceDurationMs = 45000L, trimStartMs = 0L, trimEndMs = 45000L, filterName = "SOFT_GLOW")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_motivation_grind",
                name = "Morning Focus Motivation",
                category = "Motivation",
                description = "High contrast black-and-white grade, bold quote text overlay, and heavy bass.",
                aspectRatio = "9:16",
                durationMs = 20000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Gym / Focus Clip", sourceDurationMs = 20000L, trimStartMs = 0L, trimEndMs = 20000L, filterName = "NOIR_BW")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_cinematic_neon",
                name = "Cyberpunk Neon Nights",
                category = "Cinematic",
                description = "Neon glow accents, anamorphic widescreen bars, and deep teal & orange grade.",
                aspectRatio = "16:9",
                durationMs = 30000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Night City Shot", sourceDurationMs = 30000L, trimStartMs = 0L, trimEndMs = 30000L, filterName = "CYBERPUNK")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_slideshow_memories",
                name = "Memory Photo Slideshow",
                category = "Photo Slideshow",
                description = "Auto pan-and-zoom keyframes for still photos with soft slide transitions.",
                aspectRatio = "4:5",
                durationMs = 24000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Photo 1", mediaType = "IMAGE", sourceDurationMs = 6000L, trimStartMs = 0L, trimEndMs = 6000L),
                        TimelineClip(id = "p2", uri = "", name = "Photo 2", mediaType = "IMAGE", sourceDurationMs = 6000L, trimStartMs = 0L, trimEndMs = 6000L),
                        TimelineClip(id = "p3", uri = "", name = "Photo 3", mediaType = "IMAGE", sourceDurationMs = 6000L, trimStartMs = 0L, trimEndMs = 6000L),
                        TimelineClip(id = "p4", uri = "", name = "Photo 4", mediaType = "IMAGE", sourceDurationMs = 6000L, trimStartMs = 0L, trimEndMs = 6000L)
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_status_lofi",
                name = "Aesthetic Lofi Status",
                category = "Status",
                description = "Typewriter quote caption, vignette border, and relaxing lofi soundtrack.",
                aspectRatio = "9:16",
                durationMs = 15000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Aesthetic Clip", sourceDurationMs = 15000L, trimStartMs = 0L, trimEndMs = 15000L, filterName = "PASTEL_DREAM")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_festival_party",
                name = "Festival Carnival Beats",
                category = "Festival",
                description = "Chromatic aberration effect, saturated colors, and fast multi-angle cuts.",
                aspectRatio = "9:16",
                durationMs = 15000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Festival Crowd", sourceDurationMs = 15000L, trimStartMs = 0L, trimEndMs = 15000L, filterName = "VIVID_WARM")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_product_showcase",
                name = "Modern Product Showcase",
                category = "Product",
                description = "Clean minimal backdrop, price badge overlay, and sharp transitions.",
                aspectRatio = "1:1",
                durationMs = 20000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Product Angle 1", sourceDurationMs = 10000L, trimStartMs = 0L, trimEndMs = 10000L, filterName = "CLEAN_PORTRAIT"),
                        TimelineClip(id = "p2", uri = "", name = "Product Angle 2", sourceDurationMs = 10000L, trimStartMs = 0L, trimEndMs = 10000L, filterName = "CLEAN_PORTRAIT")
                    )
                )
            ),
            TemplateEntity(
                id = "tpl_story_quick",
                name = "Quick Daily Story",
                category = "Story",
                description = "10s snappy vertical update with sticker text overlay and light grain.",
                aspectRatio = "9:16",
                durationMs = 10000L,
                templateJson = JsonUtils.toJsonFromClips(
                    listOf(
                        TimelineClip(id = "p1", uri = "", name = "Story Shot", sourceDurationMs = 10000L, trimStartMs = 0L, trimEndMs = 10000L, filterName = "GOLDEN_HOUR")
                    )
                )
            )
        )
        templateDao.insertTemplates(defaults)
    }

    suspend fun saveProjectAsTemplate(
        name: String,
        category: String,
        description: String,
        aspectRatio: String,
        clipsJson: String
    ): String = withContext(Dispatchers.IO) {
        val newId = "tpl_custom_${UUID.randomUUID()}"
        val template = TemplateEntity(
            id = newId,
            name = name,
            category = category,
            description = description.ifBlank { "Custom user created template" },
            aspectRatio = aspectRatio,
            isCustom = true,
            templateJson = clipsJson
        )
        templateDao.insertTemplate(template)
        newId
    }

    suspend fun deleteTemplate(template: TemplateEntity) = withContext(Dispatchers.IO) {
        templateDao.deleteTemplate(template)
    }
}
