package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BindingFragment<T : ViewDataBinding> : Fragment() {
    protected lateinit var binding: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        onBinding()
        binding.executePendingBindings()

        return binding.root
    }

    /**
     * This function should return the layout resource id.
     *
     * @return The layout resource id to be inflated.
     */
    protected abstract fun layoutId(): Int

    /**
     * Override this function to bind data to the binding. This function is called in [onCreateView]
     * before returning the root view of the binding.
     *
     */
    protected open fun onBinding() {
        // Do nothing by default
    }
}