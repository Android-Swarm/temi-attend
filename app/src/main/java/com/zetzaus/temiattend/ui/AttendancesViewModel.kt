package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.zetzaus.temiattend.database.AttendanceRepository

class AttendancesViewModel @ViewModelInject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val userLiveData = MutableLiveData<String>()
    val attendances = userLiveData.switchMap { user ->
        repository.getUserAttendances(user).asLiveData()
    }

    fun setUser(user: String) {
        userLiveData.value = user
    }
}