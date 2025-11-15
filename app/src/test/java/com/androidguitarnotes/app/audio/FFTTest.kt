package com.androidguitarnotes.app.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class FFTTest {
    companion object {
        private const val EPSILON = 0.01f // Tolerance for floating-point comparison
    }

    @Test
    fun testFFTSizeValidation() {
        // Valid power of 2 sizes
        FFT(2)
        FFT(4)
        FFT(8)
        FFT(1024)
        FFT(4096)

        // Invalid sizes should throw
        try {
            FFT(0)
            throw AssertionError("Should throw for size 0")
        } catch (e: IllegalArgumentException) {
            // Expected
        }

        try {
            FFT(3)
            throw AssertionError("Should throw for non-power-of-2 size")
        } catch (e: IllegalArgumentException) {
            // Expected
        }

        try {
            FFT(100)
            throw AssertionError("Should throw for non-power-of-2 size")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun testFFTSineWave() {
        val n = 4096 // Larger FFT for better frequency resolution
        val sampleRate = 44100
        val frequency = 440.0 // A4 note

        // Generate sine wave
        val signal =
            FloatArray(n) { i ->
                sin(2.0 * PI * frequency * i / sampleRate).toFloat()
            }

        // Compute FFT
        val fft = FFT(n)
        val magnitudes = fft.computeMagnitudeSpectrum(signal)

        // Find peak frequency
        val binWidth = sampleRate.toDouble() / n
        var peakBin = 0
        var peakMagnitude = 0f
        for (i in magnitudes.indices) {
            if (magnitudes[i] > peakMagnitude) {
                peakMagnitude = magnitudes[i]
                peakBin = i
            }
        }

        val detectedFrequency = peakBin * binWidth

        // Verify detected frequency is close to input frequency
        // With 4096 samples at 44100 Hz, bin width is ~10.77 Hz, so tolerance is half a bin
        val error = abs(detectedFrequency - frequency)
        assertTrue("Detected frequency $detectedFrequency should be close to $frequency Hz (error: $error)", error < binWidth / 2.0 + 1.0)
    }

    @Test
    fun testFFTMultipleSineWaves() {
        val n = 2048
        val sampleRate = 44100

        // Generate signal with two frequencies: 440 Hz (A4) and 880 Hz (A5)
        val signal =
            FloatArray(n) { i ->
                val t = i.toDouble() / sampleRate
                (sin(2.0 * PI * 440.0 * t) + 0.5 * sin(2.0 * PI * 880.0 * t)).toFloat()
            }

        // Compute FFT
        val fft = FFT(n)
        val magnitudes = fft.computeMagnitudeSpectrum(signal)

        // Find peaks
        val binWidth = sampleRate.toDouble() / n
        val peaks = mutableListOf<Double>()
        for (i in 1 until magnitudes.size - 1) {
            // Local maximum detection
            if (magnitudes[i] > magnitudes[i - 1] && magnitudes[i] > magnitudes[i + 1] && magnitudes[i] > 10f) {
                peaks.add(i * binWidth)
            }
        }

        // Should detect both frequencies
        assertTrue("Should detect at least 2 peaks", peaks.size >= 2)

        // Check if 440 Hz and 880 Hz are among the detected peaks
        val has440 = peaks.any { abs(it - 440.0) < 10.0 }
        val has880 = peaks.any { abs(it - 880.0) < 15.0 }

        assertTrue("Should detect 440 Hz peak", has440)
        assertTrue("Should detect 880 Hz peak", has880)
    }

    @Test
    fun testFFTDCOffset() {
        val n = 512
        val dcValue = 1.0f

        // Signal with DC offset only (constant value)
        val signal = FloatArray(n) { dcValue }

        // Compute FFT
        val fft = FFT(n)
        val magnitudes = fft.computeMagnitudeSpectrum(signal)

        // DC component should be at bin 0
        assertTrue("DC component should be strongest at bin 0", magnitudes[0] > 100f)

        // All other bins should be near zero
        for (i in 1 until magnitudes.size) {
            assertTrue("Bin $i should be near zero", magnitudes[i] < 1f)
        }
    }

    @Test
    fun testFFTZeroSignal() {
        val n = 256
        val signal = FloatArray(n) // All zeros

        val fft = FFT(n)
        val magnitudes = fft.computeMagnitudeSpectrum(signal)

        // All magnitudes should be zero
        for (i in magnitudes.indices) {
            assertEquals("All magnitudes should be zero", 0f, magnitudes[i], EPSILON)
        }
    }

    @Test
    fun testFFTGuitarNote() {
        val n = 4096
        val sampleRate = 44100
        val frequency = 82.41 // Low E2

        // Generate signal similar to guitar note (fundamental + harmonics)
        val signal =
            FloatArray(n) { i ->
                val t = i.toDouble() / sampleRate
                (
                    sin(2.0 * PI * frequency * t) + // Fundamental
                        0.5 * sin(2.0 * PI * frequency * 2.0 * t) + // 2nd harmonic
                        0.3 * sin(2.0 * PI * frequency * 3.0 * t) // 3rd harmonic
                ).toFloat()
            }

        // Compute FFT
        val fft = FFT(n)
        val magnitudes = fft.computeMagnitudeSpectrum(signal)

        // Find peak frequency
        val binWidth = sampleRate.toDouble() / n
        var peakBin = 0
        var peakMagnitude = 0f
        // Start from bin 5 to skip DC and extremely low frequencies (< 50 Hz)
        // but include guitar low E2 range (~82 Hz which is at bin 7-8)
        for (i in 5 until magnitudes.size) {
            if (magnitudes[i] > peakMagnitude) {
                peakMagnitude = magnitudes[i]
                peakBin = i
            }
        }

        val detectedFrequency = peakBin * binWidth

        // Verify detected frequency is the fundamental
        // With 4096 samples at 44100 Hz, bin width is ~10.77 Hz
        val error = abs(detectedFrequency - frequency)
        assertTrue(
            "Detected frequency $detectedFrequency should be close to fundamental $frequency Hz (error: $error, bin width: $binWidth)",
            error < binWidth / 2.0 + 1.0,
        )
    }

    @Test
    fun testFFTPerformance() {
        val n = 4096
        val signal =
            FloatArray(n) { i ->
                sin(2.0 * PI * 440.0 * i / 44100.0).toFloat()
            }

        val fft = FFT(n)

        // Measure time for 100 FFT computations
        val startTime = System.nanoTime()
        repeat(100) {
            fft.computeMagnitudeSpectrum(signal)
        }
        val endTime = System.nanoTime()
        val avgTimeMs = (endTime - startTime) / 1_000_000.0 / 100.0

        // Should complete in less than 5ms per FFT (conservative estimate for mobile)
        // On modern devices, this should be well under 1ms
        assertTrue(
            "FFT should be fast enough for real-time audio (avg: ${avgTimeMs}ms per FFT)",
            avgTimeMs < 5.0,
        )

        println("Average FFT time: ${avgTimeMs}ms per 4096-sample FFT")
    }
}
