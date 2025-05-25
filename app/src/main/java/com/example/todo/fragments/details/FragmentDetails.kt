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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.data.notifications.NotificationReceiver
import com.example.todo.data.sharedPreferences.NotificationTimePreferences
import com.example.todo.data.tasks.Task
import com.example.todo.data.tasks.TasksDatabaseHelper
import com.example.todo.databinding.FragmentDetailsBinding
import com.example.todo.fragments.addTask.AttachmentAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class FragmentDetails : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentDetailsArgs by navArgs()

    private lateinit var dbHelper: TasksDatabaseHelper
    private lateinit var attachmentAdapter: AttachmentAdapter
    private lateinit var calendar: Calendar
    private lateinit var adapter: ArrayAdapter<String>

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

        val taskDetails = args.taskDetails

        binding.taskTitleInput.setText(taskDetails.taskTitle)
        binding.taskStatusText.text = getString(
            R.string.task_status,
            if (taskDetails.taskStatus == 1) "Done" else "Undone"
        )
        binding.taskCreationTimeText.text = getString(
            R.string.task_status,
            taskDetails.taskCreationTime
        )
        binding.taskNotificationToggle.isChecked =
            taskDetails.taskNotification == 1
        binding.taskDescriptionInput.setText(taskDetails.taskDescription)
        binding.taskExecutionDate.setText(taskDetails.taskExecutionDate)
        binding.taskCategoryDropdown.setText(taskDetails.taskCategory)

        val attachmentsList = mutableListOf<String>()
        initCategoryDropdown()

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(3)) { uris ->
                if (uris.isNotEmpty()) {
                    attachmentsList.clear()
                    uris.forEach { uri ->
                        attachmentsList.add(uri.toString())
                    }

                    attachmentAdapter = AttachmentAdapter(attachmentsList) { attachment ->
                        attachmentAdapter.removeAttachment(attachment)
                        attachmentsList.remove(attachment)
                    }

                    binding.attachmentsRecyclerView.apply {
                        layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        adapter = attachmentAdapter
                    }
                }
            }

        binding.taskEditButton.setOnClickListener {
            enableEditing()
        }

        return binding.root
    }

    private fun initCategoryDropdown() {
        val categoriesList = dbHelper.getAllCategories().toMutableList()
        categoriesList.add("+Add new")
        val defaultCategories = listOf("Education", "Home", "Hobby", "Shopping", "Work", "+Add new")

        adapter = object : ArrayAdapter<String>(
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
                                adapter.remove(category)
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

        binding.taskCategoryDropdown.setAdapter(adapter)
        binding.taskCategoryDropdown.setOnItemClickListener { _, _, position, _ ->
            if (position == adapter.count - 1) {
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

                        adapter.clear()
                        adapter.addAll(categoriesList)
                        adapter.notifyDataSetChanged()

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

    private fun enableEditing() {
        binding.taskTitleInput.isEnabled = true
        binding.taskDescriptionInput.isEnabled = true
        binding.taskExecutionDate.isEnabled = true
        binding.taskCategoryDropdown.isEnabled = true
        binding.taskNotificationToggle.isEnabled = true
        binding.addAttachmentButton.isEnabled = true

        binding.taskTitleInput.addTextChangedListener { checkForChanges(args.taskDetails) }
        binding.taskDescriptionInput.addTextChangedListener { checkForChanges(args.taskDetails) }
        binding.taskExecutionDate.addTextChangedListener { checkForChanges(args.taskDetails) }
        binding.taskCategoryDropdown.addTextChangedListener { checkForChanges(args.taskDetails) }
        binding.taskNotificationToggle.setOnCheckedChangeListener { _, _ -> checkForChanges(args.taskDetails) }
    }

    private fun checkForChanges(originalTask: Task) {
        val hasChanged =
            binding.taskTitleInput.text.toString() != originalTask.taskTitle ||
                    binding.taskDescriptionInput.text.toString() != originalTask.taskDescription ||
                    binding.taskExecutionDate.text.toString() != originalTask.taskExecutionDate ||
                    binding.taskCategoryDropdown.text.toString() != originalTask.taskCategory ||
                    binding.taskNotificationToggle.isChecked != (originalTask.taskNotification == 1)

        binding.taskSaveChangesButton.isEnabled = hasChanged
    }
}