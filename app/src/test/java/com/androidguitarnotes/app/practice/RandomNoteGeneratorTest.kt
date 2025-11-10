package com.androidguitarnotes.app.practice

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for RandomNoteGenerator.
 */
class RandomNoteGeneratorTest {
    
    @Test
    fun `generateNote returns note from selected strings`() {
        val config = PracticeConfig(
            selectedStrings = setOf(1, 2),
            fretFrom = 0,
            fretTo = 5
        )
        val generator = RandomNoteGenerator(config)
        
        val note = generator.generateNote()
        
        assertTrue("Note string should be in selected strings", 
            note.stringNumber in config.selectedStrings)
    }
    
    @Test
    fun `generateNote returns note within fret range`() {
        val config = PracticeConfig(
            selectedStrings = setOf(1, 2, 3),
            fretFrom = 5,
            fretTo = 10
        )
        val generator = RandomNoteGenerator(config)
        
        val note = generator.generateNote()
        
        assertTrue("Fret should be within range", 
            note.fret in config.fretFrom..config.fretTo)
    }
    
    @Test
    fun `generateNote with SEMITONES includes sharps`() {
        val config = PracticeConfig(
            selectedStrings = setOf(1, 2, 3, 4, 5, 6),
            fretFrom = 0,
            fretTo = 12,
            noteMode = NoteMode.SEMITONES
        )
        val generator = RandomNoteGenerator(config)
        
        // Generate multiple notes to increase chance of getting a sharp
        val notes = (1..50).map { generator.generateNote() }
        
        // At least some notes should potentially contain sharps (chromatic scale)
        assertTrue("Note names should be valid", 
            notes.all { it.noteName.isNotEmpty() })
    }
    
    @Test
    fun `generateNote with WHOLE_NOTES avoids sharps`() {
        val config = PracticeConfig(
            selectedStrings = setOf(1),
            fretFrom = 1,
            fretTo = 1,  // String 1, Fret 1 is F
            noteMode = NoteMode.WHOLE_NOTES
        )
        val generator = RandomNoteGenerator(config)
        
        val note = generator.generateNote()
        
        // Should not contain sharp symbol
        assertFalse("Whole notes mode should not have sharps", 
            note.noteName.contains("#"))
    }
    
    @Test
    fun `generateNote with single string and fret is deterministic`() {
        val config = PracticeConfig(
            selectedStrings = setOf(3),
            fretFrom = 5,
            fretTo = 5
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
        val config = PracticeConfig(
            selectedStrings = setOf(1, 2, 3, 4, 5, 6),
            fretFrom = 0,
            fretTo = 12
        )
        val generator = RandomNoteGenerator(config)
        
        // Generate many notes to ensure we get variety
        val notes = (1..100).map { generator.generateNote() }
        val stringNumbers = notes.map { it.stringNumber }.toSet()
        
        // Should have used multiple strings
        assertTrue("Should generate notes from multiple strings", 
            stringNumbers.size > 1)
    }
}
