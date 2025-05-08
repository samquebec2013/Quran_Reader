package com.education.quran8

import android.content.Context
import android.content.SharedPreferences
import java.util.*

object LanguageManager {

    fun setLanguage(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        prefs.edit().putString("Language", language).apply()
    }

    fun loadLanguage(context: Context, prefs: SharedPreferences) {
        val language = prefs.getString("Language", "en") ?: "en"
        setLanguage(context, language)
    }
}