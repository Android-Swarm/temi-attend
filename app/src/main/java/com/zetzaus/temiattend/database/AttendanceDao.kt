package com.zetzaus.temiattend.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAttendance(attendance: Attendance)

    @Query("SELECT * FROM Attendance WHERE user = :user ORDER BY dateTime DESC")
    fun getByUser(user: String): Flow<List<Attendance>>

    @Query("SELECT * FROM Attendance WHERE user = :user AND dateTime >= :start AND dateTime <= :end")
    fun getByUserBetween(user: String, start: Date, end: Date): Flow<List<Attendance>>

    @Delete
    suspend fun delete(attendance: Attendance)
}