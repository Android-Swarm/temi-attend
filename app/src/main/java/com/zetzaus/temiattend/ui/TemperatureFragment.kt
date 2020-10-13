package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentTemperatureBinding
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.addTransitionCompleteListener
import com.zetzaus.temiattend.ext.isNormalTemperature
import com.zetzaus.temiattend.ext.navigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_temperature.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TemperatureFragment : TransitionalFragment<FragmentTemperatureBinding>() {

    private val viewModel by viewModels<TemperatureViewModel>()

    private val mainViewModel by activityViewModels<MainActivityViewModel>()

    private val args by navArgs<TemperatureFragmentArgs>()

    override fun transitionId(): Int = android.R.transition.move

    override fun layoutId(): Int = R.layout.fragment_temperature

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.submitUser(args.user)

        // TODO: Change with real temperature
//        Handler(Looper.getMainLooper())
//            .postDelayed(
//                { viewModel.updateTemperature(35 + Random.Default.nextFloat() * 5) },
//                Random.Default.nextInt(2000, 5000).toLong()
//            )

        lifecycleScope.launchWhenCreated {
            mainViewModel.startTemperatureCollection(this)
        }

        mainViewModel.cameraWorking.observe(viewLifecycleOwner) { isWorking ->
            if (!isWorking) {
                requireActivity().onBackPressed()
                mainViewModel.showSnackBar(getString(R.string.snack_bar_camera_not_ready))
            }
        }

        mainViewModel.temperature.observe(viewLifecycleOwner) { (temp, distance) ->
//            Log.d(
//                this@TemperatureFragment.LOG_TAG,
//                "Measured temperature: $temp, " +
//                        "distance: $distance"
//            )

            // Measuring distance is 0.3 to 0.5m; See TM-IR v3.0 Product Specification
            when (distance) {
                in 300 until 500 -> viewModel.sendTemperature(temp).also {
                    Log.d(LOG_TAG, "Sent valid temperature candidate")
                }
                in Int.MIN_VALUE until 300 ->
                    mainViewModel.requestTemiSpeak(getString(R.string.tts_user_too_close), false)
                else ->
                    mainViewModel.requestTemiSpeak(getString(R.string.tts_user_too_far), false)
            }
        }

        viewModel.averageTemperature.observe(viewLifecycleOwner) { (count, acc) ->
            if (count == 5) {
                val avg = acc / count

                Log.d(LOG_TAG, "Collected $count temperature!")
                Log.d(LOG_TAG, "User's average temperature: $avg")

                viewModel.finalizeTemperature(avg.toFloat())
            }
        }

        mainViewModel.thermalImage.observe(viewLifecycleOwner) {
            thermalCameraImage.setImageBitmap(it)
        }


        lifecycleScope.launch {
            viewModel.attendanceToSave
                .collect { (office, temperature) ->
                    // Save attendance to local database
                    viewModel.recordAttendance(args.user, temperature, office)
                }
        }

        viewModel.temperatureLiveData.observe(viewLifecycleOwner) { temp ->
            animatedThermometer.run {
                val normalTemp = temp.isNormalTemperature()
                cancelAnimation()

                progress = if (normalTemp) 0.427f else 0.88f

                motionLayout.addTransitionCompleteListener { _, _ ->
                    Log.d(
                        this@TemperatureFragment.LOG_TAG,
                        "Temperature recorded, navigating..."
                    )

                    lifecycleScope.launch {
                        val dir = if (normalTemp) {
                            TemperatureFragmentDirections.actionTemperatureFragmentToNormalTempFragment(
                                args.user,
                                temp
                            )
                        } else {
                            TemperatureFragmentDirections.actionTemperatureFragmentToAbnormTempFragment(
                                args.user,
                                temp
                            )
                        }

                        animatedThermometer.navigate(dir)
                    }
                }

                motionLayout.transitionToEnd()
            }
        }
    }
}