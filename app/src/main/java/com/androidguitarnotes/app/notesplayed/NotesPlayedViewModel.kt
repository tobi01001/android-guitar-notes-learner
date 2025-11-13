package com.androidguitarnotes.app.notesplayed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidguitarnotes.app.audio.AudioManager
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
) : ViewModel() {
    private val _state = MutableStateFlow(NotesPlayedState())
    val state: StateFlow<NotesPlayedState> = _state.asStateFlow()

    private var listeningJob: Job? = null

    /**
     * Starts listening for audio input and detecting notes.
     */
    fun startListening() {
        if (_state.value.isListening) return

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

                    audioManager
                        .startListeningWithDetectedNote(
                            sensitivityMultiplier = sensitivity,
                            audioSource = audioSourceValue,
                            autoAdjustEnabled = autoAdjust,
                            noiseGateThreshold = noiseGateThreshold,
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
                } catch (e: Exception) {
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
