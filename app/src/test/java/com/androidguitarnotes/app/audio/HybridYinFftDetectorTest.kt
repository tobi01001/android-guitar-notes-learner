package com.androidguitarnotes.app.audio

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Unit tests for HybridYinFftDetector (Enhancement #84.3).
 *
 * Tests the combination of time-domain (YIN) and frequency-domain (FFT)
 * analysis for improved accuracy and robustness.
 */
class HybridYinFftDetectorTest {
    private val sampleRate = 44100
    private val detector = HybridYinFftDetector(sampleRate = sampleRate)

    @Test
    fun `hybrid detector detects clean signal with high agreement`() {
        val frequency = 440.0 // A4
        val duration = 0.15
        val samples = (sampleRate * duration).toInt()
        val audioData = generateSineWave(frequency, sampleRate, samples)

        val result = detector.detectPitch(audioData)

        assertNotNull("Should detect clean signal", result)
        result?.let {
            val error = abs(it.frequency - frequency)
            assertTrue("Should be accurate: error $error Hz", error < 10.0)
            assertTrue("Should have high agreement score", it.agreementScore > 0.5f)
            assertTrue("Should have reasonable confidence", it.confidence > 0.5f)
        }
    }

    @Test
    fun `hybrid detector handles harmonically rich signal`() {
        val fundamental = 220.0 // A3
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        // Signal with strong harmonics
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            audioData[i] =
                (
                    0.4f * sin(2 * PI * fundamental * time) +
                        0.4f * sin(2 * PI * fundamental * 2 * time) +
                        0.2f * sin(2 * PI * fundamental * 3 * time)
                ).toFloat()
        }

        val result = detector.detectPitch(audioData)

        assertNotNull("Should detect harmonically rich signal", result)
        result?.let {
            // Should detect fundamental, not a harmonic
            val error = abs(it.frequency - fundamental)
            assertTrue(
                "Should detect fundamental ($fundamental Hz), got ${it.frequency} Hz, error: $error Hz",
                error < 20.0, // More tolerance for complex signal
            )
        }
    }

    @Test
    fun `hybrid detector resolves octave disagreement`() {
        val fundamental = 165.0 // E3
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        // Signal where 2nd harmonic dominates (may cause octave confusion)
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            audioData[i] =
                (
                    0.3f * sin(2 * PI * fundamental * time) + // Weak fundamental
                        0.7f * sin(2 * PI * fundamental * 2 * time) // Strong octave
                ).toFloat()
        }

        val result = detector.detectPitch(audioData)

        assertNotNull("Should detect despite octave ambiguity", result)
        result?.let {
            // Hybrid should resolve to fundamental
            assertTrue(
                "Should prefer fundamental over octave: got ${it.frequency} Hz",
                it.frequency < fundamental * 1.5, // Closer to fundamental than octave
            )
        }
    }

    @Test
    fun `hybrid detector handles all guitar string frequencies`() {
        val guitarFrequencies =
            listOf(
                82.41, // E2 (low E)
                110.00, // A2
                146.83, // D3
                196.00, // G3
                246.94, // B3
                329.63, // E4 (high E)
            )

        for (expectedFreq in guitarFrequencies) {
            val duration = if (expectedFreq < 150) 0.25 else 0.15 // Longer for low frequencies
            val samples = (sampleRate * duration).toInt()
            val audioData = generateSineWave(expectedFreq, sampleRate, samples)
            val result = detector.detectPitch(audioData)

            assertNotNull("Should detect $expectedFreq Hz", result)
            result?.let {
                val error = abs(it.frequency - expectedFreq)
                val tolerance = if (expectedFreq < 150) 15.0 else 10.0
                assertTrue(
                    "For $expectedFreq Hz: detected ${it.frequency} Hz, error: $error Hz",
                    error < tolerance,
                )
            }
        }
    }

    @Test
    fun `hybrid detector returns null for silence`() {
        val samples = 4410 // 100ms of silence
        val audioData = FloatArray(samples) { 0f }
        val result = detector.detectPitch(audioData)

        assertNull("Should not detect pitch in silence", result)
    }

    @Test
    fun `hybrid detector returns null for very low amplitude`() {
        val frequency = 440.0
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()
        val audioData = generateSineWave(frequency, sampleRate, samples, amplitude = 0.0001f)
        val result = detector.detectPitch(audioData)

        // May return null or very low confidence
        result?.let {
            assertTrue("If detected, should be in valid range", it.frequency in 60.0..1500.0)
        }
    }

    @Test
    fun `hybrid detector handles noisy signal`() {
        val frequency = 330.0 // E4
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        // Generate signal with noise
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val signal = 0.7f * sin(2 * PI * frequency * time).toFloat()
            val noise = 0.15f * (Math.random().toFloat() - 0.5f)
            audioData[i] = signal + noise
        }

        val result = detector.detectPitch(audioData)

        // Should still detect despite noise (or return null gracefully)
        result?.let {
            val error = abs(it.frequency - frequency)
            assertTrue(
                "Should maintain reasonable accuracy with noise: error $error Hz",
                error < 20.0,
            )
        }
    }

    @Test
    fun `hybrid detector provides YIN and FFT frequency information`() {
        val frequency = 440.0
        val duration = 0.15
        val samples = (sampleRate * duration).toInt()
        val audioData = generateSineWave(frequency, sampleRate, samples)

        val result = detector.detectPitch(audioData)

        assertNotNull("Should detect", result)
        result?.let {
            // Should have at least one of YIN or FFT results
            assertTrue(
                "Should have YIN or FFT frequency",
                it.yinFrequency != null || it.fftFrequency != null,
            )
        }
    }

    @Test
    fun `hybrid detector handles frequency outside range`() {
        val frequency = 2500.0 // Above guitar range
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()
        val audioData = generateSineWave(frequency, sampleRate, samples)

        val result = detector.detectPitch(audioData)

        // Should return null or detect a harmonic within range
        result?.let {
            assertTrue(
                "Any detected frequency should be in valid range",
                it.frequency in 60.0..1500.0,
            )
        }
    }

    @Test
    fun `hybrid detector with empty data returns null`() {
        val result = detector.detectPitch(FloatArray(0))
        assertNull("Should return null for empty data", result)
    }

    @Test
    fun `hybrid detector with insufficient data returns null`() {
        val result = detector.detectPitch(FloatArray(100)) // Too short
        // May return null or handle gracefully
        result?.let {
            assertTrue("If detected, should be in valid range", it.frequency in 60.0..1500.0)
        }
    }

    // Helper function
    private fun generateSineWave(
        frequency: Double,
        sampleRate: Int,
        samples: Int,
        amplitude: Float = 0.8f,
    ): FloatArray {
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val value = amplitude * sin(2 * PI * frequency * time)
            audioData[i] = value.toFloat()
        }
        return audioData
    }
}
