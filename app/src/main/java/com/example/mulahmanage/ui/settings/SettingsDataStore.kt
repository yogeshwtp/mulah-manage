package com.example.mulahmanage.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates a single instance of DataStore for the entire app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {
    private val dataStore = context.dataStore

    // Keys and constant values for our theme options
    companion object {
        val THEME_OPTION_KEY = stringPreferencesKey("theme_option")
        const val THEME_SYSTEM = "System"
        const val THEME_LIGHT = "Light"
        const val THEME_DARK = "Dark"
    }

    // A flow that emits the user's chosen theme, defaulting to "System"
    val themeOption: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_OPTION_KEY] ?: THEME_SYSTEM
    }

    // A function to save the new theme choice
    suspend fun setThemeOption(option: String) {
        dataStore.edit { preferences ->
            preferences[THEME_OPTION_KEY] = option
        }
    }
}
