package com.androidguitarnotes.app.practice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating PracticeSessionViewModel with config and context parameters.
 */
class PracticeSessionViewModelFactory(
    private val config: PracticeConfig,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PracticeSessionViewModel::class.java)) {
            return PracticeSessionViewModel(config, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
