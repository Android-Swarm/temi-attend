package com.zetzaus.temiattend.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ogawa.temiirsdk.IrDataUtil
import com.ogawa.temiirsdk.IrSensorUtil
import com.robotemi.sdk.TtsRequest
import com.zetzaus.temiattend.BuildConfig
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.PreferenceRepository
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.toHexString
import com.zetzaus.temiattend.ext.toRequestBody
import com.zetzaus.temiattend.face.AzureFaceManager
import com.zetzaus.temiattend.face.NewPersonPayload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.InputStream
import java.io.PrintWriter
import java.net.Socket

@OptIn(
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
    kotlinx.coroutines.FlowPreview::class
)
class MainActivityViewModel @ViewModelInject constructor(
    private val faceManager: AzureFaceManager,
    repository: PreferenceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** Channel to update when mask become (not) detected. */
    private val maskDetectionChannel = BroadcastChannel<Boolean>(60)

    /** Observed to update the mask icon's visibility.*/
    val maskDetected = maskDetectionChannel.asFlow()
        .distinctUntilChanged() // From rapid frames, emit only if the incoming value is different than the last emitted value
        .debounce(2000) // Emit if the value doesn't change for 2s
        .asLiveData()

    /** Used to update if face recognition should be started. */
    private val _startFaceRecognition = MutableLiveData(false)

    /** Observed to update the visibility of the camera for face recognition.*/
    val startFaceRecognition: LiveData<Boolean> = _startFaceRecognition

    private val _isRecognizing = MutableLiveData(false)
    val isRecognizing: LiveData<Boolean> = _isRecognizing

    private val _temiTts = MutableLiveData<TtsRequest>()
    val temiTts: LiveData<TtsRequest> = _temiTts

    /** Used to send messages to be displayed in the snack bar.*/
    private val _snackBarMessage = MutableLiveData<String>()

    /** Observed to send messages to the user by snack bar.*/
    val snackBarMessage: LiveData<String> = _snackBarMessage

    /** This will not be null when a new face is recognized. Used by the [EmployeeLoginFragment] to
     * register a new person to the Azure cloud.
     */
    var newPersonToRegister: NewPersonPayload? = null

    private val preference = repository.preference.asLiveData()
    val cameraIp = preference.map { it.cameraIp }
    val cameraMac = preference.map { it.cameraMac }

    private lateinit var tempSocket: Socket
    private lateinit var tempReader: InputStream
    private lateinit var tempWriter: PrintWriter

    init {
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
        //Second socket
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
    }
//
//    private fun retrySocket5001(retry: Int = 6): Socket = try {
//        Socket("192.168.43.45", 5001)
//    } catch (e: Exception) {
//        if (retry > 0) {
//            Log.d("Socket", "Socket connect failed, retries left $retry")
//            retrySocket5001(retry - 1)
//        } else {
//            throw e
//        }
//    }

    fun startTemperatureTaking() = viewModelScope.launch(Dispatchers.IO) {
        tempSocket = Socket(DEVICE_IP, 5001)
        tempWriter = PrintWriter(tempSocket.getOutputStream(), true)
        tempReader = tempSocket.getInputStream()

        val eemRaw = readThermalData<TmIrResponse.EEM> {
            it.println(TEMP_COMMAND) // Need to issue the command to get EEM
            Log.d(LOG_TAG, "Sent temperature read command")
        }

        val eemData = IrDataUtil.getEEMData(eemRaw.body)
        eemData?.let {
            if (IrSensorUtil.mlx90640ExtractParameters(it) != 0) {
                Log.d(
                    this@MainActivityViewModel.LOG_TAG,
                    "Failed to correct temperature measurement!"
                )
                return@launch
            } else {
                Log.d(
                    this@MainActivityViewModel.LOG_TAG,
                    "Successfully corrected temperature measurement!"
                )
            }
        } ?: Log.d(this@MainActivityViewModel.LOG_TAG, "EEM data is null!")

        // FRM 1
        var frmOneRaw: TmIrResponse.FRM
        var frmOneData: IntArray?
        var frmOneNo = -1
        var frmOneDistance: Int

        do {
            Log.d(this@MainActivityViewModel.LOG_TAG, "Attempting to fetch first FRM")
            frmOneRaw = readThermalData()
            frmOneData = IrDataUtil.getFRMData(frmOneRaw.body)
            frmOneDistance = IrDataUtil.getDistanceData(frmOneRaw.body) ?: 0
            frmOneData?.let {
                frmOneNo = IrDataUtil.getFrmNo(it) ?: -1
            } ?: Log.d(this@MainActivityViewModel.LOG_TAG, "FRM 1 is null!")
        } while (frmOneData == null || frmOneNo !in listOf(0, 1) || frmOneDistance == 0)

        Log.d(
            this@MainActivityViewModel.LOG_TAG,
            "Success for FRM 1: frame $frmOneNo with distance $frmOneDistance"
        )

        // FRM 2
        var frmTwoRaw: TmIrResponse.FRM
        var frmTwoData: IntArray?
        var frmTwoNo = -1
        var frmTwoDistance: Int

        do {
            Log.d(this@MainActivityViewModel.LOG_TAG, "Attempting to fetch second FRM")
            frmTwoRaw = readThermalData()
            frmTwoData = IrDataUtil.getFRMData(frmTwoRaw.body)
            frmTwoDistance = IrDataUtil.getDistanceData(frmTwoRaw.body) ?: 0
            frmTwoData?.let {
                frmTwoNo = IrDataUtil.getFrmNo(it) ?: -1
            } ?: Log.d(this@MainActivityViewModel.LOG_TAG, "FRM 2 is null!")
        } while (
            frmTwoData == null ||
            frmTwoNo !in listOf(0, 1) ||
            frmTwoDistance == 0 ||
            frmTwoNo == frmOneNo
        )

        Log.d(
            this@MainActivityViewModel.LOG_TAG,
            "Success for FRM 2: frame $frmOneNo with distance $frmOneDistance"
        )

        val temperature = IrDataUtil.getResult(
            if (frmOneNo == 0) frmOneRaw.body else frmTwoRaw.body,
            if (frmTwoNo == 1) frmTwoRaw.body else frmOneRaw.body
        )

        Log.d(LOG_TAG, "Measured temperature: $temperature")
    }

    fun requestTemiSpeak(text: String, appear: Boolean = true) =
        _temiTts.postValue(TtsRequest.create(text, appear))

    fun updateMaskDetection(isWearingMask: Boolean) =
        viewModelScope.launch { maskDetectionChannel.send(isWearingMask) }

    fun updateFaceRecognitionState(state: Boolean) = _startFaceRecognition.postValue(state)

    fun detectAndRecognize(photoData: ByteArray) =
        viewModelScope.launch {
            withLoadingLiveData(_isRecognizing) {

                val requestBody = photoData.toRequestBody(AzureFaceManager.PHOTO_PAYLOAD_TYPE)

                val detectedFace = faceManager.detectFaces(requestBody).also {
                    Log.d(this@MainActivityViewModel.LOG_TAG, it.joinToString(";"))
                }.firstOrNull()

                if (detectedFace == null) {
                    showSnackBar(context.getString(R.string.snack_bar_no_face))
                    return@withLoadingLiveData
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
                        return@withLoadingLiveData
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
        }

    private suspend fun withLoadingLiveData(
        loading: MutableLiveData<Boolean>,
        block: suspend () -> Unit
    ) =
        try {
            loading.value = true
            block()
        } finally {
            loading.value = false
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

    fun showSnackBar(message: String) {
        _snackBarMessage.value = message
    }

    private fun broadcastRecognizedUser(name: String) =
        Intent(ACTION_BROADCAST_USER)
            .putExtra(EXTRA_USER_ID, name)
            .run {
                context.sendBroadcast(this, PRIVATE_BROADCAST_PERMISSION)
            }

    /**
     * Requests for either an EEM data or FRM data. The method will keep dropping frames that are not
     * of the requested type.
     *
     * @param T The type of data to be retrieved.
     * @param request A lambda function for sending commands to the socket. Make sure to use this
     *              parameter when requesting for an EEM data.
     *
     * @return An EEM or FRM data.
     */
    private inline fun <reified T : TmIrResponse> readThermalData(request: (PrintWriter) -> Unit = {}): T {
        var result: TmIrResponse

        do {
            request(tempWriter)

            result = readSocketForTemp(tempSocket.getInputStream())
                .also { response ->
                    Log.d(LOG_TAG, "${response::class.simpleName}: ${response.body}")
                }
        } while (result !is T)

        return result
    }

    /**
     * Reads temperature data from the socket's [InputStream]. The data can be of 2 type: EEM or FRM.
     * Each can be uniquely identified by the 7 bytes header.
     *
     * @param reader The temperature socket input stream.
     *
     * @return EEM or FRM data.
     */
    private fun readSocketForTemp(reader: InputStream): TmIrResponse {
        val header = ByteArray(7)

        // Read header
        reader.read(header, 0, 7)

        return when {
            header.contentEquals(byteArrayOf(0x4d, 0x4c, 0x58, 0x5f, 0x46, 0x52, 0x4d)) -> {
                // FRM data
                val data = ByteArray(1741)
                reader.read(data, 0, 1741)
                TmIrResponse.FRM(data.toHexString())
            }

            header.contentEquals(byteArrayOf(0x4d, 0x4c, 0x58, 0x5f, 0x45, 0x45, 0x4d)) -> {
                // EEM data
                val data = ByteArray(1666)
                reader.read(data, 0, 1666)
                TmIrResponse.EEM(data.toHexString())
            }

            else -> {
                Log.e(
                    LOG_TAG,
                    "Reading from the wrong starting point! result=${
                        header.joinToString("") {
                            it.toString(
                                16
                            )
                        }
                    }"
                )

                throw Exception("Wrong starting point!")
            }
        }
    }

    /**
     * Wrapper class for EEM or FRM data.
     *
     * @property body The body of the response. In other words, the raw data with the header dropped.
     */
    sealed class TmIrResponse(val body: String) {
        /** Wrapper class for FRM. */
        class FRM(body: String) : TmIrResponse(body)

        /** Wrapper class for EEM. */
        class EEM(body: String) : TmIrResponse(body)
    }

    companion object {
        const val PRIVATE_BROADCAST_PERMISSION = "com.zetzaus.temiattend.PRIVATE_BROADCAST"
        const val ACTION_BROADCAST_USER = BuildConfig.APPLICATION_ID + "_BROADCAST_USER"
        const val EXTRA_USER_ID = "user_id"

        const val DEVICE_IP = "192.168.43.45"
        const val BATTERY_COMMAND = "AT+GETBATTVOLT"
        const val BATTERY_RESPONSE_LENGTH = "+OK=800".length
        const val TEMP_COMMAND = "\$SETP=17,4"

        fun retrieveBroadcastUserId(intent: Intent) = intent.getStringExtra(EXTRA_USER_ID) ?: ""

    }
}