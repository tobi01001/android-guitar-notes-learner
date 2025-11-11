package com.androidguitarnotes.app.audio

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AudioRecorder utility functions.
 * Note: Cannot test actual recording without Android runtime.
 */
class AudioRecorderTest {
    @Test
    fun `AudioDataWithLevel contains audio data and level`() {
        val audioData = shortArrayOf(100, 200, 300)
        val level = 0.5f
        val result = AudioRecorder.AudioDataWithLevel(audioData, level)

        assertArrayEquals(audioData, result.audioData)
        assertEquals(0.5f, result.level, 0.001f)
    }

    @Test
    fun `AudioDataWithLevel level is within valid range`() {
        val audioData = shortArrayOf(100, 200, 300)
        val level = 0.5f
        val result = AudioRecorder.AudioDataWithLevel(audioData, level)

        assertTrue("Level should be between 0 and 1", result.level >= 0f && result.level <= 1f)
    }
}
