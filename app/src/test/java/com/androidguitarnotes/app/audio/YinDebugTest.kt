package com.androidguitarnotes.app.audio

import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class YinDebugTest {
    private val sampleRate = 44100

    @Test
    fun `debug YIN detection for E2`() {
        val frequency = 82.4
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()
        
        println("\n" + "=".repeat(80))
        println("YIN Debug Test for $frequency Hz")
        println("=".repeat(80))
        
        val audioData = generateSineWave(frequency, sampleRate, samples)
        
        val expectedPeriod = sampleRate / frequency
        println("Expected period: $expectedPeriod samples")
        println("Expected frequency: $frequency Hz")
        println()
        
        // Create detector and manually step through algorithm
        val minLag = (sampleRate / 1500.0).toInt().coerceAtLeast(2)
        val maxLag = (sampleRate / 60.0).toInt()
        
        println("minLag: $minLag samples (${sampleRate.toDouble() / minLag} Hz)")
        println("maxLag: $maxLag samples (${sampleRate.toDouble() / maxLag} Hz)")
        println()
        
        // Step 1: Calculate difference function
        val difference = FloatArray(maxLag + 1)
        for (tau in 0..maxLag) {
            var sum = 0.0
            for (j in 0 until (audioData.size - tau)) {
                val delta = audioData[j] - audioData[j + tau]
                sum += delta * delta
            }
            difference[tau] = sum.toFloat()
        }
        
        // Find minimum in raw difference around expected period
        val searchStart = (expectedPeriod * 0.9).toInt().coerceAtLeast(minLag)
        val searchEnd = kotlin.math.min((expectedPeriod * 1.1).toInt(), maxLag)
        
        var rawMinTau = searchStart
        var rawMinValue = difference[searchStart]
        for (tau in searchStart..searchEnd) {
            if (difference[tau] < rawMinValue) {
                rawMinValue = difference[tau]
                rawMinTau = tau
            }
        }
        
        println("Raw difference minimum in range [$searchStart, $searchEnd]:")
        println("  tau=$rawMinTau (${sampleRate.toDouble() / rawMinTau} Hz)")
        println()
        
        // Step 2: Calculate cumulative mean normalized difference
        val normalized = FloatArray(difference.size)
        normalized[0] = 1f
        var cumulativeSum = 0.0
        
        for (tau in 1 until difference.size) {
            cumulativeSum += difference[tau]
            val mean = cumulativeSum / tau
            normalized[tau] = if (mean > 0) {
                difference[tau] / mean.toFloat()
            } else {
                1f
            }
        }
        
        // Find minimum in normalized difference
        var normMinTau = searchStart
        var normMinValue = normalized[searchStart]
        for (tau in searchStart..searchEnd) {
            if (normalized[tau] < normMinValue) {
                normMinValue = normalized[tau]
                normMinTau = tau
            }
        }
        
        println("Normalized difference minimum in range:")
        println("  tau=$normMinTau (${sampleRate.toDouble() / normMinTau} Hz)")
        println("  value=$normMinValue")
        println()
        
        // Step 3: Find first below threshold
        val threshold = 0.1f
        var firstBelow = -1
        for (tau in minLag until normalized.size) {
            if (normalized[tau] < threshold) {
                firstBelow = tau
                break
            }
        }
        
        if (firstBelow > 0) {
            println("First below threshold ($threshold) starting from minLag=$minLag:")
            println("  tau=$firstBelow (${sampleRate.toDouble() / firstBelow} Hz)")
            println("  value=${normalized[firstBelow]}")
        } else {
            println("No value below threshold!")
        }
        println()
        
        // Show some values around expected period
        println("Normalized difference values around expected period:")
        println("Tau\tFreq(Hz)\tNormalized\tRaw Diff")
        for (tau in (expectedPeriod.toInt() - 50)..(expectedPeriod.toInt() + 50) step 10) {
            if (tau >= minLag && tau < normalized.size) {
                val freq = sampleRate.toDouble() / tau
                println("$tau\t${String.format("%.2f", freq)}\t\t${String.format("%.6f", normalized[tau])}\t${difference[tau]}")
            }
        }
        
        println("\n" + "=".repeat(80))
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
            audioData[i] = (amplitude * sin(2 * PI * frequency * time)).toFloat()
        }
        return audioData
    }
}
