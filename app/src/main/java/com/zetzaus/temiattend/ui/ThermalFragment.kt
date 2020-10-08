package com.zetzaus.temiattend.ui

import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentThermalBinding

class ThermalFragment : BindingMainFragment<FragmentThermalBinding>() {

    override fun layoutId() = R.layout.fragment_thermal

    override fun onBinding() {
        super.onBinding()

        binding.viewModel = mainViewModel
    }

}