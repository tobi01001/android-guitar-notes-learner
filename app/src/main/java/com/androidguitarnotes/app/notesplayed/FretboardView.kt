package com.androidguitarnotes.app.notesplayed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val GUITAR_STRINGS = 6

/**
 * Fretboard visualization composable.
 * Displays a guitar fretboard and highlights positions for the detected note.
 *
 * @param detectedNote The note name to highlight on the fretboard (e.g., 'A', 'C#', 'F').
 *                     If null, no positions will be highlighted.
 * @param maxFret The maximum fret number to display (default is 12).
 * @param modifier Modifier to be applied to the fretboard container.
 */
@Composable
fun FretboardView(
    detectedNote: String?,
    maxFret: Int = 12,
    modifier: Modifier = Modifier,
) {
    val highlightedPositions =
        if (detectedNote != null) {
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
    ) {
        // Fret numbers
        FretNumbers(maxFret = maxFret)

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
                )
            }
        }
    }
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
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // String label
        Text(
            text = stringNumber.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .height(32.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Frets
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            for (fret in 0..maxFret) {
                val isHighlighted =
                    highlightedPositions.any {
                        it.stringNumber == stringNumber && it.fret == fret
                    }

                FretMarker(
                    isHighlighted = isHighlighted,
                    noteName = detectedNote,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Displays a single fret position marker.
 */
@Composable
private fun FretMarker(
    isHighlighted: Boolean,
    noteName: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isHighlighted && noteName != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = noteName,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            )
        }
    }
}
