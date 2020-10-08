package com.zetzaus.temiattend.database

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiRepository @Inject constructor(private val wifiDao: WifiDao) {
    suspend fun saveWifiList(wifiList: List<WifiPoint>) = wifiDao.saveWifiList(wifiList)

    fun getWifiFlow() = wifiDao.getSavedWifi()
}