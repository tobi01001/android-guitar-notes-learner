package com.androidguitarnotes.app.practice

import com.androidguitarnotes.app.notesplayed.FretboardHelper

/**
 * Generates random notes based on practice configuration.
 */
class RandomNoteGenerator(
    private val config: PracticeConfig,
) {
    // Chromatic scale (all semitones)
    private val chromaticScale =
        listOf(
            "C",
            "C#",
            "D",
            "D#",
            "E",
            "F",
            "F#",
            "G",
            "G#",
            "A",
            "A#",
            "B",
        )

    // Natural notes (whole notes without sharps/flats)
    private val naturalNotes = setOf("C", "D", "E", "F", "G", "A", "B")

    // Starting position of each open string in chromatic scale
    private val openStringPositions =
        mapOf(
            1 to 4, // E (String 1)
            2 to 11, // B (String 2)
            3 to 7, // G (String 3)
            4 to 2, // D (String 4)
            5 to 9, // A (String 5)
            6 to 4, // E (String 6)
        )

    // Track the last generated note to avoid repeats
    private var lastNote: PracticeNote? = null

    /**
     * Cached list of valid (string, fret) positions based on the configuration.
     * Computed once at initialization for better performance.
     */
    private val validPositions: List<Pair<Int, Int>> by lazy {
        val positions = mutableListOf<Pair<Int, Int>>()

        for (stringNumber in config.selectedStrings) {
            for (fret in config.fretFrom..config.fretTo) {
                val noteName = calculateNoteName(stringNumber, fret)
                if (isNoteAllowed(noteName)) {
                    positions.add(stringNumber to fret)
                }
            }
        }

        if (positions.isEmpty()) {
            throw IllegalStateException(
                "No valid note positions found for the selected configuration. Please check your scale, mode, and fret range.",
            )
        }
        positions
    }

    /**
     * Generates a random note based on the configuration.
     * Avoids repeating the same note consecutively if there are multiple positions available.
     */
    fun generateNote(): PracticeNote {
        // If there's only one valid position, we have to return it
        if (validPositions.size == 1) {
            val (stringNumber, fret) = validPositions.first()
            val noteName = calculateNoteName(stringNumber, fret)
            val noteNameWithOctave = FretboardHelper.getNoteAtPositionWithOctave(stringNumber, fret)
            val octave = extractOctave(noteNameWithOctave)
            val note = PracticeNote(stringNumber, fret, noteName, octave, noteNameWithOctave)
            lastNote = note
            return note
        }

        // Generate a new note that's different from the last one
        var (stringNumber, fret) = validPositions.random()
        var noteName = calculateNoteName(stringNumber, fret)
        var noteNameWithOctave = FretboardHelper.getNoteAtPositionWithOctave(stringNumber, fret)
        var octave = extractOctave(noteNameWithOctave)
        var newNote = PracticeNote(stringNumber, fret, noteName, octave, noteNameWithOctave)

        // If the new note is the same as the last one, keep trying (max 10 attempts)
        var attempts = 0
        while (lastNote != null && newNote == lastNote && attempts < 10) {
            val position = validPositions.random()
            stringNumber = position.first
            fret = position.second
            noteName = calculateNoteName(stringNumber, fret)
            noteNameWithOctave = FretboardHelper.getNoteAtPositionWithOctave(stringNumber, fret)
            octave = extractOctave(noteNameWithOctave)
            newNote = PracticeNote(stringNumber, fret, noteName, octave, noteNameWithOctave)
            attempts++
        }

        lastNote = newNote
        return newNote
    }

    /**
     * Extracts the octave number from a note name with octave (e.g., "E4" -> 4).
     */
    private fun extractOctave(noteNameWithOctave: String): Int = noteNameWithOctave.takeLastWhile { it.isDigit() }.toIntOrNull() ?: 0

    /**
     * Checks if a note is allowed based on the current note mode.
     */
    private fun isNoteAllowed(noteName: String): Boolean =
        when (config.noteMode) {
            NoteMode.SEMITONES -> true // All notes allowed
            NoteMode.WHOLE_NOTES -> noteName in naturalNotes
            NoteMode.SCALE -> noteName in config.selectedScale.notes
        }

    /**
     * Calculates the note name for a given string and fret.
     */
    private fun calculateNoteName(
        stringNumber: Int,
        fret: Int,
    ): String {
        val openPosition = openStringPositions[stringNumber] ?: 0
        val chromaticPosition = (openPosition + fret) % 12
        return chromaticScale[chromaticPosition]
    }
}
