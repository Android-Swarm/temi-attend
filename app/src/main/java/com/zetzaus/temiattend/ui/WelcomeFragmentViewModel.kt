package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zetzaus.temiattend.database.PreferenceRepository

class WelcomeFragmentViewModel @ViewModelInject constructor(
    private val repository: PreferenceRepository
) : ViewModel() {
    val trial = MutableLiveData(false)
}