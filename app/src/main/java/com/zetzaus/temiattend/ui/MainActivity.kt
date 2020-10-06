package com.zetzaus.temiattend.ui

import android.Manifest
import android.graphics.*
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import com.ogawa.temiirsdk.IrManager
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.ActivityMainBinding
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.allAndroidPermissionsGranted
import com.zetzaus.temiattend.face.MaskDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    /** The activity's `ViewModel`. */
    private val mainViewModel by viewModels<MainActivityViewModel>()

    /** Face detector that takes an image as input. */
    @Inject
    lateinit var faceDetector: FaceDetector

    /** Image labeler. */
    @Inject
    lateinit var maskDetector: MaskDetector

    /** Main Activity's binding.*/
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        // Set to night mode
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            Log.d(LOG_TAG, "Changing to night mode, recreating activity")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()
        }

        // Start mask detection if permitted before
        if (allAndroidPermissionsGranted(ANDROID_PERMISSIONS)) {
            startMaskDetection()
        } else {
            ActivityCompat.requestPermissions(
                this,
                ANDROID_PERMISSIONS.toTypedArray(),
                PERMISSION_CODE
            )
        }

        mainViewModel.snackBarMessage.observe(this) {
            Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok) {}
                .show()
        }

        mainViewModel.maskDetected.observe(this) { wearing ->
            if (!wearing) {
                Robot.getInstance()
                    .speak(TtsRequest.create(getString(R.string.tts_mask_not_detected), true))
            }
        }

        prepareTemperatureMeasurement()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CODE) {
            if (allAndroidPermissionsGranted(ANDROID_PERMISSIONS))
                startMaskDetection()
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startMaskDetection() = lifecycleScope.launchWhenCreated {
        maskCameraView.setLifecycleOwner(this@MainActivity)

        maskCameraView.mapGesture(Gesture.TAP, GestureAction.TAKE_PICTURE)

        maskCameraView.addFrameProcessor {
            runBlocking {
                if (it.dataClass == ByteArray::class.java) {
                    val inputImage = InputImage.fromByteArray(
                        it.getData(),
                        it.size.width,
                        it.size.height,
                        it.rotationToView,
                        InputImage.IMAGE_FORMAT_NV21
                    )

                    val faces = faceDetector.process(inputImage).await()

                    faces.firstOrNull()?.let { face ->
                        // Crop image only the face part
                        val yuvImage =
                            YuvImage(
                                it.getData(),
                                ImageFormat.NV21,
                                it.size.width,
                                it.size.height,
                                null
                            )
                        val bytesOut = ByteArrayOutputStream()
                        yuvImage.compressToJpeg(
                            Rect(0, 0, it.size.width, it.size.height),
                            90,
                            bytesOut
                        )
                        val bytesFrame = bytesOut.toByteArray()

                        val originalImage =
                            BitmapFactory.decodeByteArray(bytesFrame, 0, bytesFrame.size)

                        val boundingBox = face.boundingBox
                        val faceImage = Bitmap.createBitmap(
                            originalImage,
                            boundingBox.centerX() - abs(boundingBox.width() / 1.25).toInt(),
                            boundingBox.centerY() - abs(boundingBox.height() / 1.25).toInt(),
                            (boundingBox.width() * 1.5).toInt(),
                            (boundingBox.height() * 1.5).toInt()
                        )

                        // Detect mask
                        val faceInputImage = InputImage.fromBitmap(faceImage, it.rotationToView)

                        maskDetector.detectMask(faceInputImage).run {
                            mainViewModel.updateMaskDetection(isWearingMask)
                        }

                    } ?: mainViewModel.updateMaskDetection(true)
                }
            }
        }

        maskCameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)

                Log.d(this@MainActivity.LOG_TAG, "Picture taken! Size: ${result.data.size}")
                mainViewModel.updateFaceRecognitionState(false)

                mainViewModel.detectAndRecognize(result.data)
            }
        })
    }

    private fun prepareTemperatureMeasurement() {
        // Temp SDK
        IrManager.initIr(
            application,
            getString(R.string.temi_ir_key),
            getString(R.string.temi_ir_app_id),
        )

        if (IrManager.getCheckSuccess()) {
            Log.d(LOG_TAG, "Temi-IR SDK initialization success! Starting temperature measurement")
            mainViewModel.startTemperatureTaking()
        } else {
            Log.d(this@MainActivity.LOG_TAG, "Temi-IR SDK initialization failed!")
        }
    }

    override fun onBackPressed() {
        // If camera is visible, hide the camera
        if (maskCameraView.isVisible) {
            mainViewModel.updateFaceRecognitionState(false)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val PERMISSION_CODE = 1111
        const val MODEL_FILENAME = "model.tflite"
        val ANDROID_PERMISSIONS = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}