package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zetzaus.temiattend.database.Attendance
import com.zetzaus.temiattend.database.AttendanceRepository
import kotlinx.coroutines.launch
import java.util.*

class TemperatureViewModel @ViewModelInject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _temperatureLiveData = MutableLiveData<Float>()
    val temperatureLiveData: LiveData<Float> = _temperatureLiveData

    private val _userLiveData = MutableLiveData<String>()
    val userLiveData: LiveData<String> = _userLiveData

    fun updateTemperature(temperature: Float) {
        _temperatureLiveData.value = temperature
    }

    fun updateUser(user: String) {
        _userLiveData.value = user
    }

    /**
     * Saves the attendance to the local database.
     *
     * @param user The attending user.
     * @param temperature The user's temperature measurement.
     */
    fun recordAttendance(user: String, temperature: Float) = viewModelScope.launch {
        val attendance = Attendance(
            user = user,
            temperature = temperature,
            dateTime = Date()
        )

        repository.saveAttendance(attendance)
    }

}