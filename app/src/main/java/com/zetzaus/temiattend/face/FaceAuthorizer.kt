package com.zetzaus.temiattend.face

import android.util.Log
import com.zetzaus.temiattend.ext.LOG_TAG
import okhttp3.Interceptor
import okhttp3.Response

/**
 * This class is an interceptor for Azure Face API service. Each requests will be added an
 * authorization header.
 *
 * @property apiKey The api key for authorization.
 */
class FaceAuthorizer(private val apiKey: String) : Interceptor {
    /**
     * Interrupts a request, add an authorization header to the current request,
     * and proceeds the request.
     *
     * @param chain The current request chain.
     * @return A new request chain that contains Azure's authorization header.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d(LOG_TAG, "Intercepted URL: ${chain.request().url()}")

        val newRequest = chain.request().newBuilder()
            .addHeader(AUTH_HEADER_KEY, apiKey)
            .build()

        return chain.proceed(newRequest)
    }

    companion object {
        /** The name of the authorization header for Azure services. */
        const val AUTH_HEADER_KEY = "Ocp-Apim-Subscription-Key"
    }
}