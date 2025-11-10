package com.androidguitarnotes.app.notesplayed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androidguitarnotes.app.audio.AudioManager

/**
 * Factory for creating NotesPlayedViewModel instances.
 * Creates and manages the AudioManager dependency internally.
 */
class NotesPlayedViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesPlayedViewModel::class.java)) {
            return NotesPlayedViewModel(AudioManager()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
