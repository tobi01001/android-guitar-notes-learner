package com.androidguitarnotes.app.practice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidguitarnotes.app.R
import com.androidguitarnotes.app.notesplayed.FretboardView
import com.androidguitarnotes.app.permissions.PermissionRationaleScreen
import com.androidguitarnotes.app.ui.KeepScreenOn
import com.androidguitarnotes.app.ui.NoteColors
import java.util.Locale

/**
 * Practice session screen showing the current note and session progress.
 */
@Composable
fun PracticeSessionScreen(
    config: PracticeConfig,
    onBack: () -> Unit,
    settingsViewModel: com.androidguitarnotes.app.settings.SettingsViewModel =
        viewModel(
            factory =
                com.androidguitarnotes.app.settings.SettingsViewModelFactory(
                    LocalContext.current.applicationContext,
                ),
        ),
    viewModel: PracticeSessionViewModel =
        viewModel(
            factory = PracticeSessionViewModelFactory(config, LocalContext.current.applicationContext, settingsViewModel),
        ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val audioPermissionRequired by viewModel.audioPermissionRequired.collectAsStateWithLifecycle()
    val showPermissionRationale by viewModel.showPermissionRationale.collectAsStateWithLifecycle()

    // Keep screen on during active practice session
    KeepScreenOn(enabled = state is PracticeSessionState.Active)

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

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen guitar fretboard background
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Semi-transparent overlay to ensure content visibility
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(NoteColors.getBackgroundOverlayColor()),
        )

        // Content layer
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.practice_session_title),
                            color = Color.White,
                        )
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            ) {
                when (val currentState = state) {
                    is PracticeSessionState.Ready -> {
                        ReadyScreen(
                            onStart = {
                                viewModel.startSession()
                                // Request audio permission for AUDIO_VERIFICATION mode (required)
                                // or for other modes to enable note detection display (optional)
                                // Request audio permission for AUDIO_VERIFICATION mode (required)
                                // or for other modes to enable note detection display (optional)
                                viewModel.checkAndRequestAudioPermission()
                            },
                            onBack = onBack,
                        )
                    }
                    is PracticeSessionState.Active -> {
                        val autoIntervalCountdown by viewModel.autoIntervalCountdown.collectAsStateWithLifecycle()
                        ActiveSessionScreen(
                            state = currentState,
                            config = config,
                            autoIntervalCountdown = autoIntervalCountdown,
                            onNext = { viewModel.nextNote() },
                            onPause = { viewModel.pauseSession() },
                            onEnd = { viewModel.endSession() },
                        )
                    }
                    is PracticeSessionState.Paused -> {
                        PausedSessionScreen(
                            state = currentState,
                            onResume = { viewModel.resumeSession() },
                            onEnd = { viewModel.endSession() },
                        )
                    }
                    is PracticeSessionState.Completed -> {
                        CompletedSessionScreen(
                            state = currentState,
                            onFinish = onBack,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadyScreen(
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.ready_to_practice),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.practice_instructions),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.85f),
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(0.6f),
        ) {
            Text(stringResource(R.string.start_practice))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.6f),
        ) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
private fun ActiveSessionScreen(
    state: PracticeSessionState.Active,
    config: PracticeConfig,
    autoIntervalCountdown: Float?,
    onNext: () -> Unit,
    onPause: () -> Unit,
    onEnd: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Progress section with semi-transparent card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A).copy(alpha = 0.7f),
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                ProgressIndicator(
                    notesCompleted = state.notesCompleted,
                    totalNotes = state.totalNotes,
                    elapsedTimeSeconds = state.elapsedTimeSeconds,
                    totalTimeSeconds = state.totalTimeSeconds,
                )
            }
        }

        // Note display section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.play_this_note),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Compact note display card with animated green glow when correct
            val isCorrect = state.noteFeedback is PracticeSessionState.NoteFeedback.Correct
            val glowAlpha by if (isCorrect) {
                val infiniteTransition = rememberInfiniteTransition(label = "glow")
                infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.8f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    label = "glowAlpha",
                )
            } else {
                remember { mutableStateOf(0f) }
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.8f)
                        .then(
                            if (isCorrect) {
                                Modifier
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        ambientColor = Color.Green,
                                        spotColor = Color.Green,
                                    ).border(
                                        width = 3.dp,
                                        color = Color.Green.copy(alpha = glowAlpha),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                            } else {
                                Modifier
                            },
                        ),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = NoteColors.getLightColorForNote(state.currentNote.noteName),
                        ),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.currentNote.noteNameWithOctave,
                            style = MaterialTheme.typography.displayMedium,
                            fontSize = 36.sp,
                            color = NoteColors.getDarkColorForNote(state.currentNote.noteName),
                            maxLines = 1,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text =
                                stringResource(
                                    R.string.string_and_fret,
                                    state.currentNote.stringNumber,
                                    state.currentNote.fret,
                                ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fretboard visualization showing target note position (only on target string)
            // with animated green glow when correct
            Box(
                modifier =
                    if (isCorrect) {
                        Modifier
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(8.dp),
                                ambientColor = Color.Green,
                                spotColor = Color.Green,
                            ).border(
                                width = 3.dp,
                                color = Color.Green.copy(alpha = glowAlpha),
                                shape = RoundedCornerShape(8.dp),
                            )
                    } else {
                        Modifier
                    },
            ) {
                FretboardView(
                    detectedNote = state.currentNote.noteName,
                    detectedNoteWithOctave = state.currentNote.noteNameWithOctave,
                    maxFret = 12,
                    highlightAlpha = 1.0f,
                    isPersisted = false,
                    targetStringNumber = state.currentNote.stringNumber, // Show only on target string
                )
            }

            // Note feedback display - always show for all modes
            Spacer(modifier = Modifier.height(16.dp))

            NoteFeedbackDisplay(feedback = state.noteFeedback)

            // Auto-interval countdown display
            if (config.progressionMode == ProgressionMode.AUTO_INTERVAL && autoIntervalCountdown != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.auto_advancing_in, autoIntervalCountdown),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        // Controls section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Show "Next Note" button only in MANUAL mode
            if (config.progressionMode == ProgressionMode.MANUAL) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.next_note))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onPause,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.pause))
                }

                OutlinedButton(
                    onClick = onEnd,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.end_session))
                }
            }
        }
    }
}

@Composable
private fun PausedSessionScreen(
    state: PracticeSessionState.Paused,
    onResume: () -> Unit,
    onEnd: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.session_paused),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors =
                CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A).copy(alpha = 0.7f),
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                ProgressIndicator(
                    notesCompleted = state.notesCompleted,
                    totalNotes = state.totalNotes,
                    elapsedTimeSeconds = state.elapsedTimeSeconds,
                    totalTimeSeconds = state.totalTimeSeconds,
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onResume,
            modifier = Modifier.fillMaxWidth(0.6f),
        ) {
            Text(stringResource(R.string.resume))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onEnd,
            modifier = Modifier.fillMaxWidth(0.6f),
        ) {
            Text(stringResource(R.string.end_session))
        }
    }
}

@Composable
private fun CompletedSessionScreen(
    state: PracticeSessionState.Completed,
    onFinish: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.session_complete),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            colors =
                CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A).copy(alpha = 0.85f),
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.notes_played),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )

                Text(
                    text = state.notesCompleted.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.total_time),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )

                Text(
                    text = formatTime(state.totalTimeSeconds),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(0.6f),
        ) {
            Text(stringResource(R.string.finish))
        }
    }
}

@Composable
private fun ProgressIndicator(
    notesCompleted: Int,
    totalNotes: Int?,
    elapsedTimeSeconds: Long,
    totalTimeSeconds: Long?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Notes progress
        if (totalNotes != null) {
            val progressNotesVal = if (totalNotes > 0) notesCompleted.toFloat() / totalNotes.toFloat() else 0f
            LinearProgressIndicator(
                progress = progressNotesVal,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
            )

            Text(
                text = stringResource(R.string.notes_progress, notesCompleted, totalNotes),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        } else {
            Text(
                text = stringResource(R.string.notes_completed_count, notesCompleted),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time progress
        if (totalTimeSeconds != null) {
            val progressTimeVal = if (totalTimeSeconds > 0) elapsedTimeSeconds.toFloat() / totalTimeSeconds.toFloat() else 0f
            LinearProgressIndicator(
                progress = progressTimeVal,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
            )

            Text(
                text =
                    stringResource(
                        R.string.time_progress,
                        formatTime(elapsedTimeSeconds),
                        formatTime(totalTimeSeconds),
                    ),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        } else {
            Text(
                text = stringResource(R.string.elapsed_time, formatTime(elapsedTimeSeconds)),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun NoteFeedbackDisplay(feedback: PracticeSessionState.NoteFeedback) {
    // Reserve consistent height to prevent UI jumping
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(72.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (feedback) {
            is PracticeSessionState.NoteFeedback.None -> {
                // Show listening indicator
                Text(
                    text = stringResource(R.string.listening),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
            is PracticeSessionState.NoteFeedback.Correct -> {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = Color(0xFF1B5E20).copy(alpha = 0.85f),
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.correct_note),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            is PracticeSessionState.NoteFeedback.Detected -> {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = Color(0xFFB71C1C).copy(alpha = 0.85f),
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.detected_note, feedback.noteName),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                        Text(
                            text = stringResource(R.string.try_again),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, remainingSeconds)
}
