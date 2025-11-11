package com.androidguitarnotes.app.notesplayed

/**
 * Helper class for fretboard calculations and note mapping.
 */
object FretboardHelper {
    private val NOTE_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    // Standard tuning open strings (from string 6 to string 1)
    private val OPEN_STRING_NOTES = listOf("E", "A", "D", "G", "B", "E")

    /**
     * Represents a position on the fretboard.
     */
    data class FretPosition(
        val stringNumber: Int, // 1-6 (1 = high E, 6 = low E)
        val fret: Int, // 0-24
    )

    /**
     * Get the note name at a specific fretboard position.
     */
    fun getNoteAtPosition(
        stringNumber: Int,
        fret: Int,
    ): String {
        val openNoteIndex = NOTE_NAMES.indexOf(OPEN_STRING_NOTES[6 - stringNumber])
        val noteIndex = (openNoteIndex + fret) % 12
        return NOTE_NAMES[noteIndex]
    }

    /**
     * Find all fretboard positions that match a given note name.
     * Returns positions up to the specified maximum fret.
     */
    fun findPositionsForNote(
        noteName: String,
        maxFret: Int = 12,
    ): List<FretPosition> {
        val positions = mutableListOf<FretPosition>()

        for (stringNumber in 1..6) {
            for (fret in 0..maxFret) {
                if (getNoteAtPosition(stringNumber, fret) == noteName) {
                    positions.add(FretPosition(stringNumber, fret))
                }
            }
        }

        return positions
    }

    /**
     * Find all fretboard positions that match a given note name with octave.
     * Returns positions up to the specified maximum fret.
     */
    fun findPositionsForNoteWithOctave(
        noteNameWithOctave: String,
        maxFret: Int = 12,
    ): List<FretPosition> {
        val positions = mutableListOf<FretPosition>()

        for (stringNumber in 1..6) {
            for (fret in 0..maxFret) {
                if (getNoteAtPositionWithOctave(stringNumber, fret) == noteNameWithOctave) {
                    positions.add(FretPosition(stringNumber, fret))
                }
            }
        }

        return positions
    }

    /**
     * Get the note name with octave at a specific fretboard position.
     */
    fun getNoteAtPositionWithOctave(
        stringNumber: Int,
        fret: Int,
    ): String {
        val openNoteIndex = NOTE_NAMES.indexOf(OPEN_STRING_NOTES[6 - stringNumber])
        val noteIndex = (openNoteIndex + fret) % 12
        val noteName = NOTE_NAMES[noteIndex]

        // Calculate octave based on open string and fret
        // Standard tuning: E2, A2, D3, G3, B3, E4
        // Order: [String 6 (low E), String 5 (A), String 4 (D), String 3 (G), String 2 (B), String 1 (high E)]
        val openOctaves = listOf(2, 2, 3, 3, 3, 4) // String 6 to String 1
        val baseOctave = openOctaves[6 - stringNumber]
        val octaveOffset = (openNoteIndex + fret) / 12
        val octave = baseOctave + octaveOffset

        return "$noteName$octave"
    }
}
