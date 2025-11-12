package com.androidguitarnotes.app.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Unit tests for HighPassFilter.
 */
class HighPassFilterTest {
    @Test
    fun `filter initialization with valid parameters`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)
        assertNotNull(filter)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `filter rejects negative sample rate`() {
        HighPassFilter(sampleRate = -1, cutoffFrequency = 60.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `filter rejects zero sample rate`() {
        HighPassFilter(sampleRate = 0, cutoffFrequency = 60.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `filter rejects negative cutoff frequency`() {
        HighPassFilter(sampleRate = 44100, cutoffFrequency = -10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `filter rejects cutoff above Nyquist frequency`() {
        HighPassFilter(sampleRate = 44100, cutoffFrequency = 25000.0)
    }

    @Test
    fun `filter attenuates DC offset`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)

        // Create DC signal (constant value)
        val samples = FloatArray(1000) { 0.5f }

        // Process through filter
        filter.process(samples)

        // After settling, DC should be heavily attenuated
        // Check last 100 samples (after transient response)
        val steadyStateSamples = samples.sliceArray(900 until 1000)
        val averageOutput = steadyStateSamples.average().toFloat()

        // DC should be attenuated to near zero
        assertTrue(
            "DC offset should be heavily attenuated, got $averageOutput",
            abs(averageOutput) < 0.05f,
        )
    }

    @Test
    fun `filter attenuates low frequency signal below cutoff`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)

        // Generate 30 Hz sine wave (well below cutoff)
        val frequency = 30.0
        val duration = 0.5 // seconds
        val sampleRate = 44100
        val samples = generateSineWave(frequency, sampleRate, (sampleRate * duration).toInt())

        // Calculate RMS of input
        val inputRMS = calculateRMS(samples)

        // Process through filter
        val filtered = filter.process(samples.copyOf())

        // Calculate RMS of output (skip first 1000 samples for transient)
        val steadyStateFiltered = filtered.sliceArray(1000 until filtered.size)
        val outputRMS = calculateRMS(steadyStateFiltered)

        // Output should be significantly attenuated (expect > 50% reduction)
        assertTrue(
            "Low frequency ($frequency Hz) should be attenuated, input RMS: $inputRMS, output RMS: $outputRMS",
            outputRMS < inputRMS * 0.5f,
        )
    }

    @Test
    fun `filter passes guitar E2 frequency with minimal attenuation`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)

        // Generate 82.41 Hz sine wave (low E string)
        val frequency = 82.41
        val duration = 0.5 // seconds
        val sampleRate = 44100
        val samples = generateSineWave(frequency, sampleRate, (sampleRate * duration).toInt())

        // Calculate RMS of input
        val inputRMS = calculateRMS(samples)

        // Process through filter
        val filtered = filter.process(samples.copyOf())

        // Calculate RMS of output (skip first 1000 samples for transient)
        val steadyStateFiltered = filtered.sliceArray(1000 until filtered.size)
        val outputRMS = calculateRMS(steadyStateFiltered)

        // Output should be mostly preserved (expect < 20% reduction)
        assertTrue(
            "Guitar E2 frequency ($frequency Hz) should pass with minimal attenuation, " +
                "input RMS: $inputRMS, output RMS: $outputRMS, attenuation: ${(1 - outputRMS / inputRMS) * 100}%",
            outputRMS > inputRMS * 0.8f,
        )
    }

    @Test
    fun `filter passes guitar A4 frequency with no significant attenuation`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)

        // Generate 440 Hz sine wave (A4)
        val frequency = 440.0
        val duration = 0.5 // seconds
        val sampleRate = 44100
        val samples = generateSineWave(frequency, sampleRate, (sampleRate * duration).toInt())

        // Calculate RMS of input
        val inputRMS = calculateRMS(samples)

        // Process through filter
        val filtered = filter.process(samples.copyOf())

        // Calculate RMS of output (skip first 1000 samples for transient)
        val steadyStateFiltered = filtered.sliceArray(1000 until filtered.size)
        val outputRMS = calculateRMS(steadyStateFiltered)

        // Output should be nearly identical (expect < 5% difference)
        assertTrue(
            "High frequency ($frequency Hz) should pass unaffected, " +
                "input RMS: $inputRMS, output RMS: $outputRMS, difference: ${abs(1 - outputRMS / inputRMS) * 100}%",
            abs(outputRMS - inputRMS) < inputRMS * 0.05f,
        )
    }

    @Test
    fun `filter passes high E4 frequency with no significant attenuation`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)

        // Generate 329.63 Hz sine wave (high E string)
        val frequency = 329.63
        val duration = 0.5 // seconds
        val sampleRate = 44100
        val samples = generateSineWave(frequency, sampleRate, (sampleRate * duration).toInt())

        // Calculate RMS of input
        val inputRMS = calculateRMS(samples)

        // Process through filter
        val filtered = filter.process(samples.copyOf())

        // Calculate RMS of output (skip first 1000 samples for transient)
        val steadyStateFiltered = filtered.sliceArray(1000 until filtered.size)
        val outputRMS = calculateRMS(steadyStateFiltered)

        // Output should be nearly identical (expect < 5% difference)
        assertTrue(
            "Guitar E4 frequency ($frequency Hz) should pass unaffected, " +
                "input RMS: $inputRMS, output RMS: $outputRMS, difference: ${abs(1 - outputRMS / inputRMS) * 100}%",
            abs(outputRMS - inputRMS) < inputRMS * 0.05f,
        )
    }

    @Test
    fun `filter handles empty array`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)
        val samples = FloatArray(0)

        val result = filter.process(samples)

        assertEquals(0, result.size)
    }

    @Test
    fun `filter handles single sample`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)
        val sample = 0.5f

        val result = filter.process(sample)

        // First sample should be processed without error
        // Exact value depends on filter coefficients
        assertTrue(result.isFinite())
    }

    @Test
    fun `filter reset clears state`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)

        // Process some samples to build up state
        val samples = FloatArray(100) { 0.5f }
        filter.process(samples)

        // Reset filter
        filter.reset()

        // Process a single sample - should behave like first sample
        val result1 = filter.process(0.5f)

        // Create new filter for comparison
        val freshFilter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)
        val result2 = freshFilter.process(0.5f)

        // Results should be identical after reset
        assertEquals(result2, result1, 0.0001f)
    }

    @Test
    fun `filter with 50 Hz cutoff attenuates 30 Hz`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 50.0)

        // Generate 30 Hz sine wave
        val frequency = 30.0
        val samples = generateSineWave(frequency, 44100, 22050)
        val inputRMS = calculateRMS(samples)

        // Process through filter
        val filtered = filter.process(samples.copyOf())
        val steadyStateFiltered = filtered.sliceArray(1000 until filtered.size)
        val outputRMS = calculateRMS(steadyStateFiltered)

        // One-pole filter has gradual 6 dB/octave roll-off
        // 30 Hz is ~0.74 octaves below 50 Hz, so expect ~20-30% attenuation
        // Just verify some attenuation occurs
        assertTrue(
            "30 Hz should be attenuated by 50 Hz cutoff filter, " +
                "input RMS: $inputRMS, output RMS: $outputRMS, attenuation: ${(1 - outputRMS / inputRMS) * 100}%",
            outputRMS < inputRMS * 0.85f,
        )
    }

    @Test
    fun `filter processes in-place modifying original array`() {
        val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)

        val originalSamples = floatArrayOf(1.0f, 0.5f, -0.5f, -1.0f, 0.0f)
        val samples = originalSamples.copyOf()

        val result = filter.process(samples)

        // Result should be the same reference
        assertTrue(result === samples)

        // Values should have changed
        var hasChanged = false
        for (i in samples.indices) {
            if (samples[i] != originalSamples[i]) {
                hasChanged = true
                break
            }
        }
        assertTrue("Filter should modify the input array", hasChanged)
    }

    // Helper function to generate sine wave for testing
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

    // Helper function to calculate RMS
    private fun calculateRMS(samples: FloatArray): Float {
        if (samples.isEmpty()) return 0f
        var sum = 0.0
        for (sample in samples) {
            sum += sample * sample
        }
        return sqrt(sum / samples.size).toFloat()
    }
}
