package com.androidguitarnotes.app.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for AudioManager.
 */
class AudioManagerTest {
    @Test
    fun `DetectedNote with detection contains all required fields`() {
        val result =
            AudioManager.DetectedNote(
                isDetected = true,
                noteName = "A",
                frequency = 440.0,
                cents = 0.0,
                confidence = 0.9f,
                audioLevel = 0.5f,
                octave = 4,
                noteNameWithOctave = "A4",
                isGated = false,
            )

        assertTrue(result.isDetected)
        assertEquals("A", result.noteName)
        assertEquals(440.0, result.frequency!!, 0.001)
        assertEquals(0.0, result.cents, 0.001)
        assertEquals(0.9f, result.confidence, 0.001f)
        assertEquals(0.5f, result.audioLevel, 0.001f)
        assertEquals(4, result.octave)
        assertEquals("A4", result.noteNameWithOctave)
        assertFalse(result.isGated)
    }

    @Test
    fun `DetectedNote without detection has default values`() {
        val result =
            AudioManager.DetectedNote(
                isDetected = false,
                noteName = "?",
                frequency = null,
                cents = 0.0,
                confidence = 0f,
                audioLevel = 0.3f,
                octave = -1,
                noteNameWithOctave = "?",
                isGated = false,
            )

        assertFalse(result.isDetected)
        assertEquals("?", result.noteName)
        assertNull(result.frequency)
        assertEquals(0.0, result.cents, 0.001)
        assertEquals(0f, result.confidence, 0.001f)
        assertEquals(0.3f, result.audioLevel, 0.001f)
        assertEquals(-1, result.octave)
        assertEquals("?", result.noteNameWithOctave)
        assertFalse(result.isGated)
    }

    @Test
    fun `DetectedNote gated signal has appropriate flags`() {
        val result =
            AudioManager.DetectedNote(
                isDetected = false,
                noteName = "?",
                frequency = null,
                cents = 0.0,
                confidence = 0f,
                audioLevel = 0.05f,
                octave = -1,
                noteNameWithOctave = "?",
                isGated = true,
            )

        assertFalse(result.isDetected)
        assertTrue(result.isGated)
        assertTrue("Gated result should indicate low signal", result.audioLevel < 0.5f)
    }

    @Test
    fun `DetectedNote uses -1 as sentinel for undetected octave`() {
        val detected =
            AudioManager.DetectedNote(
                isDetected = true,
                noteName = "C",
                frequency = 261.63,
                cents = 5.0,
                confidence = 0.8f,
                audioLevel = 0.7f,
                octave = 4,
                noteNameWithOctave = "C4",
                isGated = false,
            )
        val notDetected =
            AudioManager.DetectedNote(
                isDetected = false,
                noteName = "?",
                frequency = null,
                cents = 0.0,
                confidence = 0f,
                audioLevel = 0.2f,
                octave = -1,
                noteNameWithOctave = "?",
                isGated = false,
            )

        assertTrue("Detected note should have valid octave", detected.octave >= 0)
        assertEquals("Undetected note should use -1 sentinel", -1, notDetected.octave)
    }
}
