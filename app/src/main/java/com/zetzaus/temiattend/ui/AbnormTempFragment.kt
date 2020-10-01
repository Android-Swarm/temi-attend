package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentAbnormTempBinding

class AbnormTempFragment : BindingFragment<FragmentAbnormTempBinding>() {

    private val args by navArgs<AbnormTempFragmentArgs>()

    override fun layoutId(): Int = R.layout.fragment_abnorm_temp

    override fun onBinding() {
        binding.temperature = args.temperature
        binding.user = args.user
        binding.fragment = this
    }

    fun onConfirm() = requireActivity().onBackPressed()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}