package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class PreferencesDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "settings")
        private val SELECTED_LANGUAGE_KEY = stringPreferencesKey("selected_language")
        
        @Volatile
        private var INSTANCE: PreferencesDataStore? = null
        
        fun getInstance(context: Context): PreferencesDataStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesDataStore(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val selectedLanguage = context.dataStore.data.map { preferences ->
        preferences[SELECTED_LANGUAGE_KEY] ?: ""
    }

    suspend fun setSelectedLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE_KEY] = languageCode
        }
    }
} 