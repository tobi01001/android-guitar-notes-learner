package com.androidguitarnotes.app.notesplayed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidguitarnotes.app.notesplayed.FretboardGridSystem.GridCoord
import com.androidguitarnotes.app.notesplayed.FretboardGridSystem.PixelLayout
import com.androidguitarnotes.app.ui.NoteColors

// Realistic fretboard colors
private val FRETBOARD_WOOD_COLOR = Color(0xFF8B5A3C) // Rosewood brown
private val FRET_BAR_COLOR = Color(0xFFC0C0C0) // Silver/metal
private val NUT_COLOR = Color(0xFFF5F5DC) // Beige/bone color for nut
private val STRING_COLOR = Color(0xFF808080) // Steel grey
private val FRET_MARKER_COLOR = Color(0xFFE8E8E8) // Pearl/mother-of-pearl
private val NECK_EDGE_COLOR = Color(0xFF5A3A2C) // Darker wood for binding

/**
 * Enhanced fretboard visualization using grid-based coordinate system.
 *
 * This implementation uses a 13×27 grid where:
 * - Columns 0-12: odd columns (1,3,5,7,9,11) are string centers
 * - Rows 0-26: odd rows (1,3,5...25) are fret positions
 *
 * @param detectedNote The note name to highlight (e.g., 'A', 'C#')
 * @param detectedNoteWithOctave The note with octave (e.g., 'A4', 'C#3')
 * @param maxFret Maximum fret number to display (default 12)
 * @param highlightAlpha Opacity for highlighted notes (0.0-1.0)
 * @param isPersisted Whether note is persisted (greyscale effect)
 * @param targetStringNumber Filter highlights to specific string (for practice mode)
 * @param modifier Modifier for the container
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
    // Find grid coordinates for highlighted notes
    val highlightedCoords =
        if (detectedNoteWithOctave != null) {
            val positions = FretboardHelper.findPositionsForNoteWithOctave(detectedNoteWithOctave, maxFret)
            // Convert to grid coordinates
            positions.mapNotNull { pos ->
                if (targetStringNumber == null || pos.stringNumber == targetStringNumber) {
                    val column = FretboardGridSystem.stringToColumn(pos.stringNumber)
                    val row = FretboardGridSystem.fretToRow(pos.fret)
                    GridCoord(column, row)
                } else {
                    null
                }
            }
        } else if (detectedNote != null) {
            val positions = FretboardHelper.findPositionsForNote(detectedNote, maxFret)
            positions.mapNotNull { pos ->
                if (targetStringNumber == null || pos.stringNumber == targetStringNumber) {
                    val column = FretboardGridSystem.stringToColumn(pos.stringNumber)
                    val row = FretboardGridSystem.fretToRow(pos.fret)
                    GridCoord(column, row)
                } else {
                    null
                }
            }
        } else {
            emptyList()
        }

    // Prepare text measurers in Composable context
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Main fretboard with wooden background
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(0.7f) // Aspect ratio for realistic guitar neck proportions
                    .clip(RoundedCornerShape(8.dp))
                    .background(FRETBOARD_WOOD_COLOR)
                    .border(2.dp, NECK_EDGE_COLOR, RoundedCornerShape(8.dp)),
        ) {
            // Canvas for drawing the fretboard grid
            Canvas(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
            ) {
                val pixelLayout =
                    PixelLayout(
                        viewWidth = size.width,
                        viewHeight = size.height,
                        horizontalPadding = 24.dp,
                        verticalPadding = 32.dp,
                    )

                // Layer 1: Neck edges/binding
                drawNeckEdges(pixelLayout)

                // Layer 2: Fret bars
                drawFretBars(pixelLayout, maxFret+1)

                // Layer 3: Strings
                drawStrings(pixelLayout)

                // Layer 4: Fret markers/inlays
                drawFretMarkers(pixelLayout)

                // Layer 5: Fret numbers
                drawFretNumbers(pixelLayout, maxFret, textMeasurer)

                // Layer 6: String numbers
                drawStringNumbers(pixelLayout, textMeasurer)

                // Layer 7: Note markers
                drawNoteMarkers(
                    pixelLayout = pixelLayout,
                    highlightedCoords = highlightedCoords,
                    noteName = detectedNote,
                    highlightAlpha = highlightAlpha,
                    isPersisted = isPersisted,
                    textMeasurer = textMeasurer,
                )
            }
        }
    }
}

/**
 * Draw neck edges/binding at columns 0 and 12.
 */
private fun DrawScope.drawNeckEdges(layout: PixelLayout) {
    val edgeWidth = 3.dp.toPx()
    val offset = 10.dp.toPx()
    // Left edge (column 0)
    drawLine(
        color = NECK_EDGE_COLOR,
        start = Offset(layout.pixelX(0) - offset, layout.pixelY(0) - offset),
        end = Offset(layout.pixelX(0) - offset, layout.pixelY(FretboardGridSystem.ROWS) + offset),
        strokeWidth = edgeWidth,
    )

    // Right edge (column 12)
    drawLine(
        color = NECK_EDGE_COLOR,
        start = Offset(layout.pixelX(12) + offset, layout.pixelY(0) - offset),
        end = Offset(layout.pixelX(12) + offset, layout.pixelY(FretboardGridSystem.ROWS) + offset),
        strokeWidth = edgeWidth,
    )
}

/**
 * Draw horizontal fret bars at odd rows.
 */
private fun DrawScope.drawFretBars(
    layout: PixelLayout,
    maxFret: Int,
) {
    for (fret in 0..maxFret) {
        val row = FretboardGridSystem.fretToRow(fret)
        val y = layout.pixelY(row)
        val x1 = layout.pixelX(0)
        val x2 = layout.pixelX(12)

        if (fret == 0) {
            // Nut - thicker and different color
            drawLine(
                color = NUT_COLOR,
                start = Offset(x1, y),
                end = Offset(x2, y),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
            )
        } else {
            // Metal fret bars
            drawLine(
                color = FRET_BAR_COLOR,
                start = Offset(x1, y),
                end = Offset(x2, y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

/**
 * Draw vertical strings at odd columns.
 */
private fun DrawScope.drawStrings(layout: PixelLayout) {
    val stringWidths = listOf(4.dp, 3.5.dp, 3.dp, 2.5.dp, 2.dp, 1.5.dp) // String 6 to 1

    FretboardGridSystem.STRING_COLUMN_INDICES.forEachIndexed { index, column ->
        val x = layout.pixelX(column)
        val y1 = layout.pixelY(0)
        val y2 = layout.pixelY(FretboardGridSystem.ROWS) + 16.dp.toPx()
        val strokeWidth = stringWidths[index].toPx()

        drawLine(
            color = STRING_COLOR,
            start = Offset(x, y1),
            end = Offset(x, y2),
            strokeWidth = strokeWidth,
        )
    }
}

/**
 * Draw fret marker inlays between strings 3 and 4.
 */
private fun DrawScope.drawFretMarkers(layout: PixelLayout) {
    val markerFrets = setOf(3, 5, 7, 9)
    val doubleDotFret = 12

    // Position markers horizontally between strings 3 and 4 (columns 5 and 7)
    val markerX = (layout.pixelX(5) + layout.pixelX(7)) / 2f

    for (fret in markerFrets) {
        val fretRow = FretboardGridSystem.fretToRow(fret)
        // Position vertically in the center of the fret space (between this fret and next)
        val y1 = layout.pixelY(fretRow)
        val y2 = layout.pixelY(fretRow + 2)
        val markerY = (y1 + y2) / 2f

        drawCircle(
            color = FRET_MARKER_COLOR,
            radius = 6.dp.toPx(),
            center = Offset(markerX, markerY),
        )
    }

    // Double dots at fret 12
    if (doubleDotFret <= 12) {
        val fretRow = FretboardGridSystem.fretToRow(doubleDotFret)
        val y1 = layout.pixelY(fretRow)
        val y2 = layout.pixelY(fretRow + 2)
        val markerY = (y1 + y2) / 2f

        val offset = 8.dp.toPx()
        drawCircle(
            color = FRET_MARKER_COLOR,
            radius = 5.dp.toPx(),
            center = Offset(markerX - offset, markerY),
        )
        drawCircle(
            color = FRET_MARKER_COLOR,
            radius = 5.dp.toPx(),
            center = Offset(markerX + offset, markerY),
        )
    }
}

/**
 * Draw fret numbers on the left side.
 */
private fun DrawScope.drawFretNumbers(
    layout: PixelLayout,
    maxFret: Int,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    val textStyle =
        TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            background = FRETBOARD_WOOD_COLOR,
        )

    for (fret in 0..maxFret) {
        val row = FretboardGridSystem.fretToRow(fret)
        val y = layout.pixelY(row)
        val x = layout.pixelX(0)

        val textLayoutResult =
            textMeasurer.measure(
                text = fret.toString(),
                style = textStyle,
            )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft =
                Offset(
                    x - textLayoutResult.size.width / 2,
                    y - textLayoutResult.size.height / 2,
                ),
        )
    }
}

/**
 * Draw string numbers at the top.
 */
private fun DrawScope.drawStringNumbers(
    layout: PixelLayout,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    val textStyle =
        TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            background = FRETBOARD_WOOD_COLOR,
        )

    FretboardGridSystem.STRING_COLUMN_INDICES.forEachIndexed { index, column ->
        val stringNumber = 6 - index
        val x = layout.pixelX(column)
        val y = layout.pixelY(0) - 2.dp.toPx()

        val textLayoutResult =
            textMeasurer.measure(
                text = stringNumber.toString(),
                style = textStyle,
            )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft =
                Offset(
                    x - textLayoutResult.size.width / 2,
                    y - textLayoutResult.size.height,
                ),
        )
    }
}

/**
 * Draw note markers at highlighted positions.
 */
private fun DrawScope.drawNoteMarkers(
    pixelLayout: PixelLayout,
    highlightedCoords: List<GridCoord>,
    noteName: String?,
    highlightAlpha: Float,
    isPersisted: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    if (noteName == null) return

    val baseColor = NoteColors.getColorForNote(noteName)
    val noteColor =
        if (isPersisted) {
            baseColor.copy(
                red = 0.5f,
                green = 0.5f,
                blue = 0.5f,
                alpha = highlightAlpha,
            )
        } else {
            baseColor.copy(alpha = highlightAlpha)
        }

    val darkColor = NoteColors.getDarkColorForNote(noteName).copy(alpha = highlightAlpha)

    highlightedCoords.forEach { coord ->
        val center = pixelLayout.toPixelOffset(coord)
        val radius = 12.dp.toPx()

        // Draw filled circle
        drawCircle(
            color = noteColor,
            radius = radius,
            center = center,
        )

        // Draw border
        drawCircle(
            color = darkColor,
            radius = radius,
            center = center,
            style =
                androidx.compose.ui.graphics.drawscope
                    .Stroke(width = 2.dp.toPx()),
        )

        // Draw note name text
        val textStyle =
            TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = highlightAlpha),
            )

        val textLayoutResult =
            textMeasurer.measure(
                text = noteName,
                style = textStyle,
            )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft =
                Offset(
                    center.x - textLayoutResult.size.width / 2,
                    center.y - textLayoutResult.size.height / 2,
                ),
        )
    }
}
