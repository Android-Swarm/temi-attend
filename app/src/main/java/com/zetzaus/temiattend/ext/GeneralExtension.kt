package com.zetzaus.temiattend.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

val Any.LOG_TAG: String
    get() = this::class.java.simpleName

fun Activity.allAndroidPermissionsGranted(permissions: List<String>) = permissions.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.allAndroidPermissionsGranted(permissions: List<String>) = permissions.all {
    ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
}

fun Animator.onCompleteFinite(block: (Animator) -> Unit): Animator {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            block(this@onCompleteFinite)
        }
    })

    return this
}