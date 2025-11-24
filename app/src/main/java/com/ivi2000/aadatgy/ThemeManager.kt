package com.ivi2000.aadatgy

import android.content.Context
import androidx.annotation.StyleRes
import androidx.core.content.edit

object ThemeManager {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_INDEX = "theme_index"
    private const val THEME_COUNT = 3

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @StyleRes
    private fun indexToStyle(index: Int): Int {
        return when (index) {
            1 -> R.style.Theme_App_DarkIndustrial
            2 -> R.style.Theme_App_Warehouse
            else -> R.style.Theme_App_ModernLight
        }
    }

    fun applyTheme(context: Context) {
        val index = prefs(context).getInt(KEY_THEME_INDEX, 0)
        context.setTheme(indexToStyle(index))
    }

    fun nextTheme(context: Context) {
        val p = prefs(context)
        val next = (p.getInt(KEY_THEME_INDEX, 0) + 1) % THEME_COUNT
        p.edit {
            putInt(KEY_THEME_INDEX, next)
        }
    }
}
