package com.zetzaus.temiattend.ui

import android.net.wifi.WifiManager
import android.view.View
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentConfigureBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfigureFragment : BindingMainFragment<FragmentConfigureBinding>() {

    @Inject
    lateinit var wifiManager: WifiManager

    override fun layoutId() = R.layout.fragment_configure

    override fun onBinding() {
        super.onBinding()

        binding.fragment = this
    }

    fun onConfirmButtonClicked(button: View) {
        if (wifiManager.connectionInfo.ssid != OGAWA_SSID) {
            mainViewModel.showSnackBar(getString(R.string.snack_bar_wrong_wifi).format(OGAWA_SSID))
            return
        }

        // TODO: navigate
    }

    companion object {
        const val OGAWA_SSID = "OGAWA-IR"
    }

}