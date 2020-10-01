package com.zetzaus.temiattend.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zetzaus.temiattend.BuildConfig
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.toRequestBody
import com.zetzaus.temiattend.face.AzureFaceManager
import com.zetzaus.temiattend.face.NewPersonPayload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.Socket

@OptIn(
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
    kotlinx.coroutines.FlowPreview::class
)
class MainActivityViewModel @ViewModelInject constructor(
    private val faceManager: AzureFaceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val maskDetectionChannel = BroadcastChannel<Boolean>(60)
    val maskDetected = maskDetectionChannel.asFlow()
        .distinctUntilChanged() // From rapid frames, emit only if the incoming value is different than the last emitted value
        .debounce(1000) // Emit if the value doesn't change for 1s
        .asLiveData()

    private val _startFaceRecognition = MutableLiveData<Boolean>()
    val startFaceRecognition: LiveData<Boolean> = _startFaceRecognition // Start camera

    private val _snackBarMessage = MutableLiveData<String>()
    val snackBarMessage: LiveData<String> = _snackBarMessage // Display SnackBar with message

    var newPersonToRegister: NewPersonPayload? = null

//    init {
//        val wifiManager = ContextCompat.getSystemService(context, WifiManager::class.java)!!
//
//        val wifiName = "TP-Link_8AA4"
////        val wifiName = "Random"
//        val wifiPwd = "herkaus999"
////        val wifiPwd = "Stuffs"
//
//        viewModelScope.launch(Dispatchers.IO) {
//            // First socket
//            Socket("10.10.100.1", 5000).use {
//                Log.d("Socket", "Connected to temi-ir socket")
//
//                val msg = "AT+SETWLAN=$wifiName,$wifiPwd"  // Received reply, but backend side seems to not change targetSSID
//
//                PrintWriter(it.getOutputStream(), true).use { writer ->
//                    writer.println(msg)
//                    Log.d("Socket", "Sent WLAN info to temi-ir")
//
//                    BufferedReader(InputStreamReader(it.getInputStream())).use { reader ->
//                        Log.d("Socket", "About to read WLAN info reply")
//
//                        delay(10000)
//
//                        // Don't use readline() as it is not delimited by newline
//                        val response = mutableListOf<Char>()
//                        var currentChar = reader.read()
//                        while (currentChar != -1) {
//                            response.add(currentChar.toChar())
//
//                            if (response.size == 21) break
//
//                            currentChar = reader.read()
//                        }
//
//                        Log.d("Socket", "WLAN info reply: ${response.joinToString("")}")
//                        // Pass data through live data(?) then call the API to get device IP
//
//                        writer.println("AT+RSTWLAN")
//                        Log.d("Socket", "Sent RESET WLAN command to temi-ir")
//
//                        delay(10000)
//                        Log.d("Socket", "Delayed for 3 seconds plus")
//                    }
//                }
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                WifiNetworkSuggestion.Builder()
//                    .setSsid(wifiName)
//                    .setWpa2Passphrase(wifiPwd)
//                    .build()
//                    .run { wifiManager.addNetworkSuggestions(listOf(this)) }
//
//                Log.d("Socket", "Sent network suggestion")
//            } else {
//                val wifiConfig = WifiConfiguration().apply {
//                    SSID = "\"$wifiName\""
//                    preSharedKey = "\"$wifiPwd\""
//                }
//
//                val id = wifiManager.addNetwork(wifiConfig)
//                wifiManager.enableNetwork(id, true)
//            }
//
//            Log.d("Socket", "Supposedly android should connect to the original wifi")
//
//            delay(10000)
//            Log.d("Socket", "Delaying 10000 (just to wait until wifi is connected)")
//            // Retrieving IP address
//            URL("https://roboapi-intl.cozzia.com.cn/api/temi/v1/ir?mac=520291E24E53")
//                .readText()
//                .also { Log.d("Socket", it) }
//
//            // Port 5001 -> read temperature
//            // Port 5002 -> read battery voltage
//
//            //Second socket
//            val tempSocket = retrySocket5001()
//            PrintWriter(tempSocket.getOutputStream(), true).use { writer ->
//                writer.println("\$SETP=17,4")
//
//                delay(1000)
//                BufferedReader(InputStreamReader(tempSocket.getInputStream())).use {
//                    val response = mutableListOf<Char>()
//                    (1..7).forEach { _ -> response.add(it.read().toChar()) }
//                    Log.d("Socket", "Battery level: ${response.joinToString("")}")
//                }
//            }
//
//
//        }
//    }

    private fun retrySocket5001(retry: Int = 6): Socket = try {
        Socket("192.168.43.45", 5001)
    } catch (e: Exception) {
        if (retry > 0) {
            Log.d("Socket", "Socket connect failed, retries left $retry")
            retrySocket5001(retry - 1)
        } else {
            throw e
        }
    }

    suspend fun updateMaskDetection(isWearingMask: Boolean) =
        maskDetectionChannel.send(isWearingMask)

    fun updateFaceRecognitionState(state: Boolean) {
        _startFaceRecognition.value = state
    }

    fun detect(photoData: ByteArray) =
        viewModelScope.launch {
            val requestBody = photoData.toRequestBody(AzureFaceManager.PHOTO_PAYLOAD_TYPE)

            val detectedFace = faceManager.detectFaces(requestBody).also {
                Log.d(this@MainActivityViewModel.LOG_TAG, it.joinToString(";"))
            }.firstOrNull()

            if (detectedFace == null) {
                showSnackBar(context.getString(R.string.snack_bar_no_face))
                return@launch
            }

            try {
                val candidate = faceManager.identifyFaces(listOf(detectedFace.faceId))
                    .first().candidates
                    .also {
                        Log.d(this@MainActivityViewModel.LOG_TAG, it.joinToString(";"))
                    }.firstOrNull()

                if (candidate == null) {
                    showSnackBar(context.getString(R.string.snack_bar_new_face))
                    newPersonToRegister =
                        NewPersonPayload(null, requestBody, detectedFace.faceRectangle)
                    return@launch
                }

                faceManager.getPerson(candidate.personId).run {
                    Log.d(this@MainActivityViewModel.LOG_TAG, "Recognized person: $name")
                    broadcastRecognizedUser(name)
                }

            } catch (e: HttpException) {
                faceManager.parseError(e).run {
                    String.format(context.getString(R.string.snack_bar_error), message).run {
                        showSnackBar(this)
                    }

                    Log.e(this@MainActivityViewModel.LOG_TAG, "$code : $message", e)
                }
            }
        }

    fun saveNewPerson(newPersonPayload: NewPersonPayload) =
        viewModelScope.launch {
            snackBarAndLogOnHttpError {
                val newPerson = faceManager.createPerson(newPersonPayload.user!!)
                Log.d(
                    this@MainActivityViewModel.LOG_TAG,
                    "Created the person with id ${newPerson.personId}"
                )

                faceManager.addFaceToPerson(
                    photoData = newPersonPayload.photoData!!,
                    personId = newPerson.personId,
                    targetFace = newPersonPayload.rect!!
                )
                Log.d(this@MainActivityViewModel.LOG_TAG, "Added face to ${newPersonPayload.user}")

                faceManager.trainPersonGroup()
                Log.d(this@MainActivityViewModel.LOG_TAG, "Training person group")
            }
        }

    private suspend fun snackBarAndLogOnHttpError(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: HttpException) {
            faceManager.parseError(e).run {
                showSnackBar("Encountered error: $message")
                Log.e(this@MainActivityViewModel.LOG_TAG, "$code : $message", e)
            }
        }
    }

    private fun showSnackBar(message: String) {
        _snackBarMessage.value = message
    }

    private fun broadcastRecognizedUser(name: String) =
        Intent(ACTION_BROADCAST_USER)
            .putExtra(EXTRA_USER_ID, name)
            .run {
                context.sendBroadcast(this, PRIVATE_BROADCAST_PERMISSION)
            }


//    class Factory(private val faceManager: AzureFaceManager, private val context: Context) :
//        ViewModelProvider.NewInstanceFactory() {
//
//        @Suppress("UNCHECKED_CAST")
//        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
//                return MainActivityViewModel(faceManager, context) as T
//            }
//
//            return super.create(modelClass)
//        }
//    }

    companion object {
        const val PRIVATE_BROADCAST_PERMISSION = "com.zetzaus.temiattend.PRIVATE_BROADCAST"
        const val ACTION_BROADCAST_USER = BuildConfig.APPLICATION_ID + "_BROADCAST_USER"
        const val EXTRA_USER_ID = "user_id"

        fun retrieveBroadcastUserId(intent: Intent) = intent.getStringExtra(EXTRA_USER_ID) ?: ""

    }
}