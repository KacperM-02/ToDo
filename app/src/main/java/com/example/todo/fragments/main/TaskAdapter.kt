package com.example.todo.fragments.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.data.Task

class TasksAdapter(
    private val tasksList: List<Task>,
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.taskTitle)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.taskDueDate)
        private val attachmentIcon: ImageView = itemView.findViewById(R.id.attachmentIcon)

        fun bind(task: Task) {
            titleTextView.text = task.taskTitle
            dueDateTextView.text = "Termin: ${task.taskExecutionDate}"

            attachmentIcon.visibility = if (task.attachments.isNotEmpty()) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onItemClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasksList[position])
    }

    override fun getItemCount() = tasksList.size
}