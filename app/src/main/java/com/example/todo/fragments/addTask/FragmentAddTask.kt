package com.example.todo.fragments.addTask

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.data.tasks.Task
import com.example.todo.data.tasks.TasksDatabaseHelper
import com.example.todo.databinding.FragmentAddTaskBinding
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class FragmentAddTask : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private var _binding: FragmentAddTaskBinding? = null

    private lateinit var dbHelper: TasksDatabaseHelper
    private lateinit var attachmentAdapter: AttachmentAdapter
    private lateinit var task: Task
    private lateinit var calendar: Calendar

    private var dayOfMonth = 0
    private var month = 0
    private var year = 0
    private var hourOfDay = 0
    private var minute = 0

    private var selectedDayOfMonth = 0
    private var selectedMonth = 0
    private var selectedYear = 0

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        dbHelper = TasksDatabaseHelper(requireContext())
        task = Task()
        calendar = Calendar.getInstance()
        val attachmentsList = mutableListOf<String>()

        initCategoryDropdown(dbHelper.getAllCategories().toMutableList())

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(3)) { uris ->
            if(uris.isNotEmpty())
            {
                attachmentsList.clear()
                uris.forEach { uri ->
                    attachmentsList.add(uri.toString())
                }

                attachmentAdapter = AttachmentAdapter(attachmentsList) { attachment ->
                    attachmentAdapter.removeAttachment(attachment)
                    attachmentsList.remove(attachment)
                }

                binding.attachmentsRecyclerView.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    adapter = attachmentAdapter
                }
            }
        }

        binding.addTaskButton.setOnClickListener {
            task = Task(
                taskTitle = binding.taskTitleInput.text.toString(),
                taskDescription = binding.taskDescriptionInput.text.toString(),
                taskCreationTime = LocalDate.now().toString(),
                taskExecutionDate = binding.taskExecutionDate.text.toString(),
                taskNotification = if(binding.taskNotificationToggle.isChecked) 1 else 0,
                taskCategory = binding.taskCategoryDropdown.text.toString(),
            )

            if(!validateTask(task)) Toast.makeText(requireContext(), "Fill the title, " +
                    "execution date or choose task category!", Toast.LENGTH_LONG).show()
            else{
                val taskId = dbHelper.insertTask(task)
                for (a in attachmentsList) {
                    dbHelper.insertAttachment(a, taskId)
                }

                Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.FragmentAddTaskToFragmentMainAction)
            }
        }

        binding.taskExecutionDate.setOnClickListener {
            setCalendarDate()
            DatePickerDialog(requireContext(), this, year, month, dayOfMonth).show()
        }

        binding.addAttachmentButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        return binding.root
    }

    private fun initCategoryDropdown(categories : MutableList<String>) {
        val defaultCategories = listOf("Education", "Home", "Hobby", "Shopping", "Work", "+Add new")

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.category_item,
            categories.apply { addLast("+Add new") }
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.category_item, parent, false)
                val textView = view.findViewById<TextView>(R.id.categoryText)
                val deleteIcon = view.findViewById<ImageView>(R.id.deleteIcon)
                val category = getItem(position) ?: ""

                textView.text = category

                if (category !in defaultCategories) {
                    deleteIcon.visibility = View.VISIBLE
                    deleteIcon.setOnClickListener {
                        AlertDialog.Builder(context)
                            .setTitle("Delete category?")
                            .setMessage("Are you sure you want to delete \"$category\"?")
                            .setPositiveButton("Yes") { _, _ ->
                                dbHelper.deleteCategory(category)
                                categories.remove(category)
                                notifyDataSetChanged()
                                binding.taskCategoryDropdown.setText("")
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                } else {
                    deleteIcon.visibility = View.GONE
                    deleteIcon.setOnClickListener(null)
                }

                return view
            }
        }

        binding.taskCategoryDropdown.setAdapter(adapter)
        binding.taskCategoryDropdown.setOnItemClickListener { _, _, position, _ ->
            if (position == adapter.count - 1) {
                binding.taskCategoryDropdown.setText("")
                showAddCategoryDialog(adapter)
            }
        }
    }

    private fun showAddCategoryDialog(adapter: ArrayAdapter<String>) {
        val dialogView = layoutInflater.inflate(R.layout.input_dialog, null)
        val input = dialogView.findViewById<TextInputEditText>(R.id.inputCategory)
        val errorText = dialogView.findViewById<TextView>(R.id.errorTextView)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add new category")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                var newCategory = input.text.toString()
                if (newCategory.isNotBlank()) {
                    newCategory = newCategory
                        .trim()
                        .replace(Regex("[^A-Za-z]"), "_")
                        .replaceFirstChar { it.uppercaseChar() }

                    if (dbHelper.insertCategory(newCategory) == -1L)
                        errorText.text = getString(R.string.same_category_error)
                    else {
                        val categories = dbHelper.getAllCategories().toMutableList()
                        categories.addLast("+Add new")
                        adapter.clear()
                        adapter.addAll(categories)
                        adapter.notifyDataSetChanged()

                        binding.taskCategoryDropdown.setText(newCategory, false)
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Added new category!", Toast.LENGTH_LONG).show()
                    }
                }
                else
                    errorText.text = getString(R.string.empty_category_error)
            }
        }
        dialog.show()
    }

    private fun setCalendarDate() {
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun setCalendarTime() {
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
    }

    private fun validateTask(task: Task): Boolean {
        return !(task.taskTitle.isEmpty() || task.taskExecutionDate.isEmpty() || task.taskCategory.isEmpty())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        setSelectedDate(year, month, dayOfMonth)

        setCalendarTime()
        TimePickerDialog(requireContext(), this, hourOfDay, minute, true).show()
    }

    private fun setSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        selectedYear = year
        selectedMonth = month + 1
        selectedDayOfMonth = dayOfMonth
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val selectedDate = convertDate(selectedYear, selectedMonth, selectedDayOfMonth, hourOfDay, minute)
        binding.taskExecutionDate.setText(selectedDate)
    }

    private fun convertDate(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hourOfDay: Int,
        minute: Int
    ): String {
        val dateTime = LocalDateTime.of(year, month, dayOfMonth, hourOfDay, minute)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return dateTime.format(formatter)
    }
}