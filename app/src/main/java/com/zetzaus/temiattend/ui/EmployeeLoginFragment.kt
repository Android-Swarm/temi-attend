package com.zetzaus.temiattend.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.face.NewPersonPayload
import kotlinx.android.synthetic.main.fragment_employee_login.*

class EmployeeLoginFragment : Fragment() {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()
    private val viewModel by viewModels<EmployeeLoginViewModel>()

    private lateinit var userReceiver: UserReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_login, container, false)
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

        viewModel.recognizedUser.observe(viewLifecycleOwner) { recognizedUserId ->
            schaefflerIdTextInput.editText?.setText(recognizedUserId)
        }

        buttonFaceRecognition.setOnClickListener {
            activityViewModel.updateFaceRecognitionState(true)
        }

        buttonConfirmId.setOnClickListener {
            try {
                activityViewModel.newPersonToRegister?.let {
                    Log.d(
                        LOG_TAG,
                        "New person need to be registered\n" +
                                "Image Size: ${it.photoData?.contentLength()}"
                    )

                    val payloadCopy = NewPersonPayload(
                        user = schaefflerIdTextInput.editText?.text.toString(),
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
        }
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