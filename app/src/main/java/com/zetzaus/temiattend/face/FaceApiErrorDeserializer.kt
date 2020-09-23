package com.zetzaus.temiattend.face

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class FaceApiErrorDeserializer : JsonDeserializer<FaceApiError> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FaceApiError {
        return Gson().run {
            val target = json.asJsonObject
                .getAsJsonObject("error")

            fromJson(target, FaceApiError::class.java)
        }
    }

}