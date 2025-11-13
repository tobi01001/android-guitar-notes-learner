package com.androidguitarnotes.app.audio

import kotlin.math.sqrt

/**
 * Detects the pitch (fundamental frequency) from audio samples using normalized autocorrelation.
 * 
 * ## Normalized Autocorrelation
 * 
 * This implementation uses normalized autocorrelation to make pitch detection amplitude-independent.
 * Instead of raw correlation values (which depend on signal strength), we normalize by the
 * autocorrelation at lag=0, producing a confidence value between 0.0 and 1.0.
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
        private const val MIN_CONFIDENCE = 0.3f // Minimum confidence threshold for detection
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
     * @param audioData Array of audio samples (PCM float, no amplitude restrictions)
     * @return PitchResult with frequency and confidence, or null if no clear pitch detected
     */
    fun detectPitchWithConfidence(audioData: FloatArray): PitchResult? {
        if (audioData.isEmpty()) return null

        // Calculate autocorrelation at lag 0 (signal energy)
        var energy = 0.0
        for (sample in audioData) {
            energy += sample * sample
        }
        
        if (energy < 1e-10) return null // Signal too weak

        // Calculate normalized autocorrelation for each lag
        val minLag = (sampleRate / MAX_FREQUENCY).toInt()
        val maxLag = (sampleRate / MIN_FREQUENCY).toInt()

        if (maxLag >= audioData.size) return null

        var bestLag = 0
        var bestNormalizedCorrelation = 0f

        for (lag in minLag..maxLag) {
            var correlation = 0.0
            var lagEnergy = 0.0
            
            for (i in 0 until (audioData.size - lag)) {
                correlation += audioData[i] * audioData[i + lag]
                lagEnergy += audioData[i + lag] * audioData[i + lag]
            }

            // Normalize correlation by geometric mean of energies
            val normalizedCorrelation = if (lagEnergy > 1e-10) {
                (correlation / sqrt(energy * lagEnergy)).toFloat()
            } else {
                0f
            }

            if (normalizedCorrelation > bestNormalizedCorrelation) {
                bestNormalizedCorrelation = normalizedCorrelation
                bestLag = lag
            }
        }

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

    /**
     * Detects the fundamental frequency from audio samples.
     * 
     * Legacy method for backward compatibility. Use detectPitchWithConfidence() for better results.
     *
     * @param audioData Array of audio samples (PCM float)
     * @return Detected frequency in Hz, or null if no clear pitch detected
     */
    fun detectPitch(audioData: FloatArray): Double? {
        return detectPitchWithConfidence(audioData)?.frequency
    }
}
