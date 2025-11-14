package com.androidguitarnotes.app.tuner

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidguitarnotes.app.R
import com.androidguitarnotes.app.audio.AudioManager
import com.androidguitarnotes.app.permissions.PermissionManager
import com.androidguitarnotes.app.permissions.PermissionRationaleScreen
import com.androidguitarnotes.app.ui.KeepScreenOn
import com.androidguitarnotes.app.ui.NoteColors
import kotlin.math.abs

/**
 * Tuner screen composable.
 */
@Composable
fun TunerScreen(
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
    val audioManager = remember { AudioManager() }
    val permissionManager = remember { PermissionManager(context) }

    DisposableEffect(audioManager) {
        onDispose {
            audioManager.stopListening()
        }
    }
    val viewModel: TunerViewModel =
        viewModel(
            factory = TunerViewModelFactory(audioManager, settingsViewModel, permissionManager),
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
                title = { Text(stringResource(R.string.tuner_title)) },
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
            // String selector
            StringSelector(
                strings = GuitarString.STANDARD_TUNING,
                selectedString = state.selectedString,
                onStringSelected = viewModel::selectString,
            )

            // Tuning indicator
            TuningIndicator(
                state = state,
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
                        stringResource(R.string.stop_tuning)
                    } else {
                        stringResource(R.string.start_tuning)
                    },
                )
            }
        }
    }
}

/**
 * String selector component.
 */
@Composable
private fun StringSelector(
    strings: List<GuitarString>,
    selectedString: GuitarString,
    onStringSelected: (GuitarString) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            stringResource(R.string.select_string_to_tune),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            strings.forEach { guitarString ->
                StringButton(
                    guitarString = guitarString,
                    isSelected = guitarString == selectedString,
                    onClick = { onStringSelected(guitarString) },
                )
            }
        }
    }
}

/**
 * String button component.
 */
@Composable
private fun StringButton(
    guitarString: GuitarString,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        if (isSelected) {
            NoteColors.getLightColorForNote(guitarString.noteName)
        } else {
            MaterialTheme.colorScheme.surface
        }

    val contentColor =
        if (isSelected) {
            NoteColors.getDarkColorForNote(guitarString.noteName)
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Button(
        onClick = onClick,
        modifier =
            modifier
                .size(56.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = contentColor,
            ),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                guitarString.noteName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                guitarString.number.toString(),
                fontSize = 10.sp,
            )
        }
    }
}

/**
 * Tuning indicator component.
 */
@Composable
private fun TuningIndicator(
    state: TunerState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Target frequency display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(
                        R.string.string_name_octave,
                        state.selectedString.noteName,
                        state.selectedString.octave,
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.target_frequency, state.selectedString.frequency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Visual tuning indicator - always show the bar
        val status = state.tuningStatus
        when (status) {
            is TuningStatus.NotDetected -> {
                // Show bar with grey dot at far left
                TuningGauge(
                    cents = null, // null indicates no detection
                    detectedFrequency = null,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (state.isListening) {
                        stringResource(R.string.no_sound_detected)
                    } else {
                        stringResource(R.string.select_string_to_tune)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            is TuningStatus.Detecting -> {
                TuningGauge(
                    cents = status.cents,
                    detectedFrequency = status.detectedFrequency,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.sound_detecting),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Tuning gauge component showing cents deviation.
 */
@Composable
private fun TuningGauge(
    cents: Double?,
    detectedFrequency: Double?,
    modifier: Modifier = Modifier,
) {
    // When cents is null, we're not detecting - show grey dot at far left
    val isDetecting = cents != null
    val safeCents = cents ?: -TunerConstants.MAX_CENTS // Default to far left when not detecting

    val isInTune = cents != null && abs(cents) <= TunerConstants.IN_TUNE_THRESHOLD_CENTS
    val isTooFlat = cents != null && cents < -TunerConstants.IN_TUNE_THRESHOLD_CENTS
    val isTooSharp = cents != null && cents > TunerConstants.IN_TUNE_THRESHOLD_CENTS

    // Animate the indicator color
    val indicatorColor by animateColorAsState(
        targetValue =
            when {
                !isDetecting -> Color.Gray // Grey when not detecting
                isInTune -> Color(0xFF4CAF50) // Green
                else -> Color(0xFFFF9800) // Orange
            },
        label = "indicatorColor",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        // Status text - always visible to prevent jumping
        // Shows status when detecting, shows placeholder when not
        val statusAlpha by animateFloatAsState(
            targetValue = if (isDetecting) 1.0f else 0.3f,
            label = "statusAlpha",
        )

        val statusText =
            when {
                isInTune -> stringResource(R.string.in_tune)
                isTooFlat -> stringResource(R.string.tune_up)
                isTooSharp -> stringResource(R.string.tune_down)
                else -> stringResource(R.string.tune_up) // Default placeholder
            }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                statusText,
                style = MaterialTheme.typography.headlineMedium,
                color = indicatorColor.copy(alpha = statusAlpha),
                fontWeight = FontWeight.Bold,
            )
        }

        // Cents deviation bar - always show
        CentsDeviationBar(
            cents = safeCents,
            color = indicatorColor,
        )

        // Numeric display - always visible to prevent jumping
        val numericAlpha by animateFloatAsState(
            targetValue = if (isDetecting) 1.0f else 0.3f,
            label = "numericAlpha",
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(
                        R.string.detected_frequency,
                        detectedFrequency ?: 0.0,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = numericAlpha),
                )
                Text(
                    stringResource(
                        R.string.cents_deviation,
                        cents ?: 0.0,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = indicatorColor.copy(alpha = numericAlpha),
                )
            }
        }
    }
}

/**
 * Cents deviation bar showing visual position.
 */
@Composable
private fun CentsDeviationBar(
    cents: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val maxCents = 50.0 // Show ±50 cents
    val targetPosition = (cents / maxCents).coerceIn(-1.0, 1.0).toFloat()

    // Animate the position with cubic easing for smooth motion
    val animatedPosition by animateFloatAsState(
        targetValue = targetPosition,
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing, // Cubic Bezier curve for smooth transitions
            ),
        label = "dotPosition",
    )

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val containerWidth = maxWidth
        val indicatorSize = 40.dp
        val offsetX = (containerWidth - indicatorSize) * ((animatedPosition + 1f) / 2f)

        // Center line
        Box(
            modifier =
                Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.outline),
        )

        // Indicator
        Box(
            modifier =
                Modifier
                    .size(indicatorSize)
                    .align(Alignment.CenterStart)
                    .offset(x = offsetX)
                    .clip(CircleShape)
                    .background(color)
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
        )
    }
}
