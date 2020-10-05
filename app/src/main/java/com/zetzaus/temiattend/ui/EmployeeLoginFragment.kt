package com.zetzaus.temiattend.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.Attendance
import com.zetzaus.temiattend.databinding.FragmentEmployeeLoginBinding
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.isAlphabetical
import com.zetzaus.temiattend.ext.navigateWithExtras
import com.zetzaus.temiattend.face.NewPersonPayload
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_employee_login.*

@AndroidEntryPoint
class EmployeeLoginFragment : BindingFragment<FragmentEmployeeLoginBinding>() {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()
    private val viewModel by viewModels<EmployeeLoginViewModel>()

    private lateinit var userReceiver: UserReceiver

    override fun layoutId() = R.layout.fragment_employee_login

    override fun onBinding() {
        super.onBinding()

        binding.viewModel = viewModel
        binding.fragment = this
        binding.mainViewModel = activityViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userReceiver = UserReceiver()

        requireContext().registerReceiver(
            userReceiver,
            IntentFilter(MainActivityViewModel.ACTION_BROADCAST_USER),
            MainActivityViewModel.PRIVATE_BROADCAST_PERMISSION,
            null
        )

        viewModel.abnormalAttendsLiveData.observe(viewLifecycleOwner) { (user, attendance) ->
            navigateAccordingly(user, attendance)
        }
    }

    fun onConfirmButtonClicked() {
        // Register the new face if there is any
        try {
            activityViewModel.newPersonToRegister?.let {
                Log.d(
                    LOG_TAG,
                    "New person need to be registered\n" +
                            "Image Size: ${it.photoData?.contentLength()}"
                )

                val payloadCopy = NewPersonPayload(
                    user = schaefflerIdEditText.text.toString(),
                    photoData = it.photoData,
                    rect = it.rect
                )

                if (payloadCopy.isReady()) {
                    activityViewModel.saveNewPerson(payloadCopy)
                }
            }
        } finally {
            activityViewModel.newPersonToRegister = null
        }

        // Check if the Schaeffler ID is valid
        schaefflerIdEditText.text?.toString().run {
            Log.d(this@EmployeeLoginFragment.LOG_TAG, "Checking schaeffler ID before navigating")

            when {
                isNullOrBlank() -> viewModel.sendErrorInput(getString(R.string.error_blank))
                    .also { Log.d(this@EmployeeLoginFragment.LOG_TAG, "Schaeffler ID is blank!") }

                !isAlphabetical() -> viewModel.sendErrorInput(getString(R.string.error_non_alphabet))
                    .also { Log.d(this@EmployeeLoginFragment.LOG_TAG, "Schaeffler ID invalid!") }

                else -> viewModel.submitUser(this)
            }
        }
    }

    private fun navigateAccordingly(user: String, abnormalAttendsToday: List<Attendance>) {
        val dir = if (viewModel.getRemainingChance(user, abnormalAttendsToday) <= 0) {
            Log.d(LOG_TAG, "No more chance for $this@run")

            EmployeeLoginFragmentDirections
                .actionEmployeeLoginFragmentToAbnormTempFragment(
                    user,
                    abnormalAttendsToday.last().temperature
                )
        } else {
            Log.d(LOG_TAG, "Still have chance for user $user")

            EmployeeLoginFragmentDirections
                .actionEmployeeLoginFragmentToTemperatureFragment(user)
        }

        val extras = FragmentNavigatorExtras(schaefflerIdEditText to "username")

        buttonConfirmId.navigateWithExtras(dir, extras)
    }

    override fun onDestroyView() {
        requireContext().unregisterReceiver(userReceiver)
        super.onDestroyView()
    }

    /**
     * This receiver will listen when the user's id is recognized from other sources, such as face
     * recognition.
     *
     */
    inner class UserReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == MainActivityViewModel.ACTION_BROADCAST_USER) {
                    MainActivityViewModel.retrieveBroadcastUserId(it).run {
                        viewModel.newUserRecognition(this)
                    }
                }
            }
        }
    }
}