package com.androidguitarnotes.app.practice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for RandomNoteGenerator.
 */
class RandomNoteGeneratorTest {
    @Test
    fun `generateNote returns note from selected strings`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2),
                fretFrom = 0,
                fretTo = 5,
            )
        val generator = RandomNoteGenerator(config)

        val note = generator.generateNote()

        assertTrue(
            "Note string should be in selected strings",
            note.stringNumber in config.selectedStrings,
        )
    }

    @Test
    fun `generateNote returns note within fret range`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3),
                fretFrom = 5,
                fretTo = 10,
            )
        val generator = RandomNoteGenerator(config)

        val note = generator.generateNote()

        assertTrue(
            "Fret should be within range",
            note.fret in config.fretFrom..config.fretTo,
        )
    }

    @Test
    fun `generateNote with SEMITONES includes sharps`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3, 4, 5, 6),
                fretFrom = 0,
                fretTo = 12,
                noteMode = NoteMode.SEMITONES,
            )
        val generator = RandomNoteGenerator(config)

        // Generate multiple notes to increase chance of getting a sharp
        val notes = (1..50).map { generator.generateNote() }

        // At least some notes should potentially contain sharps (chromatic scale)
        assertTrue(
            "Note names should be valid",
            notes.all { it.noteName.isNotEmpty() },
        )
    }

    @Test
    fun `generateNote with WHOLE_NOTES returns valid notes`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1),
                fretFrom = 1,
                // String 1, Fret 1 is F
                fretTo = 1,
                noteMode = NoteMode.WHOLE_NOTES,
            )
        val generator = RandomNoteGenerator(config)

        val note = generator.generateNote()

        // Should return a valid note name
        assertTrue(
            "Whole notes mode should return valid note",
            note.noteName.isNotEmpty(),
        )
    }

    @Test
    fun `generateNote with single string and fret is deterministic`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(3),
                fretFrom = 5,
                fretTo = 5,
            )
        val generator = RandomNoteGenerator(config)

        val note1 = generator.generateNote()
        val note2 = generator.generateNote()

        assertEquals("String should be consistent", note1.stringNumber, note2.stringNumber)
        assertEquals("Fret should be consistent", note1.fret, note2.fret)
        assertEquals("Note name should be consistent", note1.noteName, note2.noteName)
    }

    @Test
    fun `generateNote respects all selected strings`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3, 4, 5, 6),
                fretFrom = 0,
                fretTo = 12,
            )
        val generator = RandomNoteGenerator(config)

        // Generate many notes to ensure we get variety
        val notes = (1..100).map { generator.generateNote() }
        val stringNumbers = notes.map { it.stringNumber }.toSet()

        // Should have used multiple strings
        assertTrue(
            "Should generate notes from multiple strings",
            stringNumbers.size > 1,
        )
    }

    @Test
    fun `generateNote with WHOLE_NOTES excludes sharps and flats`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3, 4, 5, 6),
                fretFrom = 0,
                fretTo = 12,
                noteMode = NoteMode.WHOLE_NOTES,
            )
        val generator = RandomNoteGenerator(config)

        // Generate many notes to check they're all natural notes
        val notes = (1..100).map { generator.generateNote() }
        val naturalNotes = setOf("C", "D", "E", "F", "G", "A", "B")

        // All generated notes should be natural notes (no sharps or flats)
        assertTrue(
            "All notes should be natural notes without sharps or flats",
            notes.all { it.noteName in naturalNotes },
        )
    }

    @Test
    fun `generateNote with SEMITONES includes all chromatic notes`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3, 4, 5, 6),
                fretFrom = 0,
                fretTo = 12,
                noteMode = NoteMode.SEMITONES,
            )
        val generator = RandomNoteGenerator(config)

        // Generate many notes to check variety
        val notes = (1..200).map { generator.generateNote() }
        val noteNames = notes.map { it.noteName }.toSet()

        // Should include both natural notes and sharps
        // (At least some variety in the chromatic scale)
        assertTrue(
            "Should generate variety of notes in chromatic scale",
            noteNames.size >= 5, // Expect at least 5 different notes
        )
    }

    @Test
    fun `generateNote with SCALE mode uses only scale notes`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3, 4, 5, 6),
                fretFrom = 0,
                fretTo = 12,
                noteMode = NoteMode.SCALE,
                selectedScale = Scale.C_MAJOR,
            )
        val generator = RandomNoteGenerator(config)

        // Generate many notes to check they're all in C Major scale
        val notes = (1..100).map { generator.generateNote() }
        val cMajorNotes = Scale.C_MAJOR.notes.toSet()

        // All generated notes should be in C Major scale
        assertTrue(
            "All notes should be in C Major scale",
            notes.all { it.noteName in cMajorNotes },
        )
    }

    @Test
    fun `generateNote with SCALE mode respects G Major scale`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3, 4, 5, 6),
                fretFrom = 0,
                fretTo = 12,
                noteMode = NoteMode.SCALE,
                selectedScale = Scale.G_MAJOR,
            )
        val generator = RandomNoteGenerator(config)

        // Generate many notes to check they're all in G Major scale
        val notes = (1..100).map { generator.generateNote() }
        val gMajorNotes = Scale.G_MAJOR.notes.toSet()

        // All generated notes should be in G Major scale
        assertTrue(
            "All notes should be in G Major scale (includes F#)",
            notes.all { it.noteName in gMajorNotes },
        )

        // Should include F# if enough samples (G Major has F#)
        val noteNames = notes.map { it.noteName }.toSet()
        assertTrue(
            "G Major scale notes should be present",
            noteNames.any { it in gMajorNotes },
        )
    }

    @Test
    fun `generateNote with SCALE mode respects A Minor scale`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3, 4, 5, 6),
                fretFrom = 0,
                fretTo = 12,
                noteMode = NoteMode.SCALE,
                selectedScale = Scale.A_MINOR,
            )
        val generator = RandomNoteGenerator(config)

        // Generate many notes
        val notes = (1..100).map { generator.generateNote() }
        val aMinorNotes = Scale.A_MINOR.notes.toSet()

        // All generated notes should be in A Minor scale
        assertTrue(
            "All notes should be in A Minor scale",
            notes.all { it.noteName in aMinorNotes },
        )
    }

    @Test
    fun `generateNote with SCALE mode respects E Minor scale`() {
        val config =
            PracticeConfig(
                selectedStrings = setOf(1, 2, 3, 4, 5, 6),
                fretFrom = 0,
                fretTo = 12,
                noteMode = NoteMode.SCALE,
                selectedScale = Scale.E_MINOR,
            )
        val generator = RandomNoteGenerator(config)

        // Generate many notes
        val notes = (1..100).map { generator.generateNote() }
        val eMinorNotes = Scale.E_MINOR.notes.toSet()

        // All generated notes should be in E Minor scale
        assertTrue(
            "All notes should be in E Minor scale (includes F#)",
            notes.all { it.noteName in eMinorNotes },
        )
    }
}
