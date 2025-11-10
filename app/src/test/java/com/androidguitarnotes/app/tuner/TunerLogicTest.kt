package com.androidguitarnotes.app.tuner

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.log2

/**
 * Unit tests for tuner logic calculations.
 */
class TunerLogicTest {
    
    /**
     * Helper function to calculate cents deviation (same logic as in TunerViewModel).
     */
    private fun calculateCentsDeviation(detectedFreq: Double, targetFreq: Double): Double {
        return 1200.0 * log2(detectedFreq / targetFreq)
    }
    
    @Test
    fun `cents deviation is zero for exact match`() {
        val targetFreq = 82.41  // Low E
        val detectedFreq = 82.41
        
        val cents = calculateCentsDeviation(detectedFreq, targetFreq)
        
        assertTrue("Cents should be close to 0", abs(cents) < 0.1)
    }
    
    @Test
    fun `positive cents means frequency is sharp`() {
        val targetFreq = 82.41  // Low E
        val detectedFreq = 85.0  // Higher frequency
        
        val cents = calculateCentsDeviation(detectedFreq, targetFreq)
        
        assertTrue("Positive cents indicates sharp", cents > 0)
    }
    
    @Test
    fun `negative cents means frequency is flat`() {
        val targetFreq = 82.41  // Low E
        val detectedFreq = 80.0  // Lower frequency
        
        val cents = calculateCentsDeviation(detectedFreq, targetFreq)
        
        assertTrue("Negative cents indicates flat", cents < 0)
    }
    
    @Test
    fun `one semitone difference is 100 cents`() {
        val targetFreq = 440.0  // A4
        val detectedFreq = 466.16  // A#4 (one semitone up)
        
        val cents = calculateCentsDeviation(detectedFreq, targetFreq)
        
        assertEquals(100.0, cents, 1.0)
    }
    
    @Test
    fun `slightly sharp frequency is within tuning threshold`() {
        val targetFreq = 110.0  // A2
        val detectedFreq = 110.6  // Slightly sharp (~9.4 cents)
        val threshold = 10.0
        
        val cents = calculateCentsDeviation(detectedFreq, targetFreq)
        
        assertTrue("Should be within threshold", abs(cents) <= threshold)
    }
    
    @Test
    fun `very sharp frequency is outside tuning threshold`() {
        val targetFreq = 110.0  // A2
        val detectedFreq = 115.0  // Very sharp (~75 cents)
        val threshold = 10.0
        
        val cents = calculateCentsDeviation(detectedFreq, targetFreq)
        
        assertTrue("Should be outside threshold", abs(cents) > threshold)
    }
    
    @Test
    fun `tuning status with low E string`() {
        val lowE = GuitarString.STANDARD_TUNING[0]
        
        assertEquals("E", lowE.noteName)
        assertEquals(82.41, lowE.frequency, 0.01)
        
        // Test a slightly flat low E
        val detectedFreq = 81.5
        val cents = calculateCentsDeviation(detectedFreq, lowE.frequency)
        
        assertTrue("Should be flat", cents < 0)
        assertTrue("Should be within reasonable range", abs(cents) < 20.0)
    }
    
    @Test
    fun `tuning status with high E string`() {
        val highE = GuitarString.STANDARD_TUNING[5]
        
        assertEquals("E", highE.noteName)
        assertEquals(329.63, highE.frequency, 0.01)
        
        // Test a slightly sharp high E
        val detectedFreq = 331.0
        val cents = calculateCentsDeviation(detectedFreq, highE.frequency)
        
        assertTrue("Should be sharp", cents > 0)
        assertTrue("Should be within reasonable range", abs(cents) < 10.0)
    }
}
