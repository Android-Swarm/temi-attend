package com.zetzaus.temiattend.ext

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

fun String.toQrCode(width: Int, height: Int): Bitmap =
    MultiFormatWriter().encode(
        this,
        BarcodeFormat.QR_CODE,
        (width * 1.5).toInt(),
        (height * 1.5).toInt()
    ).run {
        BarcodeEncoder().createBitmap(this)
    }
