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

/**
 * Repository for this application's settings.
 *
 * @param context The application context.
 */
@Singleton
class PreferenceRepository @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.createDataStore(FILE_NAME, PreferenceSerializer)

    /** Contains this application's settings. */
    val preference = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(this@PreferenceRepository.LOG_TAG, "Error reading preference.", exception)
                emit(Preference.getDefaultInstance())
            } else {
                throw exception
            }
        }

    /**
     * Persist the thermal camera's MAC address in the application's settings.
     *
     * @param mac The thermal camera's MAC address.
     */
    suspend fun saveCameraMac(mac: String) = savePreference { setCameraMac(mac) }

    /**
     * Persist the thermal camera's IP address in the application's settings.
     *
     * @param ip The thermal camera's IP address.
     */
    suspend fun saveCameraIp(ip: String) = savePreference { setCameraIp(ip) }

    /**
     * Persist the office location to be used in the application's settings.
     *
     * @param location The office location to be used.
     */
    suspend fun saveLocation(location: OfficeName) = savePreference { setLocation(location) }

    /**
     * Persist the admin panel's encrypted password in the application's settings.
     *
     * @param encrypted The encrypted password.
     * @param iv The initialization vector used to encrypt the password.
     */
    suspend fun savePassword(encrypted: String, iv: String) = savePreference {
        password = encrypted
        setIv(iv)
    }

    /**
     * Saves data into the application's settings.
     *
     * @param block Data changes to be made.
     */
    private suspend fun savePreference(block: Preference.Builder.() -> Preference.Builder) {
        dataStore.updateData {
            it.toBuilder().apply { block(this) }.build()
        }
    }

    companion object {
        const val FILE_NAME = "preference"
    }
}