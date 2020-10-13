package com.zetzaus.temiattend.ext

import kotlin.math.max
import kotlin.math.min

fun Float.isNormalTemperature() = this < 37.3f

/**
 * Limits the current value between the [minValue] and the [maxValue].
 *
 * @param minValue The minimum value.
 * @param maxValue The maximum value.
 */
fun Float.setRange(minValue: Float, maxValue: Float) = min(maxValue, max(this, minValue))