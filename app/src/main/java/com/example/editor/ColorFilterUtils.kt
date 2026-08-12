package com.example.editor

import androidx.compose.ui.graphics.ColorMatrix
import com.example.data.model.ColorAdjustments
import com.example.data.model.FilterPreset

object ColorFilterUtils {

    /**
     * Builds a combined 4x5 float array matrix for Compose and Canvas ColorMatrix.
     */
    fun createCombinedColorMatrix(
        adjustments: ColorAdjustments,
        filterName: String,
        filterIntensity: Float
    ): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Filter Preset Matrix
        val preset = FilterPreset.ALL_FILTERS.firstOrNull { it.name.equals(filterName, ignoreCase = true) || it.id.equals(filterName, ignoreCase = true) }
        if (preset != null && filterIntensity > 0f && preset.id != "original") {
            val presetMatrix = createPresetMatrix(preset, filterIntensity)
            matrix.set(presetMatrix)
        }

        // 2. Adjustments Matrix
        if (!adjustments.isDefault) {
            val adjMatrix = createAdjustmentsMatrix(adjustments)
            matrix.timesAssign(adjMatrix)
        }

        return matrix
    }

    private fun createPresetMatrix(preset: FilterPreset, intensity: Float): ColorMatrix {
        val cm = ColorMatrix()
        if (preset.isMonochrome) {
            val mono = ColorMatrix()
            mono.setToSaturation(0f)
            val blendedMono = blendMatrix(ColorMatrix(), mono, intensity)
            cm.timesAssign(blendedMono)
        }

        if (preset.saturationScale != 1f) {
            val sat = ColorMatrix()
            val targetSat = 1f + (preset.saturationScale - 1f) * intensity
            sat.setToSaturation(targetSat.coerceIn(0f, 3f))
            cm.timesAssign(sat)
        }

        // RGB Scales & Brightness Offset
        val rScale = 1f + (preset.redScale - 1f) * intensity
        val gScale = 1f + (preset.greenScale - 1f) * intensity
        val bScale = 1f + (preset.blueScale - 1f) * intensity
        val bOffset = preset.brightnessOffset * intensity

        val rgbMatrix = ColorMatrix(
            floatArrayOf(
                rScale * preset.contrastScale, 0f, 0f, 0f, bOffset,
                0f, gScale * preset.contrastScale, 0f, 0f, bOffset,
                0f, 0f, bScale * preset.contrastScale, 0f, bOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.timesAssign(rgbMatrix)

        if (preset.sepiaFactor > 0f) {
            val sepiaCoeff = preset.sepiaFactor * intensity
            val sepia = ColorMatrix(
                floatArrayOf(
                    1f - sepiaCoeff + sepiaCoeff * 0.393f, sepiaCoeff * 0.769f, sepiaCoeff * 0.189f, 0f, 0f,
                    sepiaCoeff * 0.349f, 1f - sepiaCoeff + sepiaCoeff * 0.686f, sepiaCoeff * 0.168f, 0f, 0f,
                    sepiaCoeff * 0.272f, sepiaCoeff * 0.534f, 1f - sepiaCoeff + sepiaCoeff * 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.timesAssign(sepia)
        }

        return cm
    }

    private fun createAdjustmentsMatrix(adj: ColorAdjustments): ColorMatrix {
        val cm = ColorMatrix()

        // Saturation (-100 to +100 -> 0.0 to 2.0)
        if (adj.saturation != 0f) {
            val satValue = ((adj.saturation + 100f) / 100f).coerceIn(0f, 2.5f)
            val satCM = ColorMatrix()
            satCM.setToSaturation(satValue)
            cm.timesAssign(satCM)
        }

        // Brightness (-100 to +100 -> -255 to +255 offset)
        // Exposure (-100 to +100)
        val totalBrightness = (adj.brightness * 1.5f + adj.exposure * 2.0f)

        // Contrast (-100 to +100 -> scale 0.2 to 2.2)
        val contrastFactor = if (adj.contrast >= 0) {
            1f + (adj.contrast / 100f) * 1.2f
        } else {
            1f + (adj.contrast / 100f) * 0.7f
        }.coerceIn(0.1f, 3f)

        // Temperature (-100 -> Cool, +100 -> Warm)
        val tempR = 1f + (adj.temperature / 100f) * 0.25f
        val tempB = 1f - (adj.temperature / 100f) * 0.25f

        // Tint (-100 -> Green, +100 -> Magenta)
        val tintG = 1f - (adj.tint / 100f) * 0.2f
        val tintM = 1f + (adj.tint / 100f) * 0.15f

        // Highlights / Shadows
        val highFactor = 1f + (adj.highlights / 100f) * 0.2f
        val shadowOffset = (adj.shadows / 100f) * 20f

        // Fade (-100 to +100)
        val fadeOffset = (adj.fade / 100f).coerceAtLeast(0f) * 30f

        val r = tempR * contrastFactor * highFactor
        val g = tintG * contrastFactor * highFactor
        val b = tempB * tintM * contrastFactor * highFactor
        val offset = totalBrightness + shadowOffset + fadeOffset

        val customCM = ColorMatrix(
            floatArrayOf(
                r, 0f, 0f, 0f, offset,
                0f, g, 0f, 0f, offset,
                0f, 0f, b, 0f, offset,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.timesAssign(customCM)

        return cm
    }

    private fun blendMatrix(from: ColorMatrix, to: ColorMatrix, fraction: Float): ColorMatrix {
        val fArray = from.values
        val tArray = to.values
        val resultArray = FloatArray(20)
        for (i in 0 until 20) {
            resultArray[i] = fArray[i] + (tArray[i] - fArray[i]) * fraction
        }
        return ColorMatrix(resultArray)
    }

    /**
     * Convert Compose ColorMatrix to Android android.graphics.ColorMatrix for Canvas / Paint.
     */
    fun toAndroidColorMatrix(composeMatrix: ColorMatrix): android.graphics.ColorMatrix {
        return android.graphics.ColorMatrix(composeMatrix.values)
    }
}
