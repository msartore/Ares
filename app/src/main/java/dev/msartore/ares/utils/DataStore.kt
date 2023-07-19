package dev.msartore.ares.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

suspend fun <T> DataStore<Preferences>.write(key: String, value: T) {
    edit { settings ->
        when (value) {
            is Boolean -> settings[booleanPreferencesKey(key)] = value
            is String -> settings[stringPreferencesKey(key)] = value
            is Int -> settings[intPreferencesKey(key)] = value
        }
    }
}

suspend fun DataStore<Preferences>.readBool(key: String) =
    data.map { it.toPreferences() }.first()[booleanPreferencesKey(key)]

suspend fun DataStore<Preferences>.readInt(key: String) =
    data.map { it.toPreferences() }.first()[intPreferencesKey(key)]

suspend fun DataStore<Preferences>.readString(key: String) =
    data.map { it.toPreferences() }.first()[stringPreferencesKey(key)]