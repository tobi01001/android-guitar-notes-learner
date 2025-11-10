package com.androidguitarnotes.app.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing settings state.
 */
class SettingsViewModel : ViewModel() {
    private val _audioFeedbackEnabled = MutableStateFlow(true)
    val audioFeedbackEnabled: StateFlow<Boolean> = _audioFeedbackEnabled.asStateFlow()

    private val _defaultTuning = MutableStateFlow("Standard")
    val defaultTuning: StateFlow<String> = _defaultTuning.asStateFlow()

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
}
