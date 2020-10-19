package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zetzaus.temiattend.database.AttendanceRepository
import com.zetzaus.temiattend.ext.isNormalTemperature
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AttendancesFragmentViewModel @ViewModelInject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {
    private val queryChannel = ConflatedBroadcastChannel("")

    private val abnormalOnlyChannel = ConflatedBroadcastChannel(false)
    val abnormalOnly = abnormalOnlyChannel.asFlow().asLiveData()

    private val userLiveData = MutableLiveData<String>()
    val attendances = userLiveData.switchMap { user ->
        if (user.isNotBlank()) {
            repository.getUserAttendances(user).asLiveData()
        } else {
            queryChannel.asFlow()
                .combine(abnormalOnlyChannel.asFlow()) { query, abnormal -> Pair(query, abnormal) }
                .combine(repository.getAllAttendance()) { (query, abnormal), db ->
                    Triple(query, abnormal, db)
                }.map { (query, abnormalOnly, attendances) ->
                    val byTemperature = if (abnormalOnly) {
                        attendances.filter { !it.temperature.isNormalTemperature() }
                    } else {
                        attendances
                    }

                    byTemperature.filter { it.user.contains(query, true) }
                }.asLiveData()
        }
    }

    fun setUser(user: String) {
        userLiveData.value = user
    }

    fun submitQuery(query: String) = viewModelScope.launch { queryChannel.send(query) }

    fun showAbnormalOnly(abnormalOnly: Boolean) =
        viewModelScope.launch { abnormalOnlyChannel.send(abnormalOnly) }
}