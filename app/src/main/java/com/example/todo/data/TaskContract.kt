package com.example.todo.data

import android.provider.BaseColumns

object TaskContract {
    object TaskEntry : BaseColumns {
        // Database
        const val DATABASE_NAME = "todo.db"
        const val DATABASE_VERSION = 1

        // Tasks table
        const val TABLE_TASKS = "tasks_table"
        const val COLUMN_TASK_ID = "task_id"
        const val COLUMN_TASK_TITLE = "task_title"
        const val COLUMN_TASK_STATUS = "task_status"
        const val COLUMN_TASK_DESCRIPTION = "task_description"
        const val COLUMN_TASK_CREATION_TIME = "task_creation_time"
        const val COLUMN_TASK_EXECUTION_DATE = "task_execution_date"
        const val COLUMN_TASK_NOTIFICATION = "task_notification"
        const val COLUMN_TASK_CATEGORY = "task_category"


        // Attachments table
        const val TABLE_ATTACHMENTS = "attachments_table"
        const val COLUMN_ATTACHMENT_ID = "attachment_id"
        const val COLUMN_ATTACHMENT_TASK_ID = "attachment_task_id"
        const val COLUMN_ATTACHMENT_PATH = "attachment_path"
    }
}