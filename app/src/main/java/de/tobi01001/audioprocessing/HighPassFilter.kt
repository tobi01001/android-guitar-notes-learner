package de.tobi01001.audioprocessing

import kotlin.math.PI

/**
 * One-pole high-pass filter (IIR) using the simple RC approximation.
 * process(sample) -> filtered sample
 */
class HighPassFilter(
    private val sampleRate: Int = 44100,
    cutoffHz: Float = 50f,
) {
    private var prevInput = 0f
    private var prevOutput = 0f
    private var alpha: Float

    var cutoffHz: Float = cutoffHz
        set(value) {
            field = value
            computeAlpha()
        }

    init {
        alpha = 0f
        computeAlpha()
    }

    private fun computeAlpha() {
        val rc = 1.0f / (2.0f * PI.toFloat() * cutoffHz)
        val dt = 1.0f / sampleRate.toFloat()
        alpha = rc / (rc + dt)
    }

    fun process(input: Float): Float {
        val output = alpha * (prevOutput + input - prevInput)
        prevInput = input
        prevOutput = output
        return output
    }

    fun reset() {
        prevInput = 0f
        prevOutput = 0f
    }
}
