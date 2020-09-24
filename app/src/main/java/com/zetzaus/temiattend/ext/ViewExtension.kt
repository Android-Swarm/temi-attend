package com.zetzaus.temiattend.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import androidx.core.view.isVisible
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator

/**
 * Animates the [View] with the circular reveal animation.
 *
 * @param hide `true` for circular hide, `false` for circular reveal.
 * @param hiddenVisibility Determines the Visibility after circular hide. Defaults to [View.GONE].
 */
fun View.circularHideOrReveal(hide: Boolean, hiddenVisibility: Int = View.GONE) =
    ViewAnimationUtils.createCircularReveal(
        this,
        width / 2,
        height / 2,
        if (hide) width.toFloat() else 0f,
        if (hide) 0f else width.toFloat()
    ).apply {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                this@circularHideOrReveal.visibility = if (hide) hiddenVisibility else View.VISIBLE
            }
        })

        isVisible = true
    }.start()

fun View?.navigate(resId: Int) = this?.findNavController()?.navigate(resId)

fun View?.navigate(dir: NavDirections) = this?.findNavController()?.navigate(dir)

fun View?.navigateWithExtras(dir: NavDirections, extra: FragmentNavigator.Extras) =
    this?.findNavController()?.navigate(dir, extra)