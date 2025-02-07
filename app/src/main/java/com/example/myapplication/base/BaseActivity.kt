package com.example.myapplication.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.PreferencesDataStore
import com.example.myapplication.util.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var preferencesDataStore: PreferencesDataStore
    private var currentLanguage: String? = null

    override fun attachBaseContext(newBase: Context) {
        preferencesDataStore = PreferencesDataStore(newBase)
        val languageCode = runBlocking { 
            preferencesDataStore.selectedLanguage.first() 
        }
        currentLanguage = languageCode
        super.attachBaseContext(LocaleHelper.updateLocale(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesDataStore = PreferencesDataStore(this)
        observeLanguageChanges()
    }

    private fun observeLanguageChanges() {
        lifecycleScope.launch {
            preferencesDataStore.selectedLanguage.collect { languageCode ->
                // Only recreate if language actually changed
                if (currentLanguage != languageCode) {
                    currentLanguage = languageCode
                    updateLocale(languageCode)
                }
            }
        }
    }

    private fun updateLocale(languageCode: String) {
        LocaleHelper.updateLocale(this, languageCode)
        recreate() // Recreate the activity to apply the new locale
    }
} 