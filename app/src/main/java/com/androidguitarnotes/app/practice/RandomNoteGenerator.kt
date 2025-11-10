package com.androidguitarnotes.app.practice

import kotlin.random.Random

/**
 * Generates random notes based on practice configuration.
 */
class RandomNoteGenerator(private val config: PracticeConfig) {
    
    // Standard tuning notes for each string (open string)
    // String 1 (high E) to String 6 (low E)
    private val openStringNotes = listOf(
        "E",  // String 1
        "B",  // String 2
        "G",  // String 3
        "D",  // String 4
        "A",  // String 5
        "E"   // String 6
    )
    
    // Chromatic scale (all semitones)
    private val chromaticScale = listOf(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    )
    
    // Whole notes only (no sharps/flats)
    private val wholeNotes = listOf(
        "C", "D", "E", "F", "G", "A", "B"
    )
    
    // Starting position of each open string in chromatic scale
    private val openStringPositions = mapOf(
        1 to 4,  // E (String 1)
        2 to 11, // B (String 2)
        3 to 7,  // G (String 3)
        4 to 2,  // D (String 4)
        5 to 9,  // A (String 5)
        6 to 4   // E (String 6)
    )
    
    /**
     * Generates a random note based on the configuration.
     */
    fun generateNote(): PracticeNote {
        // Select random string from configured strings
        val stringNumber = config.selectedStrings.random()
        
        // Select random fret from configured range
        val fret = Random.nextInt(config.fretFrom, config.fretTo + 1)
        
        // Calculate note name based on string, fret, and note mode
        val noteName = calculateNoteName(stringNumber, fret)
        
        return PracticeNote(stringNumber, fret, noteName)
    }
    
    /**
     * Calculates the note name for a given string and fret.
     */
    private fun calculateNoteName(stringNumber: Int, fret: Int): String {
        val openPosition = openStringPositions[stringNumber] ?: 0
        val chromaticPosition = (openPosition + fret) % 12
        val chromaticNote = chromaticScale[chromaticPosition]
        
        return when (config.noteMode) {
            NoteMode.SEMITONES -> chromaticNote
            NoteMode.WHOLE_NOTES -> {
                // Map sharps to their enharmonic flat equivalents for whole notes mode
                // In practice, for simplicity, we just return the chromatic note
                // A proper implementation would map: C#->Db, D#->Eb, F#->Gb, G#->Ab, A#->Bb
                // For now, return the chromatic note and let the UI handle display
                chromaticNote
            }
            NoteMode.SCALE -> {
                // For now, treat scale mode same as chromatic
                // TODO: Implement actual scale selection based on key and scale type
                chromaticNote
            }
        }
    }
}
