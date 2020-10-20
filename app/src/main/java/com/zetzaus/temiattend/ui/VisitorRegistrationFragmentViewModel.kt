package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zetzaus.temiattend.database.Visitor
import com.zetzaus.temiattend.database.VisitorRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class VisitorRegistrationFragmentViewModel @ViewModelInject constructor(
    private val repository: VisitorRepository
) : ViewModel() {
    private val _travelStartDate = MutableLiveData<Date>()
    val travelStartDate: LiveData<Date> = _travelStartDate

    private val _travelEndDate = MutableLiveData<Date>()
    val travelEndDate: LiveData<Date> = _travelEndDate

    fun updateStartDate(startDate: Date) = _travelStartDate.postValue(startDate)

    fun updateEndDate(endDate: Date) = _travelEndDate.postValue(endDate)

    fun saveVisitor(visitor: Visitor) = viewModelScope.launch { repository.saveVisitor(visitor) }
}