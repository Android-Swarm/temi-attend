package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentConnectingBinding
import com.zetzaus.temiattend.ext.LOG_TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectingFragment : BindingMainFragment<FragmentConnectingBinding>() {

    private val args by navArgs<ConnectingFragmentArgs>()

    private val viewModel by viewModels<ConnectingFragmentViewModel>()

    override fun layoutId() = R.layout.fragment_connecting

    override fun onBinding() {
        super.onBinding()

        binding.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestCameraDetails()

        viewModel.cameraDetails.observe(viewLifecycleOwner) {
            if (it.targetSsid != args.wifiSsid) {
                Log.d(LOG_TAG, "Failed! Camera details: $it")

                if (viewModel.retryCount > TRY_COUNT) {
                    viewModel.updateState(false)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        requestCameraDetails()
                    }, DELAY_PER_TRY)
                }
            } else {
                mainViewModel.saveCameraDetails(it.macAddress, it.deviceIp)
                viewModel.updateState(true)
            }
        }
    }

    private fun requestCameraDetails() = viewModel.requestCameraDetails(args.cameraMac)

    companion object {
        const val TRY_COUNT = 6
        const val DELAY_PER_TRY = 5000L
    }
}