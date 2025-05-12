package com.example.todo.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.data.TasksDatabaseHelper
import com.example.todo.databinding.FragmentMainBinding

class FragmentMain : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var dbHelper: TasksDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        dbHelper = TasksDatabaseHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        tasksAdapter = TasksAdapter(dbHelper.getAllTasks()) { task ->
//            val action = FragmentMainDirections.actionFragmentMainToFragmentTaskDetails(task.taskId)
//            findNavController().navigate(action)
            Toast.makeText(requireContext(), "Clicked task: ${task.taskTitle}", Toast.LENGTH_SHORT).show()
        }

        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tasksAdapter
        }
    }

    private fun setupListeners() {
        binding.addTaskFloatingButton.setOnClickListener {
            findNavController().navigate(R.id.FragmentMainToFragmentAddTaskAction)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}