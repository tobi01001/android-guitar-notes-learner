package com.androidguitarnotes.app.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for PitchConfirmationFilter.
 *
 * Tests the multi-frame confirmation logic as specified in ENH-001.
 */
class PitchConfirmationFilterTest {
    /**
     * Simple detection data class for testing.
     */
    private data class TestDetection(
        val noteName: String,
        val octave: Int,
        val frequency: Double,
    )

    private val noteExtractor: (TestDetection) -> Pair<String, Int> = { detection ->
        Pair(detection.noteName, detection.octave)
    }

    @Test
    fun `confirmation requires consecutive matching frames with default 2 frames`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)

        val noteE = TestDetection("E", 4, 329.63)

        // First frame - not yet confirmed
        assertNull(filter.confirm(noteE, noteExtractor))

        // Second frame - confirmed!
        assertEquals(noteE, filter.confirm(noteE, noteExtractor))
    }

    @Test
    fun `confirmation requires consecutive matching frames with 3 frames`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 3)

        val noteA = TestDetection("A", 4, 440.0)

        // First frame
        assertNull(filter.confirm(noteA, noteExtractor))

        // Second frame
        assertNull(filter.confirm(noteA, noteExtractor))

        // Third frame - confirmed!
        assertEquals(noteA, filter.confirm(noteA, noteExtractor))
    }

    @Test
    fun `mismatch resets confirmation`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)

        val noteE = TestDetection("E", 4, 329.63)
        val noteA = TestDetection("A", 3, 220.00)

        // First frame
        assertNull(filter.confirm(noteE, noteExtractor))

        // Mismatch - resets buffer
        assertNull(filter.confirm(noteA, noteExtractor))

        // Back to first frame for E
        assertNull(filter.confirm(noteE, noteExtractor))

        // Second frame for E - confirmed
        assertEquals(noteE, filter.confirm(noteE, noteExtractor))
    }

    @Test
    fun `null detection requires consecutive null frames`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)

        // First null frame
        assertNull(filter.confirm(null, noteExtractor))

        // Second null frame - confirmed silence
        assertNull(filter.confirm(null, noteExtractor))
    }

    @Test
    fun `null detection between notes resets confirmation`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)

        val noteE = TestDetection("E", 4, 329.63)

        // First E frame
        assertNull(filter.confirm(noteE, noteExtractor))

        // Silence - resets
        assertNull(filter.confirm(null, noteExtractor))

        // Back to first frame for E
        assertNull(filter.confirm(noteE, noteExtractor))

        // Second frame for E - confirmed
        assertEquals(noteE, filter.confirm(noteE, noteExtractor))
    }

    @Test
    fun `same note name different octave is treated as different note`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)

        val noteE4 = TestDetection("E", 4, 329.63)
        val noteE3 = TestDetection("E", 3, 164.81)

        // First E4 frame
        assertNull(filter.confirm(noteE4, noteExtractor))

        // E3 (different octave) - resets
        assertNull(filter.confirm(noteE3, noteExtractor))

        // Back to first frame for E4
        assertNull(filter.confirm(noteE4, noteExtractor))
    }

    @Test
    fun `same note with slight frequency variation is confirmed`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)

        // Same note E4 with slightly different frequencies (within same semitone)
        val noteE1 = TestDetection("E", 4, 329.63)
        val noteE2 = TestDetection("E", 4, 330.50) // Slightly sharp but still E4

        // First frame
        assertNull(filter.confirm(noteE1, noteExtractor))

        // Second frame with slight frequency variation - should still confirm
        assertEquals(noteE2, filter.confirm(noteE2, noteExtractor))
    }

    @Test
    fun `reset clears confirmation buffer`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)

        val noteE = TestDetection("E", 4, 329.63)

        // First frame
        assertNull(filter.confirm(noteE, noteExtractor))

        // Reset
        filter.reset()

        // Should need two frames again
        assertNull(filter.confirm(noteE, noteExtractor))
        assertEquals(noteE, filter.confirm(noteE, noteExtractor))
    }

    @Test
    fun `buffer size increases up to required frames`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 3)

        val noteA = TestDetection("A", 4, 440.0)

        assertEquals(0, filter.getBufferSize())

        filter.confirm(noteA, noteExtractor)
        assertEquals(1, filter.getBufferSize())

        filter.confirm(noteA, noteExtractor)
        assertEquals(2, filter.getBufferSize())

        filter.confirm(noteA, noteExtractor)
        assertEquals(3, filter.getBufferSize())

        // Buffer shouldn't grow beyond required frames
        filter.confirm(noteA, noteExtractor)
        assertEquals(3, filter.getBufferSize())
    }

    @Test
    fun `reset clears buffer size`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)

        val noteE = TestDetection("E", 4, 329.63)

        filter.confirm(noteE, noteExtractor)
        assertEquals(1, filter.getBufferSize())

        filter.reset()
        assertEquals(0, filter.getBufferSize())
    }

    @Test
    fun `confirmed detection continues to confirm on subsequent frames`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)

        val noteA = TestDetection("A", 4, 440.0)

        // First two frames to confirm
        assertNull(filter.confirm(noteA, noteExtractor))
        assertEquals(noteA, filter.confirm(noteA, noteExtractor))

        // Subsequent frames should continue to confirm
        assertEquals(noteA, filter.confirm(noteA, noteExtractor))
        assertEquals(noteA, filter.confirm(noteA, noteExtractor))
    }

    @Test
    fun `single frame requirement confirms immediately`() {
        val filter = PitchConfirmationFilter(requiredConsecutiveFrames = 1)

        val noteE = TestDetection("E", 4, 329.63)

        // With single frame requirement, should confirm immediately
        assertEquals(noteE, filter.confirm(noteE, noteExtractor))
    }
}
