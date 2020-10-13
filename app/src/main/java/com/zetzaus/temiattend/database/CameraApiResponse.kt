package com.zetzaus.temiattend.database

import com.google.gson.annotations.SerializedName

data class CameraApiResponse(val data: CameraDetails)

data class CameraDetails(
    @SerializedName("mac") val macAddress: String,
    val deviceIp: String,
    @SerializedName("targetSSID") val targetSsid: String,
    val version: String
)