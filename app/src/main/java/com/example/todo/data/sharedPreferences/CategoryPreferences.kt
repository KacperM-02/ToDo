package com.example.todo.data.sharedPreferences

import android.content.Context
import androidx.core.content.edit

object CategoryPreferences {
    private const val PREFS_NAME = "ToDoPreferences"
    private const val CATEGORIES_KEY = "categories"

    fun loadCategories(context: Context): MutableList<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCategories = prefs.getStringSet(CATEGORIES_KEY, null)
        return savedCategories?.sorted()?.toMutableList() ?: mutableListOf("Education", "Home", "Hobby", "Shopping", "Work")
    }

    fun saveCategories(context: Context, categories: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putStringSet(CATEGORIES_KEY, categories.toSortedSet()) }
    }

    fun addCategory(context: Context, category: String) {
        val categories = loadCategories(context)
        if (!categories.contains(category)) {
            categories.add(category)
            saveCategories(context, categories)
        }
    }
}