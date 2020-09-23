package com.zetzaus.temiattend.face

import okhttp3.RequestBody
import retrofit2.http.*

interface AzureFaceService {
    @POST("detect")
    suspend fun detect(
        @Body photo: RequestBody,
        @Query("recognitionModel") recognitionModel: String = "recognition_03"
    ): List<FaceDetected>

    @POST("identify")
    suspend fun identify(@Body data: IdentifyRequestBody): List<FaceIdentified>

    @POST("persongroups/{personGroupId}/train")
    suspend fun trainPersonGroup(@Path("personGroupId") personGroupId: String)

    @POST("persongroups/{personGroupId}/persons")
    suspend fun createPerson(
        @Body data: CreatePersonBody,
        @Path("personGroupId") personGroupId: String
    ): CreatedPerson

    @POST("persongroups/{personGroupId}/persons/{personId}/persistedFaces")
    suspend fun addFaceToPerson(
        @Body photo: RequestBody,
        @Path("personId") personId: String,
        @Path("personGroupId") personGroupId: String,
        @Query("targetFace") targetFace: String
    )

    @GET("persongroups/{personGroupId}/persons/{personId}")
    suspend fun getPerson(
        @Path("personId") personId: String,
        @Path("personGroupId") personGroupId: String
    ): Person
}