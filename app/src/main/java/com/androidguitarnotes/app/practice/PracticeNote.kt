package com.androidguitarnotes.app.practice

/**
 * Represents a specific note on the guitar fretboard.
 */
data class PracticeNote(
    val stringNumber: Int,  // 1-6 (1 is high E, 6 is low E)
    val fret: Int,          // 0-24
    val noteName: String    // e.g., "E", "F#", "A"
)
