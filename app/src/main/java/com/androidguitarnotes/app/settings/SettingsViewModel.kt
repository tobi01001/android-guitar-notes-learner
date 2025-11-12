package com.androidguitarnotes.app.settings

import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Audio source options for recording.
 */
enum class AudioSource(
    val value: Int,
    val displayName: String,
) {
    AUTO(-1, "Auto (Best Quality)"),
    UNPROCESSED(MediaRecorder.AudioSource.UNPROCESSED, "Unprocessed"),
    VOICE_RECOGNITION(MediaRecorder.AudioSource.VOICE_RECOGNITION, "Voice Recognition"),
    MIC(MediaRecorder.AudioSource.MIC, "Microphone"),
    ;

    companion object {
        fun fromValue(value: Int): AudioSource = entries.find { it.value == value } ?: AUTO
    }
}

/**
 * ViewModel for managing settings state.
 */
class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {
    private val _audioFeedbackEnabled = MutableStateFlow(true)
    val audioFeedbackEnabled: StateFlow<Boolean> = _audioFeedbackEnabled.asStateFlow()

    private val _defaultTuning = MutableStateFlow("Standard")
    val defaultTuning: StateFlow<String> = _defaultTuning.asStateFlow()

    private val _microphoneSensitivity = MutableStateFlow(1.0f)
    val microphoneSensitivity: StateFlow<Float> = _microphoneSensitivity.asStateFlow()

    private val _autoAdjustSensitivity = MutableStateFlow(false)
    val autoAdjustSensitivity: StateFlow<Boolean> = _autoAdjustSensitivity.asStateFlow()

    private val _audioSource = MutableStateFlow(AudioSource.AUTO)
    val audioSource: StateFlow<AudioSource> = _audioSource.asStateFlow()

    init {
        // Load saved audio source
        viewModelScope.launch {
            repository.audioSource.collect { savedSource ->
                _audioSource.value = savedSource
            }
        }
    }

    /**
     * Toggles audio feedback setting.
     */
    fun toggleAudioFeedback(enabled: Boolean) {
        _audioFeedbackEnabled.value = enabled
    }

    /**
     * Updates the default tuning setting.
     */
    fun setDefaultTuning(tuning: String) {
        _defaultTuning.value = tuning
    }

    /**
     * Updates microphone sensitivity (0.5 to 2.0).
     */
    fun setMicrophoneSensitivity(sensitivity: Float) {
        _microphoneSensitivity.value = sensitivity.coerceIn(0.5f, 2.0f)
    }

    /**
     * Toggles auto-adjust sensitivity setting.
     *
     * NOTE: This setting is currently not implemented in the audio processing pipeline.
     * It exists as a placeholder for future implementation. See AudioRecorder class
     * documentation for details on planned implementation.
     */
    fun toggleAutoAdjustSensitivity(enabled: Boolean) {
        _autoAdjustSensitivity.value = enabled
    }

    /**
     * Sets the audio source for recording.
     */
    fun setAudioSource(audioSource: AudioSource) {
        _audioSource.value = audioSource
        viewModelScope.launch {
            repository.saveAudioSource(audioSource)
        }
    }
}
