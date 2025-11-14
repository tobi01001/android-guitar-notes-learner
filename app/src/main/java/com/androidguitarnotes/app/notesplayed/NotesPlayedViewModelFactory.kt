package com.androidguitarnotes.app.notesplayed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androidguitarnotes.app.audio.AudioManager
import com.androidguitarnotes.app.permissions.PermissionManager
import com.androidguitarnotes.app.settings.SettingsViewModel

/**
 * Factory for creating NotesPlayedViewModel instances.
 * Creates and manages the AudioManager dependency internally.
 */
class NotesPlayedViewModelFactory(
    private val settingsViewModel: SettingsViewModel,
    private val permissionManager: PermissionManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesPlayedViewModel::class.java)) {
            return NotesPlayedViewModel(AudioManager(), settingsViewModel, permissionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
