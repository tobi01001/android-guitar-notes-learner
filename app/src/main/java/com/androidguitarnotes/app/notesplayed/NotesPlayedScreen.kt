package com.androidguitarnotes.app.notesplayed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidguitarnotes.app.R
import com.androidguitarnotes.app.ui.NoteColors

/**
 * Notes Played screen composable.
 * Displays real-time note detection from audio input.
 */
@Composable
fun NotesPlayedScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: com.androidguitarnotes.app.settings.SettingsViewModel =
        viewModel(
            factory =
                com.androidguitarnotes.app.settings.SettingsViewModelFactory(
                    androidx.compose.ui.platform.LocalContext.current.applicationContext,
                ),
        ),
) {
    val viewModel: NotesPlayedViewModel =
        viewModel(
            factory = NotesPlayedViewModelFactory(settingsViewModel),
        )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notes_played_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Description
            Text(
                text = stringResource(R.string.notes_played_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            // Note display area
            NoteDisplayArea(
                detectedNote = state.detectedNote,
                isListening = state.isListening,
                modifier = Modifier.weight(1f),
            )

            // Control button
            Button(
                onClick = {
                    if (state.isListening) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (state.isListening) {
                        stringResource(R.string.stop_listening)
                    } else {
                        stringResource(R.string.start_listening)
                    },
                )
            }
        }
    }
}

/**
 * Note display area showing the currently detected note.
 */
@Composable
private fun NoteDisplayArea(
    detectedNote: DetectedNoteInfo?,
    isListening: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isListening) {
            // Always show card and fretboard when listening
            NoteCard(detectedNote = detectedNote)
            FretboardView(
                detectedNote = detectedNote?.noteName,
                detectedNoteWithOctave = detectedNote?.noteNameWithOctave,
                maxFret = 12,
            )
        } else {
            EmptyStateMessage(
                message = stringResource(R.string.no_note_detected),
            )
        }
    }
}

/**
 * Card displaying detected note information.
 */
@Composable
private fun NoteCard(
    detectedNote: DetectedNoteInfo?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (detectedNote != null) {
                        NoteColors.getLightColorForNote(detectedNote.noteName)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Large note name display
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (detectedNote != null) {
                                NoteColors.getColorForNote(detectedNote.noteName)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                if (detectedNote != null) {
                    Text(
                        text = detectedNote.noteName,
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Note details
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.current_note),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                if (detectedNote != null) {
                    Text(
                        text = stringResource(R.string.note_frequency, detectedNote.frequency),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Text(
                        text = stringResource(R.string.note_cents, detectedNote.cents),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.play_a_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

/**
 * Empty state message.
 */
@Composable
private fun EmptyStateMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(32.dp),
    )
}
