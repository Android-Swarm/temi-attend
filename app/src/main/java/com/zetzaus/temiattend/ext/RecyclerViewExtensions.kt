package com.zetzaus.temiattend.ext

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("itemDecoration")
fun RecyclerView.addDecoration(decoration: RecyclerView.ItemDecoration?) {
    decoration?.let {
        addItemDecoration(decoration)
    }
}

@BindingAdapter("adapter")
fun <VH : RecyclerView.ViewHolder> RecyclerView.addAdapter(adapter: RecyclerView.Adapter<VH>) {
    setAdapter(adapter)
}