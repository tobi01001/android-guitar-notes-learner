package com.androidguitarnotes.app.practice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for PracticeSessionState sealed class.
 */
class PracticeSessionStateTest {
    @Test
    fun `Ready state can be created`() {
        val state = PracticeSessionState.Ready

        assertTrue("State should be Ready", state is PracticeSessionState.Ready)
    }

    @Test
    fun `Active state contains current note and progress`() {
        val note = PracticeNote(1, 5, "A")
        val state =
            PracticeSessionState.Active(
                currentNote = note,
                notesCompleted = 5,
                totalNotes = 20,
                elapsedTimeSeconds = 60L,
                totalTimeSeconds = 300L,
                noteFeedback = PracticeSessionState.NoteFeedback.None,
            )

        assertEquals("Should have correct note", note, state.currentNote)
        assertEquals("Should have correct completed count", 5, state.notesCompleted)
        assertEquals("Should have correct total notes", 20, state.totalNotes)
        assertEquals("Should have correct elapsed time", 60L, state.elapsedTimeSeconds)
        assertEquals("Should have correct total time", 300L, state.totalTimeSeconds)
    }

    @Test
    fun `Active state supports time-based session`() {
        val note = PracticeNote(2, 3, "E")
        val state =
            PracticeSessionState.Active(
                currentNote = note,
                notesCompleted = 10,
                totalNotes = null, // No total for time-based
                elapsedTimeSeconds = 120L,
                totalTimeSeconds = 300L,
                noteFeedback = PracticeSessionState.NoteFeedback.None,
            )

        assertNull("Total notes should be null for time-based", state.totalNotes)
        assertNotNull("Total time should be set", state.totalTimeSeconds)
    }

    @Test
    fun `Active state supports count-based session`() {
        val note = PracticeNote(3, 7, "C")
        val state =
            PracticeSessionState.Active(
                currentNote = note,
                notesCompleted = 8,
                totalNotes = 30,
                elapsedTimeSeconds = 90L,
                totalTimeSeconds = null, // No time limit for count-based
                noteFeedback = PracticeSessionState.NoteFeedback.None,
            )

        assertNotNull("Total notes should be set", state.totalNotes)
        assertNull("Total time should be null for count-based", state.totalTimeSeconds)
    }

    @Test
    fun `Paused state retains session information`() {
        val note = PracticeNote(4, 2, "F#")
        val state =
            PracticeSessionState.Paused(
                currentNote = note,
                notesCompleted = 7,
                totalNotes = 15,
                elapsedTimeSeconds = 150L,
                totalTimeSeconds = 600L,
            )

        assertEquals("Should retain current note", note, state.currentNote)
        assertEquals("Should retain progress", 7, state.notesCompleted)
    }

    @Test
    fun `Completed state contains final statistics`() {
        val state =
            PracticeSessionState.Completed(
                notesCompleted = 25,
                totalTimeSeconds = 420L,
            )

        assertEquals("Should have final note count", 25, state.notesCompleted)
        assertEquals("Should have total time", 420L, state.totalTimeSeconds)
    }

    @Test
    fun `Active state can be copied with updated progress`() {
        val note1 = PracticeNote(1, 0, "E")
        val note2 = PracticeNote(2, 3, "D")

        val state1 =
            PracticeSessionState.Active(
                currentNote = note1,
                notesCompleted = 5,
                totalNotes = 20,
                elapsedTimeSeconds = 60L,
                totalTimeSeconds = 300L,
                noteFeedback = PracticeSessionState.NoteFeedback.None,
            )

        val state2 =
            state1.copy(
                currentNote = note2,
                notesCompleted = 6,
            )

        assertEquals("Should have new note", note2, state2.currentNote)
        assertEquals("Should have incremented count", 6, state2.notesCompleted)
        assertEquals("Should preserve total notes", state1.totalNotes, state2.totalNotes)
    }
}
