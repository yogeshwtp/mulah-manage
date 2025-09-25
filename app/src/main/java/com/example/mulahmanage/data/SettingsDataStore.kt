package com.example.mulahmanage.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        // Theme Keys
        val THEME_OPTION_KEY = stringPreferencesKey("theme_option")
        const val THEME_SYSTEM = "System"
        const val THEME_LIGHT = "Light"
        const val THEME_DARK = "Dark"

        // NEW: Onboarding Key
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }

    val themeOption: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_OPTION_KEY] ?: THEME_SYSTEM
    }

    suspend fun setThemeOption(option: String) {
        dataStore.edit { preferences ->
            preferences[THEME_OPTION_KEY] = option
        }
    }

    // NEW: Flow to check if onboarding is complete
    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    // NEW: Function to mark onboarding as complete
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }
}