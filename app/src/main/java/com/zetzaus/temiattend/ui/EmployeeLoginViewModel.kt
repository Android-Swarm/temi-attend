package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.zetzaus.temiattend.database.AttendanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class EmployeeLoginViewModel @ViewModelInject constructor(
    repository: AttendanceRepository
) : AppViewModel(repository) {

    private val _recognizedUser = MutableLiveData<String>()
    val recognizedUser = _recognizedUser

    private val _errorInput = MutableLiveData<String>()
    val errorInput: LiveData<String> = _errorInput

    val abnormalAttendsLiveData = abnormalAttendances.asLiveData()

    /**
     * This method passes the user name which will be reflected in the `EditText`. Call this
     * method when user is recognized by means of external sources.
     *
     * @param name The user name to be input to the `EditText`.
     */
    fun newUserRecognition(name: String) = _recognizedUser.postValue(name)

    fun sendErrorInput(errorMessage: String) = _errorInput.postValue(errorMessage)


}