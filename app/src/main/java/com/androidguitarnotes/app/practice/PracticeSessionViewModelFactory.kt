package com.androidguitarnotes.app.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating PracticeSessionViewModel with config parameter.
 */
class PracticeSessionViewModelFactory(
    private val config: PracticeConfig,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PracticeSessionViewModel::class.java)) {
            return PracticeSessionViewModel(config) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
