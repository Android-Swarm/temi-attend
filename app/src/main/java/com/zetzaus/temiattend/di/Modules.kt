package com.zetzaus.temiattend.di

import android.content.Context
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.AttendanceDatabase
import com.zetzaus.temiattend.face.AzureFaceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
class Modules {

    @Provides
    fun provideAttendanceDao(@ApplicationContext context: Context) =
        AttendanceDatabase.getInstance(context).dao

    @Provides
    fun provideAzureFaceManager(@ApplicationContext context: Context) =
        AzureFaceManager(
            endpoint = context.getString(R.string.azure_endpoint),
            apiKey = context.getString(R.string.azure_key)
        )
}