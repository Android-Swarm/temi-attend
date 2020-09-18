package com.zetzaus.temiattend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
class MainActivityViewModel : ViewModel() {

    private val maskDetectionChannel = BroadcastChannel<Boolean>(30)
    val maskDetected = maskDetectionChannel.asFlow()
        .distinctUntilChanged() // From rapid frames, take only when the value changes
        .debounce(1000) // Emit if the value doesn't change for 1s
        .asLiveData()

    suspend fun updateMaskDetection(isWearingMask: Boolean) =
        maskDetectionChannel.send(isWearingMask)
}