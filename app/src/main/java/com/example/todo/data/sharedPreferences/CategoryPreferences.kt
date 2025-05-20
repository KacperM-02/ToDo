package com.example.todo.data.sharedPreferences

import android.content.Context
import androidx.core.content.edit

object CategoryPreferences {
    private const val PREFS_NAME = "ToDoPreferences"
    private const val SELECTED_CATEGORIES_KEY = "selectedCategory"

    fun loadSelectedCategory(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_CATEGORIES_KEY, null)
    }

    fun setSelectedCategory(context: Context, category: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(SELECTED_CATEGORIES_KEY, category) }
    }
}