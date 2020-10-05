package com.zetzaus.temiattend.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.zetzaus.temiattend.database.Attendance
import com.zetzaus.temiattend.database.AttendanceRepository
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.upperCaseAndTrim
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
open class AppViewModel(protected val repository: AttendanceRepository) : ViewModel() {

    private val _user = ConflatedBroadcastChannel<String>()
    val user: LiveData<String> = _user.asFlow().asLiveData()

    private val _abnormalAttendances = _user.asFlow()
        .flatMapLatest { repository.getAbnormalAttendancesToday(it) }

    val abnormalAttendances = _user.asFlow()
        .combine(_abnormalAttendances) { user, attendances -> Pair(user, attendances) }

//    protected lateinit var abnormalAttendanceList: List<Attendance>

//    init {
//        viewModelScope.launch {
//            abnormalAttendances.collect {
//                abnormalAttendanceList = it
//            }
//        }
//    }

    fun submitUser(user: String) = viewModelScope.launch { _user.send(user.upperCaseAndTrim()) }

    /**
     * Saves the attendance to the local database.
     *
     * @param user The attending user.
     * @param temperature The user's temperature measurement.
     */
    fun recordAttendance(user: String, temperature: Float) = viewModelScope.launch {
        val attendance = Attendance(
            user = user.upperCaseAndTrim(),
            temperature = temperature,
            dateTime = Date()
        )

        repository.saveAttendance(attendance)
    }

    fun getRemainingChance(user: String, abnormalAttendances: List<Attendance>) =
        MAX_CHANCE - abnormalAttendances.size
            .also { Log.d(LOG_TAG, "User $user has $it abnormal attendances today") }

    companion object {
        const val MAX_CHANCE = 3
    }
}