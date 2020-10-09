package com.zetzaus.temiattend.ext

import android.graphics.Bitmap
import com.beust.klaxon.Klaxon
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.*

/**
 * Converts the [String] content into a QR code [Bitmap].
 *
 * @param width The width of the [Bitmap].
 * @param height The height of the [Bitmap].
 *
 * @return The QR code image.
 */
fun String.toQrCode(width: Int, height: Int): Bitmap =
    MultiFormatWriter().encode(
        this,
        BarcodeFormat.QR_CODE,
        (width * 1.5).toInt(),
        (height * 1.5).toInt()
    ).run {
        BarcodeEncoder().createBitmap(this)
    }

/**
 * Returns `true` if the string contains only letters.
 *
 */
fun String.isAlphabetical() = all(Char::isLetter)

/**
 * Trim leading and trailing spaces and convert all letters to upper case.
 *
 */
fun String.upperCaseAndTrim() = toUpperCase(Locale.ROOT).trim()

fun String.escapeCharForCamera() = replace(",", """\,""")
    .replace("""\""", """\\""")

inline fun <reified T> String.parseJson(): T = Klaxon().parse<T>(this)!!