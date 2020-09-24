package com.zetzaus.temiattend.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater

abstract class TransitionalFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(transitionId())
    }

    abstract fun transitionId(): Int
}