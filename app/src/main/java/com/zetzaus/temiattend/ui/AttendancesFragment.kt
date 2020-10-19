package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.adapter.AttendanceAdapter
import com.zetzaus.temiattend.databinding.FragmentAttendancesBinding
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

        /* There is a bug in Hilt when using temi causing the app to crash, hence the click
           listener is not bound. */
        confirmButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.attendances.observe(viewLifecycleOwner) {
            attendanceAdapter.submitList(it)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceAdapter

            // Horizontal lines between items
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

//    class AttendanceAdapter @Inject constructor(
//        differCallback: DiffUtil.ItemCallback<Attendance>
//    ) : ListAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder>(differCallback) {
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
//            val inflater = LayoutInflater.from(parent.context)
//            val binding = DataBindingUtil.inflate<ItemAttendanceBinding>(
//                inflater,
//                R.layout.item_attendance,
//                parent,
//                false
//            )
//
//            return AttendanceViewHolder(binding)
//        }
//
//        override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
//            holder.bind(getItem(position))
//        }
//
//        inner class AttendanceViewHolder(private val binding: ItemAttendanceBinding) :
//            RecyclerView.ViewHolder(binding.root) {
//
//            fun bind(attendance: Attendance) {
//                binding.attendance = attendance
//                binding.executePendingBindings()
//            }
//        }
//    }

}