package com.androidguitarnotes.app.audio

import kotlin.math.PI
import kotlin.math.cos

/**
 * Harmonic consistency validator for octave disambiguation (ENH-002).
 *
 * Analyzes the harmonic content (overtones) of a detected pitch to verify that the
 * fundamental frequency was correctly identified. Helps distinguish between a fundamental
 * frequency and its harmonics, preventing octave errors.
 *
 * ## Purpose
 * The autocorrelation-based pitch detector can occasionally confuse harmonics with
 * fundamentals, leading to octave errors in these common scenarios:
 * - Natural harmonics at 5th, 7th, or 12th frets
 * - Bright-toned guitars with strong overtones
 * - Lightly played notes with stronger harmonics than fundamental
 * - High register notes (above 12th fret) with harmonics in detection range
 *
 * ## Strategy
 * 1. Compute FFT to get frequency spectrum
 * 2. Check if detected frequency has expected harmonic structure (f, 2f, 3f, 4f, 5f)
 * 3. If harmonics are strong: likely correct fundamental
 * 4. If half the frequency has better harmonics: octave error (too high)
 * 5. If double the frequency has better harmonics: sub-octave error (too low)
 * 6. Be conservative: only correct with strong evidence
 *
 * ## Performance
 * - FFT computation: ~5-10ms on modern devices
 * - Harmonic analysis: ~2-5ms
 * - Total: ~10-20ms per validation (acceptable for real-time)
 *
 * @param sampleRate Audio sample rate (default 44100 Hz)
 * @param fftSize FFT window size (default 4096 for good frequency resolution)
 */
class HarmonicValidator(
    private val sampleRate: Int = 44100,
    private val fftSize: Int = 4096,
) {
    private val fft = FFT(fftSize)

    companion object {
        // Guitar frequency range
        private const val MIN_GUITAR_FREQ = 60.0 // Low E2 (~82 Hz) with margin
        private const val MAX_GUITAR_FREQ = 1500.0 // High E4 + harmonics

        // Harmonic analysis parameters
        private val HARMONIC_RATIOS = listOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f)
        private const val HARMONIC_ENERGY_THRESHOLD = 0.3f // 30% of total energy
        private const val FREQUENCY_TOLERANCE = 0.1f // ±10% for harmonic matching

        // Peak detection parameters
        private const val MIN_PEAK_MAGNITUDE = 0.01f
        private const val PEAK_SEARCH_BINS = 3 // Search ±3 bins around expected frequency
    }

    /**
     * Validates and potentially corrects a detected pitch using harmonic consistency analysis.
     *
     * Checks if the detected frequency has the expected harmonic structure of a guitar note.
     * If not, checks if an octave up or down has better harmonic structure and corrects
     * the frequency accordingly.
     *
     * @param samples Audio samples to analyze
     * @param detectedFrequency The frequency detected by the primary pitch detector
     * @return Validated (and possibly corrected) frequency, or null if validation fails
     */
    fun validatePitch(
        samples: FloatArray,
        detectedFrequency: Double,
    ): Double? {
        // Ensure detected frequency is in valid range
        if (detectedFrequency !in MIN_GUITAR_FREQ..MAX_GUITAR_FREQ) {
            return null
        }

        // Compute frequency spectrum
        val spectrum = computeSpectrum(samples) ?: return null

        // Calculate harmonic scores for detected frequency and its octaves
        val detectedScore = calculateHarmonicScore(spectrum, detectedFrequency.toFloat())
        val halfFreqScore =
            if (detectedFrequency / 2 >= MIN_GUITAR_FREQ) {
                calculateHarmonicScore(spectrum, (detectedFrequency / 2).toFloat())
            } else {
                0f
            }
        val doubleFreqScore =
            if (detectedFrequency * 2 <= MAX_GUITAR_FREQ) {
                calculateHarmonicScore(spectrum, (detectedFrequency * 2).toFloat())
            } else {
                0f
            }

        // Decision logic: Be conservative, only correct with strong evidence
        return when {
            // If detected frequency has good harmonic structure, keep it
            detectedScore >= HARMONIC_ENERGY_THRESHOLD -> detectedFrequency

            // If half frequency has significantly better harmonics, it's likely an octave error
            halfFreqScore > detectedScore * 1.5f && halfFreqScore >= HARMONIC_ENERGY_THRESHOLD -> {
                detectedFrequency / 2
            }

            // If double frequency has significantly better harmonics, it's likely a sub-octave error
            doubleFreqScore > detectedScore * 1.5f && doubleFreqScore >= HARMONIC_ENERGY_THRESHOLD -> {
                detectedFrequency * 2
            }

            // No strong evidence for correction, keep original (be conservative)
            else -> detectedFrequency
        }
    }

    /**
     * Computes the magnitude spectrum using FFT.
     *
     * Applies Hann window to reduce spectral leakage, then computes FFT.
     *
     * @param samples Audio samples (can be any length, will be padded/truncated to fftSize)
     * @return Magnitude spectrum, or null if computation fails
     */
    private fun computeSpectrum(samples: FloatArray): FloatArray? {
        if (samples.isEmpty()) return null

        // Prepare FFT input: take first fftSize samples or zero-pad if needed
        val fftInput =
            if (samples.size >= fftSize) {
                samples.take(fftSize).toFloatArray()
            } else {
                FloatArray(fftSize) { i ->
                    if (i < samples.size) samples[i] else 0f
                }
            }

        // Apply Hann window to reduce spectral leakage
        applyHannWindow(fftInput)

        // Compute magnitude spectrum using existing FFT implementation
        return try {
            fft.computeMagnitudeSpectrum(fftInput)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Applies Hann window function to reduce spectral leakage.
     *
     * Hann window: w(n) = 0.5 * (1 - cos(2π * n / (N-1)))
     */
    private fun applyHannWindow(data: FloatArray) {
        val n = data.size
        for (i in data.indices) {
            val window = 0.5 * (1.0 - cos(2.0 * PI * i / (n - 1)))
            data[i] = (data[i] * window).toFloat()
        }
    }

    /**
     * Calculates harmonic score for a given fundamental frequency.
     *
     * The score represents the proportion of total energy that resides in the
     * expected harmonic bins (fundamental + harmonics at 2f, 3f, 4f, 5f).
     *
     * Higher score = stronger harmonic structure = more likely to be correct fundamental.
     *
     * @param spectrum Magnitude spectrum from FFT
     * @param fundamentalFreq Candidate fundamental frequency
     * @return Harmonic score (0.0 to 1.0), proportion of energy in harmonics
     */
    private fun calculateHarmonicScore(
        spectrum: FloatArray,
        fundamentalFreq: Float,
    ): Float {
        var harmonicEnergy = 0f
        var totalEnergy = 0f

        // Calculate bin width (Hz per bin)
        val binWidth = sampleRate.toFloat() / (spectrum.size * 2)

        // Sum energy in harmonic bins
        for (ratio in HARMONIC_RATIOS) {
            val harmonicFreq = fundamentalFreq * ratio

            // Stop if harmonic exceeds Nyquist frequency
            if (harmonicFreq > sampleRate / 2) break

            // Find peak magnitude around expected harmonic frequency
            val peakMagnitude = findPeakAroundFrequency(spectrum, harmonicFreq, binWidth)
            harmonicEnergy += peakMagnitude
        }

        // Calculate total energy in relevant frequency range
        val minBin = (MIN_GUITAR_FREQ / binWidth).toInt().coerceAtLeast(0)
        val maxBin = ((MAX_GUITAR_FREQ / binWidth).toInt() + 1).coerceAtMost(spectrum.size)

        for (i in minBin until maxBin) {
            totalEnergy += spectrum[i]
        }

        // Return proportion of energy in harmonics
        return if (totalEnergy > 0f) {
            (harmonicEnergy / totalEnergy).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    /**
     * Finds the peak magnitude around an expected frequency.
     *
     * Searches in a small window (±PEAK_SEARCH_BINS) around the expected bin
     * to account for slight frequency variations and FFT resolution limits.
     *
     * @param spectrum Magnitude spectrum
     * @param frequency Expected frequency in Hz
     * @param binWidth Frequency resolution per bin in Hz
     * @return Peak magnitude around the frequency
     */
    private fun findPeakAroundFrequency(
        spectrum: FloatArray,
        frequency: Float,
        binWidth: Float,
    ): Float {
        val centerBin = (frequency / binWidth).toInt()

        // Search window
        val startBin = (centerBin - PEAK_SEARCH_BINS).coerceAtLeast(0)
        val endBin = (centerBin + PEAK_SEARCH_BINS).coerceAtMost(spectrum.size - 1)

        var peakMagnitude = 0f
        for (bin in startBin..endBin) {
            if (spectrum[bin] > peakMagnitude) {
                peakMagnitude = spectrum[bin]
            }
        }

        return peakMagnitude.coerceAtLeast(0f)
    }

    /**
     * Checks if a frequency is within the valid guitar range.
     */
    private fun isInGuitarRange(frequency: Double): Boolean = frequency in MIN_GUITAR_FREQ..MAX_GUITAR_FREQ
}
