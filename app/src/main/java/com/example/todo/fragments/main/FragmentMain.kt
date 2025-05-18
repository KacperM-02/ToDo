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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.data.sharedPreferences.CategoryPreferences
import com.example.todo.data.sharedPreferences.NotificationTimePreferences
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
                        val categoriesList = CategoryPreferences.loadCategories(requireContext())
                        val selectedCategory = CategoryPreferences.loadSelectedCategory(requireContext())
                        categoriesList.addFirst("Select category...")
                        showSpinnerDialog(
                            categoriesList,
                            getString(R.string.select_category),
                            if(selectedCategory == null) "Selected category: None" else "Selected category: $selectedCategory",
                            "Filter",
                            "Clear",
                            ::filterTasksByCategory
                        )
                        true
                    }
                    R.id.select_notification_time -> {
                        val notificationTimeList = mutableListOf("Select time...", "10 min", "15 min", "30 min", "60 min")
                        val notificationTime = NotificationTimePreferences.loadNotificationTime(requireContext())
                        showSpinnerDialog(
                            notificationTimeList,
                            getString(R.string.select_notification_time),
                            "Selected time: $notificationTime min",
                            "Confirm",
                            "Default (5 min)",
                            ::setNotificationTime
                        )
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

    private fun showSpinnerDialog(
        listOfElements: MutableList<String>,
        title: String,
        infoText: String,
        positiveButtonText: String,
        neutralButtonText: String,
        function : (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.select_dialog, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.selectSpinner)
        dialogView.findViewById<TextView>(R.id.infoTextView).text = infoText

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.dropdown_item,
            listOfElements
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
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(positiveButtonText) {_, _ ->
                function(spinner.selectedItem.toString())
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton(neutralButtonText) {_, _ ->
                function("")
            }
            .show()
    }

    private fun filterTasksByCategory(selectedCategory: String) {
        val filteredList = if(selectedCategory.isNotEmpty() && selectedCategory != "Select category...") {
            CategoryPreferences.setSelectedCategory(requireContext(), selectedCategory)
            allTasksList.filter { task ->
                task.taskCategory == selectedCategory
            }
        }
        else {
            CategoryPreferences.setSelectedCategory(requireContext(), null)
            allTasksList
        }

        tasksAdapter.updateList(filteredList)
    }

    private fun setNotificationTime(notificationTime: String) {
        val minutes =
            if(notificationTime.isEmpty() && notificationTime != "Select time...") 5
            else notificationTime.split(" ").first().toLong()
        NotificationTimePreferences.setNotificationTime(requireContext(), minutes)
    }

    private fun setupRecyclerView() {
        val selectedCategory = CategoryPreferences.loadSelectedCategory(requireContext())
        tasksAdapter = TasksAdapter(
            if(selectedCategory == null) allTasksList
            else allTasksList.filter { task ->
                task.taskCategory == selectedCategory
            }
        ) { task ->
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