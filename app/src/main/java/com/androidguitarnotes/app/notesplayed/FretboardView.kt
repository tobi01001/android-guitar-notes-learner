package com.androidguitarnotes.app.notesplayed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidguitarnotes.app.ui.NoteColors

private const val GUITAR_STRINGS = 6

// Realistic fretboard colors
private val FRETBOARD_WOOD_COLOR = Color(0xFF8B5A3C) // Rosewood brown
private val FRET_BAR_COLOR = Color(0xFFC0C0C0) // Silver/metal
private val STRING_COLOR = Color(0xFF808080) // Steel grey

/**
 * Fretboard visualization composable.
 * Displays a guitar fretboard and highlights positions for the detected note.
 *
 * @param detectedNote The note name to highlight on the fretboard (e.g., 'A', 'C#', 'F').
 *                     If null, no positions will be highlighted.
 * @param detectedNoteWithOctave The note name with octave (e.g., 'A4', 'C#3').
 *                               If provided, only positions matching the octave will be highlighted.
 * @param maxFret The maximum fret number to display (default is 12).
 * @param highlightAlpha The alpha (opacity) value for highlighted notes (0.0 to 1.0).
 * @param isPersisted Whether the note is persisted (no longer actively detected).
 * @param modifier Modifier to be applied to the fretboard container.
 */
@Composable
fun FretboardView(
    detectedNote: String?,
    detectedNoteWithOctave: String? = null,
    maxFret: Int = 12,
    highlightAlpha: Float = 1.0f,
    isPersisted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val highlightedPositions =
        if (detectedNoteWithOctave != null) {
            // Use octave-sensitive detection if available
            FretboardHelper.findPositionsForNoteWithOctave(detectedNoteWithOctave, maxFret)
        } else if (detectedNote != null) {
            // Fall back to note name only
            FretboardHelper.findPositionsForNote(detectedNote, maxFret)
        } else {
            emptyList()
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Fret numbers
        FretNumbers(maxFret = maxFret)

        Spacer(modifier = Modifier.width(8.dp))

        // Main fretboard area with wooden background
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FRETBOARD_WOOD_COLOR),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            ) {
                // Fret markers (dots) - positioned between strings 3 and 4
                FretMarkers(maxFret = maxFret)

                Spacer(modifier = Modifier.width(8.dp))

                // Strings and frets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    for (stringNumber in GUITAR_STRINGS downTo 1) {
                        StringColumn(
                            stringNumber = stringNumber,
                            maxFret = maxFret,
                            highlightedPositions = highlightedPositions,
                            detectedNote = detectedNote,
                            highlightAlpha = highlightAlpha,
                            isPersisted = isPersisted,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays standard fret markers (dots at frets 3, 5, 7, 9 and double dots at 12).
 */
@Composable
private fun FretMarkers(
    maxFret: Int,
    modifier: Modifier = Modifier,
) {
    val markerFrets = setOf(3, 5, 7, 9)

    Column(
        modifier =
            modifier
                .width(24.dp)
                // Padding aligns fret markers with the fret positions below, accounting for the string label area.
                .padding(top = 40.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (fret in 0..maxFret) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    fret == 12 -> {
                        // Double dots at fret 12
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            MarkerDot(size = 6.dp)
                            MarkerDot(size = 6.dp)
                        }
                    }
                    fret in markerFrets -> {
                        // Single dot at frets 3, 5, 7, 9
                        MarkerDot(size = 8.dp)
                    }
                }
            }
        }
    }
}

/**
 * Displays a single fret marker dot.
 */
@Composable
private fun MarkerDot(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFFE8E8E8)) // Pearl/mother-of-pearl color
                .border(0.5.dp, Color(0xFFB0B0B0), CircleShape),
    )
}

/**
 * Displays fret numbers on the left side of the fretboard.
 */
@Composable
private fun FretNumbers(
    maxFret: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                // Padding aligns fret numbers with the fret markers below, accounting for the string label area.
                .padding(top = 40.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (fret in 0..maxFret) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = fret.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/**
 * Displays a single string with its frets.
 */
@Composable
private fun StringColumn(
    stringNumber: Int,
    maxFret: Int,
    highlightedPositions: List<FretboardHelper.FretPosition>,
    detectedNote: String?,
    highlightAlpha: Float = 1.0f,
    isPersisted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // String thickness varies by string number (thicker for lower strings)
    val stringWidth = when (stringNumber) {
        1 -> 1.5.dp // High E (thinnest)
        2 -> 2.dp   // B
        3 -> 2.5.dp // G
        4 -> 3.dp   // D
        5 -> 3.5.dp // A
        6 -> 4.dp   // Low E (thickest)
        else -> 2.dp
    }

    Box(
        modifier =
            modifier
                .width(32.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Visual guitar string running vertically
        Box(
            modifier =
                Modifier
                    .width(stringWidth)
                    .fillMaxHeight()
                    .padding(top = 40.dp)
                    .background(STRING_COLOR),
        )

        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // String label
            Text(
                text = stringNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier =
                    Modifier
                        .height(32.dp)
                        .wrapContentHeight(Alignment.CenterVertically),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Frets with fret bars
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (fret in 0..maxFret) {
                    val isHighlighted =
                        highlightedPositions.any {
                            it.stringNumber == stringNumber && it.fret == fret
                        }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Fret bar (horizontal metal bar)
                        if (fret > 0) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .align(Alignment.TopCenter)
                                        .background(FRET_BAR_COLOR),
                            )
                        }

                        // Note marker at string/fret intersection
                        FretMarker(
                            isHighlighted = isHighlighted,
                            noteName = detectedNote,
                            highlightAlpha = highlightAlpha,
                            isPersisted = isPersisted,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays a single fret position marker at string/fret intersection.
 */
@Composable
private fun FretMarker(
    isHighlighted: Boolean,
    noteName: String?,
    highlightAlpha: Float = 1.0f,
    isPersisted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(28.dp)
                .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isHighlighted && noteName != null) {
            val baseColor = NoteColors.getColorForNote(noteName)
            // Apply greyscale effect when persisted
            val noteColor =
                if (isPersisted) {
                    baseColor.copy(
                        red = 0.5f,
                        green = 0.5f,
                        blue = 0.5f,
                    )
                } else {
                    baseColor
                }
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(noteColor.copy(alpha = highlightAlpha))
                        .border(2.dp, NoteColors.getDarkColorForNote(noteName).copy(alpha = highlightAlpha), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = noteName,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = highlightAlpha),
                )
            }
        }
    }
}
