package com.zetzaus.temiattend.face

import okhttp3.RequestBody

data class Person(
    val personId: String,
    val name: String,
    val persistedFaceIds: List<String>,
    val userData: String
)

data class CreatePersonBody(
    val name: String,
    val userData: String
)

data class CreatedPerson(val personId: String)

data class NewPersonPayload(
    var user: String? = null,
    var photoData: RequestBody? = null,
    var rect: FaceRectangle? = null
) {
    fun isReady() = user != null && photoData != null && rect != null
}