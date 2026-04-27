package com.example.vokabeltrainer.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Einfache App-Einstellungen, persistiert in SharedPreferences.
 * Bewusst klein gehalten — keine zusätzliche DataStore-Abhängigkeit.
 */
class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Tageslimit für fällige Karten. Default 100. */
    var dailyLimit: Int
        get() = prefs.getInt(KEY_DAILY_LIMIT, DEFAULT_DAILY_LIMIT)
        set(value) {
            prefs.edit().putInt(KEY_DAILY_LIMIT, value.coerceIn(MIN_LIMIT, MAX_LIMIT)).apply()
        }

    companion object {
        private const val PREFS_NAME = "vokabeltrainer_prefs"
        private const val KEY_DAILY_LIMIT = "daily_limit"

        const val DEFAULT_DAILY_LIMIT = 100
        const val MIN_LIMIT = 1
        const val MAX_LIMIT = 1000

        /** Auswahlmöglichkeiten im Settings-Screen. */
        val LIMIT_OPTIONS = listOf(25, 50, 75, 100, 150)
    }
}
