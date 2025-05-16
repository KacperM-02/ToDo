package com.example.todo.fragments.main

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.data.sharedPreferences.CategoryPreferences
import com.example.todo.data.tasks.Task
import com.example.todo.data.tasks.TasksDatabaseHelper
import com.example.todo.databinding.FragmentMainBinding

class FragmentMain : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var dbHelper: TasksDatabaseHelper
    private lateinit var allTasksList: MutableList<Task>

    private var showCompletedTasks = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        dbHelper = TasksDatabaseHelper(requireContext())
        allTasksList = dbHelper.getAllTasks().toMutableList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.show_done_tasks -> {
                        if(showCompletedTasks == 0)
                        {
                            showCompletedTasks = 1
                            menuItem.title = "Hide completed tasks"
                        }
                        else {
                            showCompletedTasks = 0
                            menuItem.title = "Show completed tasks"
                        }

                        val filteredList = if(showCompletedTasks == 1) {
                            allTasksList.filter { task ->
                                task.taskStatus == 1
                            }
                        } else allTasksList
                        tasksAdapter.updateList(filteredList)
                        true
                    }
                    R.id.select_category -> {
                        showCategoryDialog()
                        true
                    }
                    R.id.select_notification_time -> {
                        Toast.makeText(requireContext(), "Selecting time...", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.sort_tasks -> {
                        Toast.makeText(requireContext(), "Sorting tasks...", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> return false
                }
            }
        }, viewLifecycleOwner)

        binding.taskSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.taskSearchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrBlank()) {
                    allTasksList
                } else {
                    allTasksList.filter { task ->
                        task.taskTitle.contains(newText, ignoreCase = true)
                    }
                }
                tasksAdapter.updateList(filteredList)
                return true
            }
        })
    }

    private fun showCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_category, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)

        val categories = CategoryPreferences.loadCategories(requireContext())
        categories.addFirst("Select category")

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.dropdown_item,
            categories
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }
        spinner.adapter = adapter
        spinner.setSelection(0)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_category)
            .setView(dialogView)
            .setPositiveButton("Filter") {_, _ ->
                filterTasksByCategory(spinner.selectedItem.toString())
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Clear") {_, _ ->
                filterTasksByCategory("")
            }
            .show()

    }

    private fun filterTasksByCategory(selectedCategory: String) {
        val filteredList = if(selectedCategory.isNotEmpty() && selectedCategory != "Select category") {
            allTasksList.filter { task ->
                task.taskCategory == selectedCategory
            }
        }
        else {
            allTasksList
        }

        tasksAdapter.updateList(filteredList)
    }

    private fun setupRecyclerView() {
        tasksAdapter = TasksAdapter(allTasksList) { task ->
            val action = FragmentMainDirections.FragmentMainToFragmentDetailsAction(task)
            findNavController().navigate(action)
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