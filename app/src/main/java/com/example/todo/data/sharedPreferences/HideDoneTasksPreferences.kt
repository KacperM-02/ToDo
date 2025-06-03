package com.example.todo.data.sharedPreferences

import android.content.Context
import androidx.core.content.edit

object HideDoneTasksPreferences {
    private const val PREFS_NAME = "ToDoPreferences"
    private const val HIDE_DONE_TASKS_KEY = "hideDoneTasks"

    fun loadHideDoneTasks(context: Context) : Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(HIDE_DONE_TASKS_KEY, false)
    }

    fun setHideDoneTasks(context: Context, hideDoneTasks: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {putBoolean(HIDE_DONE_TASKS_KEY, hideDoneTasks)}
    }
}