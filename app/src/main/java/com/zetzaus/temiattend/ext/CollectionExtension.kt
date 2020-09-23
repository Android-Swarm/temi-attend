package com.zetzaus.temiattend.ext

import okhttp3.MediaType
import okhttp3.RequestBody

fun ByteArray.toRequestBody(type: String): RequestBody =
    RequestBody.create(MediaType.parse(type), this)