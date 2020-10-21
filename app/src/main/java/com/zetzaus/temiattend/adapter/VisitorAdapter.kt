package com.zetzaus.temiattend.adapter

import androidx.recyclerview.widget.DiffUtil
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.Visitor
import com.zetzaus.temiattend.databinding.ItemVisitorBinding
import javax.inject.Inject

private val callback = object : DiffUtil.ItemCallback<Visitor>() {
    override fun areItemsTheSame(oldItem: Visitor, newItem: Visitor) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Visitor, newItem: Visitor) = oldItem == newItem
}

class VisitorAdapter @Inject constructor() :
    BindingListAdapter<ItemVisitorBinding, Visitor>(callback) {
    
    override val itemLayoutId: Int
        get() = R.layout.item_visitor

    override val onBinding: ItemVisitorBinding.(Visitor) -> Unit
        get() = { data -> visitor = data }
}