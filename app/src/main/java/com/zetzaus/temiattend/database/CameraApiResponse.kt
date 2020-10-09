package com.zetzaus.temiattend.database

import com.beust.klaxon.Json

data class CameraApiResponse(val data: CameraDetails)

data class CameraDetails(
    @Json(name = "mac") val macAddress: String,
    val deviceIp: String,
    @Json(name = "targetSSID") val targetSsid: String,
    val version: String
)