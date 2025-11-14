package com.androidguitarnotes.app.notesplayed

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidguitarnotes.app.R
import com.androidguitarnotes.app.permissions.PermissionManager
import com.androidguitarnotes.app.permissions.PermissionRationaleScreen
import com.androidguitarnotes.app.ui.KeepScreenOn
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
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }

    val viewModel: NotesPlayedViewModel =
        viewModel(
            factory = NotesPlayedViewModelFactory(settingsViewModel, permissionManager),
        )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val audioPermissionRequired by viewModel.audioPermissionRequired.collectAsStateWithLifecycle()
    val showPermissionRationale by viewModel.showPermissionRationale.collectAsStateWithLifecycle()

    // Audio permission launcher
    val audioPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                viewModel.onAudioPermissionGranted()
            } else {
                viewModel.onAudioPermissionDenied()
            }
        }

    // Show permission rationale dialog
    if (showPermissionRationale) {
        PermissionRationaleScreen(
            onRequestPermission = { viewModel.requestAudioPermission() },
            onDismiss = { viewModel.onPermissionRationaleDismissed() },
        )
    }

    // Request permission when needed
    LaunchedEffect(audioPermissionRequired) {
        if (audioPermissionRequired) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Keep screen on while listening
    KeepScreenOn(enabled = state.isListening)

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
                lastDetectedNote = state.lastDetectedNote,
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
    lastDetectedNote: DetectedNoteInfo?,
    isListening: Boolean,
    modifier: Modifier = Modifier,
) {
    // Use current detected note, or fall back to last detected note for persistence
    val displayNote = detectedNote ?: lastDetectedNote
    val isPersisted = detectedNote == null && lastDetectedNote != null

    Column(
        modifier =
            modifier
                .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isListening) {
            // Always show card when listening
            NoteCard(
                detectedNote = displayNote,
                isPersisted = isPersisted,
            )

            // Animate only the highlighted notes on the fretboard, not the entire fretboard
            val highlightAlpha by animateFloatAsState(
                targetValue = if (detectedNote != null) 1.0f else 0.3f,
                animationSpec =
                    tween(
                        durationMillis = if (detectedNote != null) 200 else 600,
                    ),
                label = "highlightAlpha",
            )

            // Always show fretboard, only fade the highlighted notes
            FretboardView(
                detectedNote = displayNote?.noteName,
                detectedNoteWithOctave = displayNote?.noteNameWithOctave,
                maxFret = 12,
                highlightAlpha = highlightAlpha,
                isPersisted = isPersisted,
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
    isPersisted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // Animate note letter size based on detection (subtle pulse effect)
    val noteScale = 1.0f /* by animateFloatAsState(
        targetValue = if (!isPersisted && detectedNote != null) 1.0f else 0.95f,
        animationSpec = tween(durationMillis = 200),
        label = "noteScale",
    )
     */

    // Animate alpha for fade in/out effect
    val noteAlpha by animateFloatAsState(
        targetValue = if (!isPersisted && detectedNote != null) 1.0f else 0.4f,
        animationSpec =
            tween(
                durationMillis = if (!isPersisted && detectedNote != null) 200 else 600,
            ),
        label = "noteAlpha",
    )

    // Animate saturation for greyscale effect on persisted notes
    val saturation by animateFloatAsState(
        targetValue = if (isPersisted) 0.0f else 1.0f,
        animationSpec = tween(durationMillis = 600),
        label = "saturation",
    )

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
            // Large note name display with animation
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .graphicsLayer(
                            scaleX = noteScale,
                            scaleY = noteScale,
                            alpha = noteAlpha,
                        ).clip(RoundedCornerShape(12.dp))
                        .background(
                            if (detectedNote != null) {
                                // Reduce saturation when persisted by mixing with gray
                                val baseColor = NoteColors.getColorForNote(detectedNote.noteName)
                                if (isPersisted) {
                                    baseColor.copy(
                                        red = baseColor.red * saturation + 0.2f * (1f - saturation),
                                        green = baseColor.green * saturation + 0.2f * (1f - saturation),
                                        blue = baseColor.blue * saturation + 0.2f * (1f - saturation),
                                    )
                                } else {
                                    baseColor
                                }
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
