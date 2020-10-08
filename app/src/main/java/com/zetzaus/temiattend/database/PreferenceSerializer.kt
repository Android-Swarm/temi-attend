package com.zetzaus.temiattend.database

import androidx.datastore.CorruptionException
import androidx.datastore.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.zetzaus.temiattend.Preference
import java.io.InputStream
import java.io.OutputStream

object PreferenceSerializer : Serializer<Preference> {
    override fun readFrom(input: InputStream): Preference = try {
        Preference.parseFrom(input)
    } catch (e: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read proto.", e)
    }

    override fun writeTo(t: Preference, output: OutputStream) = t.writeTo(output)

}