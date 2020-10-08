package com.zetzaus.temiattend.database

import android.content.Context
import androidx.room.*
import com.zetzaus.temiattend.OfficeName
import java.util.*


class AttendanceConverter {
    @TypeConverter
    fun fromDate(date: Date) = date.time

    @TypeConverter
    fun toDate(time: Long) = Date(time)

    @TypeConverter
    fun fromOfficeName(name: OfficeName) = name.name

    @TypeConverter
    fun fromOfficeName(name: String) = OfficeName.valueOf(name)
}

@Database(version = 1, entities = [Attendance::class, WifiPoint::class], exportSchema = false)
@TypeConverters(AttendanceConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val attendanceDao: AttendanceDao

    abstract val wifiDao: WifiDao

    companion object {
        private lateinit var INSTANCE: AppDatabase

        private const val DB_NAME = "attendance"

        fun getInstance(context: Context) = synchronized(this) {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build()
            }

            INSTANCE
        }
    }
}