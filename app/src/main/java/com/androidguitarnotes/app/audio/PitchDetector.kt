package com.androidguitarnotes.app.audio

import kotlin.math.sqrt

/**
 * Detects the pitch (fundamental frequency) from audio samples using normalized autocorrelation.
 *
 * ## Normalized Autocorrelation
 *
 * This implementation uses normalized autocorrelation to make pitch detection amplitude-independent.
 * Instead of raw correlation values (which depend on signal strength), we normalize by the
 * geometric mean of the segment energies, producing a confidence value between 0.0 and 1.0.
 *
 * Benefits:
 * - Quiet but periodic signals are properly detected
 * - Confidence metric indicates signal periodicity quality
 * - More robust to varying input levels
 */
class PitchDetector(
    private val sampleRate: Int = 44100,
) {
    companion object {
        private const val MIN_FREQUENCY = 60.0 // Low E2 (~82 Hz), with margin
        private const val MAX_FREQUENCY = 1500.0 // High E4 + harmonics

        // Minimum confidence threshold for detection.
        // 0.25 was empirically determined to balance false positives (detecting pitch in noisy/unpitched signals)
        // versus detection sensitivity (missing quiet but periodic signals). This value may need adjustment
        // for different instruments, environments, or application requirements.
        private const val MIN_CONFIDENCE = 0.25f

        // Minimum signal energy to avoid numerical instability
        private const val MIN_ENERGY_THRESHOLD = 1e-10
    }

    /**
     * Result of pitch detection including confidence metric.
     */
    data class PitchResult(
        val frequency: Double,
        val confidence: Float, // 0.0 to 1.0, indicates signal periodicity quality
    )

    /**
     * Detects the fundamental frequency from audio samples using normalized autocorrelation.
     *
     * Applies a small bias towards shorter lags to favor the fundamental frequency over sub-harmonics.
     * This is important because normalized autocorrelation can have similar confidence values
     * for multiple periods (fundamental and integer multiples), and we want the fundamental.
     *
     * @param audioData Array of audio samples (PCM float, no amplitude restrictions)
     * @return PitchResult with frequency and confidence, or null if no clear pitch detected
     */
    fun detectPitchWithConfidence(audioData: FloatArray): PitchResult? {
        if (audioData.isEmpty()) return null

        // Calculate normalized autocorrelation for each lag
        val minLag = (sampleRate / MAX_FREQUENCY).toInt()
        val maxLag = (sampleRate / MIN_FREQUENCY).toInt()

        if (maxLag >= audioData.size) return null

        var bestLag = 0
        var bestScore = 0f

        for (lag in minLag..maxLag) {
            var correlation = 0.0
            var segmentEnergy = 0.0
            var lagEnergy = 0.0

            // Calculate correlation and energies for the overlapping segment
            for (i in 0 until (audioData.size - lag)) {
                correlation += audioData[i] * audioData[i + lag]
                segmentEnergy += audioData[i] * audioData[i]
                lagEnergy += audioData[i + lag] * audioData[i + lag]
            }

            // Normalize correlation by geometric mean of segment energies
            val normalizedCorrelation =
                if (segmentEnergy > MIN_ENERGY_THRESHOLD && lagEnergy > MIN_ENERGY_THRESHOLD) {
                    (correlation / sqrt(segmentEnergy * lagEnergy)).toFloat()
                } else {
                    0f
                }

            // Apply a small bias factor to favor shorter lags (fundamental over sub-harmonics)
            // The bias decreases as lag increases, giving preference to higher frequencies
            val lagBias = minLag.toFloat() / lag.toFloat()
            val biasFactor = 1.0f + (lagBias - 1.0f) * 0.05f // 5% maximum bias towards shorter lags
            val score = normalizedCorrelation * biasFactor

            if (score > bestScore) {
                bestScore = score
                bestLag = lag
            }
        }

        // Extract the actual normalized correlation without bias for the result
        val bestNormalizedCorrelation = bestScore / (1.0f + (minLag.toFloat() / bestLag.toFloat() - 1.0f) * 0.05f)

        // Check if we found a strong enough correlation
        if (bestNormalizedCorrelation < MIN_CONFIDENCE || bestLag == 0) {
            return null
        }

        // Calculate frequency from lag
        val frequency = sampleRate.toDouble() / bestLag.toDouble()

        // Validate frequency is in expected range
        return if (frequency in MIN_FREQUENCY..MAX_FREQUENCY) {
            PitchResult(frequency, bestNormalizedCorrelation)
        } else {
            null
        }
    }
}
