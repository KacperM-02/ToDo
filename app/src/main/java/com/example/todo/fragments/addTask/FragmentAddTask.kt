package com.example.todo.fragments.addTask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.data.Attachment
import com.example.todo.data.Task
import com.example.todo.data.TasksDatabaseHelper
import com.example.todo.databinding.FragmentAddTaskBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class FragmentAddTask : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: TasksDatabaseHelper
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
    private var selectedHourOfDay = 0
    private var selectedMinute = 0

    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        dbHelper = TasksDatabaseHelper(requireContext())
        calendar = Calendar.getInstance()
        task = Task()
        val attachmentsList = mutableListOf<Attachment>()

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(3)) { uris ->
            for(u in uris.indices) {
                attachmentsList.add(Attachment(attachmentPath=uris[u].toString()))
                if(u == 0) binding.image1.setImageURI(uris[u])
                if(u == 1) binding.image2.setImageURI(uris[u])
                if(u == 2) binding.image3.setImageURI(uris[u])
            }

            task.attachments = attachmentsList
        }

        binding.addTaskButton.setOnClickListener {
            task = Task(
                taskTitle = binding.taskTitleInput.text.toString(),
                taskDescription = binding.taskDescriptionInput.text.toString(),
                taskCreationTime = LocalDate.now().toString(),
                taskExecutionDate = binding.taskExecutionDate.text.toString(),
                taskNotification = if(binding.taskNotificationToggle.isChecked) 1 else 0,
                taskCategory = "Category",
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
        selectedMonth = month
        selectedDayOfMonth = dayOfMonth
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        setSelectedTime(hourOfDay, minute)
        val selectedDate = convertDate(year, month, dayOfMonth, hourOfDay, minute)

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

    private fun setSelectedTime(hourOfDay: Int, minute: Int) {
        selectedHourOfDay = hourOfDay
        selectedMinute = minute
    }
}