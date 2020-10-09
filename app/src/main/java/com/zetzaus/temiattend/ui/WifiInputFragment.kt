package com.zetzaus.temiattend.ui

import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentWifiInputBinding
import com.zetzaus.temiattend.ext.currentSsid
import com.zetzaus.temiattend.ext.navigate
import com.zetzaus.temiattend.ext.textString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_wifi_input.*
import javax.inject.Inject

@AndroidEntryPoint
class WifiInputFragment : BindingMainFragment<FragmentWifiInputBinding>() {

    @Inject
    lateinit var wifiManager: WifiManager

    private val viewModel by viewModels<WifiInputViewModel>()

    override fun layoutId() = R.layout.fragment_wifi_input

    override fun onBinding() {
        super.onBinding()

        binding.viewModel = viewModel
        binding.fragment = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wifiManager.scanResults.map { it.SSID }.also {
            ssidInput.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    it
                )
            )
        }
        viewModel.socketError.observe(viewLifecycleOwner) { error ->
            mainViewModel.showSnackBar(
                getString(R.string.snack_bar_socket_error).format(
                    error ?: getString(R.string.unknown_error)
                )
            )
        }
    }

    fun onConnectButtonClicked() {
        if (ssidInput.textString.isBlank()) {
            viewModel.submitSsidError(getString(R.string.error_ssid_blank))
        } else {
            viewModel.submitCredentials(ssidInput.textString, passwordInput.textString)
        }
    }

    fun onConfirmButtonClicked(v: View) {
        if (wifiManager.currentSsid != ssidInput.textString) {
            mainViewModel.showSnackBar(
                getString(R.string.snack_bar_wrong_wifi).format(ssidInput.textString)
            )

            return
        }

        val dir = WifiInputFragmentDirections.actionWifiInputFragmentToConnectingFragment(
            viewModel.macAddress.value!!,
            ssidInput.textString
        )

        v.navigate(dir)
    }
}