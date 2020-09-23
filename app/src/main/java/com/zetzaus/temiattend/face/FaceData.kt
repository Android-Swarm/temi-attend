package com.zetzaus.temiattend.face

data class FaceRectangle(val top: Int, val left: Int, val width: Int, val height: Int) {
    override fun toString() = "$left,$top,$width,$height"
}

data class FaceDetected(
    val faceId: String,
    val faceRectangle: FaceRectangle
)

data class FaceApiError(val code: String, val message: String)

data class IdentifyRequestBody(
    val faceIds: List<String>,
    val personGroupId: String,
    val confidenceThreshold: Float
)

data class FaceIdentified(
    val faceId: String,
    val candidates: List<FaceCandidate>
)

data class FaceCandidate(
    val personId: String,
    val confidence: Float
)