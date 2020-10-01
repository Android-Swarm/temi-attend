package com.zetzaus.temiattend.database

import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(private val dao: AttendanceDao) {

    suspend fun deleteAttendance(attendance: Attendance) = dao.delete(attendance)

    suspend fun saveAttendance(attendance: Attendance) = dao.saveAttendance(attendance)

    fun getUserAttendances(user: String) = dao.getByUser(user)

    fun getUserAttendancesToday(user: String): Flow<List<Attendance>> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return dao.getByUserBetween(user, start, end)
    }
}