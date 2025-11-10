package com.androidguitarnotes.app.tuner

/**
 * Represents a guitar string with its tuning information.
 */
data class GuitarString(
    val number: Int, // 1-6 (1 = high E, 6 = low E)
    val noteName: String, // E, A, D, G, B, E
    val octave: Int, // Octave number
    val frequency: Double, // Target frequency in Hz
) {
    companion object {
        /**
         * Standard guitar tuning (E A D G B E)
         */
        val STANDARD_TUNING =
            listOf(
                GuitarString(6, "E", 2, 82.41), // Low E
                GuitarString(5, "A", 2, 110.00), // A
                GuitarString(4, "D", 3, 146.83), // D
                GuitarString(3, "G", 3, 196.00), // G
                GuitarString(2, "B", 3, 246.94), // B
                GuitarString(1, "E", 4, 329.63), // High E
            )
    }
}
