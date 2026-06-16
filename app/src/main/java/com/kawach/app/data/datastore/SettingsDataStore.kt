package com.kawach.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property — one DataStore instance per Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kawach_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val KEY_SHAKE_TO_SOS = booleanPreferencesKey("shake_to_sos")
        private val KEY_DISCREET_MODE = booleanPreferencesKey("discreet_mode")
        private val KEY_FAKE_CALLER_NAME = stringPreferencesKey("fake_caller_name")
        private val KEY_LOCAL_POLICE = stringPreferencesKey("local_police_number")
        private val KEY_LOCAL_COUNCILLOR = stringPreferencesKey("local_councillor_number")
    }

    val shakeToSosEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_SHAKE_TO_SOS] ?: true }

    val discreetModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_DISCREET_MODE] ?: false }

    val fakeCallerName: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_FAKE_CALLER_NAME] ?: "Papa" }

    val localPoliceNumber: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_LOCAL_POLICE] ?: "" }

    val localCouncillorNumber: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_LOCAL_COUNCILLOR] ?: "" }

    suspend fun setShakeToSos(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SHAKE_TO_SOS] = enabled }
    }

    suspend fun setDiscreetMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DISCREET_MODE] = enabled }
    }

    suspend fun setFakeCallerName(name: String) {
        context.dataStore.edit { it[KEY_FAKE_CALLER_NAME] = name }
    }

    suspend fun setLocalPoliceNumber(number: String) {
        context.dataStore.edit { it[KEY_LOCAL_POLICE] = number }
    }

    suspend fun setLocalCouncillorNumber(number: String) {
        context.dataStore.edit { it[KEY_LOCAL_COUNCILLOR] = number }
    }
}
