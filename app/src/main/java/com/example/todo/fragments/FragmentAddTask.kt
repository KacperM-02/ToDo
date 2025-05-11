package com.example.todo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.todo.data.Task
import com.example.todo.data.TasksDatabaseHelper
import com.example.todo.databinding.FragmentAddTaskBinding
import java.time.LocalDate

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FragmentAddTask : Fragment() {
    private var _binding: FragmentAddTaskBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        val context = requireContext()
        val db = TasksDatabaseHelper(context)

        binding.addTaskButton.setOnClickListener {
            val task = Task(
                taskTitle = binding.taskTitleInput.text.toString(),
                taskStatus = if(binding.taskStatusText.equals("Done")) 1 else 0,
                taskDescription = binding.taskDescriptionInput.text.toString(),
                taskCreationTime = LocalDate.now().toString(),
                taskExecutionDate = binding.taskExecutionTime.text.toString(),
                taskNotification = if(binding.taskNotificationToggle.isChecked) 1 else 0,
                taskCategory = "Category",
            )

            db.insertTask(task)
            Toast.makeText(context, "Task added!", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}