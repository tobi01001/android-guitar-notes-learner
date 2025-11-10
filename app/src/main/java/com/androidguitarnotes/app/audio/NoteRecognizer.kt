package com.androidguitarnotes.app.audio

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round

/**
 * Recognizes musical notes from detected frequencies.
 */
class NoteRecognizer {
    companion object {
        // A4 = 440 Hz (standard tuning reference)
        private const val A4_FREQUENCY = 440.0
        private const val A4_MIDI_NOTE = 69

        // Note names in chromatic scale
        private val NOTE_NAMES =
            arrayOf(
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

        // Threshold for considering a note match (in cents)
        private const val MATCH_THRESHOLD_CENTS = 50.0 // ±50 cents (~half semitone)
    }

    /**
     * Data class representing a recognized note.
     */
    data class RecognizedNote(
        val noteName: String,
        val frequency: Double,
        val cents: Double, // Deviation from perfect pitch in cents
    )

    /**
     * Converts frequency to MIDI note number.
     */
    private fun frequencyToMidi(frequency: Double): Double = 69.0 + 12.0 * log2(frequency / A4_FREQUENCY)

    /**
     * Converts MIDI note number to note name.
     */
    private fun midiToNoteName(midiNote: Int): String = NOTE_NAMES[midiNote % 12]

    /**
     * Calculates cents deviation from nearest note.
     */
    private fun calculateCents(
        actualMidi: Double,
        targetMidi: Int,
    ): Double = (actualMidi - targetMidi) * 100.0

    /**
     * Recognizes a note from a detected frequency.
     */
    fun recognizeNote(frequency: Double): RecognizedNote {
        val midiNote = frequencyToMidi(frequency)
        val nearestMidi = round(midiNote).toInt()
        val cents = calculateCents(midiNote, nearestMidi)
        val noteName = midiToNoteName(nearestMidi)

        return RecognizedNote(
            noteName = noteName,
            frequency = frequency,
            cents = cents,
        )
    }

    /**
     * Checks if detected note matches expected note within threshold.
     */
    fun matchesNote(
        detectedFrequency: Double,
        expectedNoteName: String,
    ): Boolean {
        val recognized = recognizeNote(detectedFrequency)

        // Compare note name (ignoring octave)
        if (recognized.noteName != expectedNoteName) {
            return false
        }

        // Check if within acceptable cents range
        return abs(recognized.cents) <= MATCH_THRESHOLD_CENTS
    }

    /**
     * Gets the expected frequency for a note name (assumes middle octave).
     * This is a simplified version - in practice, we'd need octave information.
     */
    fun getNoteFrequency(
        noteName: String,
        octave: Int = 4,
    ): Double {
        val noteIndex = NOTE_NAMES.indexOf(noteName)
        if (noteIndex == -1) return 0.0

        val midiNote = (octave + 1) * 12 + noteIndex
        return A4_FREQUENCY * 2.0.pow((midiNote - A4_MIDI_NOTE) / 12.0)
    }
}
