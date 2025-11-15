package com.androidguitarnotes.app.notesplayed

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Grid-based coordinate system for the fretboard.
 *
 * Grid dimensions:
 * - Horizontal: 13 columns (indices 0..12)
 * - Vertical: 27 rows (indices 0..26)
 *
 * Layout rules:
 * - Odd columns (1,3,5,7,9,11) are string centers (6 strings)
 * - Columns 0 and 12 are neck edges/borders
 * - Odd rows (1,3,5,...,25) represent fret positions (frets 0..12)
 * - Even rows (2,4,6,...,24) represent spaces between frets
 * - Rows 0 and 26 are top/bottom padding borders
 */
object FretboardGridSystem {
    // Grid constants
    const val COLUMNS = 13
    const val ROWS = 27
    const val NUM_STRINGS = 6
    const val NUM_FRETS = 13 // 0..12 inclusive

    // String column indices (left to right: string 6 to string 1)
    val STRING_COLUMN_INDICES = listOf(1, 3, 5, 7, 9, 11)

    // Standard tuning: MIDI numbers for open strings (E2, A2, D3, G3, B3, E4)
    val STANDARD_TUNING_MIDI = listOf(40, 45, 50, 55, 59, 64)

    /**
     * Maps fret number to row index.
     * Fret 0 -> row 1, Fret 1 -> row 3, Fret 2 -> row 5, etc.
     */
    fun fretToRow(fret: Int): Int = 1 + 2 * fret

    /**
     * Maps row index to fret number (inverse of fretToRow).
     * Returns null if row doesn't correspond to a fret.
     */
    fun rowToFret(row: Int): Int? {
        if (row < 1 || row > 25 || row % 2 == 0) return null
        return (row - 1) / 2
    }

    /**
     * Maps string number (1-6) to column index.
     * String 6 (low E) -> column 1
     * String 1 (high E) -> column 11
     */
    fun stringToColumn(stringNumber: Int): Int {
        require(stringNumber in 1..6) { "String number must be 1-6" }
        return STRING_COLUMN_INDICES[6 - stringNumber]
    }

    /**
     * Maps column index to string number.
     * Returns null if column doesn't correspond to a string.
     */
    fun columnToString(column: Int): Int? {
        val index = STRING_COLUMN_INDICES.indexOf(column)
        return if (index >= 0) 6 - index else null
    }

    /**
     * Grid coordinate representing a position on the fretboard.
     */
    data class GridCoord(
        val column: Int,
        val row: Int,
    ) {
        init {
            require(column in 0..12) { "Column must be 0-12" }
            require(row in 0..26) { "Row must be 0-26" }
        }

        /**
         * Get the string number if this coordinate is on a string column.
         */
        fun getStringNumber(): Int? = columnToString(column)

        /**
         * Get the fret number if this coordinate is on a fret row.
         */
        fun getFretNumber(): Int? = rowToFret(row)
    }

    /**
     * Pixel layout calculator for converting grid coordinates to pixel positions.
     */
    class PixelLayout(
        val viewWidth: Float,
        val viewHeight: Float,
        val horizontalPadding: Dp = 16.dp,
        val verticalPadding: Dp = 32.dp,
        val density: Float = 1f,
    ) {
        private val horizontalPaddingPx = horizontalPadding.value * density
        private val verticalPaddingPx = verticalPadding.value * density

        val usableWidth = viewWidth - 2 * horizontalPaddingPx
        val usableHeight = viewHeight - 2 * verticalPaddingPx

        val columnStepPx = usableWidth / 12.0f // 12 intervals between 13 columns
        val rowStepPx = usableHeight / 26.0f // 26 intervals between 27 rows

        /**
         * Convert column index to pixel X coordinate.
         */
        fun pixelX(column: Int): Float {
            require(column in 0..COLUMNS - 1) { "Column must be 0-${COLUMNS - 1}" }
            return horizontalPaddingPx + column * columnStepPx
        }

        /**
         * Convert row index to pixel Y coordinate.
         * Note: Allows row == ROWS for drawing elements beyond the last fret.
         */
        fun pixelY(row: Int): Float {
            require(row in 0..ROWS) { "Row must be 0-$ROWS" }
            return verticalPaddingPx + row * rowStepPx
        }

        /**
         * Convert grid coordinate to pixel offset.
         */
        fun toPixelOffset(coord: GridCoord): Offset = Offset(pixelX(coord.column), pixelY(coord.row))

        /**
         * Convert grid coordinate to pixel offset for center of cell.
         */
        fun toCenterPixelOffset(coord: GridCoord): Offset =
            Offset(
                pixelX(coord.column) + columnStepPx / 2,
                pixelY(coord.row) + rowStepPx / 2,
            )
    }

    /**
     * Find all grid coordinates where a given MIDI note can be played.
     *
     * @param targetMidi Target MIDI number
     * @param tuningMidi Open string MIDI numbers (6 values, string 6 to string 1)
     * @param maxFret Maximum fret to search (0..12)
     * @return List of grid coordinates where the note can be played
     */
    fun findNotePositions(
        targetMidi: Int,
        tuningMidi: List<Int> = STANDARD_TUNING_MIDI,
        maxFret: Int = 12,
    ): List<GridCoord> {
        require(tuningMidi.size == NUM_STRINGS) { "Tuning must have $NUM_STRINGS values" }
        require(maxFret in 0..12) { "Max fret must be 0-12" }

        val positions = mutableListOf<GridCoord>()

        for (stringIndex in 0 until NUM_STRINGS) {
            val openMidi = tuningMidi[stringIndex]
            val requiredFret = targetMidi - openMidi

            if (requiredFret in 0..maxFret) {
                val stringNumber = 6 - stringIndex // Convert to string number (6..1)
                val column = stringToColumn(stringNumber)
                val row = fretToRow(requiredFret)
                positions.add(GridCoord(column, row))
            }
        }

        return positions
    }

    /**
     * Find all grid coordinates where a note with given name and octave can be played.
     *
     * @param noteName Note name (e.g., "C", "C#", "D")
     * @param octave Octave number
     * @param tuningMidi Open string MIDI numbers
     * @param maxFret Maximum fret to search
     * @return List of grid coordinates
     */
    fun findNotePositionsByName(
        noteName: String,
        octave: Int,
        tuningMidi: List<Int> = STANDARD_TUNING_MIDI,
        maxFret: Int = 12,
    ): List<GridCoord> {
        val midi = noteNameToMidi(noteName, octave)
        return findNotePositions(midi, tuningMidi, maxFret)
    }

    /**
     * Convert note name and octave to MIDI number.
     */
    fun noteNameToMidi(
        noteName: String,
        octave: Int,
    ): Int {
        val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteIndex = noteNames.indexOf(noteName)
        require(noteIndex >= 0) { "Invalid note name: $noteName" }
        return 12 + octave * 12 + noteIndex // MIDI C0 = 12
    }

    /**
     * Get MIDI number at a specific grid coordinate.
     * Returns null if coordinate is not on a string/fret intersection.
     */
    fun getMidiAtPosition(
        coord: GridCoord,
        tuningMidi: List<Int> = STANDARD_TUNING_MIDI,
    ): Int? {
        val stringNumber = coord.getStringNumber() ?: return null
        val fret = coord.getFretNumber() ?: return null
        val stringIndex = 6 - stringNumber
        return tuningMidi[stringIndex] + fret
    }
}
