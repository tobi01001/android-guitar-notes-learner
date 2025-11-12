package de.tobi01001.audioprocessing

import kotlin.math.abs
import kotlin.math.sqrt

object AudioProcessingUtils {
    /** Calculate RMS of float array */
    fun calculateRms(buffer: FloatArray, length: Int = buffer.size): Float {
        if (length <= 0) return 0f
        var sum = 0.0f
        for (i in 0 until length) {
            val v = buffer[i]
            sum += v * v
        }
        return sqrt(sum / length)
    }

    /** Apply soft headroom scaling to avoid hard clipping when applying gain. Returns Pair(adjustedBuffer, rawRms) */
    fun applyGainWithHeadroom(src: FloatArray, gain: Float): Pair<FloatArray, Float> {
        val out = FloatArray(src.size)
        val rawRms = calculateRms(src, src.size)
        // compute peak
        var peak = 0f
        for (i in src.indices) {
            val a = abs(src[i])
            if (a > peak) peak = a
        }
        val maxAllowed = if (peak * gain > 1f) 1f / (peak * gain) else 1f
        val finalGain = gain * maxAllowed
        for (i in src.indices) {
            out[i] = (src[i] * finalGain).coerceIn(-1f, 1f)
        }
        return Pair(out, rawRms)
    }

    /** Simple noise gate check */
    fun isSignalPresent(rms: Float, threshold: Float = 0.01f): Boolean {
        return rms > threshold
    }
}