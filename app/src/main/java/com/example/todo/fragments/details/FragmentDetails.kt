package com.example.todo.fragments.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.todo.R
import com.example.todo.databinding.FragmentDetailsBinding


class FragmentDetails : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentDetailsArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
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


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}