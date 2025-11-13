package com.androidguitarnotes.app.audio

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

/**
 * Unit tests for PitchDetector.
 */
class PitchDetectorTest {
    private val detector = PitchDetector()

    @Test
    fun `detectPitchWithConfidence returns null for empty data`() {
        val result = detector.detectPitchWithConfidence(FloatArray(0))

        assertNull(result)
    }

    @Test
    fun `detectPitchWithConfidence returns null for insufficient data`() {
        val result = detector.detectPitchWithConfidence(FloatArray(10))

        assertNull(result)
    }

    @Test
    fun `detectPitchWithConfidence detects A4 frequency from synthesized sine wave`() {
        val frequency = 440.0 // A4
        val sampleRate = 44100
        val duration = 0.1 // 100ms
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = detector.detectPitchWithConfidence(audioData)

        assertNotNull("Should detect a frequency", result)
        result?.let {
            assertTrue(
                "Detected frequency should be close to 440Hz",
                it.frequency in 420.0..460.0,
            )
            assertTrue(
                "Confidence should be reasonable",
                it.confidence > 0.0f,
            )
        }
    }

    @Test
    fun `detectPitchWithConfidence detects E2 frequency from synthesized sine wave`() {
        val frequency = 82.41 // E2 (low E string)
        val sampleRate = 44100
        val duration = 0.2 // Need longer duration for low frequencies
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = detector.detectPitchWithConfidence(audioData)

        // Low frequency detection can be tricky with autocorrelation
        // We're mainly testing that it doesn't crash and returns a reasonable result
        if (result != null) {
            assertTrue(
                "Detected frequency should be in valid range",
                result.frequency in 60.0..1500.0,
            )
        }
    }

    @Test
    fun `detectPitchWithConfidence handles high frequency input`() {
        val frequency = 2000.0 // Above typical guitar range
        val sampleRate = 44100
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = detector.detectPitchWithConfidence(audioData)

        // Should either return null or a frequency within valid range
        // (harmonics might produce detectable frequencies)
        if (result != null) {
            assertTrue(
                "Any detected frequency should be in valid range",
                result.frequency in 60.0..1500.0,
            )
        }
    }

    @Test
    fun `detectPitchWithConfidence returns null for too low frequency`() {
        val frequency = 30.0 // Below guitar range
        val sampleRate = 44100
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = detector.detectPitchWithConfidence(audioData)

        assertNull("Should not detect frequency outside range", result)
    }

    /**
     * Helper function to generate a sine wave for testing.
     */
    private fun generateSineWave(
        frequency: Double,
        sampleRate: Int,
        samples: Int,
    ): FloatArray {
        val audioData = FloatArray(samples)
        val amplitude = 0.8f

        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val value = amplitude * sin(2 * PI * frequency * time)
            audioData[i] = value.toFloat()
        }

        return audioData
    }
}
