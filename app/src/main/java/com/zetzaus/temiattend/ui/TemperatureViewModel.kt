package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.zetzaus.temiattend.database.AttendanceRepository
import com.zetzaus.temiattend.database.PreferenceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TemperatureViewModel @ViewModelInject constructor(
    attendanceRepo: AttendanceRepository,
    preferenceRepo: PreferenceRepository
) : AppViewModel(attendanceRepo) {
    private val tempCollector = MutableLiveData(Pair(0, 0.0))
    val averageTemperature: LiveData<Pair<Int, Double>> = tempCollector

    private val _temperatureLiveData = MutableLiveData<Float>()
    val temperatureLiveData: LiveData<Float> = _temperatureLiveData

    private val preference = preferenceRepo.preference

    val attendanceToSave =
        preference.combine(temperatureLiveData.asFlow()) { preference, temperature ->
            Pair(preference.location, temperature)
        }.take(1)

    fun finalizeTemperature(temperature: Float) = _temperatureLiveData.postValue(temperature)

    fun sendTemperature(temperature: Double) = tempCollector.postValue(
        Pair(
            (tempCollector.value?.first ?: 0) + 1,
            (tempCollector.value?.second ?: 0.0) + temperature
        )
    )


}