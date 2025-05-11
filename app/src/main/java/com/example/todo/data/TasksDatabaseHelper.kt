package com.example.todo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_ATTACHMENT_ID
import com.example.todo.data.TaskContract.TaskEntry.DATABASE_NAME
import com.example.todo.data.TaskContract.TaskEntry.DATABASE_VERSION
import com.example.todo.data.TaskContract.TaskEntry.TABLE_TASKS
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_ATTACHMENT_PATH
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_TASK_CATEGORY
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_TASK_CREATION_TIME
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_TASK_DESCRIPTION
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_TASK_EXECUTION_DATE
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_TASK_ID
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_TASK_NOTIFICATION
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_TASK_STATUS
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_TASK_TITLE
import com.example.todo.data.TaskContract.TaskEntry.TABLE_ATTACHMENTS

class TasksDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        // Tasks table
        val createTasksTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_TASKS (
                $COLUMN_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TASK_TITLE TEXT NOT NULL,
                $COLUMN_TASK_STATUS INTEGER NOT NULL DEFAULT 0,
                $COLUMN_TASK_DESCRIPTION TEXT,
                $COLUMN_TASK_CREATION_TIME TEXT NOT NULL,
                $COLUMN_TASK_EXECUTION_DATE TEXT,
                $COLUMN_TASK_NOTIFICATION INTEGER NOT NULL DEFAULT 0,
                $COLUMN_TASK_CATEGORY TEXT NOT NULL
            )
        """.trimIndent()

        // Attachments table
        val createAttachmentsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_ATTACHMENTS (
                $COLUMN_ATTACHMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TaskContract.TaskEntry.COLUMN_ATTACHMENT_TASK_ID} INTEGER,
                $COLUMN_ATTACHMENT_PATH TEXT NOT NULL,
                FOREIGN KEY (${TaskContract.TaskEntry.COLUMN_ATTACHMENT_TASK_ID}) REFERENCES $TABLE_TASKS(${TaskContract.TaskEntry.COLUMN_ATTACHMENT_TASK_ID}) ON DELETE CASCADE
            )
        """.trimIndent()

        db?.execSQL(createTasksTable)
        db?.execSQL(createAttachmentsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ATTACHMENTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    // Dodawanie zadania
    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK_TITLE, task.taskTitle)
            put(COLUMN_TASK_STATUS, task.taskStatus)
            put(COLUMN_TASK_DESCRIPTION, task.taskDescription)
            put(COLUMN_TASK_CREATION_TIME, task.taskCreationTime)
            put(COLUMN_TASK_EXECUTION_DATE, task.taskExecutionDate)
            put(COLUMN_TASK_NOTIFICATION, task.taskNotification)
            put(COLUMN_TASK_CATEGORY, task.taskCategory)
        }
        return db.insert(TABLE_TASKS, null, values).also { db.close() }
    }

    // Dodawanie załącznika
    fun insertAttachment(attachment: Attachment): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK_ID, attachment.taskId)
            put(COLUMN_ATTACHMENT_PATH, attachment.attachmentPath)
        }
        return db.insert(TABLE_ATTACHMENTS, null, values).also { db.close() }
    }

    // Pobieranie załączników dla zadania
    fun getAttachmentsForTask(taskId: Long): List<String> {
        val db = readableDatabase
        val attachments = mutableListOf<String>()
        val cursor = db.query(
            TABLE_ATTACHMENTS,
            arrayOf(COLUMN_ATTACHMENT_PATH),
            "$COLUMN_TASK_ID = ?",
            arrayOf(taskId.toString()),
            null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                attachments.add(it.getString(it.getColumnIndexOrThrow(COLUMN_ATTACHMENT_PATH)))
            }
        }
        db.close()
        return attachments
    }
}
