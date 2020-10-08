package com.zetzaus.temiattend.ext

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.zetzaus.temiattend.R
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("dateText")
fun TextView.dateText(date: Date) {
    text = SimpleDateFormat.getDateTimeInstance().format(date)
}

@BindingAdapter("temperatureText")
fun TextView.temperatureText(temperature: Float?) {
    text = if (temperature != null) {
        String.format(context.getString(R.string.label_temperature, temperature))
    } else {
        ""
    }
}

/**
 * Sets the text to [noBlankText] if it is not blank. Otherwise, set the text to [noBlankDefault].
 *
 * @param noBlankText The text that will be set if this is not blank.
 * @param noBlankDefault The text that will be set when [noBlankText] is blank or null.
 */
@BindingAdapter("noBlankText", "noBlankDefault")
fun TextView.blankDefault(noBlankText: String?, noBlankDefault: String) {
    text = if (noBlankText == null || noBlankText.isBlank()) {
        noBlankDefault
    } else {
        noBlankText
    }
}