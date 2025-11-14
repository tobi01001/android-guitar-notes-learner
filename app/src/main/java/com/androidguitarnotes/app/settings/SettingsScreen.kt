package com.androidguitarnotes.app.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidguitarnotes.app.R
import com.androidguitarnotes.app.audio.AudioManager
import com.androidguitarnotes.app.audio.PitchDetectionAlgorithm
import com.androidguitarnotes.app.permissions.PermissionManager
import com.androidguitarnotes.app.permissions.PermissionRationaleScreen

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel =
        viewModel(
            factory = SettingsViewModelFactory(androidx.compose.ui.platform.LocalContext.current.applicationContext),
        ),
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }

    val audioFeedbackEnabled by viewModel.audioFeedbackEnabled.collectAsStateWithLifecycle()
    val defaultTuning by viewModel.defaultTuning.collectAsStateWithLifecycle()
    val microphoneSensitivity by viewModel.microphoneSensitivity.collectAsStateWithLifecycle()
    val autoAdjustSensitivity by viewModel.autoAdjustSensitivity.collectAsStateWithLifecycle()
    val audioSource by viewModel.audioSource.collectAsStateWithLifecycle()
    val noiseGateThreshold by viewModel.noiseGateThreshold.collectAsStateWithLifecycle()
    val pitchDetectionAlgorithm by viewModel.pitchDetectionAlgorithm.collectAsStateWithLifecycle()

    var showAudioSourceDialog by remember { mutableStateOf(false) }
    var showAlgorithmDialog by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var permissionRequestPending by remember { mutableStateOf(false) }

    // Audio permission launcher
    val audioPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (!isGranted) {
                // If permission denied, disable audio feedback
                viewModel.toggleAudioFeedback(false)
            }
            permissionRequestPending = false
        }

    // Show permission rationale dialog
    if (showPermissionRationale) {
        PermissionRationaleScreen(
            onRequestPermission = {
                showPermissionRationale = false
                permissionRequestPending = true
            },
            onDismiss = {
                showPermissionRationale = false
                // If user dismissed rationale, turn off audio feedback
                viewModel.toggleAudioFeedback(false)
            },
        )
    }

    // Request permission when pending
    LaunchedEffect(permissionRequestPending) {
        if (permissionRequestPending) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Create a new AudioManager instance whenever the algorithm changes
    // This ensures clean state and avoids coroutine cancellation issues
    val audioManager =
        remember(pitchDetectionAlgorithm) {
            val algorithm =
                try {
                    PitchDetectionAlgorithm.valueOf(pitchDetectionAlgorithm)
                } catch (e: IllegalArgumentException) {
                    PitchDetectionAlgorithm.YIN // Default to YIN if invalid
                }
            AudioManager(algorithm)
        }
    var currentAudioLevel by remember { mutableFloatStateOf(0f) }
    var isGated by remember { mutableStateOf(false) }

    // Single effect to handle audio listening based on all relevant parameters
    LaunchedEffect(
        microphoneSensitivity,
        audioFeedbackEnabled,
        audioSource,
        noiseGateThreshold,
        autoAdjustSensitivity,
        pitchDetectionAlgorithm,
    ) {
        // Stop any previous listening session before starting a new one
        audioManager.stopListening()

        if (audioFeedbackEnabled && permissionManager.isRecordAudioPermissionGranted()) {
            try {
                val audioSourceValue = if (audioSource.value == -1) null else audioSource.value
                audioManager
                    .startListeningWithDetectedNote(
                        sensitivityMultiplier = microphoneSensitivity,
                        audioSource = audioSourceValue,
                        noiseGateThreshold = noiseGateThreshold,
                        autoAdjustEnabled = autoAdjustSensitivity,
                    ).collect { detectedNote ->
                        currentAudioLevel = detectedNote.audioLevel
                        isGated = detectedNote.isGated
                    }
            } catch (e: Exception) {
                // Ignore permission errors in settings
            }
        } else {
            currentAudioLevel = 0f
            isGated = false
        }
    }

    // Cleanup when screen is disposed or audioManager changes
    DisposableEffect(audioManager) {
        onDispose {
            audioManager.stopListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            // General Settings Section
            SettingsSectionHeader(title = stringResource(R.string.general_settings))

            SettingsItem(
                title = stringResource(R.string.default_tuning),
                subtitle = stringResource(R.string.tuning_standard),
                description = stringResource(R.string.tuning_description),
            )

            Divider()

            // Audio Settings Section
            SettingsSectionHeader(title = stringResource(R.string.audio_settings))

            SettingsSwitchItem(
                title = stringResource(R.string.audio_feedback),
                description = stringResource(R.string.audio_feedback_description),
                checked = audioFeedbackEnabled,
                onCheckedChange = { enabled ->
                    if (enabled && !permissionManager.isRecordAudioPermissionGranted()) {
                        // Request permission before enabling
                        showPermissionRationale = true
                    } else {
                        // Permission already granted or disabling
                        viewModel.toggleAudioFeedback(enabled)
                    }
                },
            )

            Divider()

            // Microphone Sensitivity Section
            if (audioFeedbackEnabled) {
                val sensitivityLabel =
                    when {
                        microphoneSensitivity <= 0.7f -> stringResource(R.string.sensitivity_low)
                        microphoneSensitivity >= 1.3f -> stringResource(R.string.sensitivity_high)
                        else -> stringResource(R.string.sensitivity_normal)
                    }

                SettingsSliderItem(
                    title = stringResource(R.string.microphone_sensitivity),
                    description = stringResource(R.string.sensitivity_description),
                    value = microphoneSensitivity,
                    onValueChange = { viewModel.setMicrophoneSensitivity(it) },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    valueLabel = sensitivityLabel,
                )

                Divider()

                SettingsSwitchItem(
                    title = stringResource(R.string.auto_adjust_sensitivity),
                    description = stringResource(R.string.auto_adjust_description),
                    checked = autoAdjustSensitivity,
                    onCheckedChange = { viewModel.toggleAutoAdjustSensitivity(it) },
                )

                Divider()

                // Audio Level Bar
                AudioLevelBar(
                    title = stringResource(R.string.audio_input_level),
                    level = currentAudioLevel,
                    isGated = isGated,
                )

                Divider()

                // Noise Gate Threshold
                val gateLabel =
                    when {
                        noiseGateThreshold <= 0.005f -> stringResource(R.string.noise_gate_very_low)
                        noiseGateThreshold <= 0.015f -> stringResource(R.string.noise_gate_low)
                        noiseGateThreshold <= 0.035f -> stringResource(R.string.noise_gate_medium)
                        else -> stringResource(R.string.noise_gate_high)
                    }

                SettingsSliderItem(
                    title = stringResource(R.string.noise_gate_threshold),
                    description = stringResource(R.string.noise_gate_description),
                    value = noiseGateThreshold,
                    onValueChange = { viewModel.setNoiseGateThreshold(it) },
                    valueRange = 0.001f..0.1f,
                    steps = 0,
                    valueLabel = gateLabel,
                )

                Divider()

                // Audio Input Source Selection
                SettingsClickableItem(
                    title = stringResource(R.string.audio_input_source),
                    subtitle = getAudioSourceName(audioSource),
                    description = stringResource(R.string.audio_input_source_description),
                    onClick = { showAudioSourceDialog = true },
                )

                Divider()

                // Pitch Detection Algorithm Selection
                SettingsClickableItem(
                    title = stringResource(R.string.pitch_detection_algorithm),
                    subtitle = getAlgorithmDisplayName(pitchDetectionAlgorithm),
                    description = stringResource(R.string.pitch_detection_description),
                    onClick = { showAlgorithmDialog = true },
                )

                Divider()
            }

            // About Section
            SettingsSectionHeader(title = stringResource(R.string.about_section))

            SettingsItem(
                title = stringResource(R.string.app_version),
                subtitle = stringResource(R.string.version_value),
            )
        }
    }

    // Audio Source Selection Dialog
    if (showAudioSourceDialog) {
        AudioSourceDialog(
            currentSource = audioSource,
            onSourceSelected = { source ->
                viewModel.setAudioSource(source)
                showAudioSourceDialog = false
            },
            onDismiss = { showAudioSourceDialog = false },
        )
    }

    // Pitch Detection Algorithm Selection Dialog
    if (showAlgorithmDialog) {
        AlgorithmDialog(
            currentAlgorithm = pitchDetectionAlgorithm,
            onAlgorithmSelected = { algorithm ->
                viewModel.setPitchDetectionAlgorithm(algorithm)
                showAlgorithmDialog = false
            },
            onDismiss = { showAlgorithmDialog = false },
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    description: String? = null,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    description: String? = null,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueLabel: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
        )
    }
}

@Composable
private fun AudioLevelBar(
    title: String,
    level: Float,
    isGated: Boolean = false,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (isGated) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.noise_gate_idle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        LinearProgressIndicator(
            progress = level,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp),
        )
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String? = null,
    description: String? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun AudioSourceDialog(
    currentSource: AudioSource,
    onSourceSelected: (AudioSource) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.audio_input_source)) },
        text = {
            Column {
                AudioSource.entries.forEach { source ->
                    AudioSourceOption(
                        name = source.displayName,
                        source = source,
                        currentSource = currentSource,
                        onSelect = onSourceSelected,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}

@Composable
private fun AudioSourceOption(
    name: String,
    source: AudioSource,
    currentSource: AudioSource,
    onSelect: (AudioSource) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onSelect(source) }
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = source == currentSource,
            onClick = { onSelect(source) },
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name)
    }
}

@Composable
private fun getAudioSourceName(source: AudioSource): String = source.displayName

@Composable
private fun getAlgorithmDisplayName(algorithm: String): String =
    when (algorithm) {
        "AUTOCORRELATION" -> stringResource(R.string.algorithm_autocorrelation)
        "YIN" -> stringResource(R.string.algorithm_yin)
        "YIN_ADAPTIVE" -> stringResource(R.string.algorithm_yin_adaptive)
        "YIN_MULTI_PERIOD" -> stringResource(R.string.algorithm_yin_multi_period)
        "YIN_ENHANCED" -> stringResource(R.string.algorithm_yin_enhanced)
        "HYBRID_YIN_FFT" -> stringResource(R.string.algorithm_hybrid_yin_fft)
        else -> stringResource(R.string.algorithm_yin) // Default to YIN
    }

@Composable
private fun AlgorithmDialog(
    currentAlgorithm: String,
    onAlgorithmSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val algorithms =
        listOf(
            "AUTOCORRELATION" to stringResource(R.string.algorithm_autocorrelation),
            "YIN" to stringResource(R.string.algorithm_yin),
            "YIN_ADAPTIVE" to stringResource(R.string.algorithm_yin_adaptive),
            "YIN_MULTI_PERIOD" to stringResource(R.string.algorithm_yin_multi_period),
            "YIN_ENHANCED" to stringResource(R.string.algorithm_yin_enhanced),
            "HYBRID_YIN_FFT" to stringResource(R.string.algorithm_hybrid_yin_fft),
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pitch_detection_algorithm)) },
        text = {
            Column {
                algorithms.forEach { (algorithmKey, displayName) ->
                    AlgorithmOption(
                        name = displayName,
                        algorithmKey = algorithmKey,
                        currentAlgorithm = currentAlgorithm,
                        onSelect = onAlgorithmSelected,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}

@Composable
private fun AlgorithmOption(
    name: String,
    algorithmKey: String,
    currentAlgorithm: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onSelect(algorithmKey) }
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = algorithmKey == currentAlgorithm,
            onClick = { onSelect(algorithmKey) },
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name)
    }
}
