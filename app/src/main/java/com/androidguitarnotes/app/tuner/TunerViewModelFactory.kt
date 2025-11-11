package com.androidguitarnotes.app.tuner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androidguitarnotes.app.audio.AudioManager
import com.androidguitarnotes.app.settings.SettingsViewModel

/**
 * Factory for creating TunerViewModel instances.
 */
class TunerViewModelFactory(
    private val audioManager: AudioManager,
    private val settingsViewModel: SettingsViewModel,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TunerViewModel::class.java)) {
            return TunerViewModel(audioManager, settingsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
