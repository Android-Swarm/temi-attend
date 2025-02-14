package com.zetzaus.temiattend.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import com.ogawa.temiirsdk.IrManager
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import com.robotemi.sdk.listeners.OnUserInteractionChangedListener
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.ActivityMainBinding
import com.zetzaus.temiattend.ext.*
import com.zetzaus.temiattend.face.MaskDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnUserInteractionChangedListener {
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

        robot.addOnUserInteractionChangedListener(this)

        // Set to night mode
        if (!isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        // Setup data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

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
                mainViewModel.requestTemiSpeak(getString(R.string.tts_mask_not_detected), true)
            }
        }

        mainViewModel.temiTts.observe(this) { request -> robot.speak(request) }

        lifecycleScope.launchWhenResumed {
            mainViewModel.isUserInteracting.collect { interacting ->
                if (!interacting && currentDestinationId != R.id.welcomeFragment) {
                    Log.d(
                        this@MainActivity.LOG_TAG,
                        "User stops interacting, returning to welcome page."
                    )

                    navHostFragment.findNavController().popBackStack(R.id.welcomeFragment, false)
                }
            }
        }

        prepareTemperatureMeasurement()
    }

    override fun onDestroy() {
        super.onDestroy()

        robot.removeOnUserInteractionChangedListener(this)
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

        maskCameraView.addFrameProcessor { frame ->
            runBlocking {
                if (frame.dataClass == ByteArray::class.java) {
                    val rotation = frame.rotationToUser

                    val inputImage = InputImage.fromByteArray(
                        frame.getData(),
                        frame.size.width,
                        frame.size.height,
                        rotation,
                        InputImage.IMAGE_FORMAT_NV21
                    )

                    val faces = faceDetector.process(inputImage).await()

                    // Check for mask only if there is a face detected
                    faces.firstOrNull { it.hasAllLandmarks() }
                        ?.let { _ ->
                            maskDetector.detectMask(inputImage).run {
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

    /**
     * Initializes the Temi-IR SDK.
     *
     */
    private fun prepareTemperatureMeasurement() {
        // Temp SDK
        lifecycleScope.launch {
            do {
                IrManager.initIr(
                    application,
                    getString(R.string.temi_ir_key),
                    getString(R.string.temi_ir_app_id),
                )

                delay(1000)

                IrManager.getCheckSuccess().also {
                    Log.d(
                        this@MainActivity.LOG_TAG,
                        if (!it) "Init failed, retrying" else "Init success! Starting measurement"
                    )
                }
            } while (!IrManager.getCheckSuccess())

            mainViewModel.sdkReady()
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

    override fun onUserInteraction(interacting: Boolean) {
        Log.d(LOG_TAG, "Change of user interaction! User is interacting: $interacting")

        mainViewModel.updateUserInteraction(interacting)

        if (interacting && currentDestinationId == R.id.welcomeFragment) {
            mainViewModel.requestTemiSpeak(getString(R.string.label_welcome_title), false)
        }
    }

    companion object {
        const val PERMISSION_CODE = 1111
        const val MODEL_FILENAME = "model.tflite"
        val ANDROID_PERMISSIONS = listOf(
            Manifest.permission.CAMERA, // For mask detection and face recognition
            Manifest.permission.ACCESS_FINE_LOCATION, // For fetching WiFi scan results
            Manifest.permission.ACCESS_COARSE_LOCATION // For fetching WiFi scan results
        )
    }
}