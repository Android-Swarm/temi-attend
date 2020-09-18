package com.zetzaus.temiattend

import android.Manifest
import android.animation.ObjectAnimator
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.allAndroidPermissionsGranted
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainActivityViewModel>()

    private val faceDetectorOptions = FaceDetectorOptions.Builder()
        .build()
    private val faceDetector = FaceDetection.getClient(faceDetectorOptions)

    private val localModel = LocalModel.Builder()
        .setAssetFilePath(MODEL_FILENAME)
        .build()

    private val customLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
        .setConfidenceThreshold(0.8f)
        .build()

    private val maskDetector = MaskDetector(customLabelerOptions)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set to night mode
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
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

        ObjectAnimator.ofInt(imageMask, "imageAlpha", 0, 255).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        mainViewModel.maskDetected.observe(this) { wearingMask ->
            imageMask.isVisible = !wearingMask
            Log.d(LOG_TAG, if (wearingMask) "Wearing mask or no person" else "No mask")
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

    private fun startMaskDetection() {
        maskCameraView.setLifecycleOwner(this)

        maskCameraView.addFrameProcessor {
            runBlocking {
                if (it.dataClass == Image::class.java) {
                    val originalImage = it.getData<Image>()
                    val inputImage = InputImage.fromMediaImage(
                        originalImage,
                        it.rotationToView
                    )
                    val faces = faceDetector.process(inputImage).await()

                    faces.firstOrNull()?.let { _ ->
                        maskDetector.detectMask(inputImage).run {
                            mainViewModel.updateMaskDetection(isWearingMask)
                        }

                    } ?: mainViewModel.updateMaskDetection(true)
                }
            }
        }
    }

    companion object {
        const val PERMISSION_CODE = 1111
        const val MODEL_FILENAME = "model.tflite"
        val ANDROID_PERMISSIONS = listOf(Manifest.permission.CAMERA)
    }
}