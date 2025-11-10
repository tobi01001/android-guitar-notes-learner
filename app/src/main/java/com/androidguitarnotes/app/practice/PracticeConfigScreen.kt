package com.androidguitarnotes.app.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidguitarnotes.app.R

@Composable
fun PracticeConfigScreen(
    onBack: () -> Unit,
    onStartPractice: (PracticeConfig) -> Unit,
    viewModel: PracticeConfigViewModel = viewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.practice_config_title)) })
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            StringSelectionSection(
                selectedStrings = config.selectedStrings,
                onToggleString = { viewModel.toggleString(it) },
            )

            FretRangeSection(
                fretFrom = config.fretFrom,
                fretTo = config.fretTo,
                onFretRangeChange = { from, to -> viewModel.setFretRange(from, to) },
            )

            NoteModeSection(
                selectedMode = config.noteMode,
                onModeSelected = { viewModel.setNoteMode(it) },
            )

            DurationSection(
                durationType = config.durationType,
                durationMinutes = config.durationMinutes,
                noteCount = config.noteCount,
                onDurationTypeChange = { viewModel.setDurationType(it) },
                onDurationMinutesChange = { viewModel.setDurationMinutes(it) },
                onNoteCountChange = { viewModel.setNoteCount(it) },
            )

            ProgressionModeSection(
                progressionMode = config.progressionMode,
                autoIntervalSeconds = config.autoIntervalSeconds,
                onProgressionModeChange = { viewModel.setProgressionMode(it) },
                onAutoIntervalChange = { viewModel.setAutoIntervalSeconds(it) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.back))
                }

                Button(
                    onClick = { onStartPractice(config) },
                    enabled = viewModel.isConfigValid(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.start_practice))
                }
            }
        }
    }
}

@Composable
private fun StringSelectionSection(
    selectedStrings: Set<Int>,
    onToggleString: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.select_strings),
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (stringNum in 1..6) {
                FilterChip(
                    selected = selectedStrings.contains(stringNum),
                    onClick = { onToggleString(stringNum) },
                    label = { Text(stringResource(R.string.string_number, stringNum)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (selectedStrings.isEmpty()) {
            Text(
                text = stringResource(R.string.at_least_one_string),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun FretRangeSection(
    fretFrom: Int,
    fretTo: Int,
    onFretRangeChange: (Int, Int) -> Unit,
) {
    var fromText by remember(fretFrom) { mutableStateOf(fretFrom.toString()) }
    var toText by remember(fretTo) { mutableStateOf(fretTo.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.fret_range),
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = fromText,
                onValueChange = {
                    fromText = it
                    it.toIntOrNull()?.let { from ->
                        if (from >= 0 && from <= 24) {
                            onFretRangeChange(from, fretTo)
                        }
                    }
                },
                label = { Text(stringResource(R.string.fret_from)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )

            Text("—")

            OutlinedTextField(
                value = toText,
                onValueChange = {
                    toText = it
                    it.toIntOrNull()?.let { to ->
                        if (to >= 0 && to <= 24) {
                            onFretRangeChange(fretFrom, to)
                        }
                    }
                },
                label = { Text(stringResource(R.string.fret_to)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        if (fretFrom > fretTo || fretTo > 24) {
            Text(
                text = stringResource(R.string.invalid_fret_range),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun NoteModeSection(
    selectedMode: NoteMode,
    onModeSelected: (NoteMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.note_mode),
            style = MaterialTheme.typography.titleMedium,
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            NoteMode.entries.forEach { mode ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text =
                            when (mode) {
                                NoteMode.SCALE -> stringResource(R.string.note_mode_scale)
                                NoteMode.WHOLE_NOTES -> stringResource(R.string.note_mode_whole)
                                NoteMode.SEMITONES -> stringResource(R.string.note_mode_semitones)
                            },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationSection(
    durationType: DurationType,
    durationMinutes: Int,
    noteCount: Int,
    onDurationTypeChange: (DurationType) -> Unit,
    onDurationMinutesChange: (Int) -> Unit,
    onNoteCountChange: (Int) -> Unit,
) {
    var minutesText by remember(durationMinutes) { mutableStateOf(durationMinutes.toString()) }
    var countText by remember(noteCount) { mutableStateOf(noteCount.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.practice_duration),
            style = MaterialTheme.typography.titleMedium,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = durationType == DurationType.TIME,
                    onClick = { onDurationTypeChange(DurationType.TIME) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.duration_mode_time),
                    modifier = Modifier.weight(1f),
                )
            }

            if (durationType == DurationType.TIME) {
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = {
                        minutesText = it
                        it.toIntOrNull()?.let { minutes ->
                            val MAX_MINUTES = 480
                            if (minutes > 0 && minutes <= MAX_MINUTES) {
                                onDurationMinutesChange(minutes)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.minutes)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = durationType == DurationType.COUNT,
                    onClick = { onDurationTypeChange(DurationType.COUNT) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.duration_mode_count),
                    modifier = Modifier.weight(1f),
                )
            }

            if (durationType == DurationType.COUNT) {
                OutlinedTextField(
                    value = countText,
                    onValueChange = {
                        countText = it
                        it.toIntOrNull()?.let { count ->
                            if (count > 0 && count <= 1000) {
                                onNoteCountChange(count)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.note_count)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ProgressionModeSection(
    progressionMode: ProgressionMode,
    autoIntervalSeconds: Float,
    onProgressionModeChange: (ProgressionMode) -> Unit,
    onAutoIntervalChange: (Float) -> Unit,
) {
    var intervalText by remember(autoIntervalSeconds) { 
        mutableStateOf(String.format("%.1f", autoIntervalSeconds)) 
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.progression_mode),
            style = MaterialTheme.typography.titleMedium,
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ProgressionMode.entries.forEach { mode ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = progressionMode == mode,
                        onClick = { onProgressionModeChange(mode) },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text =
                                when (mode) {
                                    ProgressionMode.MANUAL -> stringResource(R.string.progression_mode_manual)
                                    ProgressionMode.AUDIO_VERIFICATION -> stringResource(R.string.progression_mode_audio)
                                    ProgressionMode.AUTO_INTERVAL -> stringResource(R.string.progression_mode_interval)
                                },
                        )
                        Text(
                            text =
                                when (mode) {
                                    ProgressionMode.MANUAL -> stringResource(R.string.progression_manual_desc)
                                    ProgressionMode.AUDIO_VERIFICATION -> stringResource(R.string.progression_audio_desc)
                                    ProgressionMode.AUTO_INTERVAL -> stringResource(R.string.progression_interval_desc)
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (progressionMode == ProgressionMode.AUTO_INTERVAL) {
                Slider(
                    value = autoIntervalSeconds,
                    onValueChange = { newValue ->
                        onAutoIntervalChange(newValue)
                        intervalText = String.format("%.1f", newValue)
                    },
                    valueRange = 0.5f..10.0f,
                    steps = 18, // 0.5 step increments
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
                Text(
                    text = stringResource(R.string.auto_interval_label) + ": " + intervalText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
