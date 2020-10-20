package com.zetzaus.temiattend.database

import android.content.Context
import android.util.Log
import androidx.room.*
import com.zetzaus.temiattend.OfficeName
import com.zetzaus.temiattend.ext.LOG_TAG
import java.util.*


class AttendanceConverter {
    @TypeConverter
    fun fromDate(date: Date?) = date?.time

    @TypeConverter
    fun toDate(time: Long?) = time?.let { Date(it) }

    @TypeConverter
    fun fromOfficeName(name: OfficeName) = name.name

    @TypeConverter
    fun fromOfficeName(name: String) = try {
        OfficeName.valueOf(name)
    } catch (e: Exception) {
        Log.e(LOG_TAG, "This database contains invalid office $name!", e)

        OfficeName.UNRECOGNIZED
    }
}

@Database(version = 1, entities = [Attendance::class, Visitor::class], exportSchema = false)
@TypeConverters(AttendanceConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val attendanceDao: AttendanceDao

    abstract val visitorDao: VisitorDao

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