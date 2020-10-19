package com.zetzaus.temiattend.database

import androidx.room.*
import com.zetzaus.temiattend.OfficeName
import kotlinx.coroutines.flow.Flow
import java.util.*

@Entity
data class Attendance(
    val user: String,
    val temperature: Float,
    val dateTime: Date,
    val location: OfficeName,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
)

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAttendance(attendance: Attendance)

    @Query("SELECT * FROM Attendance ORDER BY dateTime DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM Attendance WHERE user = :user ORDER BY dateTime DESC")
    fun getByUser(user: String): Flow<List<Attendance>>

    @Query("SELECT * FROM Attendance WHERE user = :user AND dateTime >= :start AND dateTime <= :end")
    fun getByUserBetween(user: String, start: Date, end: Date): Flow<List<Attendance>>

    @Delete
    suspend fun delete(attendance: Attendance)
}