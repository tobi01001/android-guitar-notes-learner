package com.androidguitarnotes.app.notesplayed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidguitarnotes.app.audio.AudioManager
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
                    audioManager.startListening().collect { result ->
                        when (result) {
                            is AudioManager.AudioAnalysisResult.NoteDetected -> {
                                _state.value =
                                    _state.value.copy(
                                        detectedNote =
                                            DetectedNoteInfo(
                                                noteName = result.noteName,
                                                frequency = result.frequency,
                                                cents = result.cents,
                                                octave = result.octave,
                                                noteNameWithOctave = result.noteNameWithOctave,
                                            ),
                                    )
                            }
                            is AudioManager.AudioAnalysisResult.NoNoteDetected -> {
                                _state.value =
                                    _state.value.copy(
                                        detectedNote = null,
                                    )
                            }
                        }
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(isListening = false, detectedNote = null)
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
            )
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
