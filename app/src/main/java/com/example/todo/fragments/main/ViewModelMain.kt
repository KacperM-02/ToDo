package com.example.todo.fragments.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todo.data.sharedPreferences.HideDoneTasksPreferences

class ViewModelMain : ViewModel() {
    private val _hideCompletedTasks = MutableLiveData<Boolean>()
    val hideCompletedTasks: LiveData<Boolean> = _hideCompletedTasks

    private val _inputtedText = MutableLiveData<String?>()
    val inputtedText: LiveData<String?> = _inputtedText

    fun initValues(context: Context) {
        _hideCompletedTasks.value = HideDoneTasksPreferences.loadHideDoneTasks(context)
    }

    // hideCompletedTasks
    fun toggleHideCompletedTasks(context: Context) {
        _hideCompletedTasks.value = !_hideCompletedTasks.value!!
        hideCompletedTasks.value?.let {
            HideDoneTasksPreferences.setHideDoneTasks(
                context,
                it
            )
        }
    }

    // inputtedText
    fun updateInputtedText(newText: String?) {
        _inputtedText.value = newText
    }
}