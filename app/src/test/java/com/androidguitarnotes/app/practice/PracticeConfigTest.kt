package com.androidguitarnotes.app.practice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for PracticeConfig data class.
 */
class PracticeConfigTest {
    @Test
    fun `default config has all strings selected`() {
        val config = PracticeConfig()

        assertEquals("Should have all 6 strings selected", 6, config.selectedStrings.size)
        assertTrue("Should contain string 1", config.selectedStrings.contains(1))
        assertTrue("Should contain string 6", config.selectedStrings.contains(6))
    }

    @Test
    fun `default config has correct fret range`() {
        val config = PracticeConfig()

        assertEquals("Default fret from should be 0", 0, config.fretFrom)
        assertEquals("Default fret to should be 12", 12, config.fretTo)
    }

    @Test
    fun `default config has WHOLE_NOTES mode`() {
        val config = PracticeConfig()

        assertEquals(
            "Default note mode should be WHOLE_NOTES",
            NoteMode.WHOLE_NOTES,
            config.noteMode,
        )
    }

    @Test
    fun `default config has TIME duration type`() {
        val config = PracticeConfig()

        assertEquals(
            "Default duration type should be TIME",
            DurationType.TIME,
            config.durationType,
        )
    }

    @Test
    fun `config can be customized`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3),
                fretFrom = 5,
                fretTo = 10,
                noteMode = NoteMode.SEMITONES,
                durationType = DurationType.COUNT,
                durationMinutes = 10,
                noteCount = 50,
            )

        assertEquals(setOf(1, 2, 3), config.selectedStrings)
        assertEquals(5, config.fretFrom)
        assertEquals(10, config.fretTo)
        assertEquals(NoteMode.SEMITONES, config.noteMode)
        assertEquals(DurationType.COUNT, config.durationType)
        assertEquals(10, config.durationMinutes)
        assertEquals(50, config.noteCount)
    }

    @Test
    fun `config copy works correctly`() {
        val original = PracticeConfig(selectedStrings = setOf(1, 2))
        val modified = original.copy(fretFrom = 3, fretTo = 8)

        assertEquals("Should preserve selected strings", original.selectedStrings, modified.selectedStrings)
        assertEquals("Should update fretFrom", 3, modified.fretFrom)
        assertEquals("Should update fretTo", 8, modified.fretTo)
    }
}
