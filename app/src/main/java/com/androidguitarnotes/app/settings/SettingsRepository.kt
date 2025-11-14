package com.androidguitarnotes.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val AUDIO_FEEDBACK_ENABLED = booleanPreferencesKey("audio_feedback_enabled")
        val DEFAULT_TUNING = stringPreferencesKey("default_tuning")
        val MICROPHONE_SENSITIVITY = floatPreferencesKey("microphone_sensitivity")
        val AUTO_ADJUST_SENSITIVITY = booleanPreferencesKey("auto_adjust_sensitivity")
        val PITCH_DETECTION_ALGORITHM = stringPreferencesKey("pitch_detection_algorithm")
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

    /**
     * Saves the audio feedback enabled setting.
     */
    suspend fun saveAudioFeedbackEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_FEEDBACK_ENABLED] = enabled
        }
    }

    /**
     * Loads the audio feedback enabled setting (default true).
     */
    val audioFeedbackEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUDIO_FEEDBACK_ENABLED] ?: true
        }

    /**
     * Saves the default tuning setting.
     */
    suspend fun saveDefaultTuning(tuning: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_TUNING] = tuning
        }
    }

    /**
     * Loads the default tuning setting (default "Standard").
     */
    val defaultTuning: Flow<String> =
        context.settingsDataStore.data.map { preferences ->
            preferences[PreferencesKeys.DEFAULT_TUNING] ?: "Standard"
        }

    /**
     * Saves the microphone sensitivity setting.
     */
    suspend fun saveMicrophoneSensitivity(sensitivity: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.MICROPHONE_SENSITIVITY] = sensitivity
        }
    }

    /**
     * Loads the microphone sensitivity setting (default 1.0f).
     */
    val microphoneSensitivity: Flow<Float> =
        context.settingsDataStore.data.map { preferences ->
            preferences[PreferencesKeys.MICROPHONE_SENSITIVITY] ?: 1.0f
        }

    /**
     * Saves the auto-adjust sensitivity setting.
     */
    suspend fun saveAutoAdjustSensitivity(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_ADJUST_SENSITIVITY] = enabled
        }
    }

    /**
     * Loads the auto-adjust sensitivity setting (default false).
     */
    val autoAdjustSensitivity: Flow<Boolean> =
        context.settingsDataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUTO_ADJUST_SENSITIVITY] ?: false
        }

    /**
     * Saves the pitch detection algorithm setting.
     */
    suspend fun savePitchDetectionAlgorithm(algorithm: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.PITCH_DETECTION_ALGORITHM] = algorithm
        }
    }

    /**
     * Loads the pitch detection algorithm setting (default "YIN").
     */
    val pitchDetectionAlgorithm: Flow<String> =
        context.settingsDataStore.data.map { preferences ->
            preferences[PreferencesKeys.PITCH_DETECTION_ALGORITHM] ?: "YIN"
        }
}
