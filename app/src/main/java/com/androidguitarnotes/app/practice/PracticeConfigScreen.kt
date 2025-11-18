package com.androidguitarnotes.app.practice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidguitarnotes.app.R
import com.androidguitarnotes.app.ui.NoteColors.getBackgroundOverlayColor

@Composable
fun PracticeConfigScreen(
    onBack: () -> Unit,
    onStartPractice: (PracticeConfig) -> Unit,
    viewModel: PracticeConfigViewModel =
        viewModel(
            factory =
                PracticeConfigViewModelFactory(
                    androidx.compose.ui.platform.LocalContext.current.applicationContext,
                ),
        ),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val buttonTransparency = 0.6f

    Box(modifier = Modifier.fillMaxSize()) {
        // Background layer - guitar fretboard image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Semi-transparent overlay
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(getBackgroundOverlayColor()),
        )

        // Content layer with transparent Scaffold
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.practice_config_title), color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Text("←", fontSize = 24.sp, color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                    ),
                )
            },
            bottomBar = {
                // Bottom buttons permanently visible
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    ) {
                        Text(stringResource(R.string.back))
                    }

                    Button(
                        onClick = { onStartPractice(config) },
                        enabled = viewModel.isConfigValid(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.androidguitarnotes.app.ui.NoteColors
                                .getAccessibleButtonColorFor("Practice")
                                .copy(alpha = buttonTransparency),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(stringResource(R.string.start_practice))
                    }
                }
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp)
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

                if (config.noteMode == NoteMode.SCALE) {
                    ScaleSelectionSection(
                        selectedScale = config.selectedScale,
                        onScaleSelected = { viewModel.setSelectedScale(it) },
                    )
                }

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
            }
        }
    }
}

@Composable
private fun StringSelectionSection(
    selectedStrings: Set<Int>,
    onToggleString: (Int) -> Unit,
) {
    // Map string numbers to their open note names
    val stringNotes =
        mapOf(
            6 to "E",
            5 to "A",
            4 to "D",
            3 to "G",
            2 to "B",
            1 to "E",
        )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.select_strings),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (stringNum in 6 downTo 1) {
                Text(
                    text = stringNotes[stringNum] ?: "",
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }

        // String selection chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (stringNum in 6 downTo 1) {
                val noteName = stringNotes[stringNum] ?: ""
                FilterChip(
                    selected = selectedStrings.contains(stringNum),
                    onClick = { onToggleString(stringNum) },
                    label = { Text(stringNum.toString()) },
                    modifier = Modifier.weight(1f),
                    colors =
                        if (selectedStrings.contains(stringNum)) {
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor =
                                    com.androidguitarnotes.app.ui.NoteColors
                                        .getLightColorForNote(noteName)
                                        .copy(alpha = 0.6f),
                                selectedLabelColor =
                                    com.androidguitarnotes.app.ui.NoteColors
                                        .getDarkColorForNote(noteName),
                            )
                        } else {
                            FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.15f),
                                labelColor = Color.White,
                            )
                        },
                )
            }
        }

        if (selectedStrings.isEmpty()) {
            Text(
                text = stringResource(R.string.at_least_one_string),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF6B6B),
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
            color = Color.White,
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
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedLabelColor = Color.White.copy(alpha = 0.7f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = Color.White,
                ),
            )

            Text("—", color = Color.White)

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
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedLabelColor = Color.White.copy(alpha = 0.7f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = Color.White,
                ),
            )
        }

        if (fretFrom > fretTo || fretTo > 24) {
            Text(
                text = stringResource(R.string.invalid_fret_range),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF6B6B),
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
            color = Color.White,
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
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White.copy(alpha = 0.6f),
                        ),
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
                        color = Color.White,
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
            color = Color.White,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = durationType == DurationType.TIME,
                    onClick = { onDurationTypeChange(DurationType.TIME) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.White,
                        unselectedColor = Color.White.copy(alpha = 0.6f),
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.duration_mode_time),
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                )
            }

            if (durationType == DurationType.TIME) {
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = {
                        minutesText = it
                        it.toIntOrNull()?.let { minutes ->
                            val maxMinutes = 480
                            if (minutes > 0 && minutes <= maxMinutes) {
                                onDurationMinutesChange(minutes)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.minutes)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                        cursorColor = Color.White,
                    ),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = durationType == DurationType.COUNT,
                    onClick = { onDurationTypeChange(DurationType.COUNT) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.White,
                        unselectedColor = Color.White.copy(alpha = 0.6f),
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.duration_mode_count),
                    modifier = Modifier.weight(1f),
                    color = Color.White,
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
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                        cursorColor = Color.White,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ScaleSelectionSection(
    selectedScale: Scale,
    onScaleSelected: (Scale) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.select_scale),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = getScaleName(selectedScale),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedLabelColor = Color.White.copy(alpha = 0.7f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                    focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                ),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1A1A1A).copy(alpha = 0.95f)),
            ) {
                Scale.entries.forEach { scale ->
                    DropdownMenuItem(
                        text = { Text(getScaleName(scale), color = Color.White) },
                        onClick = {
                            onScaleSelected(scale)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun getScaleName(scale: Scale): String =
    when (scale) {
        Scale.C_MAJOR -> stringResource(R.string.scale_c_major)
        Scale.G_MAJOR -> stringResource(R.string.scale_g_major)
        Scale.A_MINOR -> stringResource(R.string.scale_a_minor)
        Scale.E_MINOR -> stringResource(R.string.scale_e_minor)
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
            color = Color.White,
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
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White.copy(alpha = 0.6f),
                        ),
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
                            color = Color.White,
                        )
                        Text(
                            text =
                                when (mode) {
                                    ProgressionMode.MANUAL -> stringResource(R.string.progression_manual_desc)
                                    ProgressionMode.AUDIO_VERIFICATION -> stringResource(R.string.progression_audio_desc)
                                    ProgressionMode.AUTO_INTERVAL -> stringResource(R.string.progression_interval_desc)
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White.copy(alpha = 0.7f),
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                    ),
                )
                Text(
                    text = stringResource(R.string.auto_interval_label) + ": " + intervalText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                )
            }
        }
    }
}
