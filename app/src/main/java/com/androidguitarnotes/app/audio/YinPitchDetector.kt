package com.androidguitarnotes.app.audio

import kotlin.math.abs
import kotlin.math.min

/**
 * YIN pitch detection algorithm implementation.
 *
 * Based on "YIN, a fundamental frequency estimator for speech and music"
 * by De Cheveigné & Kawahara (2002).
 *
 * The YIN algorithm improves upon basic autocorrelation by:
 * 1. Using a difference function instead of correlation
 * 2. Applying cumulative mean normalization to reduce octave errors
 * 3. Using absolute thresholding for reliable period detection
 * 4. Adding parabolic interpolation for sub-sample accuracy
 *
 * This implementation is designed for guitar note detection (60 Hz - 1500 Hz range)
 * and provides improved accuracy (±1 Hz typical, ±0.1 Hz with interpolation) compared
 * to basic autocorrelation.
 */
class YinPitchDetector(
    private val sampleRate: Int = 44100,
    private val threshold: Float = 0.1f, // Absolute threshold for detection (0.05-0.2 typical)
) {
    companion object {
        private const val MIN_FREQUENCY = 60.0 // Low E2 (~82 Hz), with margin
        private const val MAX_FREQUENCY = 1500.0 // High E4 + harmonics

        // Minimum signal energy to avoid processing silence
        private const val MIN_ENERGY_THRESHOLD = 1e-10
    }

    /**
     * Result of YIN pitch detection including confidence metric.
     */
    data class YinResult(
        val frequency: Double,
        val confidence: Float, // 0.0 to 1.0, where lower values indicate better periodicity
    )

    /**
     * Detects the fundamental frequency using the YIN algorithm.
     *
     * @param audioData Array of audio samples (PCM float)
     * @return YinResult with frequency and confidence, or null if no clear pitch detected
     */
    fun detectPitch(audioData: FloatArray): YinResult? {
        if (audioData.isEmpty()) return null

        // Calculate lag bounds based on frequency range
        val minLag = (sampleRate / MAX_FREQUENCY).toInt().coerceAtLeast(2)
        val maxLag = (sampleRate / MIN_FREQUENCY).toInt()

        // Ensure we have enough samples
        if (maxLag >= audioData.size) return null

        // Check signal energy
        val energy = calculateEnergy(audioData)
        if (energy < MIN_ENERGY_THRESHOLD) return null

        // Step 1: Calculate difference function
        val differenceFunction = calculateDifference(audioData, maxLag)

        // Step 2: Calculate cumulative mean normalized difference function
        val normalizedDifference = cumulativeMeanNormalizedDifference(differenceFunction)

        // Step 3: Find the first lag below threshold (absolute threshold)
        val detectedLag =
            findAbsoluteThreshold(normalizedDifference, minLag, threshold)
                ?: return null

        // Step 4: Apply parabolic interpolation for sub-sample accuracy
        val refinedLag = parabolicInterpolation(normalizedDifference, detectedLag)

        // Calculate frequency from refined lag
        val frequency = sampleRate.toDouble() / refinedLag

        // Validate frequency is in expected range
        if (frequency !in MIN_FREQUENCY..MAX_FREQUENCY) {
            return null
        }

        // Confidence is the normalized difference at the detected lag
        // Lower values indicate better periodicity (inverted from autocorrelation)
        val confidence = normalizedDifference[detectedLag]

        return YinResult(frequency, confidence)
    }

    /**
     * Step 1: Calculate the difference function (squared difference).
     *
     * d_t(tau) = sum((x_j - x_{j+tau})^2)
     *
     * This measures how different the signal is from a time-shifted version of itself.
     */
    private fun calculateDifference(
        audioData: FloatArray,
        maxLag: Int,
    ): FloatArray {
        val difference = FloatArray(maxLag + 1)

        for (tau in 0..maxLag) {
            var sum = 0.0
            for (j in 0 until (audioData.size - maxLag)) {
                val delta = audioData[j] - audioData[j + tau]
                sum += delta * delta
            }
            difference[tau] = sum.toFloat()
        }

        return difference
    }

    /**
     * Step 2: Calculate cumulative mean normalized difference function.
     *
     * d'_t(tau) = d_t(tau) / [(1/tau) * sum_{j=1}^{tau} d_t(j)]
     *
     * This normalization reduces the bias towards shorter periods and helps
     * prevent octave errors. The first value d'_t(0) is set to 1 by definition.
     */
    private fun cumulativeMeanNormalizedDifference(difference: FloatArray): FloatArray {
        val normalized = FloatArray(difference.size)
        normalized[0] = 1f // By definition

        var cumulativeSum = 0.0

        for (tau in 1 until difference.size) {
            cumulativeSum += difference[tau]

            // Avoid division by zero
            val mean = cumulativeSum / tau
            normalized[tau] =
                if (mean > 0) {
                    difference[tau] / mean.toFloat()
                } else {
                    1f
                }
        }

        return normalized
    }

    /**
     * Step 3: Find the first lag where normalized difference drops below threshold.
     *
     * This implements the absolute threshold method: find the smallest tau >= minLag
     * where d'_t(tau) < threshold.
     *
     * @return detected lag or null if no lag meets the threshold
     */
    private fun findAbsoluteThreshold(
        normalizedDifference: FloatArray,
        minLag: Int,
        threshold: Float,
    ): Int? {
        for (tau in minLag until normalizedDifference.size) {
            if (normalizedDifference[tau] < threshold) {
                // Search for local minimum near this point
                return findLocalMinimum(normalizedDifference, tau)
            }
        }
        return null
    }

    /**
     * Find local minimum around the given tau.
     *
     * After finding the first point below threshold, we search for a local minimum
     * to get the best period estimate.
     */
    private fun findLocalMinimum(
        normalizedDifference: FloatArray,
        startTau: Int,
    ): Int {
        var minTau = startTau
        var minValue = normalizedDifference[startTau]

        // Search forward until value increases again
        for (tau in (startTau + 1) until min(startTau + 10, normalizedDifference.size)) {
            if (normalizedDifference[tau] < minValue) {
                minValue = normalizedDifference[tau]
                minTau = tau
            } else if (normalizedDifference[tau] > minValue) {
                // Found the minimum, stop searching
                break
            }
        }

        return minTau
    }

    /**
     * Step 4: Parabolic interpolation for sub-sample accuracy.
     *
     * Fits a parabola through the minimum point and its neighbors to estimate
     * the true minimum at sub-sample resolution. This improves accuracy from
     * ±1-2 Hz to ±0.1 Hz.
     *
     * Formula: refined_tau = tau + (alpha - gamma) / (2 * (alpha - 2*beta + gamma))
     *
     * where alpha = d'(tau-1), beta = d'(tau), gamma = d'(tau+1)
     */
    private fun parabolicInterpolation(
        normalizedDifference: FloatArray,
        tau: Int,
    ): Double {
        // Need neighbors for interpolation
        if (tau < 1 || tau >= normalizedDifference.size - 1) {
            return tau.toDouble()
        }

        val alpha = normalizedDifference[tau - 1]
        val beta = normalizedDifference[tau]
        val gamma = normalizedDifference[tau + 1]

        // Calculate the parabolic correction
        val denominator = alpha - 2 * beta + gamma

        // Avoid division by zero or invalid parabola
        if (abs(denominator) < 1e-10) {
            return tau.toDouble()
        }

        val correction = (alpha - gamma) / (2 * denominator)

        // Correction should be small (typically -0.5 to 0.5)
        // If it's large, the parabola fit is poor, so use integer tau
        return if (abs(correction) < 1.0f) {
            tau + correction.toDouble()
        } else {
            tau.toDouble()
        }
    }

    /**
     * Calculate total signal energy.
     */
    private fun calculateEnergy(audioData: FloatArray): Double {
        var energy = 0.0
        for (sample in audioData) {
            energy += sample * sample
        }
        return energy
    }
}
