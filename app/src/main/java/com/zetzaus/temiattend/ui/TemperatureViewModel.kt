package com.zetzaus.temiattend.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TemperatureViewModel : ViewModel() {
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

}