package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zetzaus.temiattend.database.CameraApiResponse
import com.zetzaus.temiattend.database.CameraDetails
import com.zetzaus.temiattend.ext.parseJson
import com.zetzaus.temiattend.ext.updatesTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class ConnectingFragmentViewModel @ViewModelInject constructor() : ViewModel() {

    var retryCount = 0
        get() = ++field
        private set

    private val _success = MutableLiveData<Boolean?>(null)
    val success: LiveData<Boolean?> = _success

    private val _cameraDetails = MutableLiveData<CameraDetails>()
    val cameraDetails: LiveData<CameraDetails> = _cameraDetails

    /**
     * Calls the Temi-IR API for the camera details.
     *
     * @param cameraMac The raw mac address.
     */
    fun requestCameraDetails(cameraMac: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = URL(createEndpoint(cameraMac))
                .readText()
                .parseJson<CameraApiResponse>()

            _cameraDetails updatesTo response.data
        }
    }

    fun updateState(success: Boolean) = _success.postValue(success)

    companion object {
        private const val API_ENDPOINT = "https://roboapi-intl.cozzia.com.cn/api/temi/v1/ir?mac="

        /**
         * Returns the complete API URL as [String].
         *
         * @param mac The raw mac address.
         */
        private fun createEndpoint(mac: String) = API_ENDPOINT + mac.replace(":", "")
    }
}