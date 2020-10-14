package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.navigation.fragment.navArgs
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentNormalTempBinding
import com.zetzaus.temiattend.ext.navigate
import com.zetzaus.temiattend.ext.toQrCode
import kotlinx.android.synthetic.main.fragment_normal_temp.*

class NormalTempFragment : BindingFragment<FragmentNormalTempBinding>() {
    private val args by navArgs<NormalTempFragmentArgs>()

    override fun layoutId(): Int = R.layout.fragment_normal_temp

    override fun onBinding() {
        binding.temperature = args.temperature
        binding.username = args.user
        binding.fragment = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val barcodeTreeObserver = safeEntryQrCode.viewTreeObserver
        val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                safeEntryQrCode.apply {
                    val qrCode = SCHAEFFLER_SAFE_ENTRY_URL.toQrCode(width, height)
                    setImageBitmap(qrCode)
                }

                barcodeTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }

        barcodeTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    fun onConfirmButtonPressed() = requireActivity().onBackPressed()

    fun onMyAttendancePressed(v: View) {
        val directions =
            NormalTempFragmentDirections.actionNormalTempFragmentToAttendancesFragment(args.user)

        v.navigate(directions)
    }

    companion object {
        const val SCHAEFFLER_SAFE_ENTRY_URL =
            "https://temperaturepass.ndi-api.gov.sg/login/PROD-199604030H-125413-SCHAEFFLERSINGAPOREPTELTD-SE"
    }
}