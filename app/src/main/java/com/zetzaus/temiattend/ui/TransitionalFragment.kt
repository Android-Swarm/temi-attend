package com.zetzaus.temiattend.ui

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.transition.TransitionInflater

abstract class TransitionalFragment<T : ViewDataBinding> : BindingFragment<T>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(transitionId())
    }

    abstract fun transitionId(): Int
}