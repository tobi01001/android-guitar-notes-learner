package com.androidguitarnotes.app.practice

/**
 * Represents the configuration for a practice session.
 */
data class PracticeConfig(
    val selectedStrings: Set<Int> = setOf(1, 2, 3, 4, 5, 6), // String numbers 1-6
    val fretFrom: Int = 0,
    val fretTo: Int = 12,
    val noteMode: NoteMode = NoteMode.WHOLE_NOTES,
    val selectedScale: Scale = Scale.C_MAJOR,
    val durationType: DurationType = DurationType.TIME,
    val durationMinutes: Int = 5,
    val noteCount: Int = 20,
    val progressionMode: ProgressionMode = ProgressionMode.MANUAL,
    val autoIntervalSeconds: Float = 3.0f, // Used when progressionMode is AUTO_INTERVAL
)

/**
 * Defines the mode for note selection in practice.
 */
enum class NoteMode {
    SCALE,
    WHOLE_NOTES,
    SEMITONES,
}

/**
 * Defines whether practice duration is time-based or count-based.
 */
enum class DurationType {
    TIME,
    COUNT,
}

/**
 * Represents a musical scale with its notes.
 */
enum class Scale(val notes: List<String>) {
    C_MAJOR(listOf("C", "D", "E", "F", "G", "A", "B")),
    G_MAJOR(listOf("G", "A", "B", "C", "D", "E", "F#")),
    A_MINOR(listOf("A", "B", "C", "D", "E", "F", "G")),
    E_MINOR(listOf("E", "F#", "G", "A", "B", "C", "D")),
 * Defines how progression through notes occurs during practice.
 */
enum class ProgressionMode {
    /**
     * User manually clicks "Next Note" button to advance.
     */
    MANUAL,
    
    /**
     * Automatically advances to next note when correct note is detected via audio.
     */
    AUDIO_VERIFICATION,
    
    /**
     * Automatically advances to next note after a configured time interval.
     */
    AUTO_INTERVAL,
}
