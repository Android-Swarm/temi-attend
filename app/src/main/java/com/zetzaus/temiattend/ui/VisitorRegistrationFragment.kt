package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.Visitor
import com.zetzaus.temiattend.databinding.FragmentVisitorRegistrationBinding
import com.zetzaus.temiattend.ext.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_visitor_registration.*
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class VisitorRegistrationFragment : BindingMainFragment<FragmentVisitorRegistrationBinding>() {
    private val viewModel by viewModels<VisitorRegistrationFragmentViewModel>()

    override fun layoutId() = R.layout.fragment_visitor_registration

    override fun onBinding() {
        super.onBinding()

        binding.viewModel = viewModel
        binding.fragment = this
    }

    fun onChooseDate(v: View, request: DateRequest) {
        lifecycleScope.launchWhenCreated {
            val date = selectDateFromDialog(v) ?: return@launchWhenCreated

            Log.d(this@VisitorRegistrationFragment.LOG_TAG, "Received date $date")

            when (request) {
                DateRequest.DATE_START_TRAVEL -> viewModel.updateStartDate(date)
                DateRequest.DATE_END_TRAVEL -> viewModel.updateEndDate(date)
            }
        }
    }

    fun onSubmit(v: View) {
        // Check details are filled
        getString(R.string.error_required).let { errorMessage ->
            val nameFilled = layoutVisitorName.requireNonEmptyText(errorMessage)
            val companyFilled = layoutCompany.requireNonEmptyText(errorMessage)
            val contactFilled = layoutContact.requireNonEmptyText(errorMessage)
            val hostFilled = layoutHost.requireNonEmptyText(errorMessage)

            if (!(nameFilled && companyFilled && contactFilled && hostFilled)) return
        }

        // Check if the visitor travelled abroad, the date of travel is also filled
        if (!switchTravel.isChecked) {
            if (viewModel.travelStartDate.value == null || viewModel.travelEndDate.value == null) {
                mainViewModel.showSnackBar(getString(R.string.snack_bar_travel_date_required))
                return
            }
        }

        val visitorName = inputVisitorName.textString.toUpperCase(Locale.ROOT)

        val visitorData = Visitor(
            name = visitorName,
            company = inputCompany.textString,
            contactNo = inputContact.textString,
            host = inputHost.textString,
            dateOfVisit = Date(),
            traveledLastTwoWeeks = !switchTravel.isChecked,
            travelStartDate = if (!switchTravel.isChecked) viewModel.travelStartDate.value else null,
            travelEndDate = if (!switchTravel.isChecked) viewModel.travelEndDate.value else null,
            haveFeverOrSymptoms = !switchFever.isChecked,
            contactWithConfirmed = !switchContactConfirmed.isChecked,
            contactWithSuspected = !switchContactSuspected.isChecked,
            contactWithTraveler = !switchContactTravel.isChecked,
            contactWithQuarantine = !switchContactQuarantined.isChecked
        )

        viewModel.saveVisitor(visitorData)

        // Navigate to temperature fragment
        val dir = VisitorRegistrationFragmentDirections
            .actionVisitorRegistrationFragmentToTemperatureFragment(visitorName)

        v.navigate(dir)
    }

    /**
     * Launch a dialog that helps the user to select a date.
     *
     * @param v The button that is clicked.
     */
    private suspend fun selectDateFromDialog(v: View) =
        v.getByFragmentResult(
            DateDialog.REQUEST_DATE,
            R.id.action_visitorRegistrationFragment_to_dateDialog
        ) {
            it.getDate()
        }

    private suspend fun <T> View.getByFragmentResult(
        requestKey: String,
        destinationId: Int,
        extractBlock: (Bundle) -> T
    ) = suspendCoroutine<T> {
        setFragmentResultListener(requestKey) { _, bundle ->
            it.resume(extractBlock(bundle))
        }

        navigate(destinationId)
    }
}

enum class DateRequest {
    DATE_START_TRAVEL, DATE_END_TRAVEL
}