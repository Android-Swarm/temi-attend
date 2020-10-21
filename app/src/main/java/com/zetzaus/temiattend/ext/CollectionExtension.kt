package com.zetzaus.temiattend.ext

import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*

fun ByteArray.toRequestBody(type: String): RequestBody =
    RequestBody.create(MediaType.parse(type), this)

/**
 * Converts [ByteArray] to its hexadecimal [String] representation.
 *
 * @param uppercase `true` if the result should be in uppercase.
 */
fun ByteArray.toHexString(uppercase: Boolean = true) =
    joinToString("") { "%02x".format(it) }
        .run {
            if (uppercase) toUpperCase(Locale.ROOT)
            else this
        }

/**
 * Filter the [List] only if the [trigger] is `true`. Otherwise, return the original [List].
 *
 * @param T The list item type.
 * @param trigger `true` to filter the [List].
 * @param predicate The predicate function.
 */
fun <T> Collection<T>.filterIf(trigger: Boolean, predicate: (T) -> Boolean) =
    if (trigger) filter(predicate) else this