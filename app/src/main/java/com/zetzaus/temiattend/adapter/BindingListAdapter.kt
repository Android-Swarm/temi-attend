package com.zetzaus.temiattend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Base class for [ListAdapter] with 1 item type.
 *
 * @param T The generated data binding class for the item layout.
 * @param I The type of item that contains the data.
 *
 * @param callback Callback function for updating the list of items.
 */
abstract class BindingListAdapter<T : ViewDataBinding, I>(callback: DiffUtil.ItemCallback<I>) :
    ListAdapter<I, BindingListAdapter<T, I>.BindingViewHolder>(callback) {

    /** The resource layout id for the item layout. */
    abstract val itemLayoutId: Int

    /** The function to be executed when binding the data to the layout. */
    abstract val onBinding: T.(I) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = DataBindingUtil.inflate<T>(inflater, itemLayoutId, parent, false)

        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BindingViewHolder(private val binding: T) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: I) {
            onBinding.invoke(binding, item)
            binding.executePendingBindings()
        }
    }
}