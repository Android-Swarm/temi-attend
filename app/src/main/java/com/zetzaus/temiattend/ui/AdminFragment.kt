package com.zetzaus.temiattend.ui

import androidx.fragment.app.viewModels
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentAdminBinding
import com.zetzaus.temiattend.ext.textString
import com.zetzaus.temiattend.ext.toOfficeName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_admin.*

@AndroidEntryPoint
class AdminFragment : BindingFragment<FragmentAdminBinding>() {

    private val viewModel by viewModels<AdminFragmentViewModel>()

    override fun layoutId() = R.layout.fragment_admin

    override fun onBinding() {
        super.onBinding()

        binding.viewModel = viewModel
    }

    override fun onPause() {
        super.onPause()

        // Save chosen location
        val chosenLocation = officeInput.textString.toOfficeName()
        viewModel.saveOfficeLocation(chosenLocation)
    }
}