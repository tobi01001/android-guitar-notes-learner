package com.androidguitarnotes.app.audio

import kotlin.math.abs
import kotlin.math.min

/**
 * YIN pitch detection algorithm implementation with enhancements.
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
 * ## Enhancements (Issue #84):
 * - **Adaptive Threshold**: Dynamically adjusts threshold based on signal characteristics
 * - **Multi-Period Analysis**: Validates detected period against multiple candidates
 *
 * This implementation is designed for guitar note detection (60 Hz - 1500 Hz range)
 * and provides improved accuracy (±1 Hz typical, ±0.1 Hz with interpolation) compared
 * to basic autocorrelation.
 */
class YinPitchDetector(
    private val sampleRate: Int = 44100,
    private val threshold: Float = 0.1f, // Base threshold for detection (0.05-0.2 typical)
    private val adaptiveThreshold: Boolean = false, // Enable adaptive threshold adjustment
    private val multiPeriodAnalysis: Boolean = false, // Enable multi-period validation
) {
    companion object {
        private const val MIN_FREQUENCY = 60.0 // Low E2 (~82 Hz), with margin
        private const val MAX_FREQUENCY = 1500.0 // High E4 + harmonics

        // Minimum signal energy to avoid processing silence
        private const val MIN_ENERGY_THRESHOLD = 1e-10
        
        // Adaptive threshold parameters
        private const val ADAPTIVE_THRESHOLD_MIN = 0.05f // Minimum threshold for clean signals
        private const val ADAPTIVE_THRESHOLD_MAX = 0.25f // Maximum threshold for noisy signals
        private const val HIGH_SNR_THRESHOLD = 20.0 // High SNR in dB
        private const val LOW_SNR_THRESHOLD = 5.0 // Low SNR in dB
        
        // Multi-period analysis parameters
        private const val MAX_PERIOD_CANDIDATES = 3 // Number of period candidates to validate
    }

    /**
     * Result of YIN pitch detection including confidence metric.
     */
    data class YinResult(
        val frequency: Double,
        val confidence: Float, // 0.0 to 1.0, where lower values indicate better periodicity
    )

    /**
     * Detects the fundamental frequency using the YIN algorithm with enhancements.
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

        // Enhancement 1: Adaptive Threshold - adjust based on signal characteristics
        val effectiveThreshold = if (adaptiveThreshold) {
            calculateAdaptiveThreshold(audioData, normalizedDifference, energy)
        } else {
            threshold
        }

        // Step 3: Find the first lag below threshold (absolute threshold)
        val detectedLag =
            findAbsoluteThreshold(normalizedDifference, minLag, effectiveThreshold)
                ?: return null

        // Enhancement 2: Multi-Period Analysis - validate against multiple candidates
        val validatedLag = if (multiPeriodAnalysis) {
            validateMultiplePeriods(normalizedDifference, detectedLag, minLag, effectiveThreshold)
        } else {
            detectedLag
        }

        // Step 4: Apply parabolic interpolation for sub-sample accuracy
        val refinedLag = parabolicInterpolation(normalizedDifference, validatedLag)

        // Calculate frequency from refined lag
        val frequency = sampleRate.toDouble() / refinedLag

        // Validate frequency is in expected range
        if (frequency !in MIN_FREQUENCY..MAX_FREQUENCY) {
            return null
        }

        // Confidence is the normalized difference at the detected lag
        // Lower values indicate better periodicity (inverted from autocorrelation)
        val confidence = normalizedDifference[validatedLag]

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

    /**
     * Enhancement 1: Calculate adaptive threshold based on signal characteristics.
     *
     * Analyzes:
     * - RMS level (signal strength)
     * - Estimated SNR (signal-to-noise ratio)
     * - Harmonic content (periodicity quality from normalized difference)
     *
     * Goals:
     * - Lower threshold for clean, strong signals (better detection of subtle variations)
     * - Higher threshold for noisy, weak signals (reduce false positives)
     *
     * @param audioData Raw audio samples
     * @param normalizedDifference YIN normalized difference function
     * @param energy Total signal energy
     * @return Adapted threshold value (ADAPTIVE_THRESHOLD_MIN to ADAPTIVE_THRESHOLD_MAX)
     */
    private fun calculateAdaptiveThreshold(
        audioData: FloatArray,
        normalizedDifference: FloatArray,
        energy: Double,
    ): Float {
        // Calculate RMS level
        val rms = kotlin.math.sqrt(energy / audioData.size)
        
        // Estimate SNR: ratio of signal power to noise floor estimate
        // Use minimum difference as noise floor proxy
        val minDifference = normalizedDifference.drop(1).minOrNull() ?: 0.5f
        val avgDifference = normalizedDifference.drop(1).average().toFloat()
        
        // SNR estimate in dB (simple heuristic)
        val snrEstimate = if (minDifference > 0.0001f) {
            20 * kotlin.math.log10((avgDifference / minDifference).toDouble())
        } else {
            HIGH_SNR_THRESHOLD // Assume high SNR if very periodic
        }
        
        // Adaptive threshold calculation
        val adaptedThreshold = when {
            // High SNR and good RMS: use stricter threshold for better precision
            snrEstimate >= HIGH_SNR_THRESHOLD && rms >= 0.05 -> {
                ADAPTIVE_THRESHOLD_MIN
            }
            // Low SNR or weak signal: use looser threshold to avoid missing detections
            snrEstimate <= LOW_SNR_THRESHOLD || rms < 0.01 -> {
                ADAPTIVE_THRESHOLD_MAX
            }
            // Medium conditions: interpolate
            else -> {
                val snrFactor = ((snrEstimate - LOW_SNR_THRESHOLD) / (HIGH_SNR_THRESHOLD - LOW_SNR_THRESHOLD))
                    .toFloat()
                    .coerceIn(0f, 1f)
                val rmsFactor = ((rms - 0.01) / (0.05 - 0.01))
                    .toFloat()
                    .coerceIn(0f, 1f)
                
                // Weighted average: prioritize SNR but consider RMS
                val combinedFactor = (snrFactor * 0.7f + rmsFactor * 0.3f)
                ADAPTIVE_THRESHOLD_MAX - combinedFactor * (ADAPTIVE_THRESHOLD_MAX - ADAPTIVE_THRESHOLD_MIN)
            }
        }
        
        return adaptedThreshold.coerceIn(ADAPTIVE_THRESHOLD_MIN, ADAPTIVE_THRESHOLD_MAX)
    }

    /**
     * Enhancement 2: Validate detected period against multiple period candidates.
     *
     * Finds and analyzes multiple local minima in the normalized difference function
     * to confirm the fundamental frequency. This guards against:
     * - Octave errors (detecting 2f instead of f)
     * - Sub-harmonic errors (detecting f/2 instead of f)
     * - Noise-induced false positives
     *
     * Strategy:
     * 1. Find multiple period candidates (local minima below threshold)
     * 2. Check for harmonic relationships (2:1, 3:1, etc.)
     * 3. Choose the most likely fundamental based on:
     *    - Minimum normalized difference (best periodicity)
     *    - Harmonic support (presence of integer multiple periods)
     *
     * @param normalizedDifference YIN normalized difference function
     * @param initialLag Initially detected lag
     * @param minLag Minimum valid lag
     * @param threshold Detection threshold
     * @return Validated lag (may be same as initial or corrected)
     */
    private fun validateMultiplePeriods(
        normalizedDifference: FloatArray,
        initialLag: Int,
        minLag: Int,
        threshold: Float,
    ): Int {
        // Find multiple period candidates (local minima below threshold)
        val candidates = findPeriodCandidates(normalizedDifference, minLag, threshold)
        
        if (candidates.size <= 1) {
            // Only one candidate, no validation needed
            return initialLag
        }
        
        // Analyze candidates for harmonic relationships
        var bestLag = initialLag
        var bestScore = normalizedDifference[initialLag]
        
        for (candidate in candidates.take(MAX_PERIOD_CANDIDATES)) {
            val lag = candidate.first
            val value = candidate.second
            
            // Check if this candidate has harmonic support
            val harmonicSupport = countHarmonicSupport(lag, candidates)
            
            // Score combines periodicity quality (lower value = better)
            // with harmonic support (more harmonics = better)
            // Prefer candidates with strong harmonic support
            val score = value - (harmonicSupport * 0.02f) // Bonus for harmonic support
            
            if (score < bestScore) {
                bestScore = score
                bestLag = lag
            }
        }
        
        return bestLag
    }

    /**
     * Find period candidates: local minima in normalized difference below threshold.
     *
     * @return List of (lag, value) pairs sorted by value (best first)
     */
    private fun findPeriodCandidates(
        normalizedDifference: FloatArray,
        minLag: Int,
        threshold: Float,
    ): List<Pair<Int, Float>> {
        val candidates = mutableListOf<Pair<Int, Float>>()
        
        var tau = minLag
        while (tau < normalizedDifference.size - 1) {
            if (normalizedDifference[tau] < threshold) {
                // Found a point below threshold, find local minimum
                val localMin = findLocalMinimum(normalizedDifference, tau)
                candidates.add(Pair(localMin, normalizedDifference[localMin]))
                
                // Skip ahead to avoid finding the same minimum multiple times
                tau = localMin + (localMin / 2).coerceAtLeast(5)
            } else {
                tau++
            }
        }
        
        // Sort by value (best periodicity first)
        return candidates.sortedBy { it.second }
    }

    /**
     * Count how many other candidates are harmonics of this lag.
     *
     * A harmonic relationship exists if candidate2 ≈ n × candidate1 (where n = 2, 3, ...)
     *
     * @param lag The lag to check
     * @param candidates All period candidates
     * @return Number of harmonics found
     */
    private fun countHarmonicSupport(
        lag: Int,
        candidates: List<Pair<Int, Float>>,
    ): Int {
        var harmonicCount = 0
        
        for (candidate in candidates) {
            val otherLag = candidate.first
            if (otherLag == lag) continue
            
            // Check if otherLag is approximately an integer multiple of lag
            val ratio = otherLag.toDouble() / lag.toDouble()
            val nearestInt = kotlin.math.round(ratio).toInt()
            
            if (nearestInt >= 2 && nearestInt <= 4) {
                val error = abs(ratio - nearestInt)
                if (error < 0.05) { // Within 5% of integer ratio
                    harmonicCount++
                }
            }
        }
        
        return harmonicCount
    }
}
