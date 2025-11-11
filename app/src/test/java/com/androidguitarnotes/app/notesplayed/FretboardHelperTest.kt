package com.androidguitarnotes.app.notesplayed

import org.junit.Assert.*
import org.junit.Test

class FretboardHelperTest {
    @Test
    fun `getNoteAtPosition should return correct note for open string 1 (high E)`() {
        val note = FretboardHelper.getNoteAtPosition(1, 0)
        assertEquals("E", note)
    }

    @Test
    fun `getNoteAtPosition should return correct note for open string 6 (low E)`() {
        val note = FretboardHelper.getNoteAtPosition(6, 0)
        assertEquals("E", note)
    }

    @Test
    fun `getNoteAtPosition should return correct note for string 5 fret 5 (D)`() {
        val note = FretboardHelper.getNoteAtPosition(5, 5)
        assertEquals("D", note)
    }

    @Test
    fun `getNoteAtPosition should return correct note for string 3 fret 2 (A)`() {
        val note = FretboardHelper.getNoteAtPosition(3, 2)
        assertEquals("A", note)
    }

    @Test
    fun `findPositionsForNote should find all E positions on fretboard up to fret 12`() {
        val positions = FretboardHelper.findPositionsForNote("E", 12)

        // Should find E on multiple strings and frets
        assertTrue(positions.isNotEmpty())
        assertTrue(positions.any { it.stringNumber == 1 && it.fret == 0 }) // High E open
        assertTrue(positions.any { it.stringNumber == 6 && it.fret == 0 }) // Low E open
    }

    @Test
    fun `findPositionsForNote should find all A positions on fretboard up to fret 12`() {
        val positions = FretboardHelper.findPositionsForNote("A", 12)

        // Should find A on multiple strings and frets
        assertTrue(positions.isNotEmpty())
        assertTrue(positions.any { it.stringNumber == 5 && it.fret == 0 }) // A string open
    }

    @Test
    fun `findPositionsForNote should limit results to maxFret`() {
        val positions = FretboardHelper.findPositionsForNote("E", 5)

        // All positions should be at or below fret 5
        assertTrue(positions.all { it.fret <= 5 })
    }

    @Test
    fun `findPositionsForNote should return multiple positions for common notes`() {
        val positions = FretboardHelper.findPositionsForNote("A", 12)

        // A appears multiple times on the fretboard
        assertTrue("Expected at least 6 positions for note A", positions.size >= 6)
    }
}
