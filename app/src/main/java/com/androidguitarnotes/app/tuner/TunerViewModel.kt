package com.androidguitarnotes.app.tuner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidguitarnotes.app.audio.AudioManager
import com.androidguitarnotes.app.permissions.PermissionManager
import com.androidguitarnotes.app.settings.SettingsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2

/**
 * ViewModel for the guitar tuner screen.
 */
class TunerViewModel(
    private val audioManager: AudioManager,
    private val settingsViewModel: SettingsViewModel,
    private val permissionManager: PermissionManager,
) : ViewModel() {
    private val _state = MutableStateFlow(TunerState())
    val state: StateFlow<TunerState> = _state.asStateFlow()

    private val _audioPermissionRequired = MutableStateFlow(false)
    val audioPermissionRequired: StateFlow<Boolean> = _audioPermissionRequired.asStateFlow()

    private val _showPermissionRationale = MutableStateFlow(false)
    val showPermissionRationale: StateFlow<Boolean> = _showPermissionRationale.asStateFlow()

    private var listeningJob: Job? = null

    /**
     * Checks and requests audio permission if needed for tuner.
     */
    fun checkAndRequestAudioPermission() {
        if (permissionManager.isRecordAudioPermissionGranted()) {
            // Permission already granted, start audio listening
            startListeningInternal()
        } else {
            // Show rationale first
            _showPermissionRationale.value = true
        }
    }

    /**
     * Called when user dismisses permission rationale.
     */
    fun onPermissionRationaleDismissed() {
        _showPermissionRationale.value = false
    }

    /**
     * Requests audio permission after showing rationale.
     */
    fun requestAudioPermission() {
        _showPermissionRationale.value = false
        _audioPermissionRequired.value = true
    }

    /**
     * Called when audio permission is granted.
     */
    fun onAudioPermissionGranted() {
        _audioPermissionRequired.value = false
        startListeningInternal()
    }

    /**
     * Called when audio permission is denied.
     */
    fun onAudioPermissionDenied() {
        _audioPermissionRequired.value = false
        // Don't start listening without permission
    }

    /**
     * Selects a guitar string to tune.
     */
    fun selectString(guitarString: GuitarString) {
        _state.value = _state.value.copy(selectedString = guitarString)
    }

    /**
     * Starts listening for audio input (public interface).
     * Checks permission before starting.
     */
    fun startListening() {
        checkAndRequestAudioPermission()
    }

    /**
     * Internal method to start listening after permission is granted.
     */
    private fun startListeningInternal() {
        if (_state.value.isListening) return

        // Double-check permission
        if (!permissionManager.isRecordAudioPermissionGranted()) {
            Log.w("TunerViewModel", "Cannot start listening - permission not granted")
            return
        }

        _state.value = _state.value.copy(isListening = true)

        listeningJob?.cancel()
        listeningJob =
            viewModelScope.launch {
                try {
                    val audioSource = settingsViewModel.audioSource.value
                    val audioSourceValue = if (audioSource.value == -1) null else audioSource.value
                    val sensitivity = settingsViewModel.microphoneSensitivity.value
                    val autoAdjust = settingsViewModel.autoAdjustSensitivity.value
                    val noiseGateThreshold = settingsViewModel.noiseGateThreshold.value

                    audioManager
                        .startListeningWithDetectedNote(
                            sensitivityMultiplier = sensitivity,
                            audioSource = audioSourceValue,
                            autoAdjustEnabled = autoAdjust,
                            noiseGateThreshold = noiseGateThreshold,
                        ).collect { detectedNote ->
                            if (detectedNote.isDetected) {
                                val selectedString = _state.value.selectedString
                                val cents =
                                    calculateCentsDeviation(
                                        detectedNote.frequency!!,
                                        selectedString.frequency,
                                    )

                                _state.value =
                                    _state.value.copy(
                                        tuningStatus =
                                            TuningStatus.Detecting(
                                                detectedFrequency = detectedNote.frequency,
                                                cents = cents,
                                            ),
                                    )
                            } else {
                                _state.value =
                                    _state.value.copy(
                                        tuningStatus = TuningStatus.NotDetected,
                                    )
                            }
                        }
                } catch (e: SecurityException) {
                    Log.e("TunerViewModel", "Permission revoked during recording", e)
                    stopListening()
                } catch (e: Exception) {
                    Log.e("TunerViewModel", "Audio listening error", e)
                    stopListening()
                }
            }
    }

    /**
     * Stops listening for audio input.
     */
    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        audioManager.stopListening()
        _state.value =
            _state.value.copy(
                isListening = false,
                tuningStatus = TuningStatus.NotDetected,
            )
    }

    /**
     * Calculates the cents deviation between detected and target frequency.
     */
    private fun calculateCentsDeviation(
        detectedFreq: Double,
        targetFreq: Double,
    ): Double = 1200.0 * log2(detectedFreq / targetFreq)

    /**
     * Checks if the current tuning status is in tune.
     */
    fun isInTune(): Boolean {
        val status = _state.value.tuningStatus
        return status is TuningStatus.Detecting && abs(status.cents) <= TunerConstants.IN_TUNE_THRESHOLD_CENTS
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
