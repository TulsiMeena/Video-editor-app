package com.example.data.model

data class ColorAdjustments(
    val brightness: Float = 0f,   // -100 to +100
    val contrast: Float = 0f,     // -100 to +100
    val saturation: Float = 0f,   // -100 to +100
    val exposure: Float = 0f,     // -100 to +100
    val temperature: Float = 0f,  // -100 to +100
    val tint: Float = 0f,         // -100 to +100
    val highlights: Float = 0f,   // -100 to +100
    val shadows: Float = 0f,      // -100 to +100
    val sharpen: Float = 0f,      // -100 to +100
    val fade: Float = 0f,         // -100 to +100
    val vignette: Float = 0f,     // -100 to +100
    val grain: Float = 0f         // -100 to +100
) {
    val isDefault: Boolean
        get() = brightness == 0f && contrast == 0f && saturation == 0f &&
                exposure == 0f && temperature == 0f && tint == 0f &&
                highlights == 0f && shadows == 0f && sharpen == 0f &&
                fade == 0f && vignette == 0f && grain == 0f
}
