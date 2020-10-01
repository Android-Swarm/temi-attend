package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.Attendance
import com.zetzaus.temiattend.databinding.FragmentAttendancesBinding
import com.zetzaus.temiattend.databinding.ItemAttendanceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_attendances.*
import javax.inject.Inject

@AndroidEntryPoint
class AttendancesFragment : BindingFragment<FragmentAttendancesBinding>() {

    private val viewModel by viewModels<AttendancesViewModel>()

    private val args by navArgs<AttendancesFragmentArgs>()

    @Inject
    lateinit var attendanceAdapter: AttendanceAdapter

    override fun layoutId(): Int = R.layout.fragment_attendances

    override fun onBinding() {
        super.onBinding()

        args.user.run {
            binding.user = this
            viewModel.setUser(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceAdapter
        }

        viewModel.attendances.observe(viewLifecycleOwner) {
            attendanceAdapter.submitList(it)
        }
    }

    class AttendanceAdapter @Inject constructor(
        differCallback: DiffUtil.ItemCallback<Attendance>
    ) : ListAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder>(differCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ItemAttendanceBinding>(
                inflater,
                R.layout.item_attendance,
                parent,
                false
            )

            return AttendanceViewHolder(binding)
        }

        override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class AttendanceViewHolder(private val binding: ItemAttendanceBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(attendance: Attendance) {
                binding.attendance = attendance
                binding.executePendingBindings()
            }
        }
    }

}