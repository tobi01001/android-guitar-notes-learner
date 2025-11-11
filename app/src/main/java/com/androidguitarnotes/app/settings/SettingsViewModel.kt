package com.androidguitarnotes.app.settings

import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
class SettingsViewModel : ViewModel() {
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
    // Audio source: null means auto-select
    private val _audioSource = MutableStateFlow<Int?>(null)
    val audioSource: StateFlow<Int?> = _audioSource.asStateFlow()

    /**
     * Toggles audio feedback setting.
     */
    fun toggleAudioFeedback(enabled: Boolean) {
        _audioFeedbackEnabled.value = enabled
    }

    /**
     * Sets the audio source.
     * Pass null for automatic selection.
     */
    fun setAudioSource(source: Int?) {
        _audioSource.value = source
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
     */
    fun toggleAutoAdjustSensitivity(enabled: Boolean) {
        _autoAdjustSensitivity.value = enabled
    }

    /**
     * Sets the audio source for recording.
     */
    fun setAudioSource(audioSource: AudioSource) {
        _audioSource.value = audioSource
    }
}
