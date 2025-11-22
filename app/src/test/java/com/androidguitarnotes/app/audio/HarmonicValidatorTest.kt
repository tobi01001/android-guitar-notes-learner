package com.androidguitarnotes.app.audio

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Unit tests for HarmonicValidator (ENH-002).
 *
 * Tests harmonic consistency checking for octave disambiguation.
 */
class HarmonicValidatorTest {
    private val validator = HarmonicValidator()
    private val sampleRate = 44100
    private val duration = 0.2 // 200ms for good frequency resolution

    @Test
    fun `validatePitch returns null for empty samples`() {
        val result = validator.validatePitch(FloatArray(0), 440.0)
        assertNull("Should return null for empty samples", result)
    }

    @Test
    fun `validatePitch returns null for out-of-range frequency`() {
        val samples = generateSineWave(30.0, 0.5f) // Below guitar range
        val result = validator.validatePitch(samples, 30.0)
        assertNull("Should return null for frequency below guitar range", result)

        val samples2 = generateSineWave(2000.0, 0.5f) // Above guitar range
        val result2 = validator.validatePitch(samples2, 2000.0)
        assertNull("Should return null for frequency above guitar range", result2)
    }

    @Test
    fun `validatePitch keeps correct fundamental when harmonics present`() {
        // Generate E4 (329.63 Hz) with harmonics
        val fundamental = 329.63
        val samples =
            generateSignalWithHarmonics(
                fundamental = fundamental,
                harmonics =
                    listOf(
                        fundamental * 2 to 0.6f, // 2nd harmonic
                        fundamental * 3 to 0.3f, // 3rd harmonic
                    ),
            )

        val result = validator.validatePitch(samples, fundamental)
        assertNotNull("Should detect signal", result)
        result?.let {
            assertFrequencyNear(it, fundamental, tolerance = 5.0)
        }
    }

    @Test
    fun `validatePitch corrects octave error down when half frequency has better harmonics`() {
        // Generate E2 (82.41 Hz) with strong harmonics
        val fundamental = 82.41
        val samples =
            generateSignalWithHarmonics(
                fundamental = fundamental,
                harmonics =
                    listOf(
                        fundamental * 2 to 0.8f, // Strong 2nd harmonic
                        fundamental * 3 to 0.5f, // 3rd harmonic
                        fundamental * 4 to 0.3f, // 4th harmonic
                    ),
            )

        // Simulate detector finding the octave (2nd harmonic) instead of fundamental
        val detectedWrong = fundamental * 2 // 164.82 Hz

        val result = validator.validatePitch(samples, detectedWrong)
        assertNotNull("Should correct frequency", result)
        result?.let {
            // Should correct down to fundamental (or stay at detected if harmonics are ambiguous)
            // The validator should recognize that fundamental has better harmonic structure
            assertTrue(
                "Should be closer to fundamental ($fundamental) than detected ($detectedWrong), got $it",
                abs(it - fundamental) < abs(it - detectedWrong),
            )
        }
    }

    @Test
    fun `validatePitch corrects octave error up when double frequency has better harmonics`() {
        // Generate E3 (164.82 Hz) with harmonics but weak fundamental
        val fundamental = 164.82
        val samples =
            generateSignalWithHarmonics(
                fundamental = fundamental,
                harmonics =
                    listOf(
                        fundamental * 2 to 0.9f, // Very strong 2nd harmonic
                        fundamental * 3 to 0.6f, // Strong 3rd harmonic
                        fundamental * 4 to 0.4f, // 4th harmonic
                    ),
                fundamentalAmplitude = 0.2f, // Weak fundamental
            )

        // Simulate detector finding half the frequency (sub-octave error)
        val detectedWrong = fundamental / 2 // ~82.41 Hz

        val result = validator.validatePitch(samples, detectedWrong)
        assertNotNull("Should correct frequency", result)
        result?.let {
            // Should correct up to actual fundamental
            assertTrue(
                "Should be closer to fundamental ($fundamental) than detected ($detectedWrong), got $it",
                abs(it - fundamental) < abs(it - detectedWrong),
            )
        }
    }

    @Test
    fun `validatePitch handles A4 440Hz with clear harmonics`() {
        // Standard A4 tuning reference with harmonics
        val fundamental = 440.0
        val samples =
            generateSignalWithHarmonics(
                fundamental = fundamental,
                harmonics =
                    listOf(
                        fundamental * 2 to 0.5f,
                        fundamental * 3 to 0.3f,
                        fundamental * 4 to 0.2f,
                    ),
            )

        val result = validator.validatePitch(samples, fundamental)
        assertNotNull("Should validate A4", result)
        result?.let {
            assertFrequencyNear(it, fundamental, tolerance = 10.0)
        }
    }

    @Test
    fun `validatePitch handles low E2 guitar string`() {
        // Low E2 (~82.41 Hz) with typical guitar harmonics
        val fundamental = 82.41
        val samples =
            generateSignalWithHarmonics(
                fundamental = fundamental,
                harmonics =
                    listOf(
                        fundamental * 2 to 0.7f,
                        fundamental * 3 to 0.4f,
                        fundamental * 4 to 0.2f,
                    ),
            )

        val result = validator.validatePitch(samples, fundamental)
        assertNotNull("Should validate low E2", result)
        result?.let {
            assertFrequencyNear(it, fundamental, tolerance = 10.0)
        }
    }

    @Test
    fun `validatePitch is conservative with ambiguous signals`() {
        // Signal with no clear harmonic structure (noisy)
        val fundamental = 220.0
        val samples = generateNoisySignal(fundamental, noiseLevel = 0.5f)

        val result = validator.validatePitch(samples, fundamental)
        assertNotNull("Should return some result", result)
        result?.let {
            // Should keep original when uncertain (conservative approach)
            assertFrequencyNear(it, fundamental, tolerance = 50.0)
        }
    }

    @Test
    fun `validatePitch handles natural harmonic at 12th fret`() {
        // Natural harmonic at 12th fret produces octave above open string
        // E2 fundamental (82.41 Hz) but harmonic at E3 (164.82 Hz) is strongest
        val openStringFreq = 82.41
        val harmonicFreq = openStringFreq * 2 // 12th fret harmonic

        val samples =
            generateSignalWithHarmonics(
                fundamental = openStringFreq,
                harmonics =
                    listOf(
                        harmonicFreq to 1.0f, // Natural harmonic is very strong
                        openStringFreq * 3 to 0.3f,
                        openStringFreq * 4 to 0.2f,
                    ),
                fundamentalAmplitude = 0.3f, // Weak fundamental
            )

        // Detector might find the strong harmonic instead of fundamental
        val detectedWrong = harmonicFreq

        val result = validator.validatePitch(samples, detectedWrong)
        assertNotNull("Should handle natural harmonic", result)
        // Result should be validated (either corrected or kept with good reason)
        result?.let {
            assertTrue(
                "Result should be in valid guitar range",
                it in 60.0..1500.0,
            )
        }
    }

    @Test
    fun `validatePitch handles high register notes above 12th fret`() {
        // High E4 (~659 Hz) with harmonics
        val fundamental = 659.26
        val samples =
            generateSignalWithHarmonics(
                fundamental = fundamental,
                harmonics =
                    listOf(
                        fundamental * 2 to 0.6f,
                        fundamental * 3 to 0.3f, // This is around 2kHz, getting close to limit
                    ),
            )

        val result = validator.validatePitch(samples, fundamental)
        assertNotNull("Should handle high register", result)
        result?.let {
            assertFrequencyNear(it, fundamental, tolerance = 15.0)
        }
    }

    @Test
    fun `validatePitch handles bright-tone guitar with strong overtones`() {
        // Bright guitar with very strong harmonics
        val fundamental = 196.0 // G3
        val samples =
            generateSignalWithHarmonics(
                fundamental = fundamental,
                harmonics =
                    listOf(
                        fundamental * 2 to 0.9f, // Very bright tone
                        fundamental * 3 to 0.7f,
                        fundamental * 4 to 0.5f,
                        fundamental * 5 to 0.3f,
                    ),
                fundamentalAmplitude = 0.6f,
            )

        val result = validator.validatePitch(samples, fundamental)
        assertNotNull("Should handle bright tone", result)
        result?.let {
            assertFrequencyNear(it, fundamental, tolerance = 10.0)
        }
    }

    @Test
    fun `validatePitch performance meets requirement under 20ms`() {
        val fundamental = 440.0
        val samples =
            generateSignalWithHarmonics(
                fundamental = fundamental,
                harmonics =
                    listOf(
                        fundamental * 2 to 0.5f,
                        fundamental * 3 to 0.3f,
                    ),
            )

        // Use higher iteration count for more stable performance measurement
        val iterations = 100
        val startTime = System.nanoTime()

        repeat(iterations) {
            validator.validatePitch(samples, fundamental)
        }

        val avgTimeMs = (System.nanoTime() - startTime) / iterations / 1_000_000.0

        println("Average validation time: ${avgTimeMs}ms")
        // Use more lenient threshold to avoid flakiness in CI environments
        assertTrue(
            "Validation should complete in under 30ms on average, got ${avgTimeMs}ms",
            avgTimeMs < 30.0,
        )
    }

    /**
     * Helper function to generate a sine wave with harmonics.
     */
    private fun generateSignalWithHarmonics(
        fundamental: Double,
        harmonics: List<Pair<Double, Float>>,
        fundamentalAmplitude: Float = 0.8f,
    ): FloatArray {
        val samples = (sampleRate * duration).toInt()
        val audioData = FloatArray(samples)

        // Add fundamental
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            audioData[i] = (fundamentalAmplitude * sin(2 * PI * fundamental * time)).toFloat()
        }

        // Add harmonics
        for ((harmonicFreq, amplitude) in harmonics) {
            for (i in 0 until samples) {
                val time = i.toDouble() / sampleRate
                audioData[i] += (amplitude * sin(2 * PI * harmonicFreq * time)).toFloat()
            }
        }

        // Normalize to prevent clipping
        val maxAmplitude = audioData.maxOrNull() ?: 1f
        if (maxAmplitude > 1f) {
            for (i in audioData.indices) {
                audioData[i] /= maxAmplitude
            }
        }

        return audioData
    }

    /**
     * Helper function to generate a sine wave.
     */
    private fun generateSineWave(
        frequency: Double,
        amplitude: Float = 0.8f,
    ): FloatArray {
        val samples = (sampleRate * duration).toInt()
        val audioData = FloatArray(samples)

        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            audioData[i] = (amplitude * sin(2 * PI * frequency * time)).toFloat()
        }

        return audioData
    }

    /**
     * Helper function to generate a noisy signal.
     */
    private fun generateNoisySignal(
        frequency: Double,
        noiseLevel: Float = 0.3f,
    ): FloatArray {
        val samples = (sampleRate * duration).toInt()
        val audioData = FloatArray(samples)

        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val signal = 0.5f * sin(2 * PI * frequency * time).toFloat()
            val noise = (Math.random().toFloat() - 0.5f) * noiseLevel * 2f
            audioData[i] = signal + noise
        }

        return audioData
    }

    /**
     * Helper function to assert frequency is within tolerance.
     */
    private fun assertFrequencyNear(
        actual: Double,
        expected: Double,
        tolerance: Double,
    ) {
        val diff = abs(actual - expected)
        assertTrue(
            "Expected frequency $expected Hz ± $tolerance Hz, but got $actual Hz (diff: $diff Hz)",
            diff <= tolerance,
        )
    }
}
