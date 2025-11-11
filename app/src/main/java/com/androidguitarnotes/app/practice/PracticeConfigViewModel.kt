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
            val newConfig = currentConfig.copy(selectedStrings = newStrings)
            saveConfig(newConfig)
            newConfig
        }
    }

    fun setFretRange(
        from: Int,
        to: Int,
    ) {
        _config.update {
            val newConfig = it.copy(fretFrom = from, fretTo = to)
            saveConfig(newConfig)
            newConfig
        }
    }

    fun setNoteMode(mode: NoteMode) {
        _config.update {
            val newConfig = it.copy(noteMode = mode)
            saveConfig(newConfig)
            newConfig
        }
    }

    fun setSelectedScale(scale: Scale) {
        _config.update {
            val newConfig = it.copy(selectedScale = scale)
            saveConfig(newConfig)
            newConfig
        }
    }

    fun setDurationType(type: DurationType) {
        _config.update {
            val newConfig = it.copy(durationType = type)
            saveConfig(newConfig)
            newConfig
        }
    }

    fun setDurationMinutes(minutes: Int) {
        _config.update {
            val newConfig = it.copy(durationMinutes = minutes)
            saveConfig(newConfig)
            newConfig
        }
    }

    fun setNoteCount(count: Int) {
        _config.update {
            val newConfig = it.copy(noteCount = count)
            saveConfig(newConfig)
            newConfig
        }
    }

    fun setProgressionMode(mode: ProgressionMode) {
        _config.update {
            val newConfig = it.copy(progressionMode = mode)
            saveConfig(newConfig)
            newConfig
        }
    }

    fun setAutoIntervalSeconds(seconds: Float) {
        _config.update {
            val newConfig = it.copy(autoIntervalSeconds = seconds)
            saveConfig(newConfig)
            newConfig
        }
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
