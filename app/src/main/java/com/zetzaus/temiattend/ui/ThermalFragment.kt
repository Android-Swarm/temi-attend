package com.zetzaus.temiattend.ui

import android.net.wifi.WifiManager
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentThermalBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ThermalFragment : BindingMainFragment<FragmentThermalBinding>() {

    @Inject
    lateinit var wifiManager: WifiManager

    override fun layoutId() = R.layout.fragment_thermal

    override fun onBinding() {
        super.onBinding()

        binding.viewModel = mainViewModel
    }
}