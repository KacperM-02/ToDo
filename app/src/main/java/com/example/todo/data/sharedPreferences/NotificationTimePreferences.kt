package com.example.todo.data.sharedPreferences

import android.content.Context
import androidx.core.content.edit

object NotificationTimePreferences {
    private const val PREFS_NAME = "ToDoPreferences"
    private const val NOTIFICATION_TIME_KEY = "notificationTime"

    fun loadNotificationTime(context: Context) : Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(NOTIFICATION_TIME_KEY, 5)
    }

    fun setNotificationTime(context: Context, notificationTime: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {putLong(NOTIFICATION_TIME_KEY, notificationTime)}
    }
}