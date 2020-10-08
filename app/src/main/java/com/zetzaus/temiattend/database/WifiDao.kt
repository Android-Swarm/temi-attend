package com.zetzaus.temiattend.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity
data class WifiPoint(
    @PrimaryKey
    val ssid: String
)

@Dao
interface WifiDao {

    @Query("SELECT * FROM WifiPoint ORDER BY ssid")
    fun getSavedWifi(): Flow<List<WifiPoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWifiList(wifiList: List<WifiPoint>)
}