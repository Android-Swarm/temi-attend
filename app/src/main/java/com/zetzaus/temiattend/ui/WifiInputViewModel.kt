package com.zetzaus.temiattend.ui

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zetzaus.temiattend.ext.*
import com.zetzaus.temiattend.temperature.withSocketOperation
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class WifiInputViewModel @ViewModelInject constructor() : ViewModel() {

    private val ssidChannel = ConflatedBroadcastChannel<String>()
    private val passwordChannel = ConflatedBroadcastChannel<String>()
    private val credential = ssidChannel.asFlow().zip(passwordChannel.asFlow()) { ssid, pass ->
        Pair(ssid, pass)
    }.take(1)

    private val _isConnecting = MutableLiveData(false)
    val isConnecting: LiveData<Boolean> = _isConnecting

    private val _macAddress = MutableLiveData("")
    val macAddress: LiveData<String> = _macAddress

    private val _ssidError = MutableLiveData<String>()
    val ssidError: LiveData<String> = _ssidError

    private val _socketError = MutableLiveData<String?>()
    val socketError: LiveData<String?> = _socketError

    init {
        viewModelScope.launch {
            credential.collect { (ssid, pass) ->
                _isConnecting.value = true

                try {
                    resetWlanCamera(ssid, pass)
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Encountered exception on first socket: ", e)
                } finally {
                    _isConnecting.value = false
                }
            }
        }
    }

    fun submitCredentials(ssid: String, password: String) =
        viewModelScope.launch {
            ssidChannel.send(ssid)
            passwordChannel.send(password)
        }

    fun submitSsidError(error: String) = _ssidError.postValue(error)

    private suspend fun resetWlanCamera(ssid: String, password: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _isConnecting updatesTo true

            try {
                withSocketOperation(FIRST_SOCKET_IP, FIRST_SOCKET_PORT) { _, writer, reader ->
                    writer.writeString(createConnectCommand(ssid, password))
                    Log.d(this@WifiInputViewModel.LOG_TAG, "Sent SETWLAN command")

                    val macResponse = reader readStringLength "+OK=AA:BB:CC:DD:EE:FF".length
                    Log.d(this@WifiInputViewModel.LOG_TAG, "Response: $macResponse")

                    writer.writeString(RESET_WLAN_COMMAND)
                    Log.d(this@WifiInputViewModel.LOG_TAG, "Sent RSTWLAN command")

                    delay(2000)

                    _macAddress updatesTo macResponse.drop(4)
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Encountered exception on first socket: ", e)
                _socketError updatesTo e.localizedMessage
            } finally {
                _isConnecting updatesTo false
            }
        }

    private fun createConnectCommand(ssid: String, password: String) =
        "AT+SETWLAN=${ssid.escapeCharForCamera()},${password.escapeCharForCamera()}"

    companion object {
        const val FIRST_SOCKET_IP = "10.10.100.1"
        const val FIRST_SOCKET_PORT = 5000
        const val RESET_WLAN_COMMAND = "AT+RSTWLAN"
    }
}