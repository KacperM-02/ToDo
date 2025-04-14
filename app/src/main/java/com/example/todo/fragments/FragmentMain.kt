package com.example.todo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.FragmentMainBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FragmentMain : Fragment() {

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        _binding!!.mainAddTaskFloatingButton.setOnClickListener {
            findNavController().navigate(R.id.FragmentMainToFragmentAddTaskAction)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}