package com.androidguitarnotes.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

/**
 * Repository for persisting app settings.
 */
class SettingsRepository(
    private val context: Context,
) {
    private object PreferencesKeys {
        val AUDIO_SOURCE = intPreferencesKey("audio_source")
        val NOISE_GATE_THRESHOLD = floatPreferencesKey("noise_gate_threshold")
    }

    /**
     * Saves the audio source setting.
     */
    suspend fun saveAudioSource(audioSource: AudioSource) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_SOURCE] = audioSource.value
        }
    }

    /**
     * Loads the audio source setting.
     */
    val audioSource: Flow<AudioSource> =
        context.settingsDataStore.data.map { preferences ->
            val sourceValue = preferences[PreferencesKeys.AUDIO_SOURCE] ?: -1
            AudioSource.fromValue(sourceValue)
        }

    /**
     * Saves the noise gate threshold setting.
     */
    suspend fun saveNoiseGateThreshold(threshold: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.NOISE_GATE_THRESHOLD] = threshold
        }
    }

    /**
     * Loads the noise gate threshold setting (default 0.01f).
     */
    val noiseGateThreshold: Flow<Float> =
        context.settingsDataStore.data.map { preferences ->
            preferences[PreferencesKeys.NOISE_GATE_THRESHOLD] ?: 0.01f
        }
}
