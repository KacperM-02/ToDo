package com.example.todo.data

data class Task(
    var taskId: Long = 0,
    val taskTitle: String = "",
    val taskStatus: Int = 0,
    val taskDescription: String = "",
    val taskCreationTime: String = "",
    val taskExecutionDate: String = "",
    val taskNotification: Int = 0,
    val taskCategory: String = "",
    var attachments: List<Attachment> = emptyList()
)

data class Attachment(
    val attachmentId: Long = 0,
    val taskId: Long = 0,
    val attachmentPath: String = "")
