package com.zetzaus.temiattend.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EmployeeLoginViewModel : ViewModel() {
    private val _recognizedUser = MutableLiveData<String>()
    val recognizedUser: LiveData<String> = _recognizedUser

    fun newUserRecognition(name: String) {
        _recognizedUser.value = name
    }
}