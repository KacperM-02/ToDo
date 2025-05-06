package com.example.todo.data

import android.provider.BaseColumns

object TaskContract {
    object TaskEntry : BaseColumns {
        const val DATABASE_NAME = "todo.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
    }
}