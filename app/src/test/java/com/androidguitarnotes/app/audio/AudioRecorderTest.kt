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
        val audioData = floatArrayOf(0.1f, 0.2f, 0.3f)
        val level = 0.5f
        val result = AudioRecorder.AudioDataWithLevel(audioData, level)

        assertArrayEquals(audioData, result.audioData, 0.001f)
        assertEquals(0.5f, result.level, 0.001f)
    }

    @Test
    fun `AudioDataWithLevel level is within valid range`() {
        val audioData = floatArrayOf(0.1f, 0.2f, 0.3f)
        val level = 0.5f
        val result = AudioRecorder.AudioDataWithLevel(audioData, level)

        assertTrue("Level should be between 0 and 1", result.level >= 0f && result.level <= 1f)
    }

    @Test
    fun `AudioDataWithLevel equals compares content not reference`() {
        val audioData1 = floatArrayOf(0.1f, 0.2f, 0.3f)
        val audioData2 = floatArrayOf(0.1f, 0.2f, 0.3f)
        val result1 = AudioRecorder.AudioDataWithLevel(audioData1, 0.5f)
        val result2 = AudioRecorder.AudioDataWithLevel(audioData2, 0.5f)

        assertEquals("AudioDataWithLevel with same content should be equal", result1, result2)
    }

    @Test
    fun `AudioDataWithLevel hashCode is consistent`() {
        val audioData1 = floatArrayOf(0.1f, 0.2f, 0.3f)
        val audioData2 = floatArrayOf(0.1f, 0.2f, 0.3f)
        val result1 = AudioRecorder.AudioDataWithLevel(audioData1, 0.5f)
        val result2 = AudioRecorder.AudioDataWithLevel(audioData2, 0.5f)

        assertEquals("Equal objects should have equal hash codes", result1.hashCode(), result2.hashCode())
    }
}
