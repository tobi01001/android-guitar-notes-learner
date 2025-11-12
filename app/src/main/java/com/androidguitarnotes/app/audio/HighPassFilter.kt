package com.androidguitarnotes.app.audio

import kotlin.math.PI

/**
 * A lightweight one-pole IIR high-pass filter for removing low-frequency rumble and noise.
 *
 * This filter is designed to attenuate frequencies below the cutoff frequency (typically 50-60 Hz)
 * to reduce non-musical artifacts such as handling noise, wind rumble, and low-frequency
 * environmental noise that can interfere with guitar pitch detection.
 *
 * ## Implementation Details
 *
 * Uses a simple one-pole IIR (Infinite Impulse Response) high-pass filter with the
 * transfer function:
 * ```
 * y[n] = α * (y[n-1] + x[n] - x[n-1])
 * ```
 * where α (alpha) is the filter coefficient calculated from the cutoff frequency.
 *
 * ## Usage
 *
 * ```kotlin
 * val filter = HighPassFilter(sampleRate = 44100, cutoffFrequency = 60.0)
 * val filteredSamples = filter.process(audioSamples)
 * ```
 *
 * ## Tuning Guidelines
 *
 * - **Default 60 Hz**: Good general-purpose cutoff for guitar applications
 *   - Removes most handling noise and rumble
 *   - Well below lowest guitar note (E2 at ~82 Hz)
 *   - Minimal impact on guitar tone
 *
 * - **Lower cutoff (50 Hz)**: Use if guitar detection is affected
 *   - More conservative filtering
 *   - Allows more low-frequency content through
 *
 * - **Higher cutoff (70-80 Hz)**: Use in very noisy environments
 *   - More aggressive filtering
 *   - May slightly affect lowest E string (82 Hz)
 *   - Use with caution
 *
 * ## Performance
 *
 * - Minimal CPU overhead (one multiply, two additions per sample)
 * - Minimal memory footprint (two float state variables)
 * - Processes samples in-place for efficiency
 *
 * @param sampleRate Sample rate in Hz (typically 44100)
 * @param cutoffFrequency Cutoff frequency in Hz (typically 50-60 Hz)
 *
 * @see AUDIO_DETECTION_ANALYSIS.md Section 7.2.3 for implementation details
 */
class HighPassFilter(
    sampleRate: Int = 44100,
    cutoffFrequency: Double = 60.0,
) {
    // Filter state variables
    private var prevInput = 0f
    private var prevOutput = 0f

    // Filter coefficient (alpha)
    private val alpha: Float

    init {
        require(sampleRate > 0) { "Sample rate must be positive, got $sampleRate" }
        require(cutoffFrequency > 0) { "Cutoff frequency must be positive, got $cutoffFrequency" }
        require(cutoffFrequency < sampleRate / 2) {
            "Cutoff frequency must be less than Nyquist frequency (${sampleRate / 2} Hz), got $cutoffFrequency"
        }

        // Calculate filter coefficient
        // RC = 1 / (2π × fc)
        // α = RC / (RC + dt)
        // where dt = 1 / sampleRate
        val rc = 1.0 / (2.0 * PI * cutoffFrequency)
        val dt = 1.0 / sampleRate
        alpha = (rc / (rc + dt)).toFloat()
    }

    /**
     * Processes a single audio sample through the high-pass filter.
     *
     * @param input Input sample (typically in range -1.0 to 1.0)
     * @return Filtered output sample
     */
    fun process(input: Float): Float {
        // One-pole IIR high-pass filter
        // y[n] = α * (y[n-1] + x[n] - x[n-1])
        val output = alpha * (prevOutput + input - prevInput)

        // Update state variables
        prevInput = input
        prevOutput = output

        return output
    }

    /**
     * Processes an array of audio samples through the high-pass filter.
     *
     * This method processes samples in-place for efficiency, modifying the input array.
     *
     * @param samples Input audio samples (modified in-place)
     * @return The same array reference with filtered samples
     */
    fun process(samples: FloatArray): FloatArray {
        for (i in samples.indices) {
            samples[i] = process(samples[i])
        }
        return samples
    }

    /**
     * Resets the filter state.
     *
     * Call this when starting a new audio stream or when discontinuity is detected
     * to prevent transients from previous audio affecting the current stream.
     */
    fun reset() {
        prevInput = 0f
        prevOutput = 0f
    }
}
