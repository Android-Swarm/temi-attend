package com.zetzaus.temiattend.ui

import android.view.View
import androidx.navigation.fragment.navArgs
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentAbnormTempBinding
import com.zetzaus.temiattend.ext.navigate

class AbnormTempFragment : BindingFragment<FragmentAbnormTempBinding>() {

    private val args by navArgs<AbnormTempFragmentArgs>()

    override fun layoutId(): Int = R.layout.fragment_abnorm_temp

    override fun onBinding() {
        binding.temperature = args.temperature
        binding.user = args.user
        binding.fragment = this
    }

    fun onConfirm() = requireActivity().onBackPressed()

    fun onMyAttendanceClicked(v: View) {
        val directions =
            AbnormTempFragmentDirections.actionAbnormTempFragmentToAttendancesFragment(args.user)

        v.navigate(directions)
    }
}