package com.zetzaus.temiattend.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import com.zetzaus.temiattend.R
import java.text.SimpleDateFormat
import java.util.*

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

/**
 * Navigates to another destination.
 *
 * @param resId The destination id.
 */
fun View?.navigate(resId: Int) = this?.findNavController()?.navigate(resId)

/**
 * Navigates to another destination.
 *
 * @param dir The destination direction.
 */
fun View?.navigate(dir: NavDirections) = this?.findNavController()?.navigate(dir)

/**
 * Navigates to another destination.
 *
 * @param dir The destination direction.
 * @param extra Any navigation extras.
 */
fun View?.navigateWithExtras(dir: NavDirections, extra: FragmentNavigator.Extras) =
    this?.findNavController()?.navigate(dir, extra)

/**
 * Adds only a listener that listens to a transition complete event.
 *
 * @param listener The lambda called when the transition has completed.
 */
fun MotionLayout.addTransitionCompleteListener(listener: (MotionLayout?, Int) -> Unit) =
    addTransitionListener(object : MotionLayout.TransitionListener {
        override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) = Unit

        override fun onTransitionChange(
            p0: MotionLayout?,
            p1: Int,
            p2: Int,
            p3: Float
        ) = Unit

        override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
            listener(p0, p1)
        }

        override fun onTransitionTrigger(
            p0: MotionLayout?,
            p1: Int,
            p2: Boolean,
            p3: Float
        ) = Unit
    })

@BindingAdapter("dateText")
fun TextView.dateText(date: Date) {
    text = SimpleDateFormat.getDateTimeInstance().format(date)
}

@BindingAdapter("temperatureText")
fun TextView.temperatureText(temperature: Float?) {
    text = if (temperature != null) {
        String.format(context.getString(R.string.label_temperature, temperature))
    } else {
        ""
    }
}
