package com.zetzaus.temiattend.ui

import android.net.wifi.WifiManager
import android.view.View
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentConfigureBinding
import com.zetzaus.temiattend.ext.currentSsid
import com.zetzaus.temiattend.ext.navigate
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
        if (wifiManager.currentSsid != OGAWA_SSID) {
            mainViewModel.showSnackBar(getString(R.string.snack_bar_wrong_wifi).format(OGAWA_SSID))

            return
        }

        button.navigate(R.id.action_configureFragment_to_wifiInputFragment)
    }

    companion object {
        const val OGAWA_SSID = "OGAWA-IR"
    }

}