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

    private val _noiseGateThreshold = MutableStateFlow(0.01f)
    val noiseGateThreshold: StateFlow<Float> = _noiseGateThreshold.asStateFlow()

    private val _pitchDetectionAlgorithm = MutableStateFlow("YIN")
    val pitchDetectionAlgorithm: StateFlow<String> = _pitchDetectionAlgorithm.asStateFlow()

    private val _multiFrameConfirmation = MutableStateFlow(false)
    val multiFrameConfirmation: StateFlow<Boolean> = _multiFrameConfirmation.asStateFlow()

    init {
        // Load saved audio source
        viewModelScope.launch {
            repository.audioSource.collect { savedSource ->
                _audioSource.value = savedSource
            }
        }

        // Load saved noise gate threshold
        viewModelScope.launch {
            repository.noiseGateThreshold.collect { savedThreshold ->
                _noiseGateThreshold.value = savedThreshold
            }
        }

        // Load saved audio feedback enabled
        viewModelScope.launch {
            repository.audioFeedbackEnabled.collect { savedEnabled ->
                _audioFeedbackEnabled.value = savedEnabled
            }
        }

        // Load saved default tuning
        viewModelScope.launch {
            repository.defaultTuning.collect { savedTuning ->
                _defaultTuning.value = savedTuning
            }
        }

        // Load saved microphone sensitivity
        viewModelScope.launch {
            repository.microphoneSensitivity.collect { savedSensitivity ->
                _microphoneSensitivity.value = savedSensitivity
            }
        }

        // Load saved auto-adjust sensitivity
        viewModelScope.launch {
            repository.autoAdjustSensitivity.collect { savedAutoAdjust ->
                _autoAdjustSensitivity.value = savedAutoAdjust
            }
        }

        // Load saved pitch detection algorithm
        viewModelScope.launch {
            repository.pitchDetectionAlgorithm.collect { savedAlgorithm ->
                _pitchDetectionAlgorithm.value = savedAlgorithm
            }
        }

        // Load saved multi-frame confirmation setting
        viewModelScope.launch {
            repository.multiFrameConfirmation.collect { savedEnabled ->
                _multiFrameConfirmation.value = savedEnabled
            }
        }
    }

    /**
     * Toggles audio feedback setting.
     */
    fun toggleAudioFeedback(enabled: Boolean) {
        _audioFeedbackEnabled.value = enabled
        viewModelScope.launch {
            repository.saveAudioFeedbackEnabled(enabled)
        }
    }

    /**
     * Updates the default tuning setting.
     */
    fun setDefaultTuning(tuning: String) {
        _defaultTuning.value = tuning
        viewModelScope.launch {
            repository.saveDefaultTuning(tuning)
        }
    }

    /**
     * Updates microphone sensitivity (0.5 to 2.0).
     */
    fun setMicrophoneSensitivity(sensitivity: Float) {
        _microphoneSensitivity.value = sensitivity.coerceIn(0.5f, 2.0f)
        viewModelScope.launch {
            repository.saveMicrophoneSensitivity(_microphoneSensitivity.value)
        }
    }

    /**
     * Toggles auto-adjust sensitivity setting.
     *
     * When enabled, the audio processing pipeline will automatically adjust the sensitivity
     * multiplier based on the incoming signal level to maintain optimal pitch detection.
     * The manual sensitivity slider acts as the base multiplier, and auto-adjust applies
     * dynamic fine-tuning on top of it.
     */
    fun toggleAutoAdjustSensitivity(enabled: Boolean) {
        _autoAdjustSensitivity.value = enabled
        viewModelScope.launch {
            repository.saveAutoAdjustSensitivity(enabled)
        }
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

    /**
     * Sets the noise gate threshold (0.001 to 0.1).
     */
    fun setNoiseGateThreshold(threshold: Float) {
        _noiseGateThreshold.value = threshold.coerceIn(0.001f, 0.1f)
        viewModelScope.launch {
            repository.saveNoiseGateThreshold(_noiseGateThreshold.value)
        }
    }

    /**
     * Sets the pitch detection algorithm.
     */
    fun setPitchDetectionAlgorithm(algorithm: String) {
        _pitchDetectionAlgorithm.value = algorithm
        viewModelScope.launch {
            repository.savePitchDetectionAlgorithm(algorithm)
        }
    }

    /**
     * Toggles multi-frame confirmation setting (ENH-001).
     *
     * When enabled, requires consecutive audio frames to detect the same pitch
     * before confirming detection. Reduces false positives but adds latency.
     */
    fun toggleMultiFrameConfirmation(enabled: Boolean) {
        _multiFrameConfirmation.value = enabled
        viewModelScope.launch {
            repository.saveMultiFrameConfirmation(enabled)
        }
    }
}
