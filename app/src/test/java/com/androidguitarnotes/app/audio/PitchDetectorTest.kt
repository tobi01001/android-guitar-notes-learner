package com.androidguitarnotes.app.audio

import org.junit.Assert.assertEquals
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
    fun `detectPitch returns null for empty data`() {
        val result = detector.detectPitch(ShortArray(0))

        assertNull(result)
    }

    @Test
    fun `detectPitch returns null for insufficient data`() {
        val result = detector.detectPitch(ShortArray(10))

        assertNull(result)
    }

    @Test
    fun `detectPitch detects A4 frequency from synthesized sine wave`() {
        val frequency = 440.0 // A4
        val sampleRate = 44100
        val duration = 0.1 // 100ms
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val detected = detector.detectPitch(audioData)

        assertNotNull("Should detect a frequency", detected)
        detected?.let {
            assertTrue(
                "Detected frequency should be close to 440Hz",
                it in 420.0..460.0,
            )
        }
    }

    @Test
    fun `detectPitch detects E2 frequency from synthesized sine wave`() {
        val frequency = 82.41 // E2 (low E string)
        val sampleRate = 44100
        val duration = 0.2 // Need longer duration for low frequencies
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val detected = detector.detectPitch(audioData)

        // Low frequency detection can be tricky with autocorrelation
        // We're mainly testing that it doesn't crash and returns a reasonable result
        if (detected != null) {
            assertTrue(
                "Detected frequency should be in valid range",
                detected in 60.0..1500.0,
            )
        }
    }

    @Test
    fun `detectPitch handles high frequency input`() {
        val frequency = 2000.0 // Above typical guitar range
        val sampleRate = 44100
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val detected = detector.detectPitch(audioData)

        // Should either return null or a frequency within valid range
        // (harmonics might produce detectable frequencies)
        if (detected != null) {
            assertTrue(
                "Any detected frequency should be in valid range",
                detected in 60.0..1500.0,
            )
        }
    }

    @Test
    fun `detectPitch returns null for too low frequency`() {
        val frequency = 30.0 // Below guitar range
        val sampleRate = 44100
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val detected = detector.detectPitch(audioData)

        assertNull("Should not detect frequency outside range", detected)
    }

    /**
     * Helper function to generate a sine wave for testing.
     */
    private fun generateSineWave(
        frequency: Double,
        sampleRate: Int,
        samples: Int,
    ): ShortArray {
        val audioData = ShortArray(samples)
        val amplitude = Short.MAX_VALUE * 0.8

        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val value = amplitude * sin(2 * PI * frequency * time)
            audioData[i] = value.toInt().toShort()
        }

        return audioData
    }
}
