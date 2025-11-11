package com.androidguitarnotes.app.practice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androidguitarnotes.app.audio.AudioManager
import com.androidguitarnotes.app.permissions.PermissionManager

/**
 * Factory for creating PracticeSessionViewModel with config and context parameters.
 */
class PracticeSessionViewModelFactory(
    private val config: PracticeConfig,
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PracticeSessionViewModel::class.java)) {
            return PracticeSessionViewModel(
                config = config,
                audioManager = AudioManager(),
                permissionManager = PermissionManager(context),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
