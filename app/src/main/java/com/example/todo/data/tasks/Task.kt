package com.example.todo.data.tasks

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    var taskId: Long = 0,
    val taskTitle: String = "",
    val taskStatus: Int = 0,
    val taskDescription: String = "",
    val taskCreationTime: String = "",
    val taskExecutionDate: String = "",
    val taskNotification: Int = 0,
    val taskCategory: String = "",
    var attachments: List<String> = emptyList()
) : Parcelable
