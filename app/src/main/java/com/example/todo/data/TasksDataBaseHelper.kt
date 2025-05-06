package com.example.todo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.todo.data.TaskContract.TaskEntry.DATABASE_NAME
import com.example.todo.data.TaskContract.TaskEntry.DATABASE_VERSION
import com.example.todo.data.TaskContract.TaskEntry.TABLE_NAME
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_ID
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_CONTENT
import com.example.todo.data.TaskContract.TaskEntry.COLUMN_TITLE

class TasksDataBaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY," +
                    "$COLUMN_TITLE TEXT," +
                    "$COLUMN_CONTENT TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val deleteTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(deleteTableQuery)
        onCreate(db)
    }

    fun insertNote(task: Task)
    {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_CONTENT, task.taskDescription)
        }

        db.insert(TABLE_NAME, null, values)
        db.close()
    }
}
