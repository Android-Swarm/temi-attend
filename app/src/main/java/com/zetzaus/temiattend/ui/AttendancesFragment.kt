package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.adapter.AttendanceAdapter
import com.zetzaus.temiattend.databinding.FragmentAttendancesBinding
import com.zetzaus.temiattend.ext.addOnTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_attendances.*
import javax.inject.Inject

@AndroidEntryPoint
class AttendancesFragment : BindingFragment<FragmentAttendancesBinding>() {

    private val viewModel by viewModels<AttendancesFragmentViewModel>()

    private val args by navArgs<AttendancesFragmentArgs>()

    @Inject
    lateinit var attendanceAdapter: AttendanceAdapter

    override fun layoutId(): Int = R.layout.fragment_attendances

    override fun onBinding() {
        super.onBinding()

        args.user.run {
            binding.user = this ?: ""
            viewModel.setUser(this ?: "")

            binding.viewModel = viewModel
            binding.adapter = attendanceAdapter
            binding.itemDecoration = DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* There is a bug in Hilt when using temi causing the app to crash, hence the click
           listener is not bound. */
        confirmButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        inputSearch.addOnTextChangedListener { query, _, _, _ ->
            viewModel.submitQuery(query)
        }

        viewModel.attendances.observe(viewLifecycleOwner) {
            attendanceAdapter.submitList(it)
        }
    }
}