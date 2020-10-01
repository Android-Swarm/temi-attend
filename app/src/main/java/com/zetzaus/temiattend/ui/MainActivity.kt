package com.zetzaus.temiattend.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.allAndroidPermissionsGranted
import com.zetzaus.temiattend.ext.circularHideOrReveal
import com.zetzaus.temiattend.face.MaskDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.camera_overlay.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set to night mode
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            Log.d(LOG_TAG, "Changing to night mode, recreating activity")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()
        }

        if (allAndroidPermissionsGranted(ANDROID_PERMISSIONS)) {
            startMaskDetection()
        } else {
            ActivityCompat.requestPermissions(
                this,
                ANDROID_PERMISSIONS.toTypedArray(),
                PERMISSION_CODE
            )
        }

        // Animate mask not detected icon
        ObjectAnimator.ofInt(imageMask, "imageAlpha", 0, 255).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        mainViewModel.maskDetected.observe(this) { wearingMask ->
            imageMask.isVisible = !wearingMask
            Log.d(LOG_TAG, if (wearingMask) "Wearing mask or no person" else "No mask")
        }

        mainViewModel.startFaceRecognition.observe(this) { shouldStartFaceRecognition ->
            shouldShowCamera(shouldStartFaceRecognition)
        }

        closeCameraButton.setOnClickListener {
            shouldShowCamera(false)
        }

        mainViewModel.snackBarMessage.observe(this) {
            Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok) {}
                .show()
        }
    }

    private fun shouldShowCamera(show: Boolean) {
        if (show) {
            maskCameraView.circularHideOrReveal(false)
            cameraOverlay.circularHideOrReveal(false)
        } else {
            maskCameraView.circularHideOrReveal(true, View.INVISIBLE)
            cameraOverlay.circularHideOrReveal(true)
        }
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
                shouldShowCamera(false)

                mainViewModel.detect(result.data)
            }
        })


        // Temp SDK
//        IrManager.initIr(
//            application,
//            """MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAmx5NrqWXCEkX39U2CHiYzb90PauqEfmjV+UtgDqaqhl1KsNYCNQSWNsERqtMhHFBW6Nnftb7o8BDMigD52QYpwIDAQABAkBDJzdSKHXeLGadjFw8BpmAWSYlnK+f4IcKgjjUjopuoLG1Bi1LAr/NoYZIFZvf1t8TLyR9TPPMHf/pvPghvkZBAiEA19yRvRZyn2ccxqGSHAwTgEeauxgp1J3BXVOYlE8mFVkCIQC39j0iPqR7fqJOcwD41ub8SVuUXB/g9Zf/5N5Q5F/d/wIhAJaoprtPqI6i3A2yhRS4RQAaed8tXTy9IlFt4CdbGpx5AiAnLsyQqbURFMTvXrF7TxK988YM0J59pPHuMEpmAm6k8wIgXXguCfTq9XigD25u/A6tmkV3G3eOgqzgqba8ShUBYVU=""",
//            """sdk_zetzaus_temiattend""",
//        )
//
//        if (IrManager.getCheckSuccess()) {
//            Log.d(LOG_TAG, "Temi-IR SDK initialization success!")
//
//        } else {
//            Log.d(this@MainActivity.LOG_TAG, "Temi-IR SDK initialization failed!")
//        }
    }

    override fun onBackPressed() {
        // If camera is visible, hide the camera
        if (maskCameraView.isVisible) {
            shouldShowCamera(false)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Click handlers for calling the back button.
     *
     * @param v The [View] that is clicked.
     */
    fun onClickShouldGoBack(v: View) = onBackPressed()

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