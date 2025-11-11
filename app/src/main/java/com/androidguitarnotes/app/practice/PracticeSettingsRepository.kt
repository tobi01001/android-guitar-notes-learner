package com.androidguitarnotes.app.practice

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.practiceDataStore: DataStore<Preferences> by preferencesDataStore(name = "practice_settings")

/**
 * Repository for persisting practice configuration settings.
 */
class PracticeSettingsRepository(
    private val context: Context,
) {
    private object PreferencesKeys {
        val SELECTED_STRINGS = stringSetPreferencesKey("selected_strings")
        val FRET_FROM = intPreferencesKey("fret_from")
        val FRET_TO = intPreferencesKey("fret_to")
        val NOTE_MODE = stringPreferencesKey("note_mode")
        val SELECTED_SCALE = stringPreferencesKey("selected_scale")
        val DURATION_TYPE = stringPreferencesKey("duration_type")
        val DURATION_MINUTES = intPreferencesKey("duration_minutes")
        val NOTE_COUNT = intPreferencesKey("note_count")
        val PROGRESSION_MODE = stringPreferencesKey("progression_mode")
        val AUTO_INTERVAL_SECONDS = floatPreferencesKey("auto_interval_seconds")
    }

    /**
     * Saves the practice configuration.
     */
    suspend fun savePracticeConfig(config: PracticeConfig) {
        context.practiceDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_STRINGS] = config.selectedStrings.map { it.toString() }.toSet()
            preferences[PreferencesKeys.FRET_FROM] = config.fretFrom
            preferences[PreferencesKeys.FRET_TO] = config.fretTo
            preferences[PreferencesKeys.NOTE_MODE] = config.noteMode.name
            preferences[PreferencesKeys.SELECTED_SCALE] = config.selectedScale.name
            preferences[PreferencesKeys.DURATION_TYPE] = config.durationType.name
            preferences[PreferencesKeys.DURATION_MINUTES] = config.durationMinutes
            preferences[PreferencesKeys.NOTE_COUNT] = config.noteCount
            preferences[PreferencesKeys.PROGRESSION_MODE] = config.progressionMode.name
            preferences[PreferencesKeys.AUTO_INTERVAL_SECONDS] = config.autoIntervalSeconds
        }
    }

    /**
     * Loads the practice configuration.
     */
    val practiceConfig: Flow<PracticeConfig> =
        context.practiceDataStore.data.map { preferences ->
            val selectedStrings =
                preferences[PreferencesKeys.SELECTED_STRINGS]?.mapNotNull { it.toIntOrNull() }?.toSet()
                    ?: setOf(1, 2, 3, 4, 5, 6)
            val fretFrom = preferences[PreferencesKeys.FRET_FROM] ?: 0
            val fretTo = preferences[PreferencesKeys.FRET_TO] ?: 12
            val noteMode =
                preferences[PreferencesKeys.NOTE_MODE]?.let {
                    try {
                        NoteMode.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        NoteMode.WHOLE_NOTES
                    }
                } ?: NoteMode.WHOLE_NOTES
            val selectedScale =
                preferences[PreferencesKeys.SELECTED_SCALE]?.let {
                    try {
                        Scale.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        Scale.C_MAJOR
                    }
                } ?: Scale.C_MAJOR
            val durationType =
                preferences[PreferencesKeys.DURATION_TYPE]?.let {
                    try {
                        DurationType.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        DurationType.TIME
                    }
                } ?: DurationType.TIME
            val durationMinutes = preferences[PreferencesKeys.DURATION_MINUTES] ?: 5
            val noteCount = preferences[PreferencesKeys.NOTE_COUNT] ?: 20
            val progressionMode =
                preferences[PreferencesKeys.PROGRESSION_MODE]?.let {
                    try {
                        ProgressionMode.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        ProgressionMode.MANUAL
                    }
                } ?: ProgressionMode.MANUAL
            val autoIntervalSeconds = preferences[PreferencesKeys.AUTO_INTERVAL_SECONDS] ?: 3.0f

            PracticeConfig(
                selectedStrings = selectedStrings,
                fretFrom = fretFrom,
                fretTo = fretTo,
                noteMode = noteMode,
                selectedScale = selectedScale,
                durationType = durationType,
                durationMinutes = durationMinutes,
                noteCount = noteCount,
                progressionMode = progressionMode,
                autoIntervalSeconds = autoIntervalSeconds,
            )
        }
}
