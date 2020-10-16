package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentPasswordBinding
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.addOnTextChangedListener
import com.zetzaus.temiattend.ext.navigate
import com.zetzaus.temiattend.ext.textString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_password.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PasswordFragment : BindingFragment<FragmentPasswordBinding>() {
    private val args by navArgs<PasswordFragmentArgs>()

    private val viewModel by viewModels<PasswordFragmentViewModel>()

    override fun layoutId() = R.layout.fragment_password

    override fun onBinding() {
        super.onBinding()

        binding.fragment = this
        binding.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passwordInput.addOnTextChangedListener { s, _, _, _ ->
            Log.d(LOG_TAG, "User changed password input text")
            viewModel.updatePasswordRequirementDisplay(s)
        }

        lifecycleScope.launch {
            viewModel.adminPassword
                .combine(flow { emit(args.changePassword) }) { (encrypted, iv), changePass ->
                    Triple(encrypted, iv, changePass)
                }.collect { (encrypted, iv, changePass) ->
                    binding.operation = when {
                        changePass -> PasswordOperation.CHANGE_PASSWORD
                        encrypted.isBlank() || iv.isBlank() -> PasswordOperation.NEW_PASSWORD
                        else -> PasswordOperation.INPUT_PASSWORD
                    }

                    binding.invalidateAll()

                    Log.d(this@PasswordFragment.LOG_TAG, "Operation is ${binding.operation}")
                }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.verification.collect { correct ->
                if (correct) {
                    if (binding.operation!! == PasswordOperation.CHANGE_PASSWORD) {
                        if (checkPassAndConfirmPass()) {
                            viewModel.savePassword(passwordInput.textString)
                            navigateToAdmin()
                        }
                    } else {
                        navigateToAdmin()
                    }
                } else {
                    viewModel.tellWrongPassword(binding.operation!!)
                }
            }
        }
    }

    fun onConfirmButtonClicked(v: View) {
        val password = passwordInput.textString

        binding.operation!!.apply {
            if (this == PasswordOperation.INPUT_PASSWORD) {
                Log.d(this@PasswordFragment.LOG_TAG, "Submitting password")
                viewModel.submitPasswordToVerify(password)
            } else if (this == PasswordOperation.CHANGE_PASSWORD) {
                Log.d(this@PasswordFragment.LOG_TAG, "Submitting new password")
                viewModel.submitPasswordToVerify(oldPasswordInput.textString)
            } else {
                Log.d(this@PasswordFragment.LOG_TAG, "Submitting initial password")
                if (checkPassAndConfirmPass()) {
                    viewModel.savePassword(password)
                    navigateToAdmin()
                }
            }
        }
    }

    private fun navigateToAdmin() {
        val dir = PasswordFragmentDirections.actionPasswordFragmentToAdminFragment()
        passwordLayout.navigate(dir)
    }

    /**
     * Checks the password and confirmation password.
     *
     * @return `true` if confirmation password is the same as the password and the password
     *          satisfies the requirements.
     */
    private fun checkPassAndConfirmPass(): Boolean {
        val password = passwordInput.textString
        val confirmPassword = confirmPasswordInput.textString

        val validPassword =
            viewModel.isValidPassword(password) // Matches password requirement
        val validConfirmPassword =
            viewModel.isValidConfirmPassword(password, confirmPassword) // Matches password

        return validPassword && validConfirmPassword
    }
}

enum class PasswordOperation {
    NEW_PASSWORD, CHANGE_PASSWORD, INPUT_PASSWORD
}