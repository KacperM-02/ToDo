package com.example.todo.data.tasks

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_ATTACHMENT_ID
import com.example.todo.data.tasks.TaskContract.TaskEntry.DATABASE_NAME
import com.example.todo.data.tasks.TaskContract.TaskEntry.DATABASE_VERSION
import com.example.todo.data.tasks.TaskContract.TaskEntry.TABLE_TASKS
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_ATTACHMENT_PATH
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_ATTACHMENT_TASK_ID
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_CATEGORY_ID
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_CATEGORY_NAME
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_TASK_CATEGORY_ID
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_TASK_CREATION_TIME
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_TASK_DESCRIPTION
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_TASK_EXECUTION_DATE
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_TASK_ID
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_TASK_NOTIFICATION
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_TASK_STATUS
import com.example.todo.data.tasks.TaskContract.TaskEntry.COLUMN_TASK_TITLE
import com.example.todo.data.tasks.TaskContract.TaskEntry.TABLE_ATTACHMENTS
import com.example.todo.data.tasks.TaskContract.TaskEntry.TABLE_CATEGORIES

class TasksDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        // 1. Categories table
        val createCategoriesTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
            $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_CATEGORY_NAME TEXT UNIQUE NOT NULL COLLATE NOCASE
        )
    """.trimIndent()

        // 2. Tasks table
        val createTasksTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_TASKS (
            $COLUMN_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TASK_TITLE TEXT NOT NULL,
            $COLUMN_TASK_STATUS INTEGER NOT NULL DEFAULT 0,
            $COLUMN_TASK_DESCRIPTION TEXT,
            $COLUMN_TASK_CREATION_TIME TEXT NOT NULL,
            $COLUMN_TASK_EXECUTION_DATE TEXT NOT NULL,
            $COLUMN_TASK_NOTIFICATION INTEGER NOT NULL DEFAULT 0,
            $COLUMN_TASK_CATEGORY_ID INTEGER NOT NULL,
            FOREIGN KEY ($COLUMN_TASK_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_CATEGORY_ID)
        )
    """.trimIndent()

        // 3. Attachments table
        val createAttachmentsTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_ATTACHMENTS (
            $COLUMN_ATTACHMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_ATTACHMENT_TASK_ID INTEGER NOT NULL,
            $COLUMN_ATTACHMENT_PATH TEXT NOT NULL,
            FOREIGN KEY ($COLUMN_ATTACHMENT_TASK_ID) REFERENCES $TABLE_TASKS($COLUMN_TASK_ID) ON DELETE CASCADE
        )
    """.trimIndent()

        db?.apply {
            execSQL(createCategoriesTable)
            execSQL(createTasksTable)
            execSQL(createAttachmentsTable)

            beginTransaction()
            try {
                val initialCategories = listOf("Education", "Home", "Hobby", "Shopping", "Work")
                val values = ContentValues()
                for (category in initialCategories) {
                    values.clear()
                    values.put(COLUMN_CATEGORY_NAME, category)
                    insertWithOnConflict(
                        TABLE_CATEGORIES,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )
                }
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        db?.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ATTACHMENTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    // Tasks table
    // Adding task
    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val categoryId = getCategoryIdByName(task.taskCategory)
        if(categoryId == -1L) return categoryId

        val values = ContentValues().apply {
            put(COLUMN_TASK_TITLE, task.taskTitle)
            put(COLUMN_TASK_STATUS, task.taskStatus)
            put(COLUMN_TASK_DESCRIPTION, task.taskDescription)
            put(COLUMN_TASK_CREATION_TIME, task.taskCreationTime)
            put(COLUMN_TASK_EXECUTION_DATE, task.taskExecutionDate)
            put(COLUMN_TASK_NOTIFICATION, task.taskNotification)
            put(COLUMN_TASK_CATEGORY_ID, categoryId)
        }
        return db.insert(TABLE_TASKS, null, values).also { db.close() }
    }

    // Getting all tasks
    fun getAllTasks(): List<Task> {
        val db = readableDatabase
        val tasks = mutableListOf<Task>()

        val query = """
            SELECT t.$COLUMN_TASK_ID, 
            t.$COLUMN_TASK_TITLE, 
            t.$COLUMN_TASK_STATUS, 
            t.$COLUMN_TASK_DESCRIPTION, 
            t.$COLUMN_TASK_CREATION_TIME, 
            t.$COLUMN_TASK_EXECUTION_DATE, 
            t.$COLUMN_TASK_NOTIFICATION,
            c.$COLUMN_CATEGORY_NAME
            FROM $TABLE_TASKS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TASK_CATEGORY_ID = c.$COLUMN_CATEGORY_ID
            ORDER BY t.$COLUMN_TASK_EXECUTION_DATE ASC
        """.trimIndent()

        val tasksCursor = db.rawQuery(query, null)

        tasksCursor.use { cursor ->
            while (cursor.moveToNext()) {
                val taskId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID))

                // Get attachments for current task
                val attachments = getAttachmentsForTask(taskId)

                tasks.add(
                    Task(
                        taskId = taskId,
                        taskTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        taskStatus = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_STATUS)),
                        taskDescription = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)),
                        taskCreationTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CREATION_TIME)),
                        taskExecutionDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_EXECUTION_DATE)),
                        taskNotification = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_NOTIFICATION)),
                        taskCategory = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)),
                        attachments = attachments
                    )
                )
            }
        }

        db.close()
        return tasks
    }


    // Attachments table
    // Adding attachment
    fun insertAttachment(attachmentPath: String, taskId: Long): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ATTACHMENT_TASK_ID, taskId)
            put(COLUMN_ATTACHMENT_PATH, attachmentPath)
        }
        return db.insert(TABLE_ATTACHMENTS, null, values).also { db.close() }
    }

    // Getting attachments for tasks
    private fun getAttachmentsForTask(taskId: Long): List<String> {
        val db = readableDatabase
        val attachments = mutableListOf<String>()

        val cursor = db.query(
            TABLE_ATTACHMENTS,
            arrayOf(COLUMN_ATTACHMENT_PATH),
            "$COLUMN_ATTACHMENT_TASK_ID = ?",
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


    //Categories table
    // Getting category id
    private fun getCategoryIdByName(categoryName: String): Long {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            arrayOf(COLUMN_CATEGORY_ID),
            "$COLUMN_CATEGORY_NAME = ?",
            arrayOf(categoryName),
            null, null, null
        )

        cursor.use {
            return if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
            } else {
                -1L
            }
        }
    }

    // Getting all categories
    fun getAllCategories() : List<String> {
        val db = readableDatabase
        val categories = mutableListOf<String>()

        val cursor = db.query(
            TABLE_CATEGORIES,
            arrayOf(COLUMN_CATEGORY_NAME),
            null,
            null,
            null,
            null,
            "$COLUMN_CATEGORY_NAME COLLATE NOCASE ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                categories.add(it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)))
            }
        }

        db.close()
        return categories
    }

    // Adding category
    fun insertCategory(categoryName : String) : Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, categoryName)
        }

        return db.insert(TABLE_CATEGORIES, null, values).also { db.close() }
    }

    fun deleteCategory(categoryName: String): Int {
        val db = writableDatabase
        val deletedRows = db.delete(
            TABLE_CATEGORIES,
            "$COLUMN_CATEGORY_NAME = ?",
            arrayOf(categoryName)
        )
        db.close()
        return deletedRows
    }
}
