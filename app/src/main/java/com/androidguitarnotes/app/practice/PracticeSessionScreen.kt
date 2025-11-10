package com.androidguitarnotes.app.practice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidguitarnotes.app.R
import com.androidguitarnotes.app.permissions.PermissionRationaleScreen

/**
 * Practice session screen showing the current note and session progress.
 */
@Composable
fun PracticeSessionScreen(
    config: PracticeConfig,
    onBack: () -> Unit,
    viewModel: PracticeSessionViewModel =
        viewModel(
            factory = PracticeSessionViewModelFactory(config, LocalContext.current.applicationContext),
        ),
) {
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
            onDismiss = { viewModel.onPermissionRationaleDismissed() }
        )
    }

    // Request permission when needed
    LaunchedEffect(audioPermissionRequired) {
        if (audioPermissionRequired) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.practice_session_title)) },
            )
        },
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
                            viewModel.checkAndRequestAudioPermission()
                        },
                        onBack = onBack,
                    )
                }
                is PracticeSessionState.Active -> {
                    ActiveSessionScreen(
                        state = currentState,
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
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.practice_instructions),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        // Progress section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ProgressIndicator(
                notesCompleted = state.notesCompleted,
                totalNotes = state.totalNotes,
                elapsedTimeSeconds = state.elapsedTimeSeconds,
                totalTimeSeconds = state.totalTimeSeconds,
            )
        }

        // Note display section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.play_this_note),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large note display
            Card(
                modifier = Modifier.fillMaxWidth(0.8f),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.currentNote.noteName,
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 96.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text =
                            stringResource(
                                R.string.string_and_fret,
                                state.currentNote.stringNumber,
                                state.currentNote.fret,
                            ),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // Note feedback display
            Spacer(modifier = Modifier.height(16.dp))

            NoteFeedbackDisplay(feedback = state.noteFeedback)
        }

        // Controls section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.next_note))
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
        )

        Spacer(modifier = Modifier.height(32.dp))

        ProgressIndicator(
            notesCompleted = state.notesCompleted,
            totalNotes = state.totalNotes,
            elapsedTimeSeconds = state.elapsedTimeSeconds,
            totalTimeSeconds = state.totalTimeSeconds,
        )

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
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = state.notesCompleted.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.total_time),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = formatTime(state.totalTimeSeconds),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
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
            )
        } else {
            Text(
                text = stringResource(R.string.notes_completed_count, notesCompleted),
                style = MaterialTheme.typography.titleMedium,
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
            )
        } else {
            Text(
                text = stringResource(R.string.elapsed_time, formatTime(elapsedTimeSeconds)),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun NoteFeedbackDisplay(feedback: PracticeSessionState.NoteFeedback) {
    when (feedback) {
        is PracticeSessionState.NoteFeedback.None -> {
            // Show listening indicator
            Text(
                text = stringResource(R.string.listening),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        is PracticeSessionState.NoteFeedback.Correct -> {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.correct_note),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        is PracticeSessionState.NoteFeedback.Detected -> {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.detected_note, feedback.noteName),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = stringResource(R.string.try_again),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
