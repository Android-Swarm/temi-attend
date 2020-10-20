package com.zetzaus.temiattend.ext

import android.view.View
import android.widget.CompoundButton
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

@BindingAdapter(value = ["dependentViewId"])
fun CompoundButton.setEnableDependency(viewId: Int) =
    setOnCheckedChangeListener { _, isChecked ->
        (parent as View).findViewById<View>(viewId).isVisible = !isChecked
    }