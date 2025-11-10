package com.androidguitarnotes.app.audio

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests for NoteRecognizer.
 */
class NoteRecognizerTest {
    
    private val recognizer = NoteRecognizer()
    
    @Test
    fun `recognizeNote identifies A4 correctly`() {
        val result = recognizer.recognizeNote(440.0)
        
        assertEquals("A", result.noteName)
        assertEquals(440.0, result.frequency, 0.01)
        assertTrue("Cents should be close to 0", abs(result.cents) < 1.0)
    }
    
    @Test
    fun `recognizeNote identifies C correctly`() {
        val cFrequency = 261.63  // C4
        val result = recognizer.recognizeNote(cFrequency)
        
        assertEquals("C", result.noteName)
        assertEquals(cFrequency, result.frequency, 0.01)
    }
    
    @Test
    fun `recognizeNote identifies E correctly`() {
        val eFrequency = 329.63  // E4
        val result = recognizer.recognizeNote(eFrequency)
        
        assertEquals("E", result.noteName)
        assertEquals(eFrequency, result.frequency, 0.01)
    }
    
    @Test
    fun `recognizeNote identifies sharp notes correctly`() {
        val fSharpFrequency = 369.99  // F#4
        val result = recognizer.recognizeNote(fSharpFrequency)
        
        assertEquals("F#", result.noteName)
    }
    
    @Test
    fun `matchesNote returns true for exact match`() {
        val frequency = 440.0  // A4
        
        assertTrue(recognizer.matchesNote(frequency, "A"))
    }
    
    @Test
    fun `matchesNote returns false for wrong note`() {
        val frequency = 440.0  // A4
        
        assertFalse(recognizer.matchesNote(frequency, "C"))
    }
    
    @Test
    fun `matchesNote returns true for note within threshold`() {
        val frequency = 442.0  // Slightly sharp A4
        
        assertTrue(recognizer.matchesNote(frequency, "A"))
    }
    
    @Test
    fun `matchesNote returns false for note outside threshold`() {
        val frequency = 470.0  // Between A# and B
        
        assertFalse(recognizer.matchesNote(frequency, "A"))
    }
    
    @Test
    fun `getNoteFrequency calculates correct frequency`() {
        val aFrequency = recognizer.getNoteFrequency("A", 4)
        
        assertEquals(440.0, aFrequency, 0.01)
    }
    
    @Test
    fun `getNoteFrequency handles different octaves`() {
        val a3Frequency = recognizer.getNoteFrequency("A", 3)
        val a4Frequency = recognizer.getNoteFrequency("A", 4)
        
        // A3 should be half the frequency of A4
        assertEquals(a4Frequency / 2.0, a3Frequency, 1.0)
    }
}
