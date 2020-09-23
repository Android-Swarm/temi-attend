package com.zetzaus.temiattend.face

import android.util.Log
import com.google.gson.GsonBuilder
import com.zetzaus.temiattend.ext.LOG_TAG
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AzureFaceManager(endpoint: String, apiKey: String) {
    private val api: AzureFaceService

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(FaceAuthorizer(apiKey))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(endpoint.also { Log.d(LOG_TAG, "Azure endpoint: $it") })
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(AzureFaceService::class.java)
    }

    suspend fun detectFaces(photoData: RequestBody) = api.detect(photoData)

    suspend fun identifyFaces(
        faceIds: List<String>,
        personGroupId: String = DEFAULT_PERSON_GROUP,
        confidenceThreshold: Float = DEFAULT_CONF_THRESHOLD
    ) = IdentifyRequestBody(faceIds, personGroupId, confidenceThreshold).run {
        api.identify(this)
    }

    suspend fun createPerson(
        name: String,
        personGroupId: String = DEFAULT_PERSON_GROUP,
        userData: String = ""
    ) = CreatePersonBody(name, userData).run {
        api.createPerson(this, personGroupId)
    }

    suspend fun addFaceToPerson(
        photoData: RequestBody,
        personId: String,
        targetFace: FaceRectangle,
        personGroupId: String = DEFAULT_PERSON_GROUP
    ) = api.addFaceToPerson(
        photo = photoData,
        personId = personId,
        targetFace = targetFace.toString(),
        personGroupId = personGroupId
    )

    suspend fun getPerson(personId: String, personGroupId: String = DEFAULT_PERSON_GROUP) =
        api.getPerson(personId, personGroupId)

    suspend fun trainPersonGroup(personGroupId: String = DEFAULT_PERSON_GROUP) =
        api.trainPersonGroup(personGroupId)

    fun parseError(error: HttpException): FaceApiError {
        val parser = GsonBuilder().registerTypeAdapter(
            FaceApiError::class.java,
            FaceApiErrorDeserializer()
        ).create()

        return error.response()?.errorBody()?.let {
            parser.fromJson(it.string(), FaceApiError::class.java)
        } ?: FaceApiError("Unknown", "Unknown error as error body is empty!")
    }

    companion object {
        const val PHOTO_PAYLOAD_TYPE = "application/octet-stream"
        const val DEFAULT_PERSON_GROUP = "singapore"
        const val DEFAULT_CONF_THRESHOLD = 0.7f
    }
}