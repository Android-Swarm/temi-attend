package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.asLiveData
import com.zetzaus.temiattend.OfficeName
import com.zetzaus.temiattend.database.PreferenceRepository
import com.zetzaus.temiattend.ext.toStringRepresentation
import kotlinx.coroutines.flow.map

class AdminFragmentViewModel @ViewModelInject constructor(
    repository: PreferenceRepository
) : PreferenceViewModel(repository) {
    /** All office locations in its [String] representation. */
    val offices = OfficeName.values().map { it.toStringRepresentation() }

    /** The chosen location. */
    val chosenOfficeName = officeLocation.map { it.toStringRepresentation() }.asLiveData()
}