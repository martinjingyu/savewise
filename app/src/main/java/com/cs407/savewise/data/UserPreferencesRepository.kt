package com.cs407.savewise.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cs407.savewise.viewModel.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val region: String = "United States",
    val autoRecording: Boolean = false,
    val language: String = "English",
    val recordingStorageDays: Int = 7,
    val themeMode: AppThemeMode = AppThemeMode.System,
    val autoBackupEnabled: Boolean = false,
    val wifiOnlyBackup: Boolean = true,
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val REGION: Preferences.Key<String> = stringPreferencesKey("region")
        val AUTO_RECORDING: Preferences.Key<Boolean> = booleanPreferencesKey("auto_recording")
        val LANGUAGE: Preferences.Key<String> = stringPreferencesKey("language")
        val RECORDING_STORAGE_DAYS: Preferences.Key<Int> = intPreferencesKey("recording_storage_days")
        val THEME_MODE: Preferences.Key<String> = stringPreferencesKey("theme_mode")
        val AUTO_BACKUP: Preferences.Key<Boolean> = booleanPreferencesKey("auto_backup_enabled")
        val WIFI_ONLY: Preferences.Key<Boolean> = booleanPreferencesKey("wifi_only_backup")
    }

    val preferencesFlow: Flow<UserPreferences> = context.userPrefsDataStore.data.map { prefs ->
        UserPreferences(
            region = prefs[Keys.REGION] ?: "United States",
            autoRecording = prefs[Keys.AUTO_RECORDING] ?: false,
            language = prefs[Keys.LANGUAGE] ?: "English",
            recordingStorageDays = prefs[Keys.RECORDING_STORAGE_DAYS] ?: 7,
            themeMode = prefs[Keys.THEME_MODE]?.let { stringToTheme(it) } ?: AppThemeMode.System,
            autoBackupEnabled = prefs[Keys.AUTO_BACKUP] ?: false,
            wifiOnlyBackup = prefs[Keys.WIFI_ONLY] ?: true
        )
    }

    suspend fun setRegion(region: String) {
        context.userPrefsDataStore.edit { it[Keys.REGION] = region }
    }

    suspend fun setAutoRecording(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.AUTO_RECORDING] = enabled }
    }

    suspend fun setLanguage(language: String) {
        context.userPrefsDataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setRecordingStorageDays(days: Int) {
        context.userPrefsDataStore.edit { it[Keys.RECORDING_STORAGE_DAYS] = days }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.userPrefsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.AUTO_BACKUP] = enabled }
    }

    suspend fun setWifiOnlyBackup(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.WIFI_ONLY] = enabled }
    }

    private fun stringToTheme(value: String): AppThemeMode =
        runCatching { AppThemeMode.valueOf(value) }.getOrDefault(AppThemeMode.System)
}
