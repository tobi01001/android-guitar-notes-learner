package com.androidguitarnotes.app.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing practice session configuration state.
 */
class PracticeConfigViewModel(
    private val repository: PracticeSettingsRepository,
) : ViewModel() {
    private val _config = MutableStateFlow(PracticeConfig())
    val config: StateFlow<PracticeConfig> = _config.asStateFlow()

    init {
        // Load saved configuration
        viewModelScope.launch {
            repository.practiceConfig.collect { savedConfig ->
                _config.value = savedConfig
            }
        }
    }

    fun toggleString(stringNumber: Int) {
        // Validate string number is in valid range (1-6)
        if (stringNumber !in 1..6) return

        _config.update { currentConfig ->
            val currentStrings = currentConfig.selectedStrings
            val newStrings =
                if (currentStrings.contains(stringNumber)) {
                    if (currentStrings.size > 1) { // Ensure at least one string remains selected
                        currentStrings - stringNumber
                    } else {
                        currentStrings // Don't remove if it's the last one
                    }
                } else {
                    currentStrings + stringNumber
                }
            currentConfig.copy(selectedStrings = newStrings)
        }
        saveConfig(_config.value)
    }

    fun setFretRange(
        from: Int,
        to: Int,
    ) {
        _config.update { it.copy(fretFrom = from, fretTo = to) }
        saveConfig(_config.value)
    }

    fun setNoteMode(mode: NoteMode) {
        _config.update { it.copy(noteMode = mode) }
        saveConfig(_config.value)
    }

    fun setSelectedScale(scale: Scale) {
        _config.update { it.copy(selectedScale = scale) }
        saveConfig(_config.value)
    }

    fun setDurationType(type: DurationType) {
        _config.update { it.copy(durationType = type) }
        saveConfig(_config.value)
    }

    fun setDurationMinutes(minutes: Int) {
        _config.update { it.copy(durationMinutes = minutes) }
        saveConfig(_config.value)
    }

    fun setNoteCount(count: Int) {
        _config.update { it.copy(noteCount = count) }
        saveConfig(_config.value)
    }

    fun setProgressionMode(mode: ProgressionMode) {
        _config.update { it.copy(progressionMode = mode) }
        saveConfig(_config.value)
    }

    fun setAutoIntervalSeconds(seconds: Float) {
        _config.update { it.copy(autoIntervalSeconds = seconds) }
        saveConfig(_config.value)
    }

    private fun saveConfig(config: PracticeConfig) {
        viewModelScope.launch {
            repository.savePracticeConfig(config)
        }
    }

    fun isConfigValid(): Boolean {
        val config = _config.value
        return config.selectedStrings.isNotEmpty() &&
            config.fretFrom >= 0 &&
            config.fretTo >= config.fretFrom &&
            config.fretTo <= 24 &&
            (
                (config.durationType == DurationType.TIME && config.durationMinutes > 0) ||
                    (config.durationType == DurationType.COUNT && config.noteCount > 0)
            ) &&
            (
                config.progressionMode != ProgressionMode.AUTO_INTERVAL ||
                    (config.autoIntervalSeconds >= 0.5f && config.autoIntervalSeconds <= 10.0f)
            )
    }
}
