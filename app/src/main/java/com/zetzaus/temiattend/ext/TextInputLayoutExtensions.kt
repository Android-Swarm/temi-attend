package com.zetzaus.temiattend.ext

import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

/**
 * Sets an error message to the layout.
 *
 * @param error The error message to be displayed.
 */
@BindingAdapter("errorText")
fun TextInputLayout.errorText(error: String?) {
    error?.let { if (it.isNotEmpty()) setError(it) else setError(null) } ?: setError(null)
}

@BindingAdapter("cancelErrorOnEdit")
fun TextInputLayout.cancelErrorOnEdit(cancel: Boolean) {
    if (cancel) {
        editText?.let {
            it.addOnTextChangedListener { _, _, _, _ -> error = null }
        }
    }
}

@BindingAdapter("simpleContent")
fun MaterialAutoCompleteTextView.simpleAdapter(content: List<String>) {
    setAdapter(ArrayAdapter(context, android.R.layout.simple_list_item_1, content))
}

@BindingConversion
fun <T> collectionToList(collection: Collection<T>) = collection.toList()

fun EditText.addOnTextChangedListener(listener: (String, Int, Int, Int) -> Unit) =
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let { listener(it.toString(), start, before, count) }
        }

        override fun afterTextChanged(s: Editable?) = Unit

    })