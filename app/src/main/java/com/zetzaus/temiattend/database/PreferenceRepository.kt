package com.zetzaus.temiattend.database

import android.content.Context
import android.util.Log
import androidx.datastore.createDataStore
import com.zetzaus.temiattend.OfficeName
import com.zetzaus.temiattend.Preference
import com.zetzaus.temiattend.ext.LOG_TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.createDataStore(FILE_NAME, PreferenceSerializer)

    val preference = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(this@PreferenceRepository.LOG_TAG, "Error reading preference.", exception)
                emit(Preference.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun saveCameraMac(mac: String) = savePreference { setCameraMac(mac) }

    suspend fun saveCameraIp(ip: String) = savePreference { setCameraIp(ip) }

    suspend fun saveLocation(location: OfficeName) = savePreference { setLocation(location) }

    suspend fun savePassword(encrypted: String, iv: String) = savePreference {
        password = encrypted
        setIv(iv)
    }

    private suspend fun savePreference(block: Preference.Builder.() -> Preference.Builder) {
        dataStore.updateData {
            it.toBuilder().apply { block(this) }.build()
        }
    }

    companion object {
        const val FILE_NAME = "preference"
    }
}