package com.androidguitarnotes.app.audio

import kotlin.math.abs

/**
 * Detects the pitch (fundamental frequency) from audio samples using autocorrelation.
 */
class PitchDetector(
    private val sampleRate: Int = 44100
) {
    companion object {
        private const val MIN_FREQUENCY = 60.0  // Low E2 (~82 Hz), with margin
        private const val MAX_FREQUENCY = 1500.0  // High E4 + harmonics
    }
    
    /**
     * Detects the fundamental frequency from audio samples.
     * 
     * @param audioData Array of audio samples (PCM 16-bit)
     * @return Detected frequency in Hz, or null if no clear pitch detected
     */
    fun detectPitch(audioData: ShortArray): Double? {
        if (audioData.isEmpty()) return null
        
        // Convert to float and normalize
        val normalized = FloatArray(audioData.size) { i ->
            audioData[i].toFloat() / Short.MAX_VALUE
        }
        
        // Calculate autocorrelation
        val minLag = (sampleRate / MAX_FREQUENCY).toInt()
        val maxLag = (sampleRate / MIN_FREQUENCY).toInt()
        
        if (maxLag >= normalized.size) return null
        
        var bestLag = 0
        var bestCorrelation = 0f
        
        for (lag in minLag..maxLag) {
            var correlation = 0f
            for (i in 0 until (normalized.size - lag)) {
                correlation += normalized[i] * normalized[i + lag]
            }
            
            if (correlation > bestCorrelation) {
                bestCorrelation = correlation
                bestLag = lag
            }
        }
        
        // Check if we found a strong enough correlation
        if (bestCorrelation < 0.1f || bestLag == 0) {
            return null
        }
        
        // Calculate frequency from lag
        val frequency = sampleRate.toDouble() / bestLag.toDouble()
        
        // Validate frequency is in expected range
        return if (frequency in MIN_FREQUENCY..MAX_FREQUENCY) {
            frequency
        } else {
            null
        }
    }
    
    /**
     * Applies a simple high-pass filter to remove DC offset and low-frequency noise.
     */
    private fun applyHighPassFilter(data: FloatArray): FloatArray {
        if (data.size < 2) return data
        
        val filtered = FloatArray(data.size)
        val alpha = 0.95f
        
        filtered[0] = data[0]
        for (i in 1 until data.size) {
            filtered[i] = alpha * (filtered[i - 1] + data[i] - data[i - 1])
        }
        
        return filtered
    }
}
