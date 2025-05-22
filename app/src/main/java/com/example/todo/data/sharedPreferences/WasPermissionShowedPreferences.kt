package com.example.todo.data.sharedPreferences

import android.content.Context
import androidx.core.content.edit

object WasPermissionShowedPreferences {
    private const val PREFS_NAME = "ToDoPreferences"
    private const val WAS_PERMISSION_SHOWED_KEY = "wasPermissionShowed"

    fun loadWasPermissionShowedGranted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(WAS_PERMISSION_SHOWED_KEY, true)
    }

    fun setWasPermissionShowedGranted(context: Context, permissionGranted: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(WAS_PERMISSION_SHOWED_KEY, permissionGranted) }
    }
}