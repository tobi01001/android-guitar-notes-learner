package com.androidguitarnotes.app.update

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.updateDataStore: DataStore<Preferences> by preferencesDataStore(name = "update_settings")

/** Persists update-check settings (enabled flag, interval in days, last check time). */
class UpdatePreferences(
    private val context: Context,
) {
    private object Keys {
        val AUTO_CHECK = booleanPreferencesKey("auto_check_enabled")
        val INTERVAL_DAYS = intPreferencesKey("check_interval_days")
        val LAST_CHECK = longPreferencesKey("last_check_millis")
    }

    val autoCheckEnabled: Flow<Boolean> = context.updateDataStore.data.map { it[Keys.AUTO_CHECK] ?: true }
    val intervalDays: Flow<Int> = context.updateDataStore.data.map { it[Keys.INTERVAL_DAYS] ?: DEFAULT_INTERVAL_DAYS }

    suspend fun setAutoCheckEnabled(enabled: Boolean) {
        context.updateDataStore.edit { it[Keys.AUTO_CHECK] = enabled }
    }

    suspend fun setIntervalDays(days: Int) {
        context.updateDataStore.edit { it[Keys.INTERVAL_DAYS] = days }
    }

    suspend fun markChecked(now: Long = System.currentTimeMillis()) {
        context.updateDataStore.edit { it[Keys.LAST_CHECK] = now }
    }

    /** True if auto-check is enabled and the last check is older than the configured interval. */
    suspend fun isCheckDue(now: Long = System.currentTimeMillis()): Boolean {
        val prefs = context.updateDataStore.data.first()
        if (!(prefs[Keys.AUTO_CHECK] ?: true)) return false
        val days = prefs[Keys.INTERVAL_DAYS] ?: DEFAULT_INTERVAL_DAYS
        val last = prefs[Keys.LAST_CHECK] ?: 0L
        return now - last >= days * MILLIS_PER_DAY
    }

    companion object {
        const val DEFAULT_INTERVAL_DAYS = 7
        private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
    }
}
