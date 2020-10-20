package com.zetzaus.temiattend.ext

import android.os.Bundle
import java.util.*

private enum class Keys {
    DATE_KEY
}

fun Bundle.putDate(date: Date) = putSerializable(Keys.DATE_KEY.name, date)

fun Bundle.getDate() = getSerializable(Keys.DATE_KEY.name) as? Date