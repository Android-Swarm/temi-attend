package com.zetzaus.temiattend.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import com.google.android.material.textfield.TextInputLayout


/**
 * The current text in the [EditText] as a [String].
 */
val EditText.textString
    get() = text?.toString() ?: ""

/**
 * Animates the [View] with the circular reveal animation.
 * Make sure to set the attribute `android:visibility` to set the starting visibility.
 *
 * @param hide `true` for circular hide, `false` for circular reveal.
 * @param hiddenVisibility Determines the Visibility after circular hide. Defaults to [View.GONE].
 */
@BindingAdapter(value = ["circularHide", "circularHiddenVisibility"], requireAll = false)
fun View.circularHideOrReveal(hide: Boolean, hiddenVisibility: Int = View.GONE) {
    if (isAttachedToWindow) {
        Log.d(LOG_TAG, "View is attached, launching animation for hidden $hide")
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
                    this@circularHideOrReveal.visibility =
                        if (hide) hiddenVisibility else View.VISIBLE

                    Log.d(LOG_TAG, "View is now ${if (hide) "invisible" else "visible"}")
                }
            })

            isVisible = true
        }.start()
    }
}

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
 * When the [View] is clicked, the application will navigate based on the given action id.
 *
 * @param resId The action id.
 */
@BindingAdapter("onClickNavigate")
fun View?.navigateOnClick(resId: Int) =
    this?.setOnClickListener { findNavController().navigate(resId) }

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

/**
 * If [enabled] is `true`, the [View] will play blinking animation. By default, it will blink with
 * the frequency of 2 seconds.
 *
 * @param repeatDuration The half duration of the animation (from invisible to visible or vice versa).
 * @param enabled `true` to play the animation or `false` to stop the animation.
 */
@BindingAdapter(value = ["blinkDuration", "blinkEnabled"], requireAll = false)
fun View.blink(repeatDuration: Long?, enabled: Boolean?) {
    if (enabled != false) {
        tag ?: let {
            Log.d(LOG_TAG, "Enabled blink animation")

            ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
                duration = repeatDuration ?: 1000L
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }.also {
                tag = it
            }.start()
        }
    } else {
        (tag as? ObjectAnimator)?.let {
            it.end()
            alpha = 1.0f
            tag = null
            Log.d(LOG_TAG, "Cancelling animation")
        }
    }
}

/**
 * Sets the [isVisible] property of the [View].
 *
 * @param isVisible The new value for the [isVisible] property.
 */
@BindingAdapter("isVisible")
fun View.dynamicVisibility(isVisible: Boolean) {
    this.isVisible = isVisible
}

/**
 * Sets the visibility of the [View]. Using this adapter, the [View] will go to [View.INVISIBLE]
 * instead of [View.GONE].
 *
 * @param isVisible The new visibility.
 */
@BindingAdapter("canVisible")
fun View.dynamicInvisibleVisibility(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
}

/**
 * When the [View] is clicked, pop the back stack until the given destination id.
 *
 * @param resId The destination id.
 */
@BindingAdapter("onClickPopUntil")
fun View.onClickPopUntil(resId: Int) {
    setOnClickListener { it.findNavController().popBackStack(resId, false) }
}

/**
 * Sets the error text to the [TextInputLayout].
 *
 * @param error The error message to tell the user. If null, the error will be removed.
 */
@BindingAdapter("textError")
fun TextInputLayout.textError(error: String?) {
    error?.let {
        setError(it)
    } ?: setError(null)
}
