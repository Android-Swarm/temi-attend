package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.asLiveData
import com.zetzaus.temiattend.OfficeName
import com.zetzaus.temiattend.database.PreferenceRepository
import kotlinx.coroutines.flow.map

class AdminFragmentViewModel @ViewModelInject constructor(
    repository: PreferenceRepository
) : PreferenceViewModel(repository) {
    val officeNames = mapOf(
        OfficeName.TAI_SENG to "18 Tai Seng",
        OfficeName.SHARE_NTU to "SHARELAB@NTU",
        OfficeName.AEROSPACE to "Aerospace",
    )

    val chosenOfficeName = officeLocation.map { officeNames[it] ?: "" }.asLiveData()
}