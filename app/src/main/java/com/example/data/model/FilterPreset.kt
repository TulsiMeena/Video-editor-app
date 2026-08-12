package com.example.data.model

data class FilterPreset(
    val id: String,
    val name: String,
    val category: String,
    val description: String = "",
    // Preset parameters for matrix generation
    val brightnessOffset: Float = 0f,
    val contrastScale: Float = 1f,
    val saturationScale: Float = 1f,
    val redScale: Float = 1f,
    val greenScale: Float = 1f,
    val blueScale: Float = 1f,
    val sepiaFactor: Float = 0f,
    val isMonochrome: Boolean = false
) {
    companion object {
        val ALL_FILTERS = listOf(
            FilterPreset("original", "Original", "Original"),
            // Cinematic
            FilterPreset("cinematic_teal_orange", "Teal & Orange", "Cinematic", redScale = 1.1f, greenScale = 0.95f, blueScale = 0.85f, contrastScale = 1.2f, saturationScale = 1.1f),
            FilterPreset("cinematic_blockbuster", "Blockbuster", "Cinematic", redScale = 0.9f, greenScale = 1.05f, blueScale = 1.15f, contrastScale = 1.25f),
            // Warm
            FilterPreset("warm_sunset", "Golden Hour", "Warm", redScale = 1.25f, greenScale = 1.1f, blueScale = 0.8f, contrastScale = 1.05f),
            FilterPreset("warm_cozy", "Cozy Glow", "Warm", redScale = 1.15f, greenScale = 1.05f, blueScale = 0.9f, brightnessOffset = 10f),
            // Cool
            FilterPreset("cool_nordic", "Nordic Frost", "Cool", redScale = 0.8f, greenScale = 1.05f, blueScale = 1.3f, saturationScale = 0.9f),
            FilterPreset("cool_arctic", "Arctic Cyan", "Cool", redScale = 0.75f, greenScale = 1.15f, blueScale = 1.25f, contrastScale = 1.1f),
            // Vintage
            FilterPreset("vintage_70s", "70s Warmth", "Vintage", sepiaFactor = 0.35f, contrastScale = 0.9f, redScale = 1.1f, blueScale = 0.85f),
            FilterPreset("vintage_polaroid", "Polaroid", "Vintage", redScale = 1.1f, greenScale = 1.0f, blueScale = 0.9f, brightnessOffset = 15f, contrastScale = 0.95f),
            // Film
            FilterPreset("film_kodak", "Kodak Chrome", "Film", redScale = 1.2f, greenScale = 1.05f, blueScale = 0.85f, contrastScale = 1.15f, saturationScale = 1.2f),
            FilterPreset("film_fuji", "Fuji Velvet", "Film", redScale = 0.9f, greenScale = 1.2f, blueScale = 1.05f, contrastScale = 1.1f, saturationScale = 1.15f),
            // Retro
            FilterPreset("retro_synthwave", "Synthwave", "Retro", redScale = 1.3f, greenScale = 0.7f, blueScale = 1.3f, contrastScale = 1.3f),
            FilterPreset("retro_cassette", "VHS Tape", "Retro", redScale = 1.15f, greenScale = 1.1f, blueScale = 0.8f, contrastScale = 0.9f, brightnessOffset = 10f),
            // Black & White
            FilterPreset("bw_noir", "Classic Noir", "Black & White", isMonochrome = true, contrastScale = 1.3f),
            FilterPreset("bw_high_contrast", "High Contrast B&W", "Black & White", isMonochrome = true, contrastScale = 1.5f, brightnessOffset = 5f),
            // Moody
            FilterPreset("moody_shadows", "Dark Shadows", "Moody", redScale = 0.85f, greenScale = 0.85f, blueScale = 0.95f, contrastScale = 1.3f, brightnessOffset = -15f),
            FilterPreset("moody_desaturated", "Desaturated", "Moody", saturationScale = 0.3f, contrastScale = 1.2f),
            // Bright
            FilterPreset("bright_vibrant", "Super Bright", "Bright", brightnessOffset = 20f, saturationScale = 1.3f, contrastScale = 1.05f),
            FilterPreset("bright_pop", "Summer Pop", "Bright", brightnessOffset = 15f, redScale = 1.1f, greenScale = 1.1f, blueScale = 1.1f, saturationScale = 1.25f),
            // Dreamy
            FilterPreset("dreamy_pastel", "Soft Pastel", "Dreamy", brightnessOffset = 25f, contrastScale = 0.8f, saturationScale = 0.85f, redScale = 1.05f, greenScale = 1.02f, blueScale = 1.08f),
            FilterPreset("dreamy_ether", "Ethereal", "Dreamy", brightnessOffset = 18f, contrastScale = 0.85f, redScale = 1.1f, blueScale = 1.15f),
            // Vlog
            FilterPreset("vlog_natural", "Vlog Natural", "Vlog", brightnessOffset = 8f, contrastScale = 1.05f, saturationScale = 1.1f),
            FilterPreset("vlog_clean", "Clean Skin", "Vlog", redScale = 1.08f, greenScale = 1.02f, blueScale = 0.98f, brightnessOffset = 10f, contrastScale = 0.95f),
            // Portrait
            FilterPreset("portrait_glow", "Portrait Glow", "Portrait", redScale = 1.12f, greenScale = 1.02f, blueScale = 0.92f, brightnessOffset = 12f, contrastScale = 0.95f),
            FilterPreset("portrait_warmth", "Warm Portrait", "Portrait", redScale = 1.18f, greenScale = 1.05f, blueScale = 0.88f, contrastScale = 1.0f),
            // Dramatic
            FilterPreset("dramatic_cinema", "Dramatic Epic", "Dramatic", contrastScale = 1.4f, saturationScale = 1.15f, brightnessOffset = -10f, redScale = 1.1f, blueScale = 0.9f),
            FilterPreset("dramatic_intense", "Intense Action", "Dramatic", contrastScale = 1.5f, saturationScale = 0.8f, redScale = 0.95f, greenScale = 1.05f, blueScale = 1.15f)
        )

        val CATEGORIES = listOf(
            "Original", "Cinematic", "Warm", "Cool", "Vintage", "Film", "Retro", "Black & White", "Moody", "Bright", "Dreamy", "Vlog", "Portrait", "Dramatic"
        )
    }
}
