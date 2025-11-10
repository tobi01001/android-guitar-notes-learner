package com.androidguitarnotes.app.practice

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for managing practice session configuration state.
 */
class PracticeConfigViewModel : ViewModel() {
    
    private val _config = MutableStateFlow(PracticeConfig())
    val config: StateFlow<PracticeConfig> = _config.asStateFlow()
    
    fun toggleString(stringNumber: Int) {
        // Validate string number is in valid range (1-6)
        if (stringNumber !in 1..6) return
        
        _config.update { currentConfig ->
            val currentStrings = currentConfig.selectedStrings
            val newStrings = if (currentStrings.contains(stringNumber)) {
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
    }
    
    fun setFretRange(from: Int, to: Int) {
        _config.update { it.copy(fretFrom = from, fretTo = to) }
    }
    
    fun setNoteMode(mode: NoteMode) {
        _config.update { it.copy(noteMode = mode) }
    }
    
    fun setDurationType(type: DurationType) {
        _config.update { it.copy(durationType = type) }
    }
    
    fun setDurationMinutes(minutes: Int) {
        _config.update { it.copy(durationMinutes = minutes) }
    }
    
    fun setNoteCount(count: Int) {
        _config.update { it.copy(noteCount = count) }
    }
    
    fun isConfigValid(): Boolean {
        val config = _config.value
        return config.selectedStrings.isNotEmpty() &&
                config.fretFrom >= 0 &&
                config.fretTo >= config.fretFrom &&
                config.fretTo <= 24 &&
                ((config.durationType == DurationType.TIME && config.durationMinutes > 0) ||
                 (config.durationType == DurationType.COUNT && config.noteCount > 0))
    }
}
