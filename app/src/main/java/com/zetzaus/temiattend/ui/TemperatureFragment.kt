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

    private val viewModel by viewModels<TemperatureFragmentViewModel>()

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

//        if (BuildConfig.DEBUG) {
//            Log.d(LOG_TAG, "Is in debug mode, sending fake temperature")
//            Handler(Looper.getMainLooper())
//                .postDelayed(
//                    { viewModel.finalizeTemperature(35 + Random.Default.nextFloat() * 5) },
//                    Random.Default.nextInt(2000, 5000).toLong()
//                )
//        }

        lifecycleScope.launchWhenCreated {
            // This needs to trigger collection of temperature
            mainViewModel.startTemperatureCollection(this)
        }

        // Handles the case when the thermal camera suddenly is not working
        mainViewModel.cameraWorking.observe(viewLifecycleOwner) { isWorking ->
            if (!isWorking) {
//                if (!BuildConfig.DEBUG) {
                requireActivity().onBackPressed()
                mainViewModel.showSnackBar(getString(R.string.snack_bar_camera_not_ready))
//                }
            }
        }

        mainViewModel.temperature.observe(viewLifecycleOwner) { (temp, distance) ->
            // Measuring distance is up to 0.5m; See TM-IR v3.0 Product Specification
            // Tried limiting to 0.3m as well, but not nice

            when (distance) {
                in Int.MIN_VALUE until 500 -> {
                    Log.d(LOG_TAG, "Sent valid temperature candidate: $temp")
                    viewModel.sendTemperature(temp)
                    viewModel.sendDetection(true)
                }
                else -> {
                    viewModel.sendDetection(false)
                    mainViewModel.requestTemiSpeak(getString(R.string.tts_user_too_far), false)
                }
            }
        }

        viewModel.detecting.observe(viewLifecycleOwner) { detecting ->
            if (detecting) mainViewModel.requestTemiSpeak("Detecting", false)
        }

        viewModel.averageTemperature.observe(viewLifecycleOwner) { (count, acc) ->
            if (count == TEMP_COUNT) {
                val avg = acc / count

                Log.d(LOG_TAG, "Collected $count temperature!")
                Log.d(LOG_TAG, "User's average temperature: $avg")

                viewModel.finalizeTemperature(avg.toFloat())
            }
        }

        lifecycleScope.launchWhenCreated {
            mainViewModel.thermalImage.collect {
                thermalCameraImage.setImageBitmap(it)
            }
        }

        lifecycleScope.launch {
            viewModel.attendanceToSave
                .collect { (office, temperature) ->
                    // Save attendance to local database
                    // TODO: Change from hardcoding location
                    viewModel.recordAttendance(args.user, temperature, office)
                }
        }

        viewModel.temperatureLiveData.observe(viewLifecycleOwner) { temp ->
            animatedThermometer.run {
                val normalTemp = temp.isNormalTemperature()

                getString(
                    if (normalTemp) R.string.tts_normal_temp else R.string.tts_abnormal_temp
                ).format(temp).also { toSpeak ->
                    mainViewModel.requestTemiSpeak(toSpeak, false)
                }

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

    companion object {
        const val TEMP_COUNT = 5
    }
}