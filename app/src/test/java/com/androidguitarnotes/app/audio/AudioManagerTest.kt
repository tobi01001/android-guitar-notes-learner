package com.androidguitarnotes.app.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for AudioManager.
 */
class AudioManagerTest {
    @Test
    fun `AudioAnalysisResult NoteDetected contains all required fields`() {
        val result =
            AudioManager.AudioAnalysisResult.NoteDetected(
                noteName = "A",
                frequency = 440.0,
                cents = 0.0,
                audioLevel = 0.5f,
                octave = 4,
                noteNameWithOctave = "A4",
            )

        assertEquals("A", result.noteName)
        assertEquals(440.0, result.frequency, 0.001)
        assertEquals(0.0, result.cents, 0.001)
        assertEquals(0.5f, result.audioLevel, 0.001f)
        assertEquals(4, result.octave)
        assertEquals("A4", result.noteNameWithOctave)
    }

    @Test
    fun `AudioAnalysisResult NoNoteDetected contains audio level`() {
        val result = AudioManager.AudioAnalysisResult.NoNoteDetected(audioLevel = 0.3f)

        assertEquals(0.3f, result.audioLevel, 0.001f)
    }

    @Test
    fun `AudioAnalysisResult Gated contains audio level`() {
        val result = AudioManager.AudioAnalysisResult.Gated(audioLevel = 0.1f)

        assertEquals(0.1f, result.audioLevel, 0.001f)
    }

    @Test
    fun `AudioAnalysisResult Gated indicates signal below noise gate threshold`() {
        val result = AudioManager.AudioAnalysisResult.Gated(audioLevel = 0.05f)

        assertTrue("Gated result should indicate low signal", result.audioLevel < 0.5f)
    }

    @Test
    fun `AudioAnalysisResult sealed class hierarchy is correct`() {
        val noteDetected: AudioManager.AudioAnalysisResult =
            AudioManager.AudioAnalysisResult.NoteDetected(
                noteName = "C",
                frequency = 261.63,
                cents = 5.0,
                audioLevel = 0.7f,
                octave = 4,
                noteNameWithOctave = "C4",
            )
        val noNote: AudioManager.AudioAnalysisResult =
            AudioManager.AudioAnalysisResult.NoNoteDetected(audioLevel = 0.2f)
        val gated: AudioManager.AudioAnalysisResult =
            AudioManager.AudioAnalysisResult.Gated(audioLevel = 0.05f)

        assertTrue("NoteDetected is AudioAnalysisResult", noteDetected is AudioManager.AudioAnalysisResult)
        assertTrue("NoNoteDetected is AudioAnalysisResult", noNote is AudioManager.AudioAnalysisResult)
        assertTrue("Gated is AudioAnalysisResult", gated is AudioManager.AudioAnalysisResult)
    }
}
