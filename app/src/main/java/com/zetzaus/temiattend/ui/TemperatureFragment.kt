package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
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
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class TemperatureFragment : TransitionalFragment<FragmentTemperatureBinding>() {

    private val viewModel by viewModels<TemperatureViewModel>()
    private val args by navArgs<TemperatureFragmentArgs>()

    override fun transitionId(): Int = android.R.transition.move

    override fun layoutId(): Int = R.layout.fragment_temperature

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.submitUser(args.user)

        // TODO: Change with real temperature
        Handler(Looper.getMainLooper())
            .postDelayed(
                { viewModel.updateTemperature(35 + Random.Default.nextFloat() * 5) },
                Random.Default.nextInt(2000, 5000).toLong()
            )

        viewModel.attendanceToSave.observe(viewLifecycleOwner) { (office, temperature) ->
            // Save attendance to local database
            viewModel.recordAttendance(args.user, temperature, office)
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