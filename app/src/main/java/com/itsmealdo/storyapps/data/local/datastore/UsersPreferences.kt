package com.itsmealdo.storyapps.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

    class UsersPreferences (private val dataStore: DataStore<Preferences>) {

    private val firstTimeKey = booleanPreferencesKey("first_time")
    private val tokenKey = stringPreferencesKey("token")

    fun getUserToken(): Flow<String> = dataStore.data.map {
        it[tokenKey] ?: "null"
    }

    suspend fun saveUserToken(token: String) = dataStore.edit { preferences ->
        preferences[tokenKey] = token
    }


    suspend fun setFirstTime(firstTime: Boolean) = dataStore.edit { preferences ->
        preferences[firstTimeKey] = firstTime
    }

    suspend fun deleteUserToken() = dataStore.edit { preferences ->
        preferences[tokenKey] = "null"
    }

    fun isFirstTime(): Flow<Boolean> = dataStore.data.map {
        it[firstTimeKey] ?: true
    }

    companion object {
        @Volatile
        private var instance: UsersPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): UsersPreferences =
            instance ?: synchronized(this) {
                instance ?: UsersPreferences(dataStore)
            }.also { instance = it }
    }
}