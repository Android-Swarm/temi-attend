package com.zetzaus.temiattend.ui

import androidx.fragment.app.viewModels
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentWelcomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeFragment : BindingMainFragment<FragmentWelcomeBinding>() {

//    private val viewModel by viewModels<WelcomeFragmentViewModel>()

    override fun onBinding() {
        super.onBinding()

        binding.viewModel = mainViewModel
    }

    override fun layoutId() = R.layout.fragment_welcome
}