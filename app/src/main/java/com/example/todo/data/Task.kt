package com.example.todo.data

data class Task(
    var taskId: Long = 0,
    val taskTitle: String,
    val taskStatus: Int = 0,
    val taskDescription: String,
    val taskCreationTime: String,
    val taskExecutionDate: String,
    val taskNotification: Int,
    val taskCategory: String,
    val attachments: List<String> = emptyList()
)

data class Attachment(
    val attachmentId: Long,
    val taskId: Long,
    val attachmentPath: String)
