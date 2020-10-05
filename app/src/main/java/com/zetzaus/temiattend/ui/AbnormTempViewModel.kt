package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Transformations
import androidx.lifecycle.asLiveData
import com.zetzaus.temiattend.database.AttendanceRepository
import kotlinx.coroutines.flow.map

class AbnormTempViewModel @ViewModelInject constructor(
    repository: AttendanceRepository
) : AppViewModel(repository) {
    val remainingChance = Transformations.switchMap(user) { user ->
        repository.getAbnormalAttendancesToday(user)
            .map { attendances -> getRemainingChance(user, attendances) }
            .asLiveData()
    }
}