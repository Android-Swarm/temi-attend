package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.zetzaus.temiattend.R
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : Fragment() {

    private val mainViewModel by activityViewModels<MainActivityViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonEmployee.setOnClickListener {
            it.navigate(R.id.action_welcomeFragment_to_employeeLoginFragment)
        }

        imageSettings.setOnClickListener {
            it.navigate(R.id.action_welcomeFragment_to_settingsFragment)
        }

    }

    private fun View?.navigate(action: Int) = this?.findNavController()?.navigate(action)
}