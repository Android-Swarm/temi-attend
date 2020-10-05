package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zetzaus.temiattend.database.AttendanceRepository

class TemperatureViewModel @ViewModelInject constructor(
    repository: AttendanceRepository
) : AppViewModel(repository) {

    private val _temperatureLiveData = MutableLiveData<Float>()
    val temperatureLiveData: LiveData<Float> = _temperatureLiveData

    fun updateTemperature(temperature: Float) = _temperatureLiveData.postValue(temperature)

}