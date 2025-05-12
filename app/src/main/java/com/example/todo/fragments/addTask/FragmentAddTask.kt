package com.example.todo.fragments.addTask

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.data.Task
import com.example.todo.data.TasksDatabaseHelper
import com.example.todo.databinding.FragmentAddTaskBinding
import java.time.LocalDate


class FragmentAddTask : Fragment() {
    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: TasksDatabaseHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        dbHelper = TasksDatabaseHelper(requireContext())

        binding.addTaskButton.setOnClickListener {
            val task = Task(
                taskTitle = binding.taskTitleInput.text.toString(),
                taskDescription = binding.taskDescriptionInput.text.toString(),
                taskCreationTime = LocalDate.now().toString(),
                taskExecutionDate = binding.taskExecutionTime.text.toString(),
                taskNotification = if(binding.taskNotificationToggle.isChecked) 1 else 0,
                taskCategory = "Category",
            )

            if(!validateTask(task)) Toast.makeText(requireContext(), "Fill the title, " +
                    "execution date or choose task category!", Toast.LENGTH_LONG).show()
            else{
                dbHelper.insertTask(task)
                Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.FragmentAddTaskToFragmentMainAction)
            }
        }
        return binding.root
    }

    private fun validateTask(task: Task): Boolean {
        return !(task.taskTitle.isEmpty() || task.taskExecutionDate.isEmpty() || task.taskCategory.isEmpty())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}