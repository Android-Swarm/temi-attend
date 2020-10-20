package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.zetzaus.temiattend.databinding.FragmentDateDialogBinding
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.putDate
import kotlinx.android.synthetic.main.fragment_date_dialog.*
import java.util.*

class DateDialog : DialogFragment() {

    private lateinit var binding: FragmentDateDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDateDialogBinding.inflate(inflater, container, false)
        binding.dialogFragment = this

        return binding.root
    }

    fun onConfirmButtonClicked() {
        val chosenDate = Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.DAY_OF_MONTH, datePicker.dayOfMonth)
            set(Calendar.MONTH, datePicker.month)
            set(Calendar.YEAR, datePicker.year)
        }.time

        Log.d(LOG_TAG, "Chosen date: $chosenDate")

        setFragmentResult(REQUEST_DATE, Bundle().apply { putDate(chosenDate) })
        dismiss()
    }

    companion object {
        const val REQUEST_DATE = "DATE_REQUEST"
    }
}