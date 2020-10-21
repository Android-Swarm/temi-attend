package com.zetzaus.temiattend.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.zetzaus.temiattend.database.VisitorRepository
import com.zetzaus.temiattend.ext.combineAndFilterIf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class VisitorsFragmentViewModel @ViewModelInject constructor(
    repository: VisitorRepository
) : ViewModel() {
    private val allVisitors = repository.getAllVisitors()

    private val travelledChannel = ConflatedBroadcastChannel(false)
    private val contactWithTravelledChannel = ConflatedBroadcastChannel(false)
    private val feverChannel = ConflatedBroadcastChannel(false)
    private val contactWithConfirmedChannel = ConflatedBroadcastChannel(false)
    private val contactWithSuspectedChannel = ConflatedBroadcastChannel(false)
    private val contactWithQuarantinedChannel = ConflatedBroadcastChannel(false)

    private val visitorWithConditions =
        allVisitors.combineAndFilterIf(travelledChannel.asFlow()) { it.traveledLastTwoWeeks }
            .combineAndFilterIf(feverChannel.asFlow()) { it.haveFeverOrSymptoms }
            .combineAndFilterIf(contactWithTravelledChannel.asFlow()) { it.contactWithTraveler }
            .combineAndFilterIf(contactWithConfirmedChannel.asFlow()) { it.contactWithConfirmed }
            .combineAndFilterIf(contactWithSuspectedChannel.asFlow()) { it.contactWithSuspected }
            .combineAndFilterIf(contactWithQuarantinedChannel.asFlow()) { it.contactWithQuarantine }

    private val queryVisitor = ConflatedBroadcastChannel("")

    val visitorToDisplay =
        visitorWithConditions.combine(queryVisitor.asFlow()) { visitors, query ->
            visitors.filter { it.name.contains(query, true) }
        }.asLiveData()

    fun showTravelledOnly(travel: Boolean) = viewModelScope.launch { travelledChannel.send(travel) }

    fun showFeverOnly(fever: Boolean) = viewModelScope.launch { feverChannel.send(fever) }

    fun showContactWithTravellerOnly(only: Boolean) =
        viewModelScope.launch { contactWithTravelledChannel.send(only) }

    fun showContactWithConfirmedOnly(only: Boolean) =
        viewModelScope.launch { contactWithConfirmedChannel.send(only) }

    fun showContactWithSuspectedOnly(only: Boolean) =
        viewModelScope.launch { contactWithSuspectedChannel.send(only) }

    fun showContactWithQuarantinedOnly(only: Boolean) =
        viewModelScope.launch { contactWithQuarantinedChannel.send(only) }

    fun submitQuery(query: String) = viewModelScope.launch { queryVisitor.send(query) }
}