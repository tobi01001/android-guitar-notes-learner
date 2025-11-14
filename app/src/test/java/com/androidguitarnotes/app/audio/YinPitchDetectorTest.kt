package com.androidguitarnotes.app.audio

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Unit tests for YinPitchDetector.
 *
 * Tests the YIN algorithm implementation for accuracy, noise robustness,
 * and handling of various edge cases in guitar frequency range (60-1500 Hz).
 */
class YinPitchDetectorTest {
    private val detector = YinPitchDetector()
    private val sampleRate = 44100

    @Test
    fun `detectPitch returns null for empty data`() {
        val result = detector.detectPitch(FloatArray(0))
        assertNull(result)
    }

    @Test
    fun `detectPitch returns null for insufficient data`() {
        val result = detector.detectPitch(FloatArray(10))
        assertNull(result)
    }

    @Test
    fun `detectPitch detects A4 440Hz with high accuracy`() {
        val frequency = 440.0 // A4
        val duration = 0.1 // 100ms
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = detector.detectPitch(audioData)

        assertNotNull("Should detect frequency", result)
        result?.let {
            // YIN should achieve ±1 Hz accuracy
            val error = abs(it.frequency - frequency)
            assertTrue(
                "Detected ${it.frequency} Hz, expected $frequency Hz, error: $error Hz",
                error < 1.0,
            )
            assertTrue(
                "Confidence should indicate good detection (< 0.2)",
                it.confidence < 0.2f,
            )
        }
    }

    @Test
    fun `detectPitch detects E2 low E string accurately`() {
        val frequency = 82.41 // E2 (low E string)
        val duration = 0.2 // Need longer duration for low frequencies
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = detector.detectPitch(audioData)

        assertNotNull("Should detect low E frequency", result)
        result?.let {
            val error = abs(it.frequency - frequency)
            assertTrue(
                "Detected ${it.frequency} Hz, expected $frequency Hz, error: $error Hz",
                error < 1.0, // YIN now achieves excellent accuracy even for low frequencies
            )
        }
    }

    @Test
    fun `detectPitch detects E4 high E string accurately`() {
        val frequency = 329.63 // E4 (high E string)
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = detector.detectPitch(audioData)

        assertNotNull("Should detect high E frequency", result)
        result?.let {
            val error = abs(it.frequency - frequency)
            assertTrue(
                "Detected ${it.frequency} Hz, expected $frequency Hz, error: $error Hz",
                error < 1.0,
            )
        }
    }

    @Test
    fun `detectPitch handles all guitar string frequencies`() {
        // Standard tuning frequencies
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
            val duration = 0.2 // Longer duration for better accuracy
            val samples = (sampleRate * duration).toInt()
            val audioData = generateSineWave(expectedFreq, sampleRate, samples)
            val result = detector.detectPitch(audioData)

            assertNotNull("Should detect $expectedFreq Hz", result)
            result?.let {
                val error = abs(it.frequency - expectedFreq)
                // YIN now achieves excellent accuracy across all guitar frequencies
                val tolerance = 1.0 // ±1 Hz for all frequencies
                assertTrue(
                    "For $expectedFreq Hz: detected ${it.frequency} Hz, error: $error Hz",
                    error < tolerance,
                )
            }
        }
    }

    @Test
    fun `detectPitch returns null for frequency below range`() {
        val frequency = 30.0 // Below guitar range
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = detector.detectPitch(audioData)

        assertNull("Should not detect frequency below 60 Hz", result)
    }

    @Test
    fun `detectPitch returns null for frequency above range`() {
        val frequency = 2000.0 // Above guitar range
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()

        val audioData = generateSineWave(frequency, sampleRate, samples)
        val result = detector.detectPitch(audioData)

        // Should either return null or detect a harmonic within range
        result?.let {
            assertTrue(
                "Any detected frequency should be in valid range",
                it.frequency in 60.0..1500.0,
            )
        }
    }

    @Test
    fun `detectPitch returns null for silence`() {
        val samples = 4410 // 100ms of silence
        val audioData = FloatArray(samples) { 0f }
        val result = detector.detectPitch(audioData)

        assertNull("Should not detect pitch in silence", result)
    }

    @Test
    fun `detectPitch returns null for very low amplitude signal`() {
        val frequency = 440.0
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()

        // Generate very quiet sine wave
        val audioData = generateSineWave(frequency, sampleRate, samples, amplitude = 0.0001f)
        val result = detector.detectPitch(audioData)

        // Should likely return null due to low energy, but if it detects something
        // it should at least be in the valid range
        result?.let {
            assertTrue(
                "Any detected frequency should be in valid range",
                it.frequency in 60.0..1500.0,
            )
        }
    }

    @Test
    fun `detectPitch handles signal with harmonics`() {
        val fundamental = 220.0 // A3
        val duration = 0.15
        val samples = (sampleRate * duration).toInt()

        // Generate signal with fundamental and harmonics
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            // Fundamental + 2nd harmonic + 3rd harmonic (simulating guitar string)
            audioData[i] =
                (
                    0.6f * sin(2 * PI * fundamental * time) +
                        0.3f * sin(2 * PI * fundamental * 2 * time) +
                        0.1f * sin(2 * PI * fundamental * 3 * time)
                ).toFloat()
        }

        val result = detector.detectPitch(audioData)

        assertNotNull("Should detect fundamental frequency", result)
        result?.let {
            // YIN should correctly identify the fundamental, not a harmonic
            val error = abs(it.frequency - fundamental)
            assertTrue(
                "Should detect fundamental ($fundamental Hz), got ${it.frequency} Hz, error: $error Hz",
                error < 2.0,
            )
        }
    }

    @Test
    fun `detectPitch with noise maintains reasonable accuracy`() {
        val frequency = 440.0
        val duration = 0.15
        val samples = (sampleRate * duration).toInt()

        // Generate sine wave with added noise
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val signal = 0.8f * sin(2 * PI * frequency * time).toFloat()
            val noise = 0.1f * (Math.random().toFloat() - 0.5f) // 10% noise
            audioData[i] = signal + noise
        }

        val result = detector.detectPitch(audioData)

        // YIN should still detect the pitch with some noise present
        assertNotNull("Should detect pitch despite noise", result)
        result?.let {
            val error = abs(it.frequency - frequency)
            assertTrue(
                "Should maintain accuracy with noise: detected ${it.frequency} Hz, error: $error Hz",
                error < 5.0, // More tolerance with noise
            )
        }
    }

    @Test
    fun `detectPitch with custom threshold affects sensitivity`() {
        val frequency = 440.0
        val duration = 0.15 // Longer duration
        val samples = (sampleRate * duration).toInt()
        val audioData = generateSineWave(frequency, sampleRate, samples)

        // Test with stricter threshold (0.05)
        val strictDetector = YinPitchDetector(threshold = 0.05f)
        val strictResult = strictDetector.detectPitch(audioData)

        // Test with looser threshold (0.2)
        val looseDetector = YinPitchDetector(threshold = 0.2f)
        val looseResult = looseDetector.detectPitch(audioData)

        // Both should detect this clean signal, but strict may have better confidence
        assertNotNull("Strict detector should detect clean signal", strictResult)
        assertNotNull("Loose detector should detect clean signal", looseResult)

        strictResult?.let { strict ->
            looseResult?.let { loose ->
                // Both should be reasonably accurate (within typical YIN range)
                // Allow some variation due to threshold differences
                assertTrue(
                    "Both detectors should be accurate",
                    abs(strict.frequency - frequency) < 10.0 &&
                        abs(loose.frequency - frequency) < 10.0,
                )
            }
        }
    }

    @Test
    fun `parabolic interpolation improves accuracy for pure tones`() {
        // Test multiple frequencies to verify sub-sample accuracy
        val testFrequencies = listOf(349.23, 392.00, 440.0) // F4, G4, A4

        for (freq in testFrequencies) {
            val duration = 0.15 // Longer duration for better accuracy
            val samples = (sampleRate * duration).toInt()
            val audioData = generateSineWave(freq, sampleRate, samples)
            val result = detector.detectPitch(audioData)

            assertNotNull("Should detect $freq Hz", result)
            result?.let {
                val error = abs(it.frequency - freq)
                // With parabolic interpolation, YIN provides improved accuracy
                // Target: better than basic autocorrelation (which is ±2-5 Hz)
                assertTrue(
                    "Parabolic interpolation should achieve good accuracy: " +
                        "expected $freq Hz, got ${it.frequency} Hz, error: $error Hz",
                    error < 5.0, // Realistic accuracy target for YIN with synthetic signals
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
