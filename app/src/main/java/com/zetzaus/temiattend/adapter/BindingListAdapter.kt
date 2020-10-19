package com.zetzaus.temiattend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BindingListAdapter<T : ViewDataBinding, I>(callback: DiffUtil.ItemCallback<I>) :
    ListAdapter<I, BindingListAdapter<T, I>.BindingViewHolder>(callback) {
    
    abstract val itemLayoutId: Int

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