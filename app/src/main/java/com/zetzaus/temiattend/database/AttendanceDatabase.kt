package com.zetzaus.temiattend.database

import android.content.Context
import androidx.room.*
import java.util.*

@Entity
data class Attendance(
    val user: String,
    val temperature: Float,
    val dateTime: Date,
    val location: String = "18 Tai Seng",
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
)

class AttendanceConverter {
    @TypeConverter
    fun fromDate(date: Date) = date.time

    @TypeConverter
    fun toDate(time: Long) = Date(time)
}

@Database(version = 1, entities = [Attendance::class], exportSchema = false)
@TypeConverters(AttendanceConverter::class)
abstract class AttendanceDatabase : RoomDatabase() {
    abstract val dao: AttendanceDao

    companion object {
        private lateinit var INSTANCE: AttendanceDatabase

        private const val DB_NAME = "attendance"

        fun getInstance(context: Context) = synchronized(this) {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AttendanceDatabase::class.java,
                    DB_NAME
                ).build()
            }

            INSTANCE
        }
    }
}