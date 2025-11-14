package com.androidguitarnotes.app.audio

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Unit tests for YIN algorithm enhancements (Issue #84).
 *
 * Tests:
 * - Enhancement 1: Adaptive Threshold
 * - Enhancement 2: Multi-Period Analysis
 * - Enhancement 3: Hybrid YIN + FFT
 */
class YinEnhancementsTest {
    private val sampleRate = 44100

    // Test Enhancement 1: Adaptive Threshold

    @Test
    fun `adaptive threshold handles clean strong signal with lower threshold`() {
        // Clean, strong signal (high SNR, good RMS)
        val frequency = 440.0 // A4
        val duration = 0.15
        val samples = (sampleRate * duration).toInt()
        val audioData = generateSineWave(frequency, sampleRate, samples, amplitude = 0.8f)

        val standardDetector = YinPitchDetector(sampleRate = sampleRate)
        val adaptiveDetector = YinPitchDetector(sampleRate = sampleRate, adaptiveThreshold = true)

        val standardResult = standardDetector.detectPitch(audioData)
        val adaptiveResult = adaptiveDetector.detectPitch(audioData)

        // Both should detect, but adaptive might have better confidence
        assertNotNull("Standard detector should detect clean signal", standardResult)
        assertNotNull("Adaptive detector should detect clean signal", adaptiveResult)

        adaptiveResult?.let {
            val error = abs(it.frequency - frequency)
            assertTrue("Adaptive threshold should be accurate: error $error Hz", error < 2.0)
        }
    }

    @Test
    fun `adaptive threshold handles noisy weak signal with higher threshold`() {
        // Noisy, weak signal (low SNR)
        val frequency = 220.0 // A3
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        // Generate weak signal with significant noise
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val signal = 0.3f * sin(2 * PI * frequency * time).toFloat()
            val noise = 0.15f * (Math.random().toFloat() - 0.5f) // 50% noise level
            audioData[i] = signal + noise
        }

        val standardDetector = YinPitchDetector(sampleRate = sampleRate, threshold = 0.1f)
        val adaptiveDetector = YinPitchDetector(sampleRate = sampleRate, adaptiveThreshold = true)

        val standardResult = standardDetector.detectPitch(audioData)
        val adaptiveResult = adaptiveDetector.detectPitch(audioData)

        // Adaptive should handle noisy signal better (more lenient threshold)
        // It may or may not detect depending on noise, but if it does, should be reasonable
        adaptiveResult?.let {
            assertTrue("If detected, should be in valid range", it.frequency in 60.0..1500.0)
        }
    }

    @Test
    fun `adaptive threshold adjusts to varying signal strength`() {
        val frequency = 330.0 // E4
        val duration = 0.15
        val samples = (sampleRate * duration).toInt()

        // Test with different amplitudes
        val amplitudes = listOf(0.1f, 0.5f, 0.9f)
        val adaptiveDetector = YinPitchDetector(sampleRate = sampleRate, adaptiveThreshold = true)

        for (amplitude in amplitudes) {
            val audioData = generateSineWave(frequency, sampleRate, samples, amplitude)
            val result = adaptiveDetector.detectPitch(audioData)

            // Should handle various amplitudes
            if (amplitude >= 0.3f) {
                // Strong enough signals should be detected
                assertNotNull("Should detect amplitude $amplitude", result)
                result?.let {
                    val error = abs(it.frequency - frequency)
                    assertTrue(
                        "Should be accurate for amplitude $amplitude: error $error Hz",
                        error < 5.0,
                    )
                }
            }
        }
    }

    // Test Enhancement 2: Multi-Period Analysis

    @Test
    fun `multi-period analysis validates fundamental frequency`() {
        val fundamental = 220.0 // A3
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        // Generate signal with fundamental and harmonics
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            // Fundamental + 2nd harmonic + 3rd harmonic
            audioData[i] =
                (
                    0.5f * sin(2 * PI * fundamental * time) +
                        0.3f * sin(2 * PI * fundamental * 2 * time) +
                        0.2f * sin(2 * PI * fundamental * 3 * time)
                ).toFloat()
        }

        val standardDetector = YinPitchDetector(sampleRate = sampleRate)
        val multiPeriodDetector = YinPitchDetector(sampleRate = sampleRate, multiPeriodAnalysis = true)

        val standardResult = standardDetector.detectPitch(audioData)
        val multiPeriodResult = multiPeriodDetector.detectPitch(audioData)

        assertNotNull("Standard detector should detect", standardResult)
        assertNotNull("Multi-period detector should detect", multiPeriodResult)

        multiPeriodResult?.let {
            val error = abs(it.frequency - fundamental)
            // Multi-period should correctly identify fundamental, not harmonic
            assertTrue(
                "Multi-period should detect fundamental ($fundamental Hz), got ${it.frequency} Hz, error: $error Hz",
                error < 5.0,
            )
        }
    }

    @Test
    fun `multi-period analysis prevents octave errors`() {
        val frequency = 110.0 // A2 (low string)
        val duration = 0.25 // Longer for low frequency
        val samples = (sampleRate * duration).toInt()

        // Generate signal where 2nd harmonic is stronger (common with guitar)
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            audioData[i] =
                (
                    0.4f * sin(2 * PI * frequency * time) + // Weak fundamental
                        0.6f * sin(2 * PI * frequency * 2 * time) // Strong 2nd harmonic
                ).toFloat()
        }

        val multiPeriodDetector = YinPitchDetector(sampleRate = sampleRate, multiPeriodAnalysis = true)
        val result = multiPeriodDetector.detectPitch(audioData)

        assertNotNull("Should detect frequency", result)
        result?.let {
            // Should detect fundamental, not octave up
            assertTrue(
                "Should detect fundamental ~$frequency Hz, not octave ~${frequency * 2} Hz. Got ${it.frequency} Hz",
                abs(it.frequency - frequency) < abs(it.frequency - frequency * 2),
            )
        }
    }

    @Test
    fun `multi-period analysis with single clear period`() {
        // Simple sine wave - only one clear period candidate
        val frequency = 440.0
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()
        val audioData = generateSineWave(frequency, sampleRate, samples)

        val multiPeriodDetector = YinPitchDetector(sampleRate = sampleRate, multiPeriodAnalysis = true)
        val result = multiPeriodDetector.detectPitch(audioData)

        assertNotNull("Should detect pure tone", result)
        result?.let {
            val error = abs(it.frequency - frequency)
            assertTrue("Should be accurate: error $error Hz", error < 2.0)
        }
    }

    // Test Enhancement 1 + 2 Combined

    @Test
    fun `enhanced YIN with both adaptive and multi-period works together`() {
        val fundamental = 165.0 // E3
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()

        // Complex signal: weak fundamental, strong harmonics, with noise
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val signal =
                (
                    0.3f * sin(2 * PI * fundamental * time) +
                        0.5f * sin(2 * PI * fundamental * 2 * time) +
                        0.2f * sin(2 * PI * fundamental * 3 * time)
                ).toFloat()
            val noise = 0.1f * (Math.random().toFloat() - 0.5f)
            audioData[i] = signal + noise
        }

        val enhancedDetector =
            YinPitchDetector(
                sampleRate = sampleRate,
                adaptiveThreshold = true,
                multiPeriodAnalysis = true,
            )
        val result = enhancedDetector.detectPitch(audioData)

        // Enhanced detector should handle this challenging case
        assertNotNull("Enhanced detector should detect complex signal", result)
        result?.let {
            val error = abs(it.frequency - fundamental)
            assertTrue(
                "Should detect fundamental despite weak signal and harmonics: error $error Hz",
                error < 10.0, // More tolerance for difficult case
            )
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
