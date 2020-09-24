package com.zetzaus.temiattend.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.databinding.FragmentTemperatureBinding
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.navigateWithExtras
import com.zetzaus.temiattend.ext.onCompleteFinite
import kotlinx.android.synthetic.main.fragment_temperature.*
import kotlin.random.Random

class TemperatureFragment : TransitionalFragment() {

    private val viewModel by viewModels<TemperatureViewModel>()
    private val args by navArgs<TemperatureFragmentArgs>()

    private lateinit var binding: FragmentTemperatureBinding

    override fun transitionId(): Int = android.R.transition.move

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_temperature, container, false)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(transitionId())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.updateUser(args.user)

        // TODO: Change with real temperature
        Handler(Looper.getMainLooper())
            .postDelayed(
                { viewModel.updateTemperature(35 + Random.Default.nextFloat() * 5) },
                Random.Default.nextInt(2000, 5000).toLong()
            )

        viewModel.temperatureLiveData.observe(viewLifecycleOwner) { temp ->
            animatedThermometer.run {
                val normalTemp = temp < 37.3f
                cancelAnimation()

                progress = if (normalTemp) 0.427f else 0.88f

                ObjectAnimator.ofFloat(
                    this,
                    "translationX",
                    translationX,
                    translationX - 80f
                ).onCompleteFinite {
                    if (normalTemp) {
                        Log.d(this@TemperatureFragment.LOG_TAG, "Normal temperature, navigating...")
                        val dir =
                            TemperatureFragmentDirections.actionTemperatureFragmentToNormalTempFragment(
                                args.user,
                                viewModel.temperatureLiveData.value!!
                            )

                        val extras = FragmentNavigatorExtras(
                            animatedThermometer to "thermometer",
                            temperatureText to "temperature"
                        )

                        animatedThermometer.navigateWithExtras(dir, extras)
                    }
                }.start()
            }
        }
    }
}

@BindingAdapter("textAppear")
fun TextView.textAppear(text: String) {
    this.text = text

    ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
        .start()
}

// TODO: modify shared element into motion layout.