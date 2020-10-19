package com.zetzaus.temiattend.adapter

import androidx.recyclerview.widget.DiffUtil
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.Attendance
import com.zetzaus.temiattend.databinding.ItemAttendanceBinding
import javax.inject.Inject

class AttendanceAdapter @Inject constructor(
    differCallback: DiffUtil.ItemCallback<Attendance>
) : BindingListAdapter<ItemAttendanceBinding, Attendance>(differCallback) {
    
    override val itemLayoutId: Int
        get() = R.layout.item_attendance

    override val onBinding: ItemAttendanceBinding.(Attendance) -> Unit
        get() = { toBind ->
            attendance = toBind
        }
}