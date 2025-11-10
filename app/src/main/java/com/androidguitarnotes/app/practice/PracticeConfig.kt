package com.androidguitarnotes.app.practice

/**
 * Represents the configuration for a practice session.
 */
data class PracticeConfig(
    val selectedStrings: Set<Int> = setOf(1, 2, 3, 4, 5, 6), // String numbers 1-6
    val fretFrom: Int = 0,
    val fretTo: Int = 12,
    val noteMode: NoteMode = NoteMode.WHOLE_NOTES,
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
