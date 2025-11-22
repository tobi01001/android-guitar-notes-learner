package com.androidguitarnotes.app.audio

import kotlin.math.sqrt

/**
 * Pitch detection algorithm selection.
 */
enum class PitchDetectionAlgorithm {
    /**
     * Normalized autocorrelation method (original implementation).
     * Good for basic pitch detection, moderate accuracy.
     */
    AUTOCORRELATION,

    /**
     * YIN algorithm with parabolic interpolation.
     * Improved accuracy (±1 Hz), better noise robustness, fewer octave errors.
     * Recommended for guitar tuning and precise note detection.
     */
    YIN,

    /**
     * YIN with adaptive threshold (Enhancement #84.1).
     * Dynamically adjusts threshold based on signal characteristics.
     * Better for varying signal conditions (quiet notes, noisy backgrounds).
     */
    YIN_ADAPTIVE,

    /**
     * YIN with multi-period analysis (Enhancement #84.2).
     * Validates detected period against multiple candidates.
     * Reduces octave errors and false positives.
     */
    YIN_MULTI_PERIOD,

    /**
     * YIN with both adaptive threshold and multi-period analysis (Enhancement #84.1+2).
     * Combines both enhancements for maximum robustness.
     */
    YIN_ENHANCED,

    /**
     * Hybrid YIN + FFT detector (Enhancement #84.3).
     * Combines time-domain (YIN) and frequency-domain (FFT) analysis.
     * Best accuracy and robustness for challenging cases.
     */
    HYBRID_YIN_FFT,

    /**
     * YIN with harmonic consistency validation (Enhancement ENH-002).
     * Uses FFT-based harmonic analysis to detect and correct octave errors.
     * Significant accuracy improvement for natural harmonics and high register.
     */
    YIN_HARMONIC,

    /**
     * YIN Enhanced with harmonic consistency validation (ENH-002).
     * Combines adaptive threshold, multi-period analysis, and harmonic validation.
     * Maximum accuracy and octave error reduction.
     */
    YIN_ENHANCED_HARMONIC,
}

/**
 * Detects the pitch (fundamental frequency) from audio samples.
 *
 * Supports multiple detection algorithms:
 * - AUTOCORRELATION: Normalized autocorrelation (original implementation)
 * - YIN: YIN algorithm with parabolic interpolation (improved accuracy)
 * - YIN_ADAPTIVE: YIN with adaptive threshold (Enhancement #84.1)
 * - YIN_MULTI_PERIOD: YIN with multi-period analysis (Enhancement #84.2)
 * - YIN_ENHANCED: YIN with both adaptive threshold and multi-period analysis
 * - HYBRID_YIN_FFT: Hybrid YIN + FFT detector (Enhancement #84.3)
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
 *
 * ## YIN Algorithm
 *
 * The YIN algorithm (De Cheveigné & Kawahara, 2002) improves upon autocorrelation with:
 * - Cumulative mean normalized difference function
 * - Absolute threshold for period detection
 * - Parabolic interpolation for sub-sample accuracy
 * - Better noise robustness and fewer octave errors
 *
 * ## YIN Enhancements (Issue #84)
 *
 * ### Adaptive Threshold (#84.1)
 * Dynamically adjusts YIN's threshold based on signal characteristics (RMS, SNR, harmonic content).
 * Benefits: Better accuracy across varying conditions, fewer false positives.
 *
 * ### Multi-Period Analysis (#84.2)
 * Validates detected period against multiple candidates to confirm fundamental frequency.
 * Benefits: Reduced octave errors, better disambiguation of harmonically rich signals.
 *
 * ### Hybrid YIN + FFT (#84.3)
 * Combines time-domain (YIN) and frequency-domain (FFT) analysis for robust detection.
 * Benefits: Excellent accuracy in challenging cases, octave error correction via harmonics.
 */
class PitchDetector(
    private val sampleRate: Int = 44100,
    private val algorithm: PitchDetectionAlgorithm = PitchDetectionAlgorithm.YIN,
) {
    private val yinDetector = YinPitchDetector(sampleRate)
    private val yinAdaptiveDetector = YinPitchDetector(sampleRate, adaptiveThreshold = true)
    private val yinMultiPeriodDetector = YinPitchDetector(sampleRate, multiPeriodAnalysis = true)
    private val yinEnhancedDetector =
        YinPitchDetector(
            sampleRate,
            adaptiveThreshold = true,
            multiPeriodAnalysis = true,
        )
    private val hybridDetector = HybridYinFftDetector(sampleRate)
    private val harmonicValidator = HarmonicValidator(sampleRate)

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
     * Detects the fundamental frequency from audio samples.
     *
     * Uses the configured algorithm (YIN by default, or AUTOCORRELATION).
     *
     * @param audioData Array of audio samples (PCM float, no amplitude restrictions)
     * @return PitchResult with frequency and confidence, or null if no clear pitch detected
     */
    fun detectPitchWithConfidence(audioData: FloatArray): PitchResult? =
        when (algorithm) {
            PitchDetectionAlgorithm.YIN -> {
                val yinResult = yinDetector.detectPitch(audioData)
                yinResult?.let {
                    // YIN confidence is inverted (lower = better), so we invert it for consistency
                    PitchResult(it.frequency, 1.0f - it.confidence)
                }
            }
            PitchDetectionAlgorithm.YIN_ADAPTIVE -> {
                val yinResult = yinAdaptiveDetector.detectPitch(audioData)
                yinResult?.let {
                    PitchResult(it.frequency, 1.0f - it.confidence)
                }
            }
            PitchDetectionAlgorithm.YIN_MULTI_PERIOD -> {
                val yinResult = yinMultiPeriodDetector.detectPitch(audioData)
                yinResult?.let {
                    PitchResult(it.frequency, 1.0f - it.confidence)
                }
            }
            PitchDetectionAlgorithm.YIN_ENHANCED -> {
                val yinResult = yinEnhancedDetector.detectPitch(audioData)
                yinResult?.let {
                    PitchResult(it.frequency, 1.0f - it.confidence)
                }
            }
            PitchDetectionAlgorithm.YIN_HARMONIC -> {
                val yinResult = yinDetector.detectPitch(audioData)
                yinResult?.let {
                    // Apply harmonic validation to correct potential octave errors
                    val validatedFrequency = harmonicValidator.validatePitch(audioData, it.frequency)
                    validatedFrequency?.let { freq ->
                        PitchResult(freq, 1.0f - it.confidence)
                    }
                }
            }
            PitchDetectionAlgorithm.YIN_ENHANCED_HARMONIC -> {
                val yinResult = yinEnhancedDetector.detectPitch(audioData)
                yinResult?.let {
                    // Apply harmonic validation to correct potential octave errors
                    val validatedFrequency = harmonicValidator.validatePitch(audioData, it.frequency)
                    validatedFrequency?.let { freq ->
                        PitchResult(freq, 1.0f - it.confidence)
                    }
                }
            }
            PitchDetectionAlgorithm.HYBRID_YIN_FFT -> {
                val hybridResult = hybridDetector.detectPitch(audioData)
                hybridResult?.let {
                    PitchResult(it.frequency, it.confidence)
                }
            }
            PitchDetectionAlgorithm.AUTOCORRELATION -> {
                detectPitchAutocorrelation(audioData)
            }
        }

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
    private fun detectPitchAutocorrelation(audioData: FloatArray): PitchResult? {
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
