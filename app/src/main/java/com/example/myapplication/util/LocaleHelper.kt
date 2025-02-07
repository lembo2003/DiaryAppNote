package com.example.myapplication.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    fun updateLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "gb" -> Locale("en")
            "in" -> Locale("hi")
            "es" -> Locale("es")
            "fr" -> Locale("fr")
            "sa" -> Locale("ar")
            "bd" -> Locale("bn")
            else -> Locale("en")
        }
        Locale.setDefault(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, locale)
        } else {
            updateResourcesLegacy(context, locale)
        }
    }

    private fun updateResources(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
} 