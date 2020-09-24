package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentNormalTempBinding
import com.zetzaus.temiattend.ext.toQrCode
import kotlinx.android.synthetic.main.fragment_normal_temp.*

class NormalTempFragment : TransitionalFragment() {
    private val args by navArgs<NormalTempFragmentArgs>()

    private lateinit var binding: FragmentNormalTempBinding

    override fun transitionId(): Int = android.R.transition.move

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_normal_temp, container, false)

        binding.temperature = args.temperature
        binding.username = args.user
        binding.fragment = this

        return binding.root
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

    companion object {
        const val SCHAEFFLER_SAFE_ENTRY_URL =
            "https://temperaturepass.ndi-api.gov.sg/login/PROD-199604030H-125413-SCHAEFFLERSINGAPOREPTELTD-SE"
    }
}