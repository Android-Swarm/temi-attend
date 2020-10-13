package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zetzaus.temiattend.database.CameraDetails
import com.zetzaus.temiattend.ext.updatesTo
import com.zetzaus.temiattend.temperature.CameraDetailsFetcher
import kotlinx.coroutines.launch

class ConnectingFragmentViewModel @ViewModelInject constructor() : ViewModel() {

    var retryCount = 0
        get() = ++field
        private set

    private val _success = MutableLiveData<Boolean?>(null)
    val success: LiveData<Boolean?> = _success

    private val _cameraDetails = MutableLiveData<CameraDetails?>()
    val cameraDetails: LiveData<CameraDetails?> = _cameraDetails

    /**
     * Calls the Temi-IR API for the camera details.
     *
     * @param cameraMac The raw mac address.
     */
    fun requestCameraDetails(cameraMac: String) {
        viewModelScope.launch {
            val details = CameraDetailsFetcher(cameraMac)
                .cameraDetailsAsync(this)
                .await()

            _cameraDetails updatesTo details
        }
    }

    fun updateState(success: Boolean) = _success.postValue(success)
}