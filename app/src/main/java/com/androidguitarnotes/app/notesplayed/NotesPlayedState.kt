package com.androidguitarnotes.app.notesplayed

/**
 * Represents the UI state for the Notes Played screen.
 */
data class NotesPlayedState(
    val isListening: Boolean = false,
    val detectedNote: DetectedNoteInfo? = null,
)

/**
 * Information about a detected note.
 */
data class DetectedNoteInfo(
    val noteName: String,
    val frequency: Double,
    val cents: Double,
    val octave: Int,
    val noteNameWithOctave: String,
)
