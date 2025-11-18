package com.androidguitarnotes.app.ui

import androidx.compose.ui.graphics.Color

/**
 * Color scheme for musical notes.
 * Each natural note (A-G) has a distinct color, and semitones have blended colors.
 */
object NoteColors {
    // Natural note colors - using a color wheel approach
    private val A_COLOR = Color(0xFFFF1744)
    private val B_COLOR = Color(0xFF2979FF)
    private val C_COLOR = Color(0xFFD500F9)
    private val D_COLOR = Color(0xFF651FFF)
    private val E_COLOR = Color(0xFFFFC400)
    private val F_COLOR = Color(0xFFFF3D00)
    private val G_COLOR = Color(0xFF00E676)

    private val BackgroundOverlayColor = Color.Black.copy(alpha = 0.8f)

    /**
     * Returns the color for a given note name.
     * Semitones (sharp notes) return a blend of the adjacent natural note colors.
     */
    fun getColorForNote(noteName: String): Color =
        when (noteName) {
            "A" -> A_COLOR
            "A#" -> blendColors(A_COLOR, B_COLOR)
            "B" -> B_COLOR
            "C" -> C_COLOR
            "C#" -> blendColors(C_COLOR, D_COLOR)
            "D" -> D_COLOR
            "D#" -> blendColors(D_COLOR, E_COLOR)
            "E" -> E_COLOR
            "F" -> F_COLOR
            "F#" -> blendColors(F_COLOR, G_COLOR)
            "G" -> G_COLOR
            "G#" -> blendColors(G_COLOR, A_COLOR)
            else -> Color.Gray // Default for unrecognized notes
        }

    /**
     * Blends two colors by averaging their RGB components.
     */
    private fun blendColors(
        color1: Color,
        color2: Color,
    ): Color =
        Color(
            red = (color1.red + color2.red) / 2f,
            green = (color1.green + color2.green) / 2f,
            blue = (color1.blue + color2.blue) / 2f,
            alpha = 1f,
        )

    /**
     * Returns a lighter variant of the note color (for backgrounds).
     */
    fun getLightColorForNote(noteName: String): Color {
        val baseColor = getColorForNote(noteName)
        // Blend the base color with white to get a lighter, fully opaque color
        val blendRatio = 0.7f // 70% base color, 30% white
        return Color(
            red = baseColor.red * blendRatio + (1f - blendRatio) * 1f,
            green = baseColor.green * blendRatio + (1f - blendRatio) * 1f,
            blue = baseColor.blue * blendRatio + (1f - blendRatio) * 1f,
            alpha = 1f,
        )
    }

    /**
     * Returns a darker variant of the note color (for borders/emphasis).
     */
    fun getDarkColorForNote(noteName: String): Color {
        val baseColor = getColorForNote(noteName)
        return Color(
            red = baseColor.red * 0.7f,
            green = baseColor.green * 0.7f,
            blue = baseColor.blue * 0.7f,
            alpha = 1f,
        )
    }

    /**
     * Returns an accessible button color with good contrast against white text.
     * For colors with poor contrast (like yellow), returns a more accessible variant.
     */
    fun getAccessibleButtonColorFor(buttonName: String): Color =
        when (buttonName) {
            "Settings" -> getDarkColorForNote("F")
            "Practice" -> getDarkColorForNote("A")
            "Tuner" -> getDarkColorForNote("D")
            "Notes Played" -> getDarkColorForNote("G")
            else -> getDarkColorForNote("C")
        }

    fun getBackgroundOverlayColor(): Color = BackgroundOverlayColor

}
