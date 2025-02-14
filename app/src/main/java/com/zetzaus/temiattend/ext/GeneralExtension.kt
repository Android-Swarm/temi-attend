package com.zetzaus.temiattend.ext

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.*
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.google.mlkit.vision.face.Face
import com.otaliastudios.cameraview.frame.Frame
import com.robotemi.sdk.Robot
import com.zetzaus.temiattend.ui.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Returns the simple name of the class, suitable to be used for log tags.
 */
val Any.LOG_TAG: String
    get() = this::class.java.simpleName

/**
 * Returns temi robot instance.
 */
val Activity.robot: Robot
    get() = Robot.getInstance()

/**
 * Returns the current navigation destination id.
 */
val MainActivity.currentDestinationId
    get() = navHostFragment.findNavController().currentDestination?.id

/**
 * Returns `true` if the application is in dark theme.
 */
val Activity.isNightMode
    get() = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

/**
 * Checks if all permissions in the list has been granted.
 *
 * @param permissions The list of permissions to be checked.
 */
fun Activity.allAndroidPermissionsGranted(permissions: List<String>) = permissions.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

/**
 * Returns `true` if the detected face contains all detectable landmarks.
 *
 */
fun Face.hasAllLandmarks() = allLandmarks.size == 10

/**
 * Returns the [Bitmap] representation of the [Frame] received in the camera processor.
 *
 * @param quality The quality of the resulting [Bitmap] image.
 *
 * @return The [Bitmap] representation of the [Frame].
 */
fun Frame.toBitmap(quality: Int = 90): Bitmap {
    val yuvImage = YuvImage(
        getData(),
        ImageFormat.NV21,
        size.width,
        size.height,
        null
    )

    val bytesOut = ByteArrayOutputStream()

    yuvImage.compressToJpeg(Rect(0, 0, size.width, size.height), quality, bytesOut)

    val bytesArray = bytesOut.toByteArray()

    return BitmapFactory.decodeByteArray(bytesArray, 0, bytesArray.size)
}

/**
 * Reads up to [length] bytes from the [InputStream].
 *
 * @param length The maximum length to read.
 * @return The data obtained from the [InputStream].
 */
infix fun InputStream.readBytesLength(length: Int): ByteArray {
    return ByteArray(length).apply {
        read(this, 0, length)
    }
}

/**
 * Reads up to [length] bytes from the [InputStream], and decode it to a [String].
 *
 * @param length The maximum length to read.
 * @return The data obtained from the [InputStream].
 */
infix fun InputStream.readStringLength(length: Int): String {
    return readBytesLength(length).decodeToString()
}

fun OutputStream.writeString(message: String) = write(message.toByteArray())

/**
 * Returns the WiFi SSID the current device is connected to, after removing the leading and trailing
 * apostrophes.
 */
val WifiManager.currentSsid
    get() = connectionInfo.ssid.drop(1).dropLast(1)

/**
 * Updates the value of the [MutableLiveData]. This is guaranteed to be done in the main thread.
 *
 * @param T The type of the data held by the [MutableLiveData].
 * @param value The new value.
 */
suspend infix fun <T> MutableLiveData<T>.updatesTo(value: T) =
    withContext(Dispatchers.Main) {
        this@updatesTo.value = value
    }

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <E> ConflatedBroadcastChannel<E>.sendLastEmitted() = send(value)