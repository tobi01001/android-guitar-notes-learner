package com.androidguitarnotes.app.tuner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androidguitarnotes.app.audio.AudioManager

/**
 * Factory for creating TunerViewModel instances.
 */
class TunerViewModelFactory(
    private val audioManager: AudioManager
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TunerViewModel::class.java)) {
            return TunerViewModel(audioManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
