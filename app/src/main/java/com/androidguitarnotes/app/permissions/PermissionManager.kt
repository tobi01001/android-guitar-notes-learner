package com.androidguitarnotes.app.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Centralized manager for handling app permissions.
 */
class PermissionManager(
    private val context: Context,
) {
    /**
     * Checks if the RECORD_AUDIO permission is granted.
     */
    fun isRecordAudioPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Gets the RECORD_AUDIO permission string for requesting.
     */
    fun getRecordAudioPermission(): String = Manifest.permission.RECORD_AUDIO
}
