package com.zetzaus.temiattend.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ogawa.temiirsdk.IrDataUtil
import com.ogawa.temiirsdk.IrSensorUtil
import com.robotemi.sdk.TtsRequest
import com.zetzaus.temiattend.BuildConfig
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.CameraDetails
import com.zetzaus.temiattend.database.PreferenceRepository
import com.zetzaus.temiattend.ext.*
import com.zetzaus.temiattend.face.AzureFaceManager
import com.zetzaus.temiattend.face.NewPersonPayload
import com.zetzaus.temiattend.temperature.CameraDetailsFetcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.net.Socket

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MainActivityViewModel @ViewModelInject constructor(
    private val faceManager: AzureFaceManager,
    private val repository: PreferenceRepository,
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

    /**  Used to send temi TTS requests. */
    private val _temiTts = MutableLiveData<TtsRequest>()

    /** Observed for Temi TTS requests. */
    val temiTts: LiveData<TtsRequest> = _temiTts

    /** Used to send messages to be displayed in the snack bar.*/
    private val _snackBarMessage = MutableLiveData<String>()

    /** Observed to send messages to the user by snack bar.*/
    val snackBarMessage: LiveData<String> = _snackBarMessage

    /**
     * This will not be null when a new face is recognized. Used by the [EmployeeLoginFragment] to
     * register a new person to the Azure cloud.
     */
    var newPersonToRegister: NewPersonPayload? = null

    /** This application's data store flow. */
    private val preference = repository.preference.asLiveData()

    /** Thermal camera's distribution IP address stored in the data store. */
    val cameraIp = preference.map { it.cameraIp }

    /** Thermal camera's mac address stored in the data store. */
    val cameraMac = preference.map { it.cameraMac }

    /** Used to update whether the Temi-IR SDK is ready. */
    private val _sdkReady = ConflatedBroadcastChannel(false)

    /** Call this function when the SDK is ready. */
    fun sdkReady() = viewModelScope.launch { _sdkReady.send(true) }

    private val _cameraDetails = MutableLiveData<CameraDetails>()
    val cameraDetails: LiveData<CameraDetails> = _cameraDetails

    private lateinit var tempSocket: Socket
    private lateinit var tempReader: InputStream
    private lateinit var tempWriter: PrintWriter

    private val _battery = MutableLiveData("--")
    val battery: LiveData<String> = _battery

    val cameraWorking = _battery.asFlow()
        .combine(_sdkReady.asFlow()) { batteryText, isSdkReady ->
            batteryText != "--" && isSdkReady
        }.asLiveData()

    private val rawFrmOne = ConflatedBroadcastChannel<TmIrResponse.FRM>()
    private val rawFrmZero = ConflatedBroadcastChannel<TmIrResponse.FRM>()

    private val frmFlow = rawFrmOne.asFlow()
        .zip(rawFrmZero.asFlow()) { frmOne, frmZero -> Pair(frmZero, frmOne) }

    val thermalImage = frmFlow.map { (frmZero, frmOne) ->
        val rawTempValues = IrDataUtil.getTemperatureResult(frmZero.data, frmOne.data)!!

        val interpolated =
            IrDataUtil.setInterpolationAlgorithm(rawTempValues, 1)!!

        val colorList =
            IrDataUtil.getColorList(rawTempValues, interpolated, 1)!!

        // Remove gray color
        colorList.map { intArrayOf(it[1], it[2], it[3]) }.toTypedArray()
    }.catch { arrayOf(intArrayOf()) }
        .filter { it.isNotEmpty() }
        .map { colors ->
            val canvas = Bitmap.createBitmap(128, 96, Bitmap.Config.ARGB_8888)

            var index = 0

            (0 until canvas.width).forEach { x ->
                (0 until canvas.height).forEach { y ->
                    val rgbArray = colors[index++]
                    canvas.setPixel(
                        x,
                        y,
                        Color.rgb(rgbArray[0], rgbArray[1], rgbArray[2])
                    )
                }
            }

            canvas
        }.asLiveData()

    val temperature = frmFlow
        .map { (frmZero, frmOne) ->
            val distance = (frmZero.distance + frmOne.distance) / 2
            val temp = IrDataUtil.getResult(frmZero.body, frmOne.body) ?: -1.0

            Pair(temp, distance)
        }.filter { (temp, _) -> temp in 30.0..42.0 }
        .asLiveData()

    // Don't collect the flow if not needed, collect the flow together with frmFlow
    private val initialTempFlow = cameraMac.asFlow()
        .combine(cameraIp.asFlow()) { mac, ip -> Pair(mac, ip) }
        .filter { (mac, ip) -> mac.isNotBlank() && ip.isNotBlank() }
        .debounce(1000) // Delay for preference update
        .combine(_sdkReady.asFlow()) { (mac, ip), sdkReady -> Triple(mac, ip, sdkReady) }

    private val _isUserInteracting = ConflatedBroadcastChannel(false)
    val isUserInteracting = _isUserInteracting.asFlow()

    fun updateUserInteraction(interacting: Boolean) {
        viewModelScope.launch { _isUserInteracting.send(interacting) }
    }

    init {
        // Start polling for battery
        viewModelScope.launch {
            initialTempFlow.collect { (mac, ip, _) ->
                if (ip.isBlank() || mac.isBlank()) return@collect

                while (true) {
                    currentCoroutineContext().ensureActive()

                    pollBattery(mac, ip)

                    delay(10000)
                }
            }
        }

        // Get camera details
        viewModelScope.launch {
            cameraMac.asFlow()
                .filter { it.isNotBlank() }
                .map { mac ->
                    CameraDetailsFetcher(mac).cameraDetailsAsync(this).await().also {
                        if (it == null) throw Exception("Null camera details")
                    }
                }.retry(Long.MAX_VALUE) { e ->
                    Log.e(this@MainActivityViewModel.LOG_TAG, "Failed to get camera details: ", e)
                    delay(10000)
                    true
                }.collect {
                    _cameraDetails.postValue(it)
                }
        }
    }

    /**
     * Requests the thermal camera for its battery level once.
     *
     * @param mac The MAC address of the camera.
     * @param ip The IP address where the data is distributed at.
     */
    private fun pollBattery(mac: String, ip: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withSocketOperation(ip, BATTERY_PORT) { _, writer, reader ->
                    Log.d(this@MainActivityViewModel.LOG_TAG, "Connected to battery socket")
                    Log.d(this@MainActivityViewModel.LOG_TAG, "Updating battery")

                    // Send battery command
                    writer.println(BATTERY_COMMAND)

                    // Read battery response
                    val response =
                        (reader readBytesOfLength BATTERY_RESPONSE_LENGTH).decodeToString().also {
                            Log.d(this@MainActivityViewModel.LOG_TAG, "Response: $it")
                        }

                    // Update live data
                    val number = response.filter { it.isDigit() }.toFloat()
                    val battery = ((number - 800f) / 200f).setRange(0f, 1f)

                    _battery updatesTo String.format("%.1f", battery * 100) + "%"
                }
            } catch (e: IOException) {
                _battery updatesTo "--"

                fetchCameraDetails(ip, mac)
            }
        }

    private inline fun withSocketOperation(
        ip: String,
        port: Int,
        block: (Socket, PrintWriter, InputStream) -> Unit
    ) {
        Socket(ip, port).use { socket ->
            PrintWriter(socket.getOutputStream(), true).use { writer ->
                socket.getInputStream().use { out ->
                    block(socket, writer, out)
                }
            }
        }
    }

    suspend fun startTemperatureCollection(scope: CoroutineScope) {
        initialTempFlow.collect { (mac, ip, sdkReady) ->
            Log.d(this@MainActivityViewModel.LOG_TAG, "Collected from temperature flow")

            if (!sdkReady) {
                Log.d(this@MainActivityViewModel.LOG_TAG, "SDK not ready!")
            } else if (ip.isNotBlank()) {
                Log.d(
                    this@MainActivityViewModel.LOG_TAG,
                    "Temi-IR SDK is ready, and IP is found, trying to connect to socket"
                )

                startTemperatureTaking(scope, ip, mac)
            }
        }
    }

    /**
     * Calls the thermal camera's backend API to get the camera details. Also updates the
     * proto data store's camera ip and camera mac.
     *
     * @param ip The failing IP address.
     * @param mac The camera's MAC address.
     */
    private suspend fun fetchCameraDetails(ip: String, mac: String) = viewModelScope.launch {
        if (mac.isBlank()) return@launch

        Log.d(
            this@MainActivityViewModel.LOG_TAG,
            "Failed to connect to socket at $ip, re-fetching camera details"
        )

        try {
            val newDetails = CameraDetailsFetcher(mac).cameraDetailsAsync(this).await()!!
            repository.saveCameraIp(newDetails.deviceIp)
            repository.saveCameraMac(newDetails.macAddress)
        } catch (e: Exception) {
            Log.e(this@MainActivityViewModel.LOG_TAG, "Unable to refetch camera details: ", e)

            delay(10000)

            // Retry to connect
            viewModelScope.launch { _sdkReady.send(_sdkReady.value) }
        }
    }

    private fun startTemperatureTaking(scope: CoroutineScope, ip: String, mac: String) =
        scope.launch(Dispatchers.IO) {
            try {
                tempSocket = Socket(ip, TEMP_PORT)
            } catch (e: IOException) {
                fetchCameraDetails(ip, mac)
                // This should re-trigger the `temperature` flow

                return@launch
            }

            Log.d(this@MainActivityViewModel.LOG_TAG, "Connected to socket at $ip")
            tempWriter = PrintWriter(tempSocket.getOutputStream(), true)
            tempReader = tempSocket.getInputStream()

            // Analyse and correct temperature calculation parameters
            try {
                do {
                    val eem = readThermalData<TmIrResponse.EEM>(tempReader) {
                        it.println(TEMP_COMMAND) // Need to issue the command to get EEM
                        Log.d(
                            this@MainActivityViewModel.LOG_TAG,
                            "Sent first temperature read command"
                        )
                    }

                    val correction = eem.correction
                    if (correction != 0) {
                        Log.d(
                            this@MainActivityViewModel.LOG_TAG,
                            "Failed to correct temperature measurement! Correction: $correction, " +
                                    "eem data length: ${eem.data.size}"
                        )
                    } else {
                        Log.d(
                            this@MainActivityViewModel.LOG_TAG,
                            "Successfully corrected temperature measurement!"
                        )
                    }
                } while (correction != 0)

                // Fetch FRM pairs
                while (true) {
                    currentCoroutineContext().ensureActive()

                    val frmArray = mutableMapOf<Int, TmIrResponse.FRM>()

                    // FRM 1
                    var frmOne: TmIrResponse.FRM

                    do {
                        frmOne = readThermalData(tempReader)
                    } while (
                        frmOne.data.isEmpty() ||
                        frmOne.frameNo !in listOf(0, 1) ||
                        frmOne.distance == 0
                    )

                    frmArray[frmOne.frameNo] = frmOne

                    Log.d(
                        this@MainActivityViewModel.LOG_TAG,
                        "Success for FRM 1: frame ${frmOne.frameNo} with distance ${frmOne.distance}"
                    )

                    // FRM 2
                    var frmTwo: TmIrResponse.FRM

                    do {
                        frmTwo = readThermalData(tempReader)
                    } while (
                        frmTwo.data.isEmpty() ||
                        frmTwo.frameNo !in listOf(0, 1) ||
                        frmTwo.distance == 0 ||
                        frmTwo.frameNo == frmOne.frameNo
                    )

                    frmArray[frmTwo.frameNo] = frmTwo

                    Log.d(
                        this@MainActivityViewModel.LOG_TAG,
                        "Success for FRM 2: frame ${frmTwo.frameNo} with distance ${frmTwo.distance}"
                    )

                    viewModelScope.launch {
                        rawFrmZero.send(frmArray.getValue(0))
                        rawFrmOne.send(frmArray.getValue(1))
                    }
                }
            } catch (e: CancellationException) {
                Log.d(this@MainActivityViewModel.LOG_TAG, "Stopping due to coroutine cancellation")
                closeCameraSocket()
            } catch (e: IllegalStateException) {
                restartTempSocket()
            } catch (e: Exception) {
                Log.e(
                    this@MainActivityViewModel.LOG_TAG,
                    "Restarting socket! Encountered exception ",
                    e
                )

                restartTempSocket()
            }
        }

    private fun restartTempSocket() {
        Log.e(this@MainActivityViewModel.LOG_TAG, "Wrong starting point! Restarting")

        closeCameraSocket()

        sdkReady()
    }

    private fun closeCameraSocket() {
        tempReader.close()
        tempWriter.close()
        tempSocket.close()
    }

    fun saveCameraDetails(cameraMac: String, cameraIp: String) = viewModelScope.launch {
        repository.saveCameraIp(cameraIp)
        repository.saveCameraMac(cameraMac)
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
    private inline fun <reified T : TmIrResponse> readThermalData(
        reader: InputStream,
        request: (PrintWriter) -> Unit = {}
    ): T {
        var result: TmIrResponse?

        do {
            request(tempWriter)

            result = readSocketForTemp(reader)
                .also { response ->
                    Log.d(
                        LOG_TAG,
                        "${response::class.simpleName} received with length ${response.body.length}"
                    )
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
     *
     * @throws IllegalStateException If the starting header is not EEM's or FRM's. **Do note that
     * this happens pretty often.**
     */
    private fun readSocketForTemp(reader: InputStream): TmIrResponse {
        // Read header
        val header = reader readBytesOfLength 7

        return when {
            header.contentEquals(byteArrayOf(0x4d, 0x4c, 0x58, 0x5f, 0x46, 0x52, 0x4d)) -> {
                // FRM data
                val data = reader readBytesOfLength 1741

                // BIG NOTE: MAKE SURE IT IS UPPERCASE!!!!!!!!!!!!!!!!!!! DANG IT U DOCU NOT SAYING ANYTHING
                TmIrResponse.FRM(data.toHexString())
            }

            header.contentEquals(byteArrayOf(0x4d, 0x4c, 0x58, 0x5f, 0x45, 0x45, 0x4d)) -> {
                // EEM data
                val data = reader readBytesOfLength 1666

                // BIG NOTE: MAKE SURE IT IS UPPERCASE!!!!!!!!!!!!!!!!!!! DANG IT U DOCU NOT SAYING ANYTHING
                TmIrResponse.EEM(data.toHexString())
            }

            else -> {
                Log.e(
                    LOG_TAG,
                    "Reading from the wrong starting point! result=${header.toHexString()}"
                )

                throw IllegalStateException("Wrong starting point!")
            }
        }
    }

    /**
     * Wrapper class for EEM or FRM data.
     *
     * @property body The body of the response. In other words, the raw data with the header dropped.
     */
    sealed class TmIrResponse(val body: String) {

        abstract val data: IntArray

        /** Wrapper class for FRM. */
        class FRM(body: String) : TmIrResponse(body) {
            override val data = IrDataUtil.getFRMData(body) ?: intArrayOf()

            val distance = IrDataUtil.getDistanceData(body) ?: 0

            val frameNo = IrDataUtil.getFrmNo(data) ?: -1
        }

        /** Wrapper class for EEM. */
        class EEM(body: String) : TmIrResponse(body) {
            override val data = IrDataUtil.getEEMData(body) ?: intArrayOf()

            val correction = IrSensorUtil.mlx90640ExtractParameters(data) ?: -1
        }
    }

    companion object {
        const val PRIVATE_BROADCAST_PERMISSION = "com.zetzaus.temiattend.PRIVATE_BROADCAST"
        const val ACTION_BROADCAST_USER = BuildConfig.APPLICATION_ID + "_BROADCAST_USER"
        const val EXTRA_USER_ID = "user_id"

        const val TEMP_PORT = 5001
        const val BATTERY_PORT = 5002

        const val BATTERY_COMMAND = "AT+GETBATTVOLT"
        const val BATTERY_RESPONSE_LENGTH = "+OK=1000".length
        const val TEMP_COMMAND = "\$SETP=17,4"

        fun retrieveBroadcastUserId(intent: Intent) = intent.getStringExtra(EXTRA_USER_ID) ?: ""

    }
}