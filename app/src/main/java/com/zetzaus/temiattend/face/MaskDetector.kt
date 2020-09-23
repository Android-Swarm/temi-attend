package com.zetzaus.temiattend.face

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import kotlinx.coroutines.tasks.await

data class DetectionResult(val confidence: Float, val isWearingMask: Boolean)

class MaskDetector(options: CustomImageLabelerOptions, private val maskIndex: Int = 0) {
    private val labeler = ImageLabeling.getClient(options)

    suspend fun detectMask(inputImage: InputImage) =
        labeler.process(inputImage)
            .await()
            .maxByOrNull { it.confidence }
            ?.let {
                DetectionResult(it.confidence, it.index == maskIndex)
            }
            ?: DetectionResult(1f, false)
}