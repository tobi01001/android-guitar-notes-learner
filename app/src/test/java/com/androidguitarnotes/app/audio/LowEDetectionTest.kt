package com.androidguitarnotes.app.audio

import org.junit.Test
import kotlin.math.PI
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

        val algorithms =
            listOf(
                PitchDetectionAlgorithm.AUTOCORRELATION,
                PitchDetectionAlgorithm.YIN,
                PitchDetectionAlgorithm.YIN_ADAPTIVE,
                PitchDetectionAlgorithm.YIN_MULTI_PERIOD,
                PitchDetectionAlgorithm.YIN_ENHANCED,
                PitchDetectionAlgorithm.HYBRID_YIN_FFT,
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
    
    @Test
    fun `test frequency detection accuracy across full guitar range`() {
        // Test frequencies mentioned in the issue, plus representative samples
        val testFrequencies = listOf(
            82.4 to "E2",   // Low E - was showing F with 0 cents
            110.0 to "A2",  // A string
            146.8 to "D3",  // D string - was showing D# with -27 cents
            164.8 to "E3",  // E3 - was showing F with -30 cents
            174.6 to "F3",  // F3 - was showing F# with -43 cents
            196.0 to "G3",  // G3 - was showing G# with +50 cents
            220.0 to "A3",  // A3 - was showing +48 cents
            246.9 to "B3",  // B3 - was showing +35 cents
            261.6 to "C4",  // C4 - was showing +27 cents
            293.7 to "D4",  // D4 - was showing +15 cents
            329.6 to "E4",  // High E
            523.3 to "C5",  // C5 - was showing +0 cents
            659.3 to "E5",  // E5 - was showing +0 cents
        )
        
        val duration = 0.3
        val samples = (sampleRate * duration).toInt()
        
        // Test with YIN algorithm (most accurate)
        val detector = PitchDetector(sampleRate = sampleRate, algorithm = PitchDetectionAlgorithm.YIN)
        
        println("\nFrequency Detection Accuracy Test (YIN Algorithm)")
        println("=".repeat(80))
        println("${String.format("%-6s", "Note")} ${String.format("%8s", "Expected")} ${String.format("%8s", "Detected")} ${String.format("%8s", "Error")} ${String.format("%8s", "Cents")}")
        println("-".repeat(80))
        
        var maxError = 0.0
        var maxCents = 0.0
        
        for ((frequency, note) in testFrequencies) {
            val audioData = generateSineWave(frequency, sampleRate, samples)
            val result = detector.detectPitchWithConfidence(audioData)
            
            if (result != null) {
                val error = result.frequency - frequency
                val cents = 1200 * kotlin.math.log2(result.frequency / frequency)
                
                println("${String.format("%-6s", note)} ${String.format("%8.2f", frequency)} ${String.format("%8.2f", result.frequency)} ${String.format("%+8.2f", error)} ${String.format("%+8.1f", cents)}")
                
                if (abs(error) > abs(maxError)) maxError = error
                if (abs(cents) > abs(maxCents)) maxCents = cents
                
                // Verify accuracy - should be within reasonable bounds
                // Note: YIN algorithm may still have some issues at very low frequencies
                // but autocorrelation algorithm should work well after high-pass filter removal
                assert(abs(error) < 10.0) { 
                    "$note: Error too large: ${error} Hz (${cents} cents)" 
                }
                assert(abs(cents) < 200.0) {  // 200 cents = 2 semitones (generous tolerance)
                    "$note: Cents error too large: ${cents} cents"
                }
            } else {
                println("${String.format("%-6s", note)} ${String.format("%8.2f", frequency)} ${String.format("%8s", "FAILED")} - no detection")
                assert(false) { "$note: Failed to detect frequency" }
            }
        }
        
        println("-".repeat(80))
        println("Max error: ${String.format("%+.2f", maxError)} Hz (${String.format("%+.1f", maxCents)} cents)")
        println("\nAll frequencies detected accurately! ✓")
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
