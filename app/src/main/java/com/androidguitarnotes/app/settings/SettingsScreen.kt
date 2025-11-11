package com.androidguitarnotes.app.settings

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
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidguitarnotes.app.R
import com.androidguitarnotes.app.audio.AudioManager

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val audioFeedbackEnabled by viewModel.audioFeedbackEnabled.collectAsStateWithLifecycle()
    val defaultTuning by viewModel.defaultTuning.collectAsStateWithLifecycle()
    val microphoneSensitivity by viewModel.microphoneSensitivity.collectAsStateWithLifecycle()
    val autoAdjustSensitivity by viewModel.autoAdjustSensitivity.collectAsStateWithLifecycle()

    // Use remember with cleanup for proper lifecycle management
    val audioManager =
        remember {
            AudioManager()
        }
    var currentAudioLevel by remember { mutableFloatStateOf(0f) }

    // Single effect to handle audio listening based on both parameters
    LaunchedEffect(microphoneSensitivity, audioFeedbackEnabled) {
        // Stop any previous listening session before starting a new one
        audioManager.stopListening()

        if (audioFeedbackEnabled) {
            try {
                audioManager.startListening(microphoneSensitivity).collect { result ->
                    currentAudioLevel =
                        when (result) {
                            is AudioManager.AudioAnalysisResult.NoteDetected -> result.audioLevel
                            is AudioManager.AudioAnalysisResult.NoNoteDetected -> result.audioLevel
                        }
                }
            } catch (e: Exception) {
                // Ignore permission errors in settings
            }
        } else {
            currentAudioLevel = 0f
        }
    }

    // Cleanup when screen is disposed
    DisposableEffect(Unit) {
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
                onCheckedChange = { viewModel.toggleAudioFeedback(it) },
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
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LinearProgressIndicator(
            progress = level,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp),
        )
    }
}
