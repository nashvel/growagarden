package com.glowagarden.stocknotifier

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance, tied to the application context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    // Define a key for storing the set of selected item names
    private object PreferencesKeys {
        val SELECTED_ITEMS = stringSetPreferencesKey("selected_stock_items")
        val INITIAL_SETUP_COMPLETE = booleanPreferencesKey("initial_setup_complete")
        val SELECTED_NOTIFICATION_SOUND = stringPreferencesKey("selected_notification_sound")
    }

    // Function to save the set of selected item names
    suspend fun saveSelectedItems(selectedItems: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_ITEMS] = selectedItems
        }
    }

    // Function to load the set of selected item names as a Flow
    val loadSelectedItems: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_ITEMS] ?: emptySet() // Return empty set if no preferences found
        }

    // Flow to observe if initial setup has been completed
    val initialSetupComplete: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.INITIAL_SETUP_COMPLETE] ?: false
        }

    // Function to mark initial setup as complete
    suspend fun markInitialSetupComplete() {
        context.dataStore.edit {
            it[PreferencesKeys.INITIAL_SETUP_COMPLETE] = true
        }
    }

    // Flow to observe the selected notification sound
    val selectedNotificationSound: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_NOTIFICATION_SOUND] ?: "default.mp3" // Default sound
        }

    // Function to save the selected notification sound
    suspend fun saveSelectedNotificationSound(soundFileName: String) {
        context.dataStore.edit {
            it[PreferencesKeys.SELECTED_NOTIFICATION_SOUND] = soundFileName
        }
    }
}
