package com.zetzaus.temiattend.ext

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.zetzaus.temiattend.OfficeName
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

@BindingAdapter("officeText")
fun TextView.officeLocationText(location: OfficeName) {
    text = location.toStringRepresentation()
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

/**
 * Sets the text to [mac] in mac address format if it is not blank. Otherwise, set the text to [noBlankDefault].
 *
 * @param mac The text that will be set if this is not blank.
 * @param noBlankDefault The text that will be set when [mac] is blank or null.
 */
@BindingAdapter("noBlankMac", "noBlankDefault")
fun TextView.blankDefaultMac(mac: String?, noBlankDefault: String) {
    text = if (mac == null || mac.isBlank()) {
        noBlankDefault
    } else {
        mac.chunked(2).joinToString(":")
    }
}