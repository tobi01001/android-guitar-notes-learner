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
private val NUT_COLOR = Color(0xFFF5F5DC) // Beige/bone color for nut (fret 0)
private val STRING_COLOR = Color(0xFF808080) // Steel grey

/**
 * Enhanced fretboard visualization composable with realistic guitar appearance.
 * Displays a guitar fretboard with wooden background, metal fret bars, and realistic strings.
 * Highlights note positions at string/fret intersections.
 *
 * @param detectedNote The note name to highlight on the fretboard (e.g., 'A', 'C#', 'F').
 *                     If null, no positions will be highlighted.
 * @param detectedNoteWithOctave The note name with octave (e.g., 'A4', 'C#3').
 *                               If provided, only positions matching the octave will be highlighted.
 * @param maxFret The maximum fret number to display (default is 12).
 * @param highlightAlpha The alpha (opacity) value for highlighted notes (0.0 to 1.0).
 * @param isPersisted Whether the note is persisted (no longer actively detected).
 * @param targetStringNumber If specified, only highlight notes on this string (for practice mode).
 * @param modifier Modifier to be applied to the fretboard container.
 */
@Composable
fun FretboardView(
    detectedNote: String?,
    detectedNoteWithOctave: String? = null,
    maxFret: Int = 12,
    highlightAlpha: Float = 1.0f,
    isPersisted: Boolean = false,
    targetStringNumber: Int? = null,
    modifier: Modifier = Modifier,
) {
    val highlightedPositions =
        if (detectedNoteWithOctave != null) {
            // Use octave-sensitive detection if available
            val positions = FretboardHelper.findPositionsForNoteWithOctave(detectedNoteWithOctave, maxFret)
            // Filter by target string if specified (for practice mode)
            if (targetStringNumber != null) {
                positions.filter { it.stringNumber == targetStringNumber }
            } else {
                positions
            }
        } else if (detectedNote != null) {
            // Fall back to note name only
            val positions = FretboardHelper.findPositionsForNote(detectedNote, maxFret)
            // Filter by target string if specified (for practice mode)
            if (targetStringNumber != null) {
                positions.filter { it.stringNumber == targetStringNumber }
            } else {
                positions
            }
        } else {
            emptyList()
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
    ) {
        // Main fretboard area with wooden background and border
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FRETBOARD_WOOD_COLOR)
                    .border(2.dp, Color(0xFF5A3A2C), RoundedCornerShape(8.dp)) // Darker wood border
                    .padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            // Fret numbers and fretboard content
            Row {
                // Fret numbers aligned with fret bars
                FretNumbers(maxFret = maxFret)

                Spacer(modifier = Modifier.width(8.dp))

                // Fretboard with strings, frets, markers, and notes
                Box {
                    // Background layer: Fret bars crossing all strings
                    FretBars(maxFret = maxFret)

                    // Background layer: Fret markers (inlays)
                    FretMarkers(maxFret = maxFret)

                    // Foreground layer: Strings with note markers
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // All strings from 6 (low E) to 1 (high E)
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
                    } // End Row (strings)
                } // End Box (fretboard content)
            } // End Row (fret numbers + content)
        } // End Box (wooden background)
    } // End Column (outer container)
}

/**
 * Displays standard fret markers (inlays) as a background decoration.
 * Positioned in the center of the fretboard, between fret lines.
 * Dots appear at frets 3, 5, 7, 9 (single) and 12 (double).
 */
@Composable
private fun FretMarkers(
    maxFret: Int,
    modifier: Modifier = Modifier,
) {
    val markerFrets = setOf(3, 5, 7, 9)

    // Position markers in the horizontal center between strings 3 and 4
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = modifier.width(24.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (fret in 0..maxFret) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center, // Center dots in the fret space
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
 * Displays fret bars crossing the entire neck.
 */
@Composable
private fun FretBars(
    maxFret: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (fret in 0..maxFret) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (fret == 0) {
                    // Nut at fret 0 (thicker, bone/plastic color)
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(NUT_COLOR),
                    )
                } else {
                    // Metal fret bars
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(FRET_BAR_COLOR),
                    )
                }
            }
        }
    }
}

/**
 * Displays fret numbers on the left side of the fretboard, aligned with fret bars.
 */
@Composable
private fun FretNumbers(
    maxFret: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (fret in 0..maxFret) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    text = fret.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Better contrast
                )
            }
        }
    }
}

/**
 * Displays a single string with its number label and note markers.
 * The string number appears at the top, and notes are centered in fret spaces.
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
    val stringWidth =
        when (stringNumber) {
            1 -> 1.5.dp // High E (thinnest)
            2 -> 2.dp // B
            3 -> 2.5.dp // G
            4 -> 3.dp // D
            5 -> 3.5.dp // A
            6 -> 4.dp // Low E (thickest)
            else -> 2.dp
        }

    Column(
        modifier =
            modifier
                .width(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // String number label at the top
        Text(
            text = stringNumber.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.height(24.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // String and note markers
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            // Visual guitar string running vertically
            Box(
                modifier =
                    Modifier
                        .width(stringWidth)
                        .fillMaxHeight()
                        .background(STRING_COLOR),
            )

            // Note markers in fret spaces
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (fret in 0..maxFret) {
                    val isHighlighted =
                        highlightedPositions.any {
                            it.stringNumber == stringNumber && it.fret == fret
                        }

                    Box(
                        modifier = Modifier.weight(1f),
                        // Fret 0 (open string) aligns to top (near nut)
                        // Other frets center notes in the fret space
                        contentAlignment = if (fret == 0) Alignment.TopCenter else Alignment.Center,
                    ) {
                        // Note marker centered in the fret space
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
 * Positioned directly on the fret bar.
 */
@Composable
private fun FretMarker(
    isHighlighted: Boolean,
    noteName: String?,
    highlightAlpha: Float = 1.0f,
    isPersisted: Boolean = false,
    modifier: Modifier = Modifier,
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
                modifier
                    .size(24.dp)
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
