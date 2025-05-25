package com.example.todo.data.tasks

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    var taskId: Long = 0,
    var taskTitle: String = "",
    var taskStatus: Int = 0,
    var taskDescription: String = "",
    val taskCreationTime: String = "",
    var taskExecutionDate: String = "",
    var taskNotification: Int = 0,
    var taskCategory: String = "",
    var attachments: List<String> = emptyList()
) : Parcelable
