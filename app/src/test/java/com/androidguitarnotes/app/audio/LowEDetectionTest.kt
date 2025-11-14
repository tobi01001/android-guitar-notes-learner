package com.androidguitarnotes.app.audio

import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class LowEDetectionTest {
    private val sampleRate = 44100
    
    @Test
    fun `test E2 detection at 82_4 Hz with YIN`() {
        val frequency = 82.4 // Low E string
        val detector = YinPitchDetector(sampleRate = sampleRate)
        
        // Test with different durations
        for (duration in listOf(0.1, 0.2, 0.3, 0.5)) {
            val samples = (sampleRate * duration).toInt()
            val audioData = generateSineWave(frequency, sampleRate, samples)
            val result = detector.detectPitch(audioData)
            
            println("Duration: ${duration}s, Samples: $samples")
            if (result != null) {
                println("  Detected: ${result.frequency} Hz, Confidence: ${result.confidence}")
                println("  Error: ${result.frequency - frequency} Hz")
            } else {
                println("  No detection")
            }
        }
    }
    
    @Test
    fun `test E2 detection at 82_4 Hz with all algorithms`() {
        val frequency = 82.4
        val duration = 0.3
        val samples = (sampleRate * duration).toInt()
        val audioData = generateSineWave(frequency, sampleRate, samples)
        
        val algorithms = listOf(
            PitchDetectionAlgorithm.AUTOCORRELATION,
            PitchDetectionAlgorithm.YIN,
            PitchDetectionAlgorithm.YIN_ADAPTIVE,
            PitchDetectionAlgorithm.YIN_MULTI_PERIOD,
            PitchDetectionAlgorithm.YIN_ENHANCED,
            PitchDetectionAlgorithm.HYBRID_YIN_FFT
        )
        
        for (algo in algorithms) {
            val detector = PitchDetector(sampleRate = sampleRate, algorithm = algo)
            val result = detector.detectPitchWithConfidence(audioData)
            
            println("Algorithm: $algo")
            if (result != null) {
                println("  Detected: ${result.frequency} Hz, Confidence: ${result.confidence}")
                println("  Error: ${result.frequency - frequency} Hz")
                println("  Semitone error: ${calculateSemitoneError(result.frequency, frequency)}")
            } else {
                println("  No detection")
            }
        }
    }
    
    private fun calculateSemitoneError(detected: Double, expected: Double): Double {
        return 12 * kotlin.math.log2(detected / expected)
    }
    
    private fun generateSineWave(
        frequency: Double,
        sampleRate: Int,
        samples: Int,
        amplitude: Float = 0.8f,
    ): FloatArray {
        val audioData = FloatArray(samples)
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val value = amplitude * sin(2 * PI * frequency * time)
            audioData[i] = value.toFloat()
        }
        return audioData
    }
}
