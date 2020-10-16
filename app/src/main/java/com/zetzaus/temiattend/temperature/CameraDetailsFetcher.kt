package com.zetzaus.temiattend.temperature

import android.util.Log
import com.zetzaus.temiattend.database.CameraApiResponse
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.parseJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.net.URL


class CameraDetailsFetcher(private val mac: String) {

    fun cameraDetailsAsync(coroutineScope: CoroutineScope) =
        coroutineScope.async(Dispatchers.IO) {
            try {
                val response = URL(createEndpoint(mac))
                    .readText().also {
                        Log.d(this@CameraDetailsFetcher.LOG_TAG, "Response: $it")
                    }
                    .parseJson<CameraApiResponse>()

                response.data
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to get camera details: ", e)
                null
            }
        }

    companion object {
        private const val API_ENDPOINT = "https://roboapi-intl.cozzia.com.cn/api/temi/v1/ir?mac="

        /**
         * Returns the complete API URL as [String].
         *
         * @param mac The raw mac address.
         */
        private fun createEndpoint(mac: String) = API_ENDPOINT + mac.replace(":", "")
    }
}