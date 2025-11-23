package com.androidguitarnotes.app.notesplayed

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

/**
 * ViewModel for the Notes Played screen.
 * Manages audio listening and note detection state.
 */
class NotesPlayedViewModel(
    private val audioManager: AudioManager,
    private val settingsViewModel: SettingsViewModel,
    private val permissionManager: PermissionManager,
) : ViewModel() {
    private val _state = MutableStateFlow(NotesPlayedState())
    val state: StateFlow<NotesPlayedState> = _state.asStateFlow()

    private val _audioPermissionRequired = MutableStateFlow(false)
    val audioPermissionRequired: StateFlow<Boolean> = _audioPermissionRequired.asStateFlow()

    private val _showPermissionRationale = MutableStateFlow(false)
    val showPermissionRationale: StateFlow<Boolean> = _showPermissionRationale.asStateFlow()

    private var listeningJob: Job? = null

    /**
     * Checks and requests audio permission if needed for notes detection.
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
     * Starts listening for audio input and detecting notes (public interface).
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
            Log.w("NotesPlayedViewModel", "Cannot start listening - permission not granted")
            return
        }

        listeningJob?.cancel()
        listeningJob =
            viewModelScope.launch {
                try {
                    _state.value = _state.value.copy(isListening = true)
                    val audioSource = settingsViewModel.audioSource.value
                    val audioSourceValue = if (audioSource.value == -1) null else audioSource.value
                    val sensitivity = settingsViewModel.microphoneSensitivity.value
                    val autoAdjust = settingsViewModel.autoAdjustSensitivity.value
                    val noiseGateThreshold = settingsViewModel.noiseGateThreshold.value

                    val multiFrameConfirmation = settingsViewModel.multiFrameConfirmation.value

                    audioManager
                        .startListeningWithDetectedNote(
                            sensitivityMultiplier = sensitivity,
                            audioSource = audioSourceValue,
                            autoAdjustEnabled = autoAdjust,
                            noiseGateThreshold = noiseGateThreshold,
                            multiFrameConfirmationEnabled = multiFrameConfirmation,
                        ).collect { detectedNote ->
                            if (detectedNote.isDetected) {
                                val noteInfo =
                                    DetectedNoteInfo(
                                        noteName = detectedNote.noteName,
                                        frequency = detectedNote.frequency!!,
                                        cents = detectedNote.cents,
                                        octave = detectedNote.octave,
                                        noteNameWithOctave = detectedNote.noteNameWithOctave,
                                    )
                                _state.value =
                                    _state.value.copy(
                                        detectedNote = noteInfo,
                                        lastDetectedNote = noteInfo,
                                        lastDetectionTimestamp = System.currentTimeMillis(),
                                    )
                            } else {
                                _state.value =
                                    _state.value.copy(
                                        detectedNote = null,
                                    )
                            }
                        }
                } catch (e: SecurityException) {
                    Log.e("NotesPlayedViewModel", "Permission revoked during recording", e)
                    _state.value =
                        _state.value.copy(
                            isListening = false,
                            detectedNote = null,
                            lastDetectedNote = null,
                            lastDetectionTimestamp = 0L,
                        )
                } catch (e: Exception) {
                    Log.e("NotesPlayedViewModel", "Audio listening error", e)
                    _state.value =
                        _state.value.copy(
                            isListening = false,
                            detectedNote = null,
                            lastDetectedNote = null,
                            lastDetectionTimestamp = 0L,
                        )
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
                detectedNote = null,
                lastDetectedNote = null,
                lastDetectionTimestamp = 0L,
            )
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
