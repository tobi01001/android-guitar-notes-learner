package com.androidguitarnotes.app.tuner

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for GuitarString.
 */
class GuitarStringTest {
    @Test
    fun `standard tuning has 6 strings`() {
        assertEquals(6, GuitarString.STANDARD_TUNING.size)
    }

    @Test
    fun `standard tuning string 6 is low E`() {
        val string6 = GuitarString.STANDARD_TUNING[0]

        assertEquals(6, string6.number)
        assertEquals("E", string6.noteName)
        assertEquals(2, string6.octave)
        assertEquals(82.41, string6.frequency, 0.01)
    }

    @Test
    fun `standard tuning string 1 is high E`() {
        val string1 = GuitarString.STANDARD_TUNING[5]

        assertEquals(1, string1.number)
        assertEquals("E", string1.noteName)
        assertEquals(4, string1.octave)
        assertEquals(329.63, string1.frequency, 0.01)
    }

    @Test
    fun `standard tuning contains correct notes`() {
        val noteNames = GuitarString.STANDARD_TUNING.map { it.noteName }

        assertEquals(listOf("E", "A", "D", "G", "B", "E"), noteNames)
    }

    @Test
    fun `standard tuning strings are ordered correctly`() {
        val stringNumbers = GuitarString.STANDARD_TUNING.map { it.number }

        assertEquals(listOf(6, 5, 4, 3, 2, 1), stringNumbers)
    }
}
