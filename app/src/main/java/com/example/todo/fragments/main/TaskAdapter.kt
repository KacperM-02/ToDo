package com.example.todo.fragments.main

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.data.tasks.Task

class TasksAdapter(
    private var tasksList: List<Task>,
    private val onItemClick: (Task) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.taskTitle)
        private val executionDateTextView: TextView = itemView.findViewById(R.id.taskDueDate)
        private val attachmentIcon: ImageView = itemView.findViewById(R.id.attachmentIcon)

        fun bind(task: Task) {
            titleTextView.text = task.taskTitle
            if(task.taskStatus == 1) {
                titleTextView.setTextColor(getColor(context, R.color.green))
                titleTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }
            else {
                titleTextView.setTextColor(getColor(context, R.color.black))
                titleTextView.paintFlags = titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            executionDateTextView.text = itemView.context.getString(R.string.execution_date_main, task.taskExecutionDate)
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


    fun updateList(newList: List<Task>) {
        tasksList = newList
        notifyDataSetChanged()
    }
}