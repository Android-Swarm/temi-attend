package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.zetzaus.temiattend.database.AttendanceRepository
import com.zetzaus.temiattend.database.PreferenceRepository
import kotlinx.coroutines.flow.combine

class TemperatureViewModel @ViewModelInject constructor(
    attendanceRepo: AttendanceRepository,
    preferenceRepo: PreferenceRepository
) : AppViewModel(attendanceRepo) {

    private val _temperatureLiveData = MutableLiveData<Float>()
    val temperatureLiveData: LiveData<Float> = _temperatureLiveData

    private val preference = preferenceRepo.preference

    val attendanceToSave =
        preference.combine(temperatureLiveData.asFlow()) { preference, temperature ->
            Pair(preference.location, temperature)
        }.asLiveData()

    fun updateTemperature(temperature: Float) = _temperatureLiveData.postValue(temperature)


}