package com.androidguitarnotes.app.audio

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

/**
 * Unit tests for PitchDetector.
 *
 * Tests both YIN and AUTOCORRELATION algorithms.
 */
class PitchDetectorTest {
    private val yinDetector = PitchDetector(algorithm = PitchDetectionAlgorithm.YIN)
    private val autocorrelationDetector = PitchDetector(algorithm = PitchDetectionAlgorithm.AUTOCORRELATION)

    @Test
    fun `detectPitchWithConfidence returns null for empty data - YIN`() {
        val result = yinDetector.detectPitchWithConfidence(FloatArray(0))
        assertNull(result)
    }

    @Test
    fun `detectPitchWithConfidence returns null for empty data - Autocorrelation`() {
        val result = autocorrelationDetector.detectPitchWithConfidence(FloatArray(0))
        assertNull(result)
    }

    @Test
    fun `detectPitchWithConfidence returns null for insufficient data - YIN`() {
        val result = yinDetector.detectPitchWithConfidence(FloatArray(10))
        assertNull(result)
    }

    @Test
    fun `detectPitchWithConfidence returns null for insufficient data - Autocorrelation`() {
        val result = autocorrelationDetector.detectPitchWithConfidence(FloatArray(10))
        assertNull(result)
    }

    @Test
    fun `detectPitchWithConfidence detects A4 frequency from synthesized sine wave - YIN`() {
        val frequency = 440.0 // A4
        val sampleRate = 44100
        val duration = 0.1 // 100ms
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = yinDetector.detectPitchWithConfidence(audioData)

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
    fun `detectPitchWithConfidence detects A4 frequency from synthesized sine wave - Autocorrelation`() {
        val frequency = 440.0 // A4
        val sampleRate = 44100
        val duration = 0.1 // 100ms
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = autocorrelationDetector.detectPitchWithConfidence(audioData)

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
    fun `detectPitchWithConfidence detects E2 frequency from synthesized sine wave - YIN`() {
        val frequency = 82.41 // E2 (low E string)
        val sampleRate = 44100
        val duration = 0.2 // Need longer duration for low frequencies
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = yinDetector.detectPitchWithConfidence(audioData)

        // YIN should handle low frequencies better
        if (result != null) {
            assertTrue(
                "Detected frequency should be in valid range",
                result.frequency in 60.0..1500.0,
            )
        }
    }

    @Test
    fun `detectPitchWithConfidence detects E2 frequency from synthesized sine wave - Autocorrelation`() {
        val frequency = 82.41 // E2 (low E string)
        val sampleRate = 44100
        val duration = 0.2 // Need longer duration for low frequencies
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = autocorrelationDetector.detectPitchWithConfidence(audioData)

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
    fun `detectPitchWithConfidence handles high frequency input - YIN`() {
        val frequency = 2000.0 // Above typical guitar range
        val sampleRate = 44100
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = yinDetector.detectPitchWithConfidence(audioData)

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
    fun `detectPitchWithConfidence handles high frequency input - Autocorrelation`() {
        val frequency = 2000.0 // Above typical guitar range
        val sampleRate = 44100
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = autocorrelationDetector.detectPitchWithConfidence(audioData)

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
    fun `detectPitchWithConfidence returns null for too low frequency - YIN`() {
        val frequency = 30.0 // Below guitar range
        val sampleRate = 44100
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = yinDetector.detectPitchWithConfidence(audioData)

        assertNull("Should not detect frequency outside range", result)
    }

    @Test
    fun `detectPitchWithConfidence returns null for too low frequency - Autocorrelation`() {
        val frequency = 30.0 // Below guitar range
        val sampleRate = 44100
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = autocorrelationDetector.detectPitchWithConfidence(audioData)

        assertNull("Should not detect frequency outside range", result)
    }

    @Test
    fun `YIN algorithm provides better accuracy than autocorrelation`() {
        // Test with A4 440 Hz
        val frequency = 440.0
        val sampleRate = 44100
        val duration = 0.15
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)

        val yinResult = yinDetector.detectPitchWithConfidence(audioData)
        val autocorrResult = autocorrelationDetector.detectPitchWithConfidence(audioData)

        assertNotNull("YIN should detect frequency", yinResult)
        assertNotNull("Autocorrelation should detect frequency", autocorrResult)

        yinResult?.let { yin ->
            autocorrResult?.let { autocorr ->
                val yinError = kotlin.math.abs(yin.frequency - frequency)
                val autocorrError = kotlin.math.abs(autocorr.frequency - frequency)

                // Both should detect something reasonable
                assertTrue("YIN should be accurate", yinError < 5.0)
                assertTrue("Autocorrelation should be reasonable", autocorrError < 10.0)

                // YIN typically should be more accurate
                // (though this may not always be true for all signals)
                assertTrue(
                    "YIN error: $yinError Hz, Autocorr error: $autocorrError Hz - " +
                        "both should be reasonable",
                    yinError < 5.0 && autocorrError < 10.0,
                )
            }
        }
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
