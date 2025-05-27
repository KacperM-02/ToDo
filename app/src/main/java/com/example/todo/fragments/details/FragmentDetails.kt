package com.example.todo.fragments.details

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.data.notifications.NotificationReceiver
import com.example.todo.data.sharedPreferences.NotificationTimePreferences
import com.example.todo.data.tasks.Task
import com.example.todo.data.tasks.TasksDatabaseHelper
import com.example.todo.databinding.FragmentDetailsBinding
import com.example.todo.fragments.addTask.AttachmentAdapter
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class FragmentDetails : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private val binding get() = _binding!!
    private val args: FragmentDetailsArgs by navArgs()

    private lateinit var dbHelper: TasksDatabaseHelper
    private lateinit var attachmentAdapter: AttachmentAdapter
    private lateinit var categoriesAdapter: ArrayAdapter<String>
    private lateinit var calendar: Calendar
    private lateinit var taskDetails: Task
    private lateinit var taskOriginalDetails: Task
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var attachmentsList: MutableList<String>

    private var _binding: FragmentDetailsBinding? = null

    private var dayOfMonth = 0
    private var month = 0
    private var year = 0
    private var hourOfDay = 0
    private var minute = 0

    private var selectedDayOfMonth = 0
    private var selectedMonth = 0
    private var selectedYear = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        dbHelper = TasksDatabaseHelper(requireContext())
        calendar = Calendar.getInstance()

        taskOriginalDetails = args.taskDetails.copy()

        setInitialData()

        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
                if (uris.isNotEmpty()) {
                    attachmentsList.clear()
                    uris.forEach { uri ->
                        attachmentsList.add(uri.toString())
                    }
                    attachmentAdapter.notifyDataSetChanged()
                    checkForChanges()
                }
            }

        binding.taskTitleInput.addTextChangedListener { checkForChanges() }
        binding.taskDescriptionInput.addTextChangedListener { checkForChanges() }
        binding.taskExecutionDate.addTextChangedListener { checkForChanges() }
        binding.taskCategoryDropdown.addTextChangedListener { checkForChanges() }
        binding.taskNotificationToggle.setOnCheckedChangeListener { _, _ ->
            checkForChanges()
        }

        binding.taskExecutionDate.setOnClickListener {
            setCalendarDate()
            DatePickerDialog(requireContext(), this, year, month, dayOfMonth).show()
        }

        binding.addAttachmentButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.taskEditButton.setOnClickListener {
            toggleEditing()
        }

        binding.taskMakeDoneUndone.setOnClickListener {
            updateTaskStatus()
        }

        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Are you sure you want to delete the task?")
                .setPositiveButton("Yes") { _, _ ->
                    dbHelper.deleteTask(taskDetails.taskId)
                    findNavController().navigate(R.id.FragmentDetailsToFragmentMainAction)
                }
                .setNegativeButton("No", null)
                .show()
        }

        return binding.root
    }

    private fun setInitialData() {
        taskDetails = taskOriginalDetails.copy()

        binding.taskTitleInput.setText(taskOriginalDetails.taskTitle)
        binding.taskStatusText.text = getString(
            R.string.task_status,
            if (taskOriginalDetails.taskStatus == 1) "Done" else "Undone"
        )
        binding.taskCreationTimeText.text = getString(
            R.string.task_creation_time,
            taskOriginalDetails.taskCreationTime
        )
        binding.taskNotificationToggle.isChecked =
            taskOriginalDetails.taskNotification == 1
        binding.taskDescriptionInput.setText(taskOriginalDetails.taskDescription)
        binding.taskExecutionDate.setText(taskOriginalDetails.taskExecutionDate)
        binding.taskCategoryDropdown.setText(taskOriginalDetails.taskCategory)
        binding.taskMakeDoneUndone.text =
            getString(
                if (taskOriginalDetails.taskStatus == 1)
                    R.string.task_make_undone else R.string.task_make_done
            )

        attachmentsList = taskOriginalDetails.attachments.toMutableList()
        attachmentAdapter = AttachmentAdapter(attachmentsList, null)
        binding.attachmentsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = attachmentAdapter
        }

        initCategoryDropdown()
    }

    private fun updateTaskStatus() {
        binding.taskStatusText.text = when (taskDetails.taskStatus) {
            1 -> {
                taskDetails.taskStatus = 0
                binding.taskMakeDoneUndone.text = getString(R.string.task_make_done)
                getString(
                    R.string.task_status, "Undone"
                )
            }

            else -> {
                taskDetails.taskStatus = 1
                binding.taskMakeDoneUndone.text = getString(R.string.task_make_undone)
                getString(
                    R.string.task_status, "Done"
                )
            }
        }
        checkForChanges()
    }

    private fun initCategoryDropdown() {
        val categoriesList = dbHelper.getAllCategories().toMutableList()
        categoriesList.add("+Add new")
        val defaultCategories = listOf("Education", "Home", "Hobby", "Shopping", "Work", "+Add new")

        categoriesAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.category_item,
            categoriesList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.category_item, parent, false)
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
                                categoriesAdapter.remove(category)
                                notifyDataSetChanged()
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

        binding.taskCategoryDropdown.setAdapter(categoriesAdapter)
        binding.taskCategoryDropdown.setOnItemClickListener { _, _, position, _ ->
            if (position == categoriesAdapter.count - 1) {
                binding.taskCategoryDropdown.setText("")
                showAddCategoryDialog()
            }
        }
    }

    private fun showAddCategoryDialog() {
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
                        val categoriesList = dbHelper.getAllCategories().toMutableList()
                        categoriesList.add("+Add new")

                        categoriesAdapter.clear()
                        categoriesAdapter.addAll(categoriesList)
                        categoriesAdapter.notifyDataSetChanged()

                        val text = binding.taskCategoryDropdown.text
                        binding.taskCategoryDropdown.text = text
                        binding.taskCategoryDropdown.setSelection(0)
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Added new category!", Toast.LENGTH_LONG)
                            .show()
                    }
                } else
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
        val selectedDate =
            convertDate(selectedYear, selectedMonth, selectedDayOfMonth, hourOfDay, minute)
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

    private fun scheduleTaskNotification(context: Context, task: Task) {
        if (task.taskNotification == 0) return

        val notificationOffsetMinutes = NotificationTimePreferences.loadNotificationTime(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", task.taskTitle)
            putExtra("description", task.taskDescription)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val executionDateTime = LocalDateTime.parse(task.taskExecutionDate, formatter)

        val triggerTimeMillis = executionDateTime
            .atZone(ZoneId.systemDefault())
            .minusMinutes(notificationOffsetMinutes)
            .toInstant()
            .toEpochMilli()


        if (triggerTimeMillis > System.currentTimeMillis() && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    private fun toggleEditing() {
        if (binding.taskEditButton.text == getString(R.string.task_edit)) {
            binding.taskTitleInput.isEnabled = true
            binding.taskDescriptionInput.isEnabled = true
            binding.taskExecutionDate.isEnabled = true
            binding.taskCategoryDropdown.isEnabled = true
            binding.taskNotificationToggle.isEnabled = true
            binding.addAttachmentButton.isEnabled = true

            attachmentAdapter.updateOnDeleteAttachmentFunction { attachment ->
                attachmentAdapter.removeAttachment(attachment)
                attachmentsList.remove(attachment)
                checkForChanges()
            }
        } else {
            setInitialData()

            binding.taskTitleInput.isEnabled = false
            binding.taskDescriptionInput.isEnabled = false
            binding.taskExecutionDate.isEnabled = false
            binding.taskCategoryDropdown.isEnabled = false
            binding.taskNotificationToggle.isEnabled = false
            binding.addAttachmentButton.isEnabled = false

            binding.taskEditButton.text = getString(R.string.task_edit)
            binding.taskSaveChangesButton.isEnabled = false
            attachmentAdapter.updateOnDeleteAttachmentFunction(null)
        }
    }

    private fun checkForChanges() {
        val hasChanged =
            binding.taskTitleInput.text.toString() != taskOriginalDetails.taskTitle ||
                    binding.taskDescriptionInput.text.toString() != taskOriginalDetails.taskDescription ||
                    binding.taskExecutionDate.text.toString() != taskOriginalDetails.taskExecutionDate ||
                    binding.taskCategoryDropdown.text.toString() != taskOriginalDetails.taskCategory ||
                    binding.taskNotificationToggle.isChecked != (taskOriginalDetails.taskNotification == 1) ||
                    taskDetails.taskStatus != taskOriginalDetails.taskStatus ||
                    attachmentsList != taskOriginalDetails.attachments

        binding.taskEditButton.setText(
            if (hasChanged)
                R.string.task_restore_edit
            else
                R.string.task_edit
        )
        binding.taskSaveChangesButton.isEnabled = hasChanged
    }
}